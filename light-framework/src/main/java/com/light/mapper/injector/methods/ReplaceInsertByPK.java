package com.light.mapper.injector.methods;

import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import com.light.mapper.injector.ExtendAbstractMethod;
import com.light.mapper.injector.ExtendSqlMethod;

public class ReplaceInsertByPK extends ExtendAbstractMethod {
    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        KeyGenerator keyGenerator = new NoKeyGenerator();
        ExtendSqlMethod sqlMethod = ExtendSqlMethod.REPLACE_ONE_PK;
        String columnScript = SqlScriptUtils.convertTrim(
            this.getKeyInsertSqlColumn(tableInfo)
                + tableInfo.getFieldList().stream().map(i -> i.getInsertSqlColumnMaybeIf(EMPTY))
                    .filter(Objects::nonNull).collect(Collectors.joining(NEWLINE)),
            LEFT_BRACKET, RIGHT_BRACKET, null, COMMA);
        String valuesScript = SqlScriptUtils.convertTrim(
            this.getKeyInsertSqlProperty(tableInfo)
                + tableInfo.getFieldList().stream().map(i -> i.getInsertSqlPropertyMaybeIf(EMPTY))
                    .filter(Objects::nonNull).collect(Collectors.joining((NEWLINE))),
            LEFT_BRACKET, RIGHT_BRACKET, null, COMMA);

        String sql = String.format(sqlMethod.getSql(), tableInfo.getTableName(), columnScript, valuesScript);
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);

        return this.addInsertMappedStatement(mapperClass, modelClass, sqlMethod.getMethod(), sqlSource, keyGenerator,
            tableInfo.getKeyProperty(), tableInfo.getKeyColumn());
    }

    private String getKeyInsertSqlColumn(TableInfo tableInfo) {
        if (StringUtils.isBlank(tableInfo.getKeyProperty())) {
            return EMPTY;
        }
        return tableInfo.getKeyColumn() + COMMA + NEWLINE;
    }

    private String getKeyInsertSqlProperty(TableInfo tableInfo) {
        if (StringUtils.isBlank(tableInfo.getKeyProperty())) {
            return EMPTY;
        }
        return SqlScriptUtils.safeParam(tableInfo.getKeyProperty()) + COMMA + NEWLINE;
    }
}
