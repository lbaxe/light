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
package com.light.mapper.injector;

import java.util.List;

import com.baomidou.mybatisplus.core.injector.AbstractMethod;
import com.baomidou.mybatisplus.core.injector.DefaultSqlInjector;
import com.light.mapper.injector.methods.*;

public class ExtendSqlInjector extends DefaultSqlInjector {
    @Override
    public List<AbstractMethod> getMethodList(Class<?> mapperClass) {
        List<AbstractMethod> methodList = super.getMethodList(mapperClass);
        methodList.add(new SelectPKList());
        methodList.add(new SelectPKListByIds());
        methodList.add(new SelectPKListByMap());
        methodList.add(new SelectFirst());
        methodList.add(new SelectFirstIgnoreLogicDelete());
        methodList.add(new SelectByIdIgnoreLogicDelete());
        methodList.add(new SelectBatchByIdsIgnoreLogicDelete());
        methodList.add(new SelectByMapIgnoreLogicDelete());
        methodList.add(new SelectListIgnoreLogicDelete());
        methodList.add(new SelectPageIgnoreLogicDelete());
        methodList.add(new SelectCountIgnoreLogicDelete());
        methodList.add(new InsertBatch());
        methodList.add(new InsertIgnoreBatch());
        methodList.add(new InsertIgnore());
        methodList.add(new ReplaceInsert());
        methodList.add(new ReplaceInsertBatch());
        methodList.add(new DeleteByIdIgnoreLogicDelete());
        methodList.add(new DeleteByMapIgnoreLogicDelete());
        methodList.add(new DeleteIgnoreLogicDelete());
        methodList.add(new DeleteBatchIdsIgnoreLogicDelete());
        return methodList;
    }
}
