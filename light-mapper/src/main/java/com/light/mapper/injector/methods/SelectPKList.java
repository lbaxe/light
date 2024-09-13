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
package com.light.mapper.injector.methods;

import java.lang.reflect.Field;

import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;

import com.baomidou.mybatisplus.core.metadata.TableInfo;
import com.light.mapper.injector.ExtendAbstractMethod;
import com.light.mapper.injector.ExtendSqlMethod;
import com.light.mapper.util.EntityUtil;

/**
 * 查询满足条件所有数据
 *
 * @author hubin
 * @since 2018-04-06
 */
public class SelectPKList extends ExtendAbstractMethod {

    @Override
    public MappedStatement injectMappedStatement(Class<?> mapperClass, Class<?> modelClass, TableInfo tableInfo) {
        ExtendSqlMethod sqlMethod = ExtendSqlMethod.SELECT_PK_LIST;
        String sql = String.format(sqlMethod.getSql(), sqlFirst(), tableInfo.getKeyColumn(), tableInfo.getTableName(),
            sqlWhereEntityWrapper(true, tableInfo), sqlComment());

        SqlSource sqlSource = languageDriver.createSqlSource(configuration, sql, modelClass);
        Field pkField = EntityUtil.getPKField(modelClass);
        return this.addSelectMappedStatementForOther(mapperClass, sqlMethod.getMethod(), sqlSource, pkField.getType());
    }
}
