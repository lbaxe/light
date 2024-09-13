package com.light.common.poi.excel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.exception.ExcelDataConvertException;
import com.alibaba.fastjson2.JSON;

public abstract class BaseListener<T> extends AnalysisEventListener<T> {

    private Logger log = LoggerFactory.getLogger(BaseListener.class);
    private String processMsg =
        "excel={1}，sheetSize={2},currentSheet={3}，totalRows={4}，dealRows={5},totalRows={6}，sheetTotalRows={7},sheetDealRows={8}";
    private String finishMsg = "excel={1}，解析完成，总条数={3}，总处理条数={4}";
    private static final int DEFAULT_BATCH_SIZE = 200;

    private AtomicInteger sheetSize = new AtomicInteger(1);

    private int batchSize = DEFAULT_BATCH_SIZE;

    private int totalRows = 0;
    private int dealRows = 0;
    private Map<String, Integer> sheetTotalRowsMap = new HashMap<>();
    private Map<String, Integer> sheetDealRowsMap = new HashMap<>();

    public BaseListener() {

    }

    public BaseListener(int batchSize) {
        this.batchSize = batchSize;
    }

    /**
     * 表格数据集合
     */
    private List<T> listData = new ArrayList();

    /**
     * 这个每一条数据解析都会来调用
     *
     * @param data one row value. Is is same as {@link AnalysisContext#readRowHolder()}
     * @param context
     */
    @Override
    public void invoke(T data, AnalysisContext context) {
        String sheetName = context.readSheetHolder().getSheetName();
        listData.add(data);
        // 总数
        totalRows++;
        // sheet总数
        Integer sheetTotalRows = sheetTotalRowsMap.get(sheetName);
        if (sheetTotalRows == null) {
            sheetTotalRowsMap.put(sheetName, 1);
        } else {
            sheetTotalRowsMap.put(sheetName, sheetTotalRows + 1);
        }
        // 达到BATCH_COUNT了，需要去存储一次数据库，防止数据几万条数据在内存，容易OOM
        if (listData.size() >= batchSize) {
            saveData(listData);
            // 总处理数
            dealRows += listData.size();
            // sheet总处理数
            Integer sheetDealRows = sheetDealRowsMap.get(sheetName);
            if (sheetTotalRows == null) {
                sheetDealRowsMap.put(sheetName, listData.size());
            } else {
                sheetDealRowsMap.put(sheetName, sheetDealRows + listData.size());
            }
            // 存储完成清理 list
            listData.clear();
        }
    }

    /**
     * 每个sheet读取完毕后调用一次
     * 
     * @param context
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        String excelName = context.readWorkbookHolder().getFile().getName();
        String sheetName = context.readSheetHolder().getSheetName();
        // 保存数据
        this.saveData(listData);
        // 总处理数
        dealRows += listData.size();
        // sheet总处理数
        Integer sheetDealRows = sheetDealRowsMap.get(sheetName);
        if (sheetDealRows == null) {
            sheetDealRowsMap.put(sheetName, listData.size());
        } else {
            sheetDealRowsMap.put(sheetName, sheetDealRows + listData.size());
        }
        listData.clear();
        // 进度日志
        log.info(processMsg, excelName, sheetName, totalRows, dealRows, sheetTotalRowsMap.get(sheetName),
            sheetDealRowsMap.get(sheetName));
        if (sheetSize.decrementAndGet() <= 0) {
            log.info(finishMsg, excelName, sheetName, totalRows, dealRows);
        }
    }

    /**
     * 这里会一行行的返回头
     *
     * @param headMap
     * @param context
     */
    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        log.info("解析到一条头数据:{}", JSON.toJSONString(headMap));
    }

    /**
     * 在转换异常 获取其他异常下会调用本接口。抛出异常则停止读取。如果这里不抛出异常则 继续读取下一行。
     *
     * @param exception
     * @param context
     * @throws Exception
     */
    @Override
    public void onException(Exception exception, AnalysisContext context) {
        log.error("解析失败，但是继续解析下一行:{}", exception.getMessage());
        // 如果是某一个单元格的转换异常 能获取到具体行号
        // 如果要获取头的信息 配合invokeHeadMap使用
        if (exception instanceof ExcelDataConvertException) {
            ExcelDataConvertException excelDataConvertException = (ExcelDataConvertException)exception;
            log.error("第{}行，第{}列解析异常", excelDataConvertException.getRowIndex(),
                excelDataConvertException.getColumnIndex());
        }
    }

    public AtomicInteger getSheetSize() {
        return sheetSize;
    }

    public void setSheetSize(AtomicInteger sheetSize) {
        this.sheetSize = sheetSize;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    /**
     * 加上存储数据库，达到{BATCH_COUNT}后会触发这个方法
     */
    public abstract void saveData(List<T> listData);
}
