package com.light.mapper.support;

import java.io.StringReader;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.ibatis.session.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import com.light.mapper.entity.IEntity;
import com.light.mapper.entity.Metadata;
import com.light.mapper.magic.ExtendMybatisConfiguration;
import com.light.mapper.util.EntityUtil;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.util.TablesNamesFinder;

public class OpCacheProxy {
    private static final String VERSION = "0.0.1";

    private String groupName = "defalut";
    private String dataSourceId;
    private RedisTemplate<String, Object> redisTemplate;

    public OpCacheProxy(Configuration configuration) {
        if (configuration instanceof ExtendMybatisConfiguration) {
            this.redisTemplate = ((ExtendMybatisConfiguration)configuration).getRedisTemplate();
        }
        this.dataSourceId = configuration.getDatabaseId();
    }

    public void setByPK(IEntity entity) {
        Metadata metadata = entity.metadata();
        String tableKey = keyForTable(entity.metadata().getTableName());
        String rowKey = keyForPK(metadata.getTableName(), metadata.getPkField().getName(), entity.pkValue());

        redisTemplate.opsForHash().put(tableKey, rowKey, entity);
    }

    public void setByPKs(List<IEntity> entityList) {
        entityList.forEach(entity -> {
            setByPK(entity);
        });
    }

    public IEntity getByPk(Object pk, Class<IEntity> entityClass) {
        Metadata metadata = EntityUtil.getMetadata(entityClass);
        String tableKey = keyForTable(metadata.getTableName());
        String rowKey = keyForPK(metadata.getTableName(), metadata.getPkField().getName(), pk);
        return (IEntity)redisTemplate.opsForHash().get(tableKey, rowKey);
    }

    public List<IEntity> getByPks(List<Object> pkList, Class<IEntity> entityClass) {
        return pkList.stream().map(pk -> getByPk(pk, entityClass)).filter(entity -> entity != null)
            .collect(Collectors.toList());
    }

    public void delByPks(List<Object> pkList, Class<IEntity> entityClass) {
        Metadata metadata = EntityUtil.getMetadata(entityClass);
        String tableKey = keyForTable(metadata.getTableName());
        Boolean hasTableKey = redisTemplate.hasKey(tableKey);
        if (hasTableKey != Boolean.TRUE) {
            return;
        }
        pkList.stream().forEach(pk -> {
            String rowKey = keyForPK(metadata.getTableName(), metadata.getPkField().getName(), pk);
            redisTemplate.opsForHash().delete(tableKey, rowKey);
        });
    }

    public void delAll(Class<IEntity> entityClass) {
        Metadata metadata = EntityUtil.getMetadata(entityClass);
        String tableKey = keyForTable(metadata.getTableName());
        redisTemplate.delete(tableKey);
    }

    public void delAll(String tableName) {
        String tableKey = keyForTable(tableName);
        redisTemplate.delete(tableKey);
    }

    public void delAll(List<String> tableNames) {
        for (String tableName : tableNames) {
            delAll(tableName);
        }
    }

    public String keyForPK(String tableName, String pkName, Object pkValue) {
        StringBuilder key = new StringBuilder();
        key.append(tableName);
        key.append(".");
        key.append(pkName);
        key.append(".");
        key.append(pkValue.toString());
        return key.toString();
    }

    public String keyForTable(String tableName) {
        StringBuilder key = new StringBuilder();
        key.append(groupName);
        key.append(".");
        key.append(dataSourceId);
        key.append(".");
        key.append(tableName);
        key.append(".");
        key.append(version());
        return key.toString();
    }

    private String version() {
        return VERSION;
    }

    public boolean useCache(Metadata metadata) {
        return redisTemplate != null && metadata.isUseCache();
    }

    public static void main(String[] args) throws JSQLParserException {
        String sql = "SELECT ax.ADDRESS_CITY, COUNT(1)\n" + "FROM ajb_xmba ax\n" + "JOIN (\n" + "\tSELECT ds.PROID\n"
            + "\tFROM ajb_dev_setup ds\n" + "\tJOIN ajb_dev_use du ON ds.GUID=du.setup_id\n"
            + "\tJOIN ajb_device d ON du.DEVID=d.GUID\n" + "\tLEFT JOIN ajb_dev_tear dt ON ds.GUID=dt.setup_id\n"
            + "\tWHERE (d.iscancel = 'N' OR d.iscancel IS NULL) AND ds.FSTATE='FL-P' AND du.FSTATE='TC-P' AND (dt.FSTATE!='FL-P' OR dt.GUID IS NULL)\n"
            + ") d ON ax.GUID=d.proid\n"
            + "WHERE (ax.prostate !='FINISH' OR ax.prostate IS NULL) AND ax.prostate != 'MIDDLE' AND ax.fstate IN ('TD-P','TC-P')\n"
            + "GROUP BY ax.ADDRESS_CITY\n" + "ORDER BY ax.ADDRESS_CITY";
        CCJSqlParserManager parserManager = new CCJSqlParserManager();
        Statement statement = parserManager.parse(new StringReader(sql));
        TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
        List<String> tablesNames = tablesNamesFinder.getTableList(statement);
        tablesNames.forEach(System.out::println);
    }
}
