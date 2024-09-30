package com.light.mapper.util;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.toolkit.ArrayUtils;
import com.light.mapper.entity.IEntity;
import com.light.mapper.entity.Metadata;
import com.light.mapper.entity.annotation.Encrypt;
import com.light.mapper.entity.annotation.TableCache;

public final class EntityUtil {
    private static final Logger logger = LoggerFactory.getLogger(EntityUtil.class);

    private static final ConcurrentMap<Class<?>, List<Field>> CLASS_FIELD_CACHE = new ConcurrentHashMap<>();

    public static Object getValue(Object obj, Field field) {
        Object value = null;
        try {
            value = field.get(obj);
        } catch (Exception e) {
            logger.warn(obj.getClass().getName() + " reflect field get " + field.getName() + " value fail");
        }
        return value;
    }

    public static void setValue(Object obj, Field field, Object value) {
        try {
            field.set(obj, value);
        } catch (Exception e) {
            logger.warn(obj.getClass().getName() + " reflect field set " + field.getName() + " value fail");
        }
    }

    public static List<Field> getClassFields(Class<?> clazz) {
        List<Field> fields = CLASS_FIELD_CACHE.get(clazz);
        if (fields == null)
            synchronized (clazz) {
                if (!CLASS_FIELD_CACHE.containsKey(clazz)) {
                    CLASS_FIELD_CACHE.put(clazz, fields = collectFields(clazz));
                } else {
                    fields = CLASS_FIELD_CACHE.get(clazz);
                }
            }
        return fields;
    }

    private static List<Field> collectFields(Class<?> clazz) {
        List<Field> list = new ArrayList<>();
        List<AccessibleObject> fields = new ArrayList<>();
        Class<?> superClazz = clazz;
        while (superClazz != null && superClazz != Object.class) {
            fields.addAll(Arrays.asList((AccessibleObject[])superClazz.getDeclaredFields()));
            superClazz = superClazz.getSuperclass();
        }
        for (AccessibleObject ao : fields) {
            ao.setAccessible(true);
            Field field = (Field)ao;
            int mod = field.getModifiers();
            if (Modifier.isFinal(mod) || Modifier.isStatic(mod))
                continue;
            TableField tableField = ao.getAnnotation(TableField.class);
            if (tableField != null && !tableField.exist()) {
                continue;
            }
            list.add(field);
        }
        Collections.sort(list, (o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
        return list;
    }

    public static boolean isEncrypt(Field field) {
        Encrypt encrypt = field.getAnnotation(Encrypt.class);
        return (encrypt != null);
    }

    /**
     * 提取泛型模型,多泛型的时候请将泛型T放在第一位
     *
     * @param mapperClass mapper 接口
     * @return mapper 泛型
     */
    public static Class<IEntity> extractModelClass(Class<?> mapperClass) {
        Type[] types = mapperClass.getGenericInterfaces();
        ParameterizedType target = null;
        for (Type type : types) {
            if (type instanceof ParameterizedType) {
                Type[] typeArray = ((ParameterizedType)type).getActualTypeArguments();
                if (ArrayUtils.isNotEmpty(typeArray)) {
                    for (Type t : typeArray) {
                        if (t instanceof TypeVariable || t instanceof WildcardType) {
                            break;
                        } else {
                            target = (ParameterizedType)type;
                            break;
                        }
                    }
                }
                break;
            }
        }
        return target == null ? null : (Class<IEntity>)target.getActualTypeArguments()[0];
    }

    public static Metadata getMetadata(Class<?> clazz) {
        Metadata metadata = new Metadata();
        metadata.setUseCache(false);
        String tableName = clazz.getSimpleName().toLowerCase();
        TableName tableAnnotation = clazz.getAnnotation(TableName.class);
        if (tableAnnotation != null) {
            if (!tableAnnotation.value().equals("")) {
                tableName = tableAnnotation.value();
            }
            TableCache tableCache = clazz.getAnnotation(TableCache.class);
            if (tableCache != null) {
                metadata.setUseCache(true);
            }
        }
        metadata.setTableName(tableName);
        TableId tableId = null;
        Field pkField = null;
        List<Field> fields = getClassFields(clazz);
        if (fields != null && fields.size() > 0) {
            for (Field field : fields) {
                field.setAccessible(true);
                tableId = field.getAnnotation(TableId.class);
                if (tableId != null) {
                    pkField = field;
                    break;
                }
            }
        }
        if (tableId != null) {
            metadata.setIdType(tableId.type());
        }
        metadata.setPkField(pkField);
        return metadata;
    }

    /**
     * 读取实体主键字段
     *
     * @param clazz
     * @return
     */
    public static Field getPKField(Class<?> clazz) {
        List<Field> fields = getClassFields(clazz);
        if (fields != null && fields.size() > 0) {
            for (Field field : fields) {
                field.setAccessible(true);
                TableId tableId = field.getAnnotation(TableId.class);
                if (tableId != null) {
                    return field;
                }
            }
        }
        return null;
    }
}
