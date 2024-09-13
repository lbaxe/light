package com.light.common.crypto;

import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

public class AEScbcUtil {
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";

    private static final String CBC_IV_PARAM = "lbaxe119";

    private final Map<String, Cipher> encryptMap = new HashMap<>();

    private final Map<String, Cipher> decryptMap = new HashMap<>();

    private static final ThreadLocal<AEScbcUtil> localAesCbcPlus = ThreadLocal.withInitial(() -> new AEScbcUtil());

    private AEScbcUtil() {}

    public static AEScbcUtil getInstance() {
        return localAesCbcPlus.get();
    }

    public String encrypt(String content, String secureKey) throws Exception {
        if (content == null || secureKey == null) {
            return null;
        }
        if (secureKey.length() != 16) {
            throw new RuntimeException("使用AES-128-CBC加密模式，要求秘钥128位，key需要为16位ascii码");
        }
        Cipher cipher = this.encryptMap.get(secureKey);
        if (cipher == null) {
            cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(secureKey.getBytes());
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(random);
            SecretKey secretKey = kgen.generateKey();
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getEncoded(), "AES");
            IvParameterSpec iv = new IvParameterSpec(CBC_IV_PARAM.getBytes());
            cipher.init(1, secretKeySpec, iv);
            this.encryptMap.put(secureKey, cipher);
        }
        byte[] encrypted = cipher.doFinal(content.getBytes("utf-8"));
        return Base64.encodeBase64String(encrypted);
    }

    public String decrypt(String content, String secureKey) throws Exception {
        if (content == null || secureKey == null) {
            return null;
        }
        if (secureKey.length() != 16) {
            throw new RuntimeException("使用AES-128-CBC加密模式，要求秘钥128位，key需要为16位ascii码");
        }
        Cipher cipher = this.decryptMap.get(secureKey);
        if (cipher == null) {
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            random.setSeed(secureKey.getBytes());
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128, random);
            SecretKey secretKey = kgen.generateKey();
            SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getEncoded(), "AES");
            cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            IvParameterSpec iv = new IvParameterSpec(CBC_IV_PARAM.getBytes());
            cipher.init(2, secretKeySpec, iv);
            this.decryptMap.put(secureKey, cipher);
        }
        byte[] encrypted = Base64.decodeBase64(content);
        byte[] original = cipher.doFinal(encrypted);
        return new String(original, "utf-8");
    }

    public static void main(String[] args) {
        System.out.println("-------当前JDK加密服务提供者-----");
        Provider[] pro = Security.getProviders();
        for (Provider p : pro) {
            System.out.println("Provider:" + p.getName() + " - version:" + p.getVersion());
            System.out.println(p.getInfo());
        }
        System.out.println("");
        System.out.println("-------当前JDK支持的消息摘要算法：");
        for (String s : Security.getAlgorithms("MessageDigest")) {
            System.out.println(s);
        }
        System.out.println("-------当前JDK支持的签名算法：");
        for (String s : Security.getAlgorithms("Signature")) {
            System.out.println(s);
        }
    }
}
