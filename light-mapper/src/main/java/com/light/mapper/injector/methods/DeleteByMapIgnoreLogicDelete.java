package com.light.mapper.injector.methods;

import java.util.Map;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.light.mapper.injector.ExtendAbstractMethod;
import com.light.mapper.injector.ExtendSqlMethod;

public class DeleteByMapIgnoreLogicDelete extends ExtendAbstractMethod {

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        ExtendSqlMethod sqlMethod = ExtendSqlMethod.DELETE_BY_MAP_IGNORE_LOGIC_DELETE;
        String sql =
            String.format(sqlMethod.getSql(), tableInfo.getTableName(), this.sqlWhereByMapIgnoreLogicDelete(tableInfo));
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, Map.class);
        return this.addDeleteMappedStatement(mapperClass, sqlMethod.getMethod(), sqlSource);
    }
}