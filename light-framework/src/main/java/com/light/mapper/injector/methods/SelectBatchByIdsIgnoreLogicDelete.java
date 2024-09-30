package com.light.mapper.injector.methods;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import com.light.mapper.injector.ExtendAbstractMethod;
import com.light.mapper.injector.ExtendSqlMethod;

public class SelectBatchByIdsIgnoreLogicDelete extends ExtendAbstractMethod {

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        ExtendSqlMethod sqlMethod = ExtendSqlMethod.SELECT_BATCH_BY_IDS_IGNORE_LOGIC_DELETE;
        SqlSource sqlSource = languageDriver.createSqlSource(configuration,
            String.format(sqlMethod.getSql(), sqlSelectColumns(tableInfo, false), tableInfo.getTableName(),
                tableInfo.getKeyColumn(), SqlScriptUtils.convertForeach("#{item}", COLLECTION, null, "item", COMMA),
                EMPTY),
            Object.class);
        return addSelectMappedStatementForTable(mapperClass, sqlMethod.getMethod(), sqlSource, tableInfo);
    }
}