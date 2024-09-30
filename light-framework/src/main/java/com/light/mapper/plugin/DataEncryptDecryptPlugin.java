package com.light.mapper.plugin;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.ParamNameResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baomidou.mybatisplus.core.conditions.AbstractWrapper;
import com.baomidou.mybatisplus.core.toolkit.ReflectionKit;
import com.light.mapper.entity.BaseEntity;
import com.light.mapper.util.EncryptUtil;
import com.light.mapper.util.EntityUtil;

@Intercepts({@Signature(type = ResultSetHandler.class, method = "handleResultSets", args = {Statement.class}),
    @Signature(type = ParameterHandler.class, method = "setParameters", args = PreparedStatement.class)})

// @Intercepts({@Signature(type = StatementHandler.class, method = "update", args = {Statement.class}),
// @Signature(type = StatementHandler.class, method = "query", args = {Statement.class, ResultHandler.class}),
// @Signature(type = StatementHandler.class, method = "batch", args = {Statement.class})})
public class DataEncryptDecryptPlugin implements Interceptor {
    private static final Logger logger = LoggerFactory.getLogger(DataEncryptDecryptPlugin.class);

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object target = invocation.getTarget();
        if (target instanceof ParameterHandler) {// 入参box,增删改都有可能
            ParameterHandler parameterHandler = (ParameterHandler)target;
            this.box(parameterHandler.getParameterObject());
            return invocation.proceed();
        } else if (target instanceof ResultSetHandler) {// 结果集unbox
            Object resultObject = invocation.proceed();
            if (resultObject == null) {
                return resultObject;
            }
            if (resultObject.getClass().getSuperclass().equals(BaseEntity.class)) {
                this.unbox((BaseEntity)resultObject);
            }
            if (resultObject instanceof List) {
                boolean isContainEntity = ((List<?>)resultObject).stream()
                    .anyMatch(e -> e.getClass().getSuperclass().equals(BaseEntity.class));
                if (!isContainEntity) {
                    return resultObject;
                }
                ((List<BaseEntity>)resultObject).forEach(this::unbox);
            }
            return resultObject;
        }
        return null;
    }

    private void box(Object parameterObject) {
        if (null == parameterObject) {
            return;
        }
        // 基本类型不拦截加密处理
        if (ReflectionKit.isPrimitiveOrWrapper(parameterObject.getClass())
            || parameterObject.getClass() == String.class) {
            return;
        }
        if (parameterObject instanceof Collection) {
            ((Collection<?>)parameterObject).stream().forEach(this::box);
            return;
        }
        if (parameterObject instanceof Map) {
            ((Map<?, Object>)parameterObject).entrySet().stream()
                .filter(e -> e.getKey().toString().matches(ParamNameResolver.GENERIC_NAME_PREFIX + "\\d+"))
                .forEach(v -> this.box(v.getValue()));
            return;
        }

        try {
            if (parameterObject instanceof BaseEntity) {
                BaseEntity entity = (BaseEntity)parameterObject;
                List<Field> fields = EntityUtil.getClassFields(entity.getClass());
                for (Field field : fields) {
                    Object obj = field.get(entity);
                    if (EntityUtil.isEncrypt(field) && (obj instanceof String)
                        && EntityUtil.getValue(entity, field) != null) {
                        EntityUtil.setValue(entity, field, EncryptUtil.encrypt((String)obj));
                    }
                }
                return;
            }
            if (parameterObject instanceof AbstractWrapper) {
                AbstractWrapper abstractWrapper = (AbstractWrapper)parameterObject;
                // MergeSegments mergeSegments = abstractWrapper.getExpression();
                Class entityClass = abstractWrapper.getEntityClass();
                if (entityClass == null || abstractWrapper.getEntity() == null) {
                    return;
                }
                List<Field> fields = EntityUtil.getClassFields(entityClass);
                for (Field field : fields) {
                    Object obj = field.get(abstractWrapper.getEntity());
                    if (EntityUtil.isEncrypt(field) && (obj instanceof String)
                        && (EntityUtil.getValue(abstractWrapper.getEntity(), field) != null)) {
                        EntityUtil.setValue(abstractWrapper.getEntity(), field, EncryptUtil.encrypt((String)obj));
                    }
                }
                return;
            }
        } catch (IllegalAccessException e) {
            logger.warn(e.getMessage(), e);
        }
    }

    protected Collection<Object> getParameters(Object parameterObject) {
        Collection<Object> parameters = null;
        if (parameterObject instanceof Collection) {
            parameters = (Collection)parameterObject;
        } else if (parameterObject instanceof Map) {
            Map parameterMap = (Map)parameterObject;
            if (parameterMap.containsKey("collection")) {
                parameters = (Collection)parameterMap.get("collection");
            } else if (parameterMap.containsKey("list")) {
                parameters = (List)parameterMap.get("list");
            } else if (parameterMap.containsKey("array")) {
                parameters = Arrays.asList((Object[])parameterMap.get("array"));
            }
        }
        return parameters;
    }

    public void unbox(BaseEntity entity) {
        List<Field> fields = EntityUtil.getClassFields(entity.getClass());
        try {
            for (Field field : fields) {
                Object obj = field.get(entity);
                if (EntityUtil.isEncrypt(field) && (obj instanceof String) && obj != null) {
                    EntityUtil.setValue(entity, field, EncryptUtil.decrypt((String)obj));
                }
            }
        } catch (IllegalAccessException e) {
            logger.warn(e.getMessage(), e);
        }
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof ResultSetHandler || target instanceof ParameterHandler) {
            return Plugin.wrap(target, this);
        }
        return target;
    }

    @Override
    public void setProperties(Properties properties) {

    }
}
