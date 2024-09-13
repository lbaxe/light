package com.light.mapper.sql;

import java.sql.PreparedStatement;

public final class SqlStopWatch {
    private String dbName;

    private String tableName;

    private long beginTime;

    private long endTime;

    private long opTime;

    private String type;

    private String templateSql;

    private int size = 0;

    private String traceId;

    public void setType(String type) {
        this.type = type;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getTemplateSql() {
        return this.templateSql;
    }

    public void setTemplateSql(String setTemplateSql) {
        this.templateSql = setTemplateSql;
    }

    public void setTemplateSql(PreparedStatement ps) {
        String sql = ps.toString();
        int pos = sql.indexOf(": ");
        sql = sql.substring(pos + 2);
    }

    public static final String template() {
        return "The default log template is dbName={1} #$# tableName={2} #$# type={3} #$# beginTime={4} #$# opTime={5} #$# size={6} #$# templateSql={7}";
    }

    public String sqlLog() {
        StringBuilder sb = new StringBuilder();
        sb.append("dbName=").append(this.dbName).append(" #$# ");
        sb.append("tableName=").append(this.tableName).append(" #$# ");
        sb.append("type=").append(this.type).append(" #$# ");
        sb.append("beginTime=").append(this.beginTime).append(" #$# ");
        sb.append("opTime=").append(this.opTime).append("ms").append(" #$# ");
        sb.append("size=").append(this.size).append(" #$# ");
        sb.append("templateSql=").append(this.templateSql);
        return sb.toString();
    }

    public void begin() {
        this.beginTime = System.currentTimeMillis();
    }

    public void end() {
        this.endTime = System.currentTimeMillis();
        this.opTime = this.endTime - this.beginTime;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void reset() {
        this.dbName = null;
        this.tableName = null;
        this.beginTime = 0L;
        this.endTime = 0L;
        this.opTime = 0L;
        this.type = null;
        this.templateSql = null;
        this.size = 0;
        this.traceId = null;
    }
}
