/*
 * Copyright (c) 2011-2021, baomidou (jobob@qq.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.light.mapper.magic;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.*;

import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ParamNameResolver;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baomidou.mybatisplus.core.enums.SqlMethod;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.PageList;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.light.mapper.entity.IEntity;
import com.light.mapper.exception.MapperException;
import com.light.mapper.injector.ExtendSqlMethod;
import com.light.mapper.support.OpCacheProxy;
import com.light.mapper.util.EntityUtil;
import com.light.mapper.util.MapperRelfectorUtil;
import com.light.mapper.util.SqlParseUtil;

/**
 * 从 {@link MapperMethod} copy 过来 </br>
 * <p>
 * 不要内部类 ParamMap
 * </p>
 * <p>
 * 不要内部类 SqlCommand
 * </p>
 * <p>
 * 不要内部类 MethodSignature
 * </p>
 *
 * @author miemie
 * @since 2018-06-09
 */
public class ExtendMybatisMapperMethod {
    private static Logger logger = LoggerFactory.getLogger(ExtendMybatisMapperMethod.class);
    private final MapperMethod.SqlCommand command;
    private final MapperMethod.MethodSignature method;
    private final Class<?> mapperInterface;
    private final Method mapperMethod;
    private final Configuration config;
    private final OpCacheProxy opCacheProxy;

    public ExtendMybatisMapperMethod(Class<?> mapperInterface, Method method, Configuration config) {
        this.mapperInterface = mapperInterface;
        this.mapperMethod = method;
        this.config = config;
        this.command = MapperRelfectorUtil.getMapperMethodSqlCommandMap(config, mapperInterface, method);
        this.method = MapperRelfectorUtil.getMapperMethodSignature(config, mapperInterface, method);
        this.opCacheProxy = new OpCacheProxy(config);
    }

    public Object execute(SqlSession sqlSession, Object[] args) {
        Object result;
        switch (command.getType()) {
            case INSERT: {
                result = rowCountResult(this._executeForInsert(sqlSession, mapperMethod, args));
                break;
            }
            case UPDATE: {
                result = rowCountResult(this._executeForUpdate(sqlSession, mapperMethod, args));
                break;
            }
            case DELETE: {
                result = rowCountResult(this._executeForDelete(sqlSession, mapperMethod, args));
                break;
            }
            case SELECT:
                if (method.returnsVoid() && method.hasResultHandler()) {
                    executeWithResultHandler(sqlSession, args);
                    result = null;
                } else if (method.returnsMany()) {
                    // result = executeForMany(sqlSession, args);
                    result = this.executeForMany4Cache(sqlSession, args);
                } else if (method.returnsMap()) {
                    result = executeForMap(sqlSession, args);
                } else if (method.returnsCursor()) {
                    result = executeForCursor(sqlSession, args);
                } else {
                    // TODO 这里下面改了
                    if (IPage.class.isAssignableFrom(method.getReturnType())) {
                        result = executeForIPage(sqlSession, args);
                        // TODO 这里上面改了
                    } else {
                        Object param = method.convertArgsToSqlCommandParam(args);
                        result = sqlSession.selectOne(command.getName(), param);
                        if (method.returnsOptional()
                            && (result == null || !method.getReturnType().equals(result.getClass()))) {
                            result = Optional.ofNullable(result);
                        }
                    }
                }
                break;
            case FLUSH:
                result = sqlSession.flushStatements();
                break;
            default:
                throw new BindingException("Unknown execution method for: " + command.getName());
        }
        if (result == null && method.getReturnType().isPrimitive() && !method.returnsVoid()) {
            throw new BindingException("Mapper method '" + command.getName()
                + " attempted to return null from a method with a primitive return type (" + method.getReturnType()
                + ").");
        }
        return result;
    }

    @SuppressWarnings("all")
    private <E> Object executeForIPage(SqlSession sqlSession, Object[] args) {
        IPage<E> result = null;
        for (Object arg : args) {
            if (arg instanceof IPage) {
                result = (IPage<E>)arg;
                break;
            }
        }
        Assert.notNull(result, "can't found IPage for args!");
        Object param = method.convertArgsToSqlCommandParam(args);
        List<E> list = sqlSession.selectList(command.getName(), param);
        if (list instanceof PageList) {
            PageList<E> pageList = (PageList<E>)list;
            result.setRecords(pageList.getRecords());
            result.setTotal(pageList.getTotal());
        } else {
            result.setRecords(list);
        }
        return result;
    }

