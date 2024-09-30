package com.light.mapper.sql;

public class ThreadLocalSqlStopWatch {
    private static final ThreadLocal<SqlStopWatch> local = new ThreadLocal<SqlStopWatch>() {
        @Override
        protected SqlStopWatch initialValue() {
            return new SqlStopWatch();
        }
    };

    public static SqlStopWatch current() {
        return local.get();
    }
}