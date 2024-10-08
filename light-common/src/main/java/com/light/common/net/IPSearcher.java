package com.light.common.net;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import org.lionsoul.ip2region.DataBlock;
import org.lionsoul.ip2region.DbConfig;
import org.lionsoul.ip2region.DbMakerConfigException;
import org.lionsoul.ip2region.DbSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 根据IP获取行政区域
 * 
 * @author luban
 */
public class IPSearcher {
    private static Logger logger = LoggerFactory.getLogger(IPSearcher.class);
    private static final ThreadLocal<DbSearcher> threadLocal = ThreadLocal.withInitial(() -> {
        try {
            DbConfig config = new DbConfig();
            URL url = IPSearcher.class.getClassLoader().getResource("ip/ip2region.db");
            return new DbSearcher(config, url.getFile());
        } catch (DbMakerConfigException | FileNotFoundException e) {
        } catch (IOException e) {
        }
        return null;
    });

    public static IPData getIpData(String ip) {
        IPData ipData = null;
        DbSearcher searcher = threadLocal.get();
        try {
            if (searcher == null) {
                return null;
            }
            DataBlock dataBlock = searcher.memorySearch(ip);
            if (dataBlock != null) {
                ipData = new IPData();
                String[] regions = dataBlock.getRegion().split("[\\|]");
                ipData.setCountry(regions[0]);
                ipData.setProvince(regions[2]);
                ipData.setCity(regions[3]);
                ipData.setIsp(regions[4]);
                return ipData;
            }
        } catch (IOException e) {
            logger.warn("ip查找区域异常", e);
        } finally {
            // threadLocal.remove();
            if (searcher != null) {
                try {
                    searcher.close();
                } catch (IOException e) {
                }
            }
        }
        return ipData;
    }

    public static class IPData {
        public String country;

        public String province;

        public String city;

        public String isp;

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String getProvince() {
            return province;
        }

        public void setProvince(String province) {
            this.province = province;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getIsp() {
            return isp;
        }

        public void setIsp(String isp) {
            this.isp = isp;
        }

        @Override
        public String toString() {
            return "IPData [country='" + country + "',province='" + province + "',city='" + city + "',isp='" + isp
                + "']";
        }
    }
}
