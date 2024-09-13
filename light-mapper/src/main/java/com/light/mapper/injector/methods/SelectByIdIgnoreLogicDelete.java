package com.light.mapper.injector.methods;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.defaults.RawSqlSource;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.light.mapper.injector.ExtendAbstractMethod;
import com.light.mapper.injector.ExtendSqlMethod;

public class SelectByIdIgnoreLogicDelete extends ExtendAbstractMethod {

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        ExtendSqlMethod sqlMethod = ExtendSqlMethod.SELECT_BY_ID_IGNORE_LOGIC_DELETE;
        SqlSource sqlSource =
            new RawSqlSource(
                configuration, String.format(sqlMethod.getSql(), sqlSelectColumns(tableInfo, false),
                    tableInfo.getTableName(), tableInfo.getKeyColumn(), tableInfo.getKeyProperty(), EMPTY),
                Object.class);
        return this.addSelectMappedStatementForTable(mapperClass, sqlMethod.getMethod(), sqlSource, tableInfo);
    }
}