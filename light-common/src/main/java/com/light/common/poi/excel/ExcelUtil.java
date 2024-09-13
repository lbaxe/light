package com.light.common.poi.excel;

import java.io.*;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelReader;
import com.alibaba.excel.read.metadata.ReadSheet;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.handler.WriteHandler;
import com.alibaba.fastjson2.JSON;

public class ExcelUtil {
    private static Logger log = LoggerFactory.getLogger(ExcelUtil.class);

    public static boolean importExcel(InputStream inputStream, Class clazz, BaseListener listener) throws IOException {
        ExcelReader excelReader = EasyExcel.read(inputStream).build();
        readExcel(excelReader, clazz, listener);
        return true;
    }

    public static boolean importExcel(File file, Class clazz, BaseListener listener) throws IOException {
        ExcelReader excelReader = EasyExcel.read(file).build();
        readExcel(excelReader, clazz, listener);
        return true;
    }

    public static <T> void exportExcel(OutputStream outputStream, Class<T> clazz, List<T> data) {
        ExcelUtil.exportExcel(outputStream, null, clazz, data);
    }

    public static <T> void exportExcel(OutputStream outputStream, String sheetName, Class<T> clazz, List<T> data) {
        ExcelUtil.exportExcel(outputStream, sheetName, clazz, data, null);
    }

    public static <T> void exportExcel(OutputStream outputStream, String sheetName, Class<T> clazz, List<T> data,
        WriteHandler... writeHandler) {
        writeExcel(outputStream, sheetName, clazz, data, writeHandler);
    }

    public static <T> void exportExcel(HttpServletResponse response, String fileName, Class<T> clazz, List<T> data) {
        ExcelUtil.exportExcel(response, fileName, null, clazz, data);
    }

    public static <T> void exportExcel(HttpServletResponse response, String fileName, String sheetName, Class<T> clazz,
        List<T> data) {
        ExcelUtil.exportExcel(response, fileName, sheetName, clazz, data, null);
    }

    public static <T> void exportExcel(HttpServletResponse response, String fileName, String sheetName, Class<T> clazz,
        List<T> data, WriteHandler... writeHandler) {
        try {
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            fileName = URLEncoder.encode(fileName, "UTF-8");
            response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

            writeExcel(response.getOutputStream(), sheetName, clazz, data, writeHandler);
        } catch (Exception e) {
            log.error("导出excel异常：{}", e);
            // 重置response
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            Map<String, String> map = new HashMap();
            map.put("code", "500");
            map.put("msg", "下载文件失败" + e.getMessage());
            try {
                response.getWriter().println(JSON.toJSONString(map));
            } catch (IOException ex) {
                log.error("导出excel异常1：{}", e);
            }
        }
    }

    private static void readExcel(ExcelReader excelReader, Class clazz, BaseListener listener) {
        try {
            List<ReadSheet> readSheets = excelReader.excelExecutor().sheetList();

            listener.setSheetSize(new AtomicInteger(readSheets.size()));

            ReadSheet[] toReadList = new ReadSheet[readSheets.size()];

            for (int i = 0; i < readSheets.size(); i++) {
                ReadSheet readSheet = readSheets.get(i);
                toReadList[i] = EasyExcel.readSheet(readSheet.getSheetNo()).head(clazz).headRowNumber(1).autoTrim(true)
                    .registerReadListener(listener).build();
            }
            excelReader.read(toReadList);
        } finally {
            if (excelReader != null) {
                excelReader.finish();
            }
        }
    }

    private static <T> void writeExcel(OutputStream outputStream, String sheetName, Class<T> clazz, List<T> data,
        WriteHandler... writeHandler) {
        ExcelWriterBuilder builder = EasyExcel.write(outputStream, clazz);
        if (writeHandler != null && writeHandler.length > 0) {
            for (WriteHandler handler : writeHandler) {
                builder.registerWriteHandler(handler);
            }
        }
        builder.autoCloseStream(Boolean.FALSE).sheet(sheetName == null ? "sheet" : sheetName).doWrite(data);
    }

    public static void main(String[] args) throws FileNotFoundException {

    }
}