    private Object rowCountResult(int rowCount) {
        final Object result;
        if (method.returnsVoid()) {
            result = null;
        } else if (Integer.class.equals(method.getReturnType()) || Integer.TYPE.equals(method.getReturnType())) {
            result = rowCount;
        } else if (Long.class.equals(method.getReturnType()) || Long.TYPE.equals(method.getReturnType())) {
            result = (long)rowCount;
        } else if (Boolean.class.equals(method.getReturnType()) || Boolean.TYPE.equals(method.getReturnType())) {
            result = rowCount > 0;
        } else {
            throw new BindingException(
                "Mapper method '" + command.getName() + "' has an unsupported return type: " + method.getReturnType());
        }
        return result;
    }

    private void executeWithResultHandler(SqlSession sqlSession, Object[] args) {
        MappedStatement ms = sqlSession.getConfiguration().getMappedStatement(command.getName());
        if (!StatementType.CALLABLE.equals(ms.getStatementType())
            && void.class.equals(ms.getResultMaps().get(0).getType())) {
            throw new BindingException(
                "method " + command.getName() + " needs either a @ResultMap annotation, a @ResultType annotation,"
                    + " or a resultType attribute in XML so a ResultHandler can be used as a parameter.");
        }
        Object param = method.convertArgsToSqlCommandParam(args);
        if (method.hasRowBounds()) {
            RowBounds rowBounds = method.extractRowBounds(args);
            sqlSession.select(command.getName(), param, rowBounds, method.extractResultHandler(args));
        } else {
            sqlSession.select(command.getName(), param, method.extractResultHandler(args));
        }
    }

    /**
     * 覆写原mybatisplus原方法
     * 
     * @param sqlSession
     * @param args
     * @param <E>
     * @return
     */
    private Object executeForMany(SqlSession sqlSession, Object[] args) {
        return this._executeForMany(sqlSession, mapperMethod, args);
    }

    /**
     * 支持返回多值的mapper指定方法调用
     * 
     * @param sqlSession
     * @param method
     * @param args
     * @param <E>
     * @return
     */
    private <E> Object _executeForMany(SqlSession sqlSession, Method method, Object[] args) {
        MapperMethod.MethodSignature methodSignature =
            MapperRelfectorUtil.getMapperMethodSignature(config, mapperInterface, method);
        MapperMethod.SqlCommand sqlCommand =
            MapperRelfectorUtil.getMapperMethodSqlCommandMap(config, mapperInterface, method);
        List<E> result;
        Object param = methodSignature.convertArgsToSqlCommandParam(args);
        if (methodSignature.hasRowBounds()) {
            RowBounds rowBounds = methodSignature.extractRowBounds(args);
            result = sqlSession.selectList(sqlCommand.getName(), param, rowBounds);
        } else {
            result = sqlSession.selectList(sqlCommand.getName(), param);
        }
        // issue #510 Collections & arrays support
        if (!method.getReturnType().isAssignableFrom(result.getClass())) {
            if (method.getReturnType().isArray()) {
                return convertToArray(result);
            } else {
                return convertToDeclaredCollection(sqlSession.getConfiguration(), result);
            }
        }
        return result;
    }

    private <T> Cursor<T> executeForCursor(SqlSession sqlSession, Object[] args) {
        Cursor<T> result;
        Object param = method.convertArgsToSqlCommandParam(args);
        if (method.hasRowBounds()) {
            RowBounds rowBounds = method.extractRowBounds(args);
            result = sqlSession.selectCursor(command.getName(), param, rowBounds);
        } else {
            result = sqlSession.selectCursor(command.getName(), param);
        }
        return result;
    }

    private <E> Object convertToDeclaredCollection(Configuration config, List<E> list) {
        Object collection = config.getObjectFactory().create(method.getReturnType());
        MetaObject metaObject = config.newMetaObject(collection);
        metaObject.addAll(list);
        return collection;
    }

    @SuppressWarnings("unchecked")
    private <E> Object convertToArray(List<E> list) {
        Class<?> arrayComponentType = method.getReturnType().getComponentType();
        Object array = Array.newInstance(arrayComponentType, list.size());
        if (arrayComponentType.isPrimitive()) {
            for (int i = 0; i < list.size(); i++) {
                Array.set(array, i, list.get(i));
            }
            return array;
        } else {
            return list.toArray((E[])array);
        }
    }

