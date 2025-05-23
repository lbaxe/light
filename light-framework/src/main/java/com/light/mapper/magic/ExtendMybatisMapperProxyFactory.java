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

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.ibatis.binding.MapperProxyFactory;
import org.apache.ibatis.session.SqlSession;

import lombok.Getter;

/**
 * <p>
 * 从 {@link MapperProxyFactory} copy 过来
 * </p>
 * <li>使用 MybatisMapperMethod</li>
 *
 * @author miemie
 * @since 2018-06-09
 */
public class ExtendMybatisMapperProxyFactory<T> {

    @Getter
    private final Class<T> mapperInterface;
    @Getter
    private final Map<Method, ExtendMybatisMapperProxy.MapperMethodInvoker> methodCache = new ConcurrentHashMap<>();

    public ExtendMybatisMapperProxyFactory(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    @SuppressWarnings("unchecked")
    protected T newInstance(ExtendMybatisMapperProxy<T> mapperProxy) {
        return (T)Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[] {mapperInterface}, mapperProxy);
    }

    public T newInstance(SqlSession sqlSession) {
        final ExtendMybatisMapperProxy<T> mapperProxy =
            new ExtendMybatisMapperProxy<>(sqlSession, mapperInterface, methodCache);
        return newInstance(mapperProxy);
    }

}
