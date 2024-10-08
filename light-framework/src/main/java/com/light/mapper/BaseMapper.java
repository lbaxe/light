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

/**
 * 扩展通用mapper
 * 
 * @param <T>
 * @author luban
 */
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
     * 查询对应的主键
     * 
     * @param idList
     * @param <PK>
     * @return
     */
    <PK> List<PK> selectPKListByIds(@Param(Constants.COLLECTION) Collection<? extends Serializable> idList);

    /**
     * 查询对应的主键
     * 
     * @param columnMap
     * @param <PK>
     * @return
     */
    <PK> List<PK> selectPKListByMap(@Param(Constants.COLUMN_MAP) Map<String, Object> columnMap);

    /**
     * 查询已有排序下第一条数据
     *
     * @param queryWrapper
     * @return
     * @see com.baomidou.mybatisplus.core.injector.methods.SelectOne
     */
    T selectFirst(@Param(Constants.WRAPPER) Wrapper<T> queryWrapper);

    /**
     * 查询已有排序下第一条数据,包含逻辑删除数据
     * 
     * @param queryWrapper
     * @return
     */
    T selectFirstIgnoreLogicDelete(@Param(Constants.WRAPPER) Wrapper<T> queryWrapper);

    /**
     * 根据主键ID查询对应实体，包含逻辑删除数据
     * 
     * @param id
     * @return
     */
    T selectByIdIgnoreLogicDelete(Serializable id);

    /**
     * 根据主键IDs查询对应实体列表，包含逻辑删除数据
     *
     * @param idList 主键ID列表(不能为 null 以及 empty)
     */
    List<T> selectBatchByIdsIgnoreLogicDelete(@Param(Constants.COLLECTION) Collection<? extends Serializable> idList);

    /**
     * 查询实体（根据 columnMap 条件），包含逻辑删除数据
     *
     * @param columnMap 表字段 map 对象
     */
    List<T> selectByMapIgnoreLogicDelete(@Param(Constants.COLUMN_MAP) Map<String, Object> columnMap);

    /**
     * 根据 entity 条件，查询全部记录，包含逻辑删除数据
     *
     * @param queryWrapper 实体对象封装操作类（可以为 null）
     */
    List<T> selectListIgnoreLogicDelete(@Param(Constants.WRAPPER) Wrapper<T> queryWrapper);

    /**
     * 根据 entity 条件，查询全部记录（并翻页），包含逻辑删除数据
     *
     * @param page 分页查询条件（可以为 RowBounds.DEFAULT）
     * @param queryWrapper 实体对象封装操作类（可以为 null）
     */
    <E extends IPage<T>> E selectPageIgnoreLogicDelete(E page, @Param(Constants.WRAPPER) Wrapper<T> queryWrapper);

    /**
     * 根据 Wrapper 条件，查询总记录数，包含逻辑删除数据
     *
     * @param queryWrapper 实体对象封装操作类（可以为 null）
     */
    Integer selectCountIgnoreLogicDelete(@Param(Constants.WRAPPER) Wrapper<T> queryWrapper);

    /**
     * <ul>
     * <li>实体无主键：全字段插入</li>
     * <li>实体自增主键：无主键插入</li>
     * <li>实体非自增主键：携带主键插入</li>
     * </ul>
     * 
     * @param entityList
     * @return
     */
    int insertBatch(List<T> entityList);

    /**
     * <ul>
     * <li>实体表无主键：全字段插入</li>
     * <li>实体表自增主键：无主键插入（无法指定主键插入），若与表唯一索引字段重复，则忽略</li>
     * <li>实体表非自增：携带主键插入且忽略除主键外的其他唯一值字段,若与表唯一索引字段(或主键)重复，则忽略</li>
     * </ul>
     *
     * @param entity
     * @return
     */
    int insertIgnore(T entity);

    /**
     * <ul>
     * <li>实体表无主键：全字段插入</li>
     * <li>实体表自增主键：无主键插入（无法指定主键插入），若与表唯一索引字段重复，则忽略；主键回填entityList</li>
     * <li>实体表非自增：携带主键插入且忽略除主键外的其他唯一值字段,若与表唯一索引字段(或主键)重复，则忽略</li>
     * </ul>
     *
     * @param entityList
     * @return
     */
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