    private <K, V> Map<K, V> executeForMap(SqlSession sqlSession, Object[] args) {
        Map<K, V> result;
        Object param = method.convertArgsToSqlCommandParam(args);
        if (method.hasRowBounds()) {
            RowBounds rowBounds = method.extractRowBounds(args);
            result = sqlSession.selectMap(command.getName(), param, method.getMapKey(), rowBounds);
        } else {
            result = sqlSession.selectMap(command.getName(), param, method.getMapKey());
        }
        return result;
    }

    private Object executeForMany4Cache(SqlSession sqlSession, Object[] args) {
        boolean isBaseMapperSelectEntity = MapperRelfectorUtil.isBaseMapperSelectEntity(mapperInterface, mapperMethod);
        Class<IEntity> entityClazz = EntityUtil.extractModelClass(mapperInterface);
        if (entityClazz == null) {
            System.out.println(mapperInterface.getSimpleName() + "." + mapperMethod.getName() + " = 未配置实体，查库");
            return executeForMany(sqlSession, args);
        }
        boolean isUseCache = opCacheProxy.useCache(EntityUtil.getMetadata(entityClazz));
        if (!isUseCache) {
            System.out
                .println(mapperInterface.getSimpleName() + "." + mapperMethod.getName() + " = 实体未配置缓存或未配置缓存客户端，查库");
            return executeForMany(sqlSession, args);
        }
        if (!isBaseMapperSelectEntity) {
            System.out
                .println(mapperInterface.getSimpleName() + "." + mapperMethod.getName() + " = 非BaseMapper实体查询，查库");
            return executeForMany(sqlSession, args);
        }

        List<IEntity> result = new ArrayList<>();
        List<Object> pkList = this.selectPKList(sqlSession, args);
        if (pkList == null || pkList.isEmpty()) {
            System.out.println(
                mapperInterface.getSimpleName() + "." + mapperMethod.getName() + " = BaseMapper接口实体查询，主键查询空，查库");
            return convertToDeclaredCollection(sqlSession.getConfiguration(), result);
        }
        // 从缓存获取
        List<IEntity> cacheList = opCacheProxy.getByPks(pkList, entityClazz);
        // 缓存获取为空
        if (cacheList == null || cacheList.isEmpty()) {
            System.out.println(
                mapperInterface.getSimpleName() + "." + mapperMethod.getName() + " = BaseMapper接口实体查询，缓存未命中，查库");
            result = (List<IEntity>)this.executeForMany(sqlSession, args);
            // 如果结果不为空放入缓存
            if (result != null && !result.isEmpty()) {
                opCacheProxy.setByPKs(result);
            }
        } else {
            // 命中缓存或部分命中
            List<Object> pksIndb = new ArrayList<>();
            for (Object pk : pkList) {
                // 从缓存取值
                IEntity entity = opCacheProxy.getByPk(pk, entityClazz);
                if (entity == null) {
                    pksIndb.add(pk);
                    continue;
                }
                result.add(entity);
            }
            if (pksIndb != null && !pksIndb.isEmpty()) {
                System.out.println(
                    mapperInterface.getSimpleName() + "." + mapperMethod.getName() + " = BaseMapper接口实体查询，部分缓存命中，查库补充");
                // 根据pks查询数据库
                List<IEntity> listIndb = (List<IEntity>)this.selectBatchIds(sqlSession, new Object[] {pksIndb});
                if (listIndb != null && !listIndb.isEmpty()) {
                    result.addAll(listIndb);
                    opCacheProxy.setByPKs(result);
                }
            } else {
                System.out.println(
                    mapperInterface.getSimpleName() + "." + mapperMethod.getName() + " = BaseMapper接口实体查询，缓存全命中");
            }
        }
        // 按照主键排序
        Collections.sort(result, Comparator.comparing(o -> o.pkValue().toString()));
        return convertToDeclaredCollection(sqlSession.getConfiguration(), result);
    }

