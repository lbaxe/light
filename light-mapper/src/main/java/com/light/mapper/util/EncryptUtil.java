package com.light.mapper.util;

import org.springframework.core.env.Environment;

import com.light.common.crypto.DESedeUtil;
import com.light.core.util.SpringContextUtil;

public class EncryptUtil {

    public static String encrypt(String value) {
        return DESedeUtil.getInstance().encryptBase64(value,
            SpringContextUtil.getBean(Environment.class).getProperty("spring.db.encrypt.key"));
    }

    public static String decrypt(String value) {
        return DESedeUtil.getInstance().decryptBase64(value,
            SpringContextUtil.getBean(Environment.class).getProperty("spring.db.encrypt.key"));
    }
}