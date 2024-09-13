package com.light.mapper.injector.methods;

import java.util.Map;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.light.mapper.injector.ExtendAbstractMethod;
import com.light.mapper.injector.ExtendSqlMethod;

public class SelectByMapIgnoreLogicDelete extends ExtendAbstractMethod {

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        ExtendSqlMethod sqlMethod = ExtendSqlMethod.SELECT_BY_MAP_IGNORE_LOGIC_DELETE;
        String sql = String.format(sqlMethod.getSql(), sqlSelectColumns(tableInfo, false), tableInfo.getTableName(),
            sqlWhereByMapIgnoreLogicDelete(tableInfo));
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, Map.class);
        return this.addSelectMappedStatementForTable(mapperClass, sqlMethod.getMethod(), sqlSource, tableInfo);
    }
}