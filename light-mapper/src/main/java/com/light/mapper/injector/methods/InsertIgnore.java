package com.light.mapper.injector.methods;

import org.apache.ibatis.executor.keygen.Jdbc3KeyGenerator;
import org.apache.ibatis.executor.keygen.KeyGenerator;
import org.apache.ibatis.executor.keygen.NoKeyGenerator;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;
import com.light.mapper.injector.ExtendAbstractMethod;
import com.light.mapper.injector.ExtendSqlMethod;

public class InsertIgnore extends ExtendAbstractMethod {
    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        ExtendSqlMethod sqlMethod = ExtendSqlMethod.INSERT_IGNORE_ONE;
        // key生成器处理
        KeyGenerator keyGenerator = new NoKeyGenerator();
        if (tableInfo.havePK()) {
            // PK自增
            if (tableInfo.getIdType() == IdType.AUTO) {
                /* 自增主键 */
                keyGenerator = new Jdbc3KeyGenerator();
            } else if (null != tableInfo.getKeySequence()) {// 指定策略生成
                keyGenerator = TableInfoHelper.genKeyGenerator(sqlMethod.getMethod(), tableInfo, builderAssistant);
            } else {
                // 非自增且未指定策略生成，是否能插入决定于表主键是否允许null
            }
        }

        // 若PK自增则默认不添加PK字段，其他添加
        String columnScript = SqlScriptUtils.convertTrim(tableInfo.getAllInsertSqlColumnMaybeIf(null), LEFT_BRACKET,
            RIGHT_BRACKET, null, COMMA);
        // 若PK自增则默认不添加PK属性，其他添加
        String valuesScript = SqlScriptUtils.convertTrim(tableInfo.getAllInsertSqlPropertyMaybeIf(null), LEFT_BRACKET,
            RIGHT_BRACKET, null, COMMA);

        String sql = String.format(sqlMethod.getSql(), tableInfo.getTableName(), columnScript, valuesScript);
        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);
        return this.addInsertMappedStatement(mapperClass, modelClass, sqlMethod.getMethod(), sqlSource, keyGenerator,
            tableInfo.getKeyProperty(), tableInfo.getKeyColumn());
    }
}
