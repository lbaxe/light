package com.light.framework.auth.domain;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.cglib.beans.BeanMap;

import com.light.framework.auth.consts.AuthConst;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.TextCodec;

public class LoginUser<T> {
    private String tenantId;
    private long userId;
    private String username;
    private String originIp;
    /**
     * 0 pc 1 移动端
     */
    private int deviceType;
    /**
     * 登录时间
     */
    private long visitTime;
    /**
     * token版本控制
     */
    private String version;
    private String random;
    private Map<String, String> extend = new HashMap<>();
    // 用户详细信息
    private T user;

    private LoginUser() {

    }

    private LoginUser(String tenantId, Long userId, String username, String originIp, int deviceType,
        String version,
        String random) {
        this.tenantId = tenantId;
        this.userId = userId;
        this.username = username;
        this.originIp = originIp;
        this.deviceType = deviceType;
        this.visitTime = System.currentTimeMillis();
        this.version = version;
        this.random = random;
    }

    public String createJwtToken(String key) {
        BeanMap beanMap = null;
        try {
            beanMap = BeanMap.create(this);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        Map<String, Object> claims = new HashMap<>();
        BeanMap finalBeanMap = beanMap;
        beanMap.keySet().forEach(e -> {
            claims.put(e.toString(), finalBeanMap.get(e));
        });
        long nowMillis = System.currentTimeMillis();
        ZonedDateTime zonedDateTime = Instant.ofEpochMilli(nowMillis).atZone(ZoneId.systemDefault());
        Date now = Date.from(zonedDateTime.toInstant());

        // ZonedDateTime expireZonedDateTime =
        // Instant.ofEpochMilli(nowMillis + 30 * 60 * 1000).atZone(ZoneId.systemDefault());
        return Jwts.builder().setClaims(claims).setId(this.random)
            .setIssuer(AuthConst.AUTH_ISSUER)// 签发人
            .setAudience(this.getTenantId())// 签发受众
            .setIssuedAt(now)// 签发时间
            .setNotBefore(now) // 生效时间
            // .setExpiration(Date.from(expireZonedDateTime.toInstant()))// 过期时间
            .signWith(SignatureAlgorithm.HS512, TextCodec.BASE64.encode(key)).compact();
    }

    public static LoginUser parseToken(String token, String key) {
        Claims claims = Jwts.parser().setSigningKey(TextCodec.BASE64.encode(key)).parseClaimsJws(token).getBody();
        BeanMap beanMap = null;
        LoginUser userToken = new LoginUser();
        try {
            beanMap = BeanMap.create(userToken);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        beanMap.putAll(claims);
        return userToken;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String tenantId = "lbaxe";
        private Long userId;
        private String username;
        private int deviceType = AuthConst.AUTH_TYPE_PC;
        private String originIp;
        private String version = AuthConst.AUTH_VERSION;

        public LoginUser build() {
            return new LoginUser(this.tenantId, this.userId, this.username, this.originIp, this.deviceType,
                this.version, UUID.randomUUID().toString());
        }

        public Builder tenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }


        public Builder originIp(String originIp) {
            this.originIp = originIp;
            return this;
        }

        public Builder deviceType(int deviceType) {
            this.deviceType = deviceType;
            return this;
        }

        public Builder version(String version) {
            this.version = version;
            return this;
        }
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getOriginIp() {
        return originIp;
    }

    public void setOriginIp(String originIp) {
        this.originIp = originIp;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    public long getVisitTime() {
        return visitTime;
    }

    public void setVisitTime(long visitTime) {
        this.visitTime = visitTime;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRandom() {
        return random;
    }

    public void setRandom(String random) {
        this.random = random;
    }

    public Map<String, String> getExtend() {
        return extend;
    }

    public void setExtend(Map<String, String> extend) {
        this.extend = extend;
    }

    public void putExtend(String key, String value) {
        this.extend.put(key, value);
    }

    public T getUser() {
        return user;
    }

    public void setUser(T user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "LoginUser [tenantId='" + tenantId + "',userId=" + userId + ",username='" + username + "',originIp='"
            + originIp + "',deviceType=" + deviceType + ",visitTime=" + visitTime
            + ",version='" + version + "',random='" + random
            + "',extend=" + extend + ",user=" + user + "]";
    }
}
