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

/**
 * MybatisPlus 支持 SQL 方法
 *
 * @author hubin
 * @since 2016-01-23
 */
public enum ExtendSqlMethod {
    INSERT_BATCH("insertBatch", "插入一条数据（全部字段插入），如果中已经存在相同的记录，则忽略当前新数据",
        "<script>\nINSERT INTO %s %s VALUES %s\n</script>"),
    INSERT_IGNORE_ONE("insertIgnore", "插入一条数据（选择字段插入），如果中已经存在相同的记录，则忽略当前新数据",
        "<script>\nINSERT IGNORE INTO %s %s VALUES %s\n</script>"),
    INSERT_IGNORE_BATCH("insertIgnoreBatch", "插入一条数据（全部字段插入），如果中已经存在相同的记录，则忽略当前新数据",
        "<script>\nINSERT IGNORE INTO %s %s VALUES %s\n</script>"),
    REPLACE_ONE_PK("replaceInsertByPK", "替换一条数据（选择字段插入），存在则替换，不存在则插入",
        "<script>\nREPLACE INTO %s %s VALUES %s\n</script>"),
    REPLACE_ONE("replaceInsert", "替换一条数据（选择字段插入），存在则替换，不存在则插入", "<script>\nREPLACE INTO %s %s VALUES %s\n</script>"),
    REPLACE_INSERT_BATCH("replaceInsertBatch", "替换一条数据（全部字段插入），存在则替换，不存在则插入",
        "<script>\nREPLACE INTO %s %s VALUES %s\n</script>"),

    /**
     * 强制物理删除
     */
    DELETE_BY_ID_IGNORE_LOGIC_DELETE("deleteByIdIgnoreLogicDelete", "根据ID 删除一条数据",
        "<script>\nDELETE FROM %s WHERE %s=#{%s}\n</script>"),
    DELETE_BY_MAP_IGNORE_LOGIC_DELETE("deleteByMapIgnoreLogicDelete", "根据columnMap 条件删除记录",
        "<script>\nDELETE FROM %s %s\n</script>"),
    DELETE_IGNORE_LOGIC_DELETE("deleteIgnoreLogicDelete", "根据 entity 条件删除记录",
        "<script>\nDELETE FROM %s %s %s\n</script>"),
    DELETE_BATCH_IDS_IGNORE_LOGIC_DELETE("deleteBatchIdsIgnoreLogicDelete", "根据ID集合，批量删除数据",
        "<script>\nDELETE FROM %s WHERE %s IN (%s)\n</script>"),

    // 查询扩展
    // 查询扩展
    SELECT_PK_LIST("selectPKList", "查询满足条件的主键ID列表", "<script>%s SELECT %s FROM %s %s %s\n</script>"),
    SELECT_PK_LIST_BY_IDS("selectPKListByIds", "根据ID集合，批量查询数据",
        "<script>SELECT %s FROM %s WHERE %s IN (%s) %s</script>"),
    SELECT_PK_LIST_BY_MAP("selectPKListByMap", "查询满足条件的主键ID列表", "<script>SELECT %s FROM %s %s\n</script>"),
    SELECT_FIRST("selectFirst", "查询满足条件一条数据", "<script>SELECT %s FROM %s %s limit 1\n</script>"),
    SELECT_FIRST_IGNORE_LOGIC_DELETE("selectFirstIgnoreLogicDelete", "查询满足条件一条数据",
        "<script>SELECT %s FROM %s %s limit 1\n</script>"),
    SELECT_BY_ID_IGNORE_LOGIC_DELETE("selectByIdIgnoreLogicDelete", "根据ID 查询一条数据，忽略逻辑删",
        "SELECT %s FROM %s WHERE %s=#{%s} %s"),
    SELECT_BATCH_BY_IDS_IGNORE_LOGIC_DELETE("selectBatchByIdsIgnoreLogicDelete", "根据ID集合，批量查询数据",
        "<script>SELECT %s FROM %s WHERE %s IN (%s) %s</script>"),
    SELECT_BY_MAP_IGNORE_LOGIC_DELETE("selectByMapIgnoreLogicDelete", "根据columnMap 查询一条数据",
        "<script>SELECT %s FROM %s %s\n</script>"),
    SELECT_COUNT_IGNORE_LOGIC_DELETE("selectCountIgnoreLogicDelete", "查询满足条件总记录数",
        "<script>%s SELECT COUNT(%s) FROM %s %s %s\n</script>"),
    SELECT_LIST_IGNORE_LOGIC_DELETE("selectListIgnoreLogicDelete", "查询满足条件所有数据",
        "<script>%s SELECT %s FROM %s %s %s\n</script>"),
    SELECT_PAGE_IGNORE_LOGIC_DELETE("selectPageIgnoreLogicDelete", "查询满足条件所有数据（并翻页）",
        "<script>%s SELECT %s FROM %s %s %s\n</script>"),

    ;

    private final String method;
    private final String desc;
    private final String sql;

    ExtendSqlMethod(String method, String desc, String sql) {
        this.method = method;
        this.desc = desc;
        this.sql = sql;
    }

    public String getMethod() {
        return method;
    }

    public String getDesc() {
        return desc;
    }

    public String getSql() {
        return sql;
    }

}
