package com.light.web.common.oauth.mapper.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.light.mapper.entity.BaseEntity;

import lombok.Data;

@Data
@TableName("oauth2_client")
public class OAuth2Client extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String clientId;
    private String clientSecret;
    /**
     * 客户端-应用名称
     */
    private String appName;
    /**
     * 客户端-应用图标
     */
    private String appIcon;
    /**
     * 客户端-应用主页
     */
    private String appUrl;
    /**
     * 客户端-应用描述
     */
    private String appDescription;
    /**
     * 客户端的重定向URI
     */
    private String appRedirectUri;
    /**
     * 允许授权类型
     * 
     * {@link org.apache.oltu.oauth2.common.message.types.GrantType}
     */
    private String authorizedGrantTypes;
    /**
     * 授权范围
     */
    private String scope;
    /**
     * 设定客户端的access_token的有效时间值(单位:秒)
     */
    private Integer accessTokenValidity;
    /**
     * 设定客户端的refresh_token的有效时间值(单位:秒
     */
    private Integer refreshTokenValidity;
    private Integer deleteFlag;
    private Date createTime;
    private Date updateTime;

}