    private <PK> List<PK> selectPKList(SqlSession sqlSession, Object[] args) {
        // 当method参数类型支持wrapper或者map类型
        Object param = method.convertArgsToSqlCommandParam(args);
        List<PK> result = null;
        if (!(param instanceof MapperMethod.ParamMap)) {
            return result;
        }
        MapperMethod.ParamMap paramMap = (MapperMethod.ParamMap)param;
        if (paramMap.containsKey(Constants.WRAPPER)) {
            Method pkMethod =
                MapperRelfectorUtil.getMapperMethods(mapperInterface, ExtendSqlMethod.SELECT_PK_LIST.getMethod());
            result = (List<PK>)this._executeForMany(sqlSession, pkMethod, args);
        } else if (paramMap.containsKey(Constants.COLLECTION)) {
            Method pkMethod = MapperRelfectorUtil.getMapperMethods(mapperInterface,
                ExtendSqlMethod.SELECT_PK_LIST_BY_IDS.getMethod());
            result = (List<PK>)this._executeForMany(sqlSession, pkMethod, args);
        } else if (paramMap.containsKey(Constants.COLUMN_MAP)) {
            Method pkMethod = MapperRelfectorUtil.getMapperMethods(mapperInterface,
                ExtendSqlMethod.SELECT_PK_LIST_BY_MAP.getMethod());
            result = (List<PK>)this._executeForMany(sqlSession, pkMethod, args);
        }
        return result;
    }

    private Object selectBatchIds(SqlSession sqlSession, Object[] args) {
        Method batchMethod =
            MapperRelfectorUtil.getMapperMethods(mapperInterface, SqlMethod.SELECT_BATCH_BY_IDS.getMethod());
        return this._executeForMany(sqlSession, batchMethod, args);
    }

    /**
     * 支持插入操作的mapper指定方法调用
     *
     * @param sqlSession
     * @param method
     * @param args
     * @param <E>
     * @return
     */
    private int _executeForInsert(SqlSession sqlSession, Method method, Object[] args) {
        Class<IEntity> entityClass = EntityUtil.extractModelClass(mapperInterface);
        if (entityClass == null) {
            throw new MapperException(mapperInterface.getName() + "未指定泛型的实体类型");
        }
        MapperMethod.MethodSignature methodSignature =
            MapperRelfectorUtil.getMapperMethodSignature(config, mapperInterface, method);
        MapperMethod.SqlCommand sqlCommand =
            MapperRelfectorUtil.getMapperMethodSqlCommandMap(config, mapperInterface, method);

        Object param = methodSignature.convertArgsToSqlCommandParam(args);
        int result = sqlSession.insert(sqlCommand.getName(), param);

        boolean isUseCache = EntityUtil.getMetadata(entityClass).isUseCache();
        if (isUseCache) {
            boolean isBaseMapperReplaceInsertEntity =
                MapperRelfectorUtil.isBaseMapperReplaceInsertEntity(mapperInterface, method);
            if (isBaseMapperReplaceInsertEntity) {
                List<Object> pkList = new ArrayList<>();
                if (param instanceof IEntity) {
                    IEntity entity = (IEntity)param;
                    pkList.add(entity.pkValue());
                } else if (param instanceof MapperMethod.ParamMap) {
                    MapperMethod.ParamMap paramMap = (MapperMethod.ParamMap)param;
                    Object object = paramMap.get("list");
                    if (object != null) {
                        List<IEntity> entityList = (List<IEntity>)object;
                        entityList.forEach(entity -> {
                            pkList.add(entity.pkValue());
                        });
                    }
                }
                opCacheProxy.delByPks(pkList, EntityUtil.extractModelClass(mapperInterface));
            }
        }
        return result;

    }

