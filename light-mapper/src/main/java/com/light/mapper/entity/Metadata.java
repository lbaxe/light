package com.light.mapper.entity;

import java.lang.reflect.Field;

import com.baomidou.mybatisplus.annotation.IdType;

public class Metadata {
    /**
     * 表名
     */
    private String tableName;
    /**
     * 是否启用一级缓存
     */
    private boolean useCache = true;
    /**
     * 主键Field
     */
    private Field pkField;
    /**
     * 主键策略
     */
    private IdType idType;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public boolean isUseCache() {
        return useCache;
    }

    public void setUseCache(boolean useCache) {
        this.useCache = useCache;
    }

    public Field getPkField() {
        return pkField;
    }

    public void setPkField(Field pkField) {
        this.pkField = pkField;
    }

    public IdType getIdType() {
        return idType;
    }

    public void setIdType(IdType idType) {
        this.idType = idType;
    }

    @Override
    public String toString() {
        return "Metadata [tableName='" + tableName + "',useCache=" + useCache + ",pkField=" + pkField + ",idType="
            + idType + "]";
    }
}
