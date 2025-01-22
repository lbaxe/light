package com.light.web.common.oauth.mapper.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.light.mapper.entity.BaseEntity;

import lombok.Data;

@Data
@TableName("oauth2_authorized_client")
public class OAuth2AuthorizedClient extends BaseEntity {
    private static final long serialVersionUID = -9161366882795848096L;
    @TableId(type = IdType.AUTO)
    private Long id;
    private String clientId;
    private String accessTokenType;
    private String accessTokenValue;
    private Date accessTokenIssuedAt;
    private Date accessTokenExpiresAt;
    private String accessTokenScopes;
    private String refreshTokenValue;
    private Date refreshTokenIssuedAt;
    private Date refreshTokenExpiresAt;
    private Integer deleteFlag;
    private Date createTime;
    private Date updateTime;

}