    /**
     * 支持更新操作的mapper指定方法调用
     *
     * @param sqlSession
     * @param method
     * @param args
     * @param <E>
     * @return
     */
    private int _executeForUpdate(SqlSession sqlSession, Method method, Object[] args) {
        Class<IEntity> entityClass = EntityUtil.extractModelClass(mapperInterface);
        if (entityClass == null) {
            throw new MapperException(mapperInterface.getName() + "未指定泛型的实体类型");
        }
        MapperMethod.MethodSignature methodSignature =
            MapperRelfectorUtil.getMapperMethodSignature(config, mapperInterface, method);
        MapperMethod.SqlCommand sqlCommand =
            MapperRelfectorUtil.getMapperMethodSqlCommandMap(config, mapperInterface, method);

        Object param = methodSignature.convertArgsToSqlCommandParam(args);
        int result = sqlSession.update(sqlCommand.getName(), param);

        boolean isUseCache = EntityUtil.getMetadata(entityClass).isUseCache();
        if (isUseCache) {
            boolean isBaseMapperUpdateEntity = MapperRelfectorUtil.isBaseMapperUpdateEntity(mapperInterface, method);
            if (isBaseMapperUpdateEntity) {
                if (method.getName().equals(SqlMethod.UPDATE_BY_ID.getMethod())) {
                    if (param instanceof MapperMethod.ParamMap) {
                        List<Object> pkList = new ArrayList<>();
                        MapperMethod.ParamMap paramMap = (MapperMethod.ParamMap)param;
                        Object object = paramMap.get(Constants.ENTITY);
                        if (object != null) {
                            IEntity entity1 = (IEntity)object;
                            pkList.add(entity1.pkValue());
                        }
                        opCacheProxy.delByPks(pkList, entityClass);
                    }
                } else if (method.getName().equals(SqlMethod.UPDATE.getMethod())) {
                    if (entityClass != null) {
                        opCacheProxy.delAll(entityClass);
                    }
                } else {
                    throw new MapperException(method.getName() + "不支持的方法调用");
                }
            } else {
                MappedStatement ms = MapperRelfectorUtil.resolveMappedStatement(mapperInterface, method.getName(),
                    method.getDeclaringClass(), config);
                List<String> tables = SqlParseUtil
                    .getTableList(ms.getBoundSql(ParamNameResolver.wrapToMapIfCollection(param, null)).getSql());
                opCacheProxy.delAll(tables);
            }
        }
        return result;
    }

    /**
     * 支持删除操作的mapper指定方法调用
     *
     * @param sqlSession
     * @param method
     * @param args
     * @param <E>
     * @return
     */
    private int _executeForDelete(SqlSession sqlSession, Method method, Object[] args) {
        Class<IEntity> entityClass = EntityUtil.extractModelClass(mapperInterface);
        if (entityClass == null) {
            throw new MapperException(mapperInterface.getName() + "未指定泛型的实体类型");
        }
        MapperMethod.MethodSignature methodSignature =
            MapperRelfectorUtil.getMapperMethodSignature(config, mapperInterface, method);
        MapperMethod.SqlCommand sqlCommand =
            MapperRelfectorUtil.getMapperMethodSqlCommandMap(config, mapperInterface, method);

        Object param = methodSignature.convertArgsToSqlCommandParam(args);
        int result = sqlSession.delete(sqlCommand.getName(), param);

        boolean isUseCache = EntityUtil.getMetadata(entityClass).isUseCache();
        if (isUseCache) {
            boolean isBaseMapperDeleteEntity = MapperRelfectorUtil.isBaseMapperDeleteEntity(mapperInterface, method);
            if (isBaseMapperDeleteEntity) {
                if (method.getName().equals(SqlMethod.DELETE_BY_ID.getMethod())
                    || method.getName().equals(ExtendSqlMethod.DELETE_BY_ID_IGNORE_LOGIC_DELETE.getMethod())) {
                    List<Object> pkList = new ArrayList<>();
                    pkList.add(param);
                    opCacheProxy.delByPks(pkList, entityClass);
                } else if (method.getName().equals(SqlMethod.DELETE_BATCH_BY_IDS.getMethod())
                    || method.getName().equals(ExtendSqlMethod.DELETE_BATCH_IDS_IGNORE_LOGIC_DELETE.getMethod())) {
                    if (param instanceof MapperMethod.ParamMap) {
                        MapperMethod.ParamMap paramMap = (MapperMethod.ParamMap)param;
                        List<Object> pkList = (List<Object>)paramMap.get(Constants.COLLECTION);
                        opCacheProxy.delByPks(pkList, entityClass);
                    }
                } else {
                    throw new MapperException(method.getName() + "不支持的方法调用");
                }
            } else {
                MappedStatement ms = MapperRelfectorUtil.resolveMappedStatement(mapperInterface, method.getName(),
                    method.getDeclaringClass(), config);
                if (ms != null) {
                    throw new MapperException(method.getName() + "不支持的方法调用1");
                }
                BoundSql boundSql = ms.getBoundSql(ParamNameResolver.wrapToMapIfCollection(param, null));
                if (boundSql != null) {
                    throw new MapperException(method.getName() + "sql语法问题");
                }
                List<String> tables = SqlParseUtil
                    .getTableList(ms.getBoundSql(ParamNameResolver.wrapToMapIfCollection(param, null)).getSql());
                opCacheProxy.delAll(tables);
            }
        }
        return result;
    }
}
