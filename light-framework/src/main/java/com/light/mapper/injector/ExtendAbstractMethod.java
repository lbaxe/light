package com.light.mapper.injector;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.baomidou.mybatisplus.core.toolkit.sql.SqlScriptUtils;

public abstract class ExtendAbstractMethod extends AbstractMethod {

    /**
     * EntityWrapper方式获取select where，忽略逻辑删除字段
     *
     * @param newLine 是否提到下一行
     * @param table 表信息
     * @return String
     */
    public String sqlWhereEntityWrapperIgnoreLogicDelete(boolean newLine, TableInfo table) {
        String sqlScript = table.getAllSqlWhere(false, true, WRAPPER_ENTITY_DOT);
        sqlScript = SqlScriptUtils.convertIf(sqlScript, String.format("%s != null", WRAPPER_ENTITY), true);
        sqlScript += NEWLINE;
        sqlScript += SqlScriptUtils.convertIf(
            String.format(
                SqlScriptUtils.convertIf(" AND",
                    String.format("%s and %s", WRAPPER_NONEMPTYOFENTITY, WRAPPER_NONEMPTYOFNORMAL), false) + " ${%s}",
                WRAPPER_SQLSEGMENT),
            String.format("%s != null and %s != '' and %s", WRAPPER_SQLSEGMENT, WRAPPER_SQLSEGMENT,
                WRAPPER_NONEMPTYOFWHERE),
            true);
        sqlScript = SqlScriptUtils.convertWhere(sqlScript) + NEWLINE;
        sqlScript += SqlScriptUtils.convertIf(String.format(" ${%s}", WRAPPER_SQLSEGMENT), String.format(
            "%s != null and %s != '' and %s", WRAPPER_SQLSEGMENT, WRAPPER_SQLSEGMENT, WRAPPER_EMPTYOFWHERE), true);
        sqlScript = SqlScriptUtils.convertIf(sqlScript, String.format("%s != null", WRAPPER), true);
        return newLine ? NEWLINE + sqlScript : sqlScript;
    }

    /**
     * SQL map 查询条件
     */
    protected String sqlWhereByMapIgnoreLogicDelete(TableInfo table) {
        String sqlScript = SqlScriptUtils.convertChoose("v == null", " ${k} IS NULL ", " ${k} = #{v} ");
        sqlScript = SqlScriptUtils.convertForeach(sqlScript, COLUMN_MAP, "k", "v", "AND");
        sqlScript = SqlScriptUtils.convertWhere(sqlScript);
        sqlScript = SqlScriptUtils.convertIf(sqlScript,
            String.format("%s != null and !%s", COLUMN_MAP, COLUMN_MAP_IS_EMPTY), true);
        return sqlScript;
    }
}