package com.light.mapper.plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.statement.PreparedStatementHandler;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

@Intercepts({
    @Signature(type = Executor.class, method = "query",
        args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
    @Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class}),
    @Signature(type = ParameterHandler.class, method = "setParameters", args = PreparedStatement.class),})
public class TenantPlugin implements Interceptor {
    private static final String TENANT_ID = "tenantId";

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object target = invocation.getTarget();
        String methodName = invocation.getMethod().getName();
        if (target instanceof Executor && methodName.equals("query") && invocation.getArgs().length == 4) {
            return doQuery(invocation);
        }
        if (target instanceof StatementHandler) {
            return changeBoundSql(invocation);
        }
        if (target instanceof ParameterHandler) {
            return doSetParameter(invocation);
        }
        return null;
    }

    private Object doQuery(Invocation invocation) throws Exception {
        Executor executor = (Executor)invocation.getTarget();
        MappedStatement ms = (MappedStatement)invocation.getArgs()[0];
        Object paramObj = invocation.getArgs()[1];
        RowBounds rowBounds = (RowBounds)invocation.getArgs()[2];

        if (paramObj instanceof Map) {
            MapperMethod.ParamMap paramMap = (MapperMethod.ParamMap)paramObj;
            if (!paramMap.containsKey(TENANT_ID)) {
                Long tenantId = 1L;
                paramMap.put("param" + (paramMap.size() / 2 + 1), tenantId);
                paramMap.put(TENANT_ID, tenantId);
                paramObj = paramMap;
            }
        }
        // 直接执行query，不用proceed()方法
        return executor.query(ms, paramObj, rowBounds, null);
    }

    private Object changeBoundSql(Invocation invocation) throws Exception {
        StatementHandler statementHandler = (StatementHandler)invocation.getTarget();
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        PreparedStatementHandler preparedStatementHandler = (PreparedStatementHandler)metaObject.getValue("delegate");
        String originalSql = (String)metaObject.getValue("delegate.boundSql.sql");
        metaObject.setValue("delegate.boundSql.sql", originalSql + " and tenant_id=?");
        return invocation.proceed();
    }

    private Object doSetParameter(Invocation invocation) throws Exception {
        ParameterHandler parameterHandler = (ParameterHandler)invocation.getTarget();
        PreparedStatement ps = (PreparedStatement)invocation.getArgs()[0];
        MetaObject metaObject = SystemMetaObject.forObject(parameterHandler);
        BoundSql boundSql = (BoundSql)metaObject.getValue("boundSql");

        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        boolean hasTenantId = false;
        for (ParameterMapping parameterMapping : parameterMappings) {
            if (parameterMapping.getProperty().equals(TENANT_ID)) {
                hasTenantId = true;
            }
        }
        // 添加参数
        if (!hasTenantId) {
            Configuration conf = (Configuration)metaObject.getValue("configuration");
            ParameterMapping parameterMapping = new ParameterMapping.Builder(conf, TENANT_ID, Long.class).build();
            parameterMappings.add(parameterMapping);
        }
        parameterHandler.setParameters(ps);
        return null;
    }
}