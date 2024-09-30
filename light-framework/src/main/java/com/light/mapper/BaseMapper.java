package com.light.mapper;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;

public interface BaseMapper<T> extends com.baomidou.mybatisplus.core.mapper.BaseMapper<T> {
    /**
     * 返回对应的主键
     * 
     * @param queryWrapper
     * @param <PK>
     * @return
     */
    <PK> List<PK> selectPKList(@Param(Constants.WRAPPER) Wrapper<T> queryWrapper);

    /**
     * 返回对应的主键
     * 
     * @param idList
     * @param <PK>
     * @return
     */
    <PK> List<PK> selectPKListByIds(@Param(Constants.COLLECTION) Collection<? extends Serializable> idList);

    /**
     * 返回对应的主键
     * 
     * @param columnMap
     * @param <PK>
     * @return
     */
    <PK> List<PK> selectPKListByMap(@Param(Constants.COLUMN_MAP) Map<String, Object> columnMap);

    /**
     * 返回第一条
     * 
     * @param queryWrapper
     * @return
     */
    T selectFirst(@Param(Constants.WRAPPER) Wrapper<T> queryWrapper);

    /**
     * 根据 entity 条件，查询一条记录
     *
     * @param queryWrapper 实体对象封装操作类（可以为 null）
     */
    T selectFirstIgnoreLogicDelete(@Param(Constants.WRAPPER) Wrapper<T> queryWrapper);

    /**
     * 根据 ID 查询
     *
     * @param id 主键ID
     */
    T selectByIdIgnoreLogicDelete(Serializable id);

    /**
     * 查询（根据ID 批量查询）
     *
     * @param idList 主键ID列表(不能为 null 以及 empty)
     */
    List<T> selectBatchByIdsIgnoreLogicDelete(@Param(Constants.COLLECTION) Collection<? extends Serializable> idList);

    /**
     * 查询（根据 columnMap 条件）
     *
     * @param columnMap 表字段 map 对象
     */
    List<T> selectByMapIgnoreLogicDelete(@Param(Constants.COLUMN_MAP) Map<String, Object> columnMap);

    /**
     * 根据 entity 条件，查询全部记录
     *
     * @param queryWrapper 实体对象封装操作类（可以为 null）
     */
    List<T> selectListIgnoreLogicDelete(@Param(Constants.WRAPPER) Wrapper<T> queryWrapper);

    /**
     * 根据 entity 条件，查询全部记录（并翻页）
     *
     * @param page 分页查询条件（可以为 RowBounds.DEFAULT）
     * @param queryWrapper 实体对象封装操作类（可以为 null）
     */
    <E extends IPage<T>> E selectPageIgnoreLogicDelete(E page, @Param(Constants.WRAPPER) Wrapper<T> queryWrapper);

    /**
     * 根据 Wrapper 条件，查询总记录数
     *
     * @param queryWrapper 实体对象封装操作类（可以为 null）
     */
    Integer selectCountIgnoreLogicDelete(@Param(Constants.WRAPPER) Wrapper<T> queryWrapper);

    int insertBatch(List<T> entityList);

    int insertIgnore(T entity);

    int insertIgnoreBatch(List<T> entityList);

    /**
     * <li>非自增主键，带主键执行replace into</li>
     * <li>自增主键，相当于insert，不支持replace，防止主从切换导致主键冲突</li>
     * 
     * @param entity
     * @return
     */
    int replaceInsert(T entity);

    /**
     * <li>非自增主键，带主键执行replace into</li>
     * <li>自增主键，相当于insert，不支持replace，防止主从切换导致主键冲突</li>
     * 
     * @param entityList
     * @return
     */
    int replaceInsertBatch(List<T> entityList);

    /**
     * 根据 ID 删除
     *
     * @param id 主键ID
     */
    int deleteByIdIgnoreLogicDelete(Serializable id);

    /**
     * 根据 columnMap 条件，删除记录
     *
     * @param columnMap 表字段 map 对象
     */
    int deleteByMapIgnoreLogicDelete(@Param(Constants.COLUMN_MAP) Map<String, Object> columnMap);

    /**
     * 根据 entity 条件，删除记录
     *
     * @param queryWrapper 实体对象封装操作类（可以为 null,里面的 entity 用于生成 where 语句）
     */
    int deleteIgnoreLogicDelete(@Param(Constants.WRAPPER) Wrapper<T> queryWrapper);

    /**
     * 删除（根据ID 批量删除）
     *
     * @param idList 主键ID列表(不能为 null 以及 empty)
     */
    int deleteBatchIdsIgnoreLogicDelete(@Param(Constants.COLLECTION) Collection<? extends Serializable> idList);

    /**
     * 按照模板创建表
     * 
     * @param table
     * @param templateTable
     */
    @Update("create table IF NOT EXISTS ${table} like  ${templateTable}")
    void createTableIfAbsent(String table, String templateTable);
}
