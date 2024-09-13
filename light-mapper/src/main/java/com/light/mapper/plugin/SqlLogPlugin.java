package com.light.mapper.plugin;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Properties;

import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;

import com.light.mapper.sql.SqlStopWatch;
import com.light.mapper.sql.ThreadLocalSqlStopWatch;

/*
 * @Intercepts({ @Signature(type = StatementHandler.class, method = "update", args = { Statement.class }),
 * 
 * @Signature(type = StatementHandler.class, method = "query", args = { Statement.class, ResultHandler.class }), })
 */
public class SqlLogPlugin implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object target = invocation.getTarget();
        Object[] args = invocation.getArgs();
        Method method = invocation.getMethod();
        if (target instanceof StatementHandler) {
            boolean isUpdate = "update".equals(method.getName());
            boolean isQuery = "query".equals(method.getName());

            final StatementHandler statementHandler = (StatementHandler)target;
            BoundSql boundSql = statementHandler.getBoundSql();
            SqlStopWatch watch = ThreadLocalSqlStopWatch.current();
            watch.reset();
            watch.setDbName("dbName");
            watch.setTableName("tableName");
            watch.setType(method.getName());
            watch.setTemplateSql(boundSql.getSql().replaceAll("\\s{1,}", " "));

            watch.begin();
            Object object = invocation.proceed();
            watch.end();
            if (isUpdate) {// update
                watch.setSize((int)object);
            } else if (isQuery) {// query
                watch.setSize(((List)object).size());
            } else {
                watch.setSize(0);
            }
            // SqlLogger.log(watch);
            return object;
        }
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof StatementHandler) {
            return Plugin.wrap(target, this);
        }
        return target;
    }

    @Override
    public void setProperties(Properties properties) {

    }
}
