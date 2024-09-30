package com.light.mapper.util;

import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.TypeParameterResolver;
import org.apache.ibatis.session.Configuration;

import com.baomidou.mybatisplus.core.toolkit.ArrayUtils;
import com.light.mapper.BaseMapper;
import com.light.mapper.entity.IEntity;

public class MapperRelfectorUtil {
    private static final ConcurrentMap<Class<?>, List<Method>> MAPPER_METHOD_CACHE = new ConcurrentHashMap<>();
    private static Map<Method, MapperMethod.MethodSignature> mapperMethodSignatureMap = new ConcurrentHashMap<>();
    private static Map<Method, MapperMethod.SqlCommand> mapperMethodSqlCommandMap = new ConcurrentHashMap<>();

    private static List<Method> collectMethods(Class<?> mapperClazz) {
        List<Method> list = new ArrayList<>();
        List<AccessibleObject> methods = new ArrayList<>();
        Class<?> superClazz = mapperClazz;
        while (superClazz != null && superClazz != Object.class) {
            methods.addAll(Arrays.asList((AccessibleObject[])superClazz.getMethods()));
            superClazz = superClazz.getSuperclass();
        }
        for (AccessibleObject ao : methods) {
            ao.setAccessible(true);
            Method method = (Method)ao;
            int mod = method.getModifiers();
            if (Modifier.isFinal(mod) || Modifier.isStatic(mod)) {
                continue;
            }
            list.add(method);
        }
        // Collections.sort(list, (o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
        return list;
    }

    public static List<Method> getMapperMethods(Class<?> mapperInterface) {
        List<Method> methods = MAPPER_METHOD_CACHE.get(mapperInterface);
        if (methods == null) {
            synchronized (mapperInterface) {
                if (!MAPPER_METHOD_CACHE.containsKey(mapperInterface)) {
                    MAPPER_METHOD_CACHE.put(mapperInterface, methods = collectMethods(mapperInterface));
                } else {
                    methods = MAPPER_METHOD_CACHE.get(mapperInterface);
                }
            }
        }
        return methods;
    }

    public static Method getMapperMethods(Class<?> mapperInterface, String methodName) {
        List<Method> methods = getMapperMethods(mapperInterface);
        return methods.stream().filter(e -> e.getName().equals(methodName)).findFirst().orElse(null);
    }

    public static MapperMethod.MethodSignature getMapperMethodSignature(Configuration config, Class<?> mapperInterface,
        Method method) {
        if (mapperMethodSignatureMap.isEmpty() || !mapperMethodSignatureMap.containsKey(method)) {
            MapperMethod.MethodSignature methodSignature =
                new MapperMethod.MethodSignature(config, mapperInterface, method);
            mapperMethodSignatureMap.put(method, methodSignature);
        }
        return mapperMethodSignatureMap.get(method);
    }

    public static MapperMethod.SqlCommand getMapperMethodSqlCommandMap(Configuration config, Class<?> mapperInterface,
        Method method) {
        if (mapperMethodSqlCommandMap.isEmpty() || !mapperMethodSqlCommandMap.containsKey(method)) {
            MapperMethod.SqlCommand sqlCommand = new MapperMethod.SqlCommand(config, mapperInterface, method);
            mapperMethodSqlCommandMap.put(method, sqlCommand);
        }
        return mapperMethodSqlCommandMap.get(method);
    }

    public static List<String> getBaseMapperSelectEntitySqlMethod() {
        Type[] types = BaseMapper.class.getGenericInterfaces();
        Type target = null;
        for (Type type : types) {
            if (type instanceof ParameterizedType) {
                Type[] typeArray = ((ParameterizedType)type).getActualTypeArguments();
                if (ArrayUtils.isNotEmpty(typeArray)) {
                    for (Type t : typeArray) {
                        if (t instanceof TypeVariable || t instanceof WildcardType) {
                            target = t;
                            break;
                        }
                    }
                }
                break;
            }
        }
        if (target == null) {
            return Collections.emptyList();
        }

        List<Method> methods = getMapperMethods(BaseMapper.class);
        for (Method m : methods) {
            Type returnType = m.getGenericReturnType();
            if (returnType instanceof ParameterizedType) {
                Type t = ((ParameterizedType)returnType).getActualTypeArguments()[0];
                if (t instanceof TypeVariable || t instanceof WildcardType) {
                    Type target1 = t;
                    if (target1.equals(target)) {
                        System.out.println(m.getName());
                    }
                }
            }
        }
        return null;

    }

    /**
     * 是否为继承BaseMapper查询IEntity的方法
     * 
     * @param mapperInterface
     * @param method
     * @return
     */
    public static boolean isBaseMapperSelectEntity(Class<?> mapperInterface, Method method) {
        if (!BaseMapper.class.isAssignableFrom(method.getDeclaringClass())
            && !com.baomidou.mybatisplus.core.mapper.BaseMapper.class.isAssignableFrom(method.getDeclaringClass())) {
            return false;
        }
        if (!isReturnIEntity(mapperInterface, method)) {
            return false;
        }
        return true;
    }

    /**
     * 是否为继承BaseMapper插入IEntity的方法
     *
     * @param mapperInterface
     * @param method
     * @return
     */
    public static boolean isBaseMapperReplaceInsertEntity(Class<?> mapperInterface, Method method) {
        if (!BaseMapper.class.isAssignableFrom(method.getDeclaringClass())
            && !com.baomidou.mybatisplus.core.mapper.BaseMapper.class.isAssignableFrom(method.getDeclaringClass())) {
            return false;
        }
        if (!isIEntityParam0(mapperInterface, method)) {
            return false;
        }
        if (!method.getName().startsWith("replace")) {
            return false;
        }
        return true;
    }

    /**
     * 是否为继承BaseMapper更新IEntity的方法
     *
     * @param mapperInterface
     * @param method
     * @return
     */
    public static boolean isBaseMapperUpdateEntity(Class<?> mapperInterface, Method method) {
        if (!BaseMapper.class.isAssignableFrom(method.getDeclaringClass())
            && !com.baomidou.mybatisplus.core.mapper.BaseMapper.class.isAssignableFrom(method.getDeclaringClass())) {
            return false;
        }
        if (!isIEntityParam0(mapperInterface, method)) {
            return false;
        }
        if (!method.getName().startsWith("update")) {
            return false;
        }
        return true;
    }

    /**
     * 是否为继承BaseMapper删除IEntity的方法
     *
     * @param mapperInterface
     * @param method
     * @return
     */
    public static boolean isBaseMapperDeleteEntity(Class<?> mapperInterface, Method method) {
        if (!BaseMapper.class.isAssignableFrom(method.getDeclaringClass())
            && !com.baomidou.mybatisplus.core.mapper.BaseMapper.class.isAssignableFrom(method.getDeclaringClass())) {
            return false;
        }
        // if (!isIEntityParam0(mapperInterface, method)) {
        // return false;
        // }
        if (!method.getName().startsWith("delete")) {
            return false;
        }
        return true;
    }

    private static boolean isReturnIEntity(Class<?> mapperInterface, Method method) {
        Type resolvedReturnType = TypeParameterResolver.resolveReturnType(method, mapperInterface);
        if (resolvedReturnType instanceof IEntity) {
            return true;
        } else if (resolvedReturnType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = ((ParameterizedType)resolvedReturnType);
            Type[] types = ((ParameterizedType)resolvedReturnType).getActualTypeArguments();
            if (List.class.isAssignableFrom((Class<?>)parameterizedType.getRawType()) && types != null
                && types.length == 1) {
                return types[0] instanceof Class<?> && IEntity.class.isAssignableFrom((Class<?>)types[0]);
            }
        }
        return false;
    }

    private static boolean isIEntityParam0(Class<?> mapperInterface, Method method) {
        Type[] paramTypes = TypeParameterResolver.resolveParamTypes(method, mapperInterface);
        if (paramTypes == null || paramTypes.length == 0) {
            return false;
        }
        if (paramTypes[0] instanceof IEntity) {
            return true;
        } else if (paramTypes[0] instanceof ParameterizedType) {
            ParameterizedType parameterizedType = ((ParameterizedType)paramTypes[0]);
            Type[] types = ((ParameterizedType)paramTypes[0]).getActualTypeArguments();
            if (List.class.isAssignableFrom((Class<?>)parameterizedType.getRawType()) && types != null
                && types.length == 1) {
                return types[0] instanceof Class<?> && IEntity.class.isAssignableFrom((Class<?>)types[0]);
            }
        }
        return false;
    }

    public static MappedStatement resolveMappedStatement(Class<?> mapperInterface, String methodName,
        Class<?> declaringClass, Configuration configuration) {
        String statementId = mapperInterface.getName() + "." + methodName;
        if (configuration.hasStatement(statementId)) {
            return configuration.getMappedStatement(statementId);
        } else if (mapperInterface.equals(declaringClass)) {
            return null;
        }
        for (Class<?> superInterface : mapperInterface.getInterfaces()) {
            if (declaringClass.isAssignableFrom(superInterface)) {
                MappedStatement ms = resolveMappedStatement(superInterface, methodName, declaringClass, configuration);
                if (ms != null) {
                    return ms;
                }
            }
        }
        return null;
    }
}
