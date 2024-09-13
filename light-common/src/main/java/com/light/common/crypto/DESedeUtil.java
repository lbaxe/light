package com.light.common.crypto;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

public class DESedeUtil {
    private static final String CIPHER_ALGORITHM = "DESede";
    private static final String CIPHERMODE = "DESede/ECB/PKCS5Padding";

    private static final String strDefaultKey = "lbaxe119";

    private final Map<String, Cipher> encryptMap = new HashMap<>();

    private final Map<String, Cipher> decryptMap = new HashMap<>();

    private static final ThreadLocal<DESedeUtil> localDesPlus = ThreadLocal.withInitial(() -> new DESedeUtil());

    private DESedeUtil() {

    }

    public static DESedeUtil getInstance() {
        return localDesPlus.get();
    }

    // des单轮加密秘钥长度不小于56位,小于的补0
    private Key getKey(String key) {
        byte[] oldKeys = key.getBytes(StandardCharsets.UTF_8);
        byte[] newKeys = new byte[24];
        for (int i = 0; i < oldKeys.length && i != 24; i++) {
            newKeys[i] = oldKeys[i];
        }
        return new SecretKeySpec(newKeys, CIPHER_ALGORITHM);
    }

    public final String encrypt(String str) {
        return encrypt(str, strDefaultKey);
    }

    public final String encrypt(String str, String key) {
        if (str == null) {
            return null;
        }
        if (key == null || key.length() < 8) {
            return null;
        }
        Cipher cipher = getEncrypt(key);
        try {
            byte[] encrypted = cipher.doFinal(str.getBytes(StandardCharsets.UTF_8));
            return Hex.encodeHexString(encrypted);
        } catch (Exception e) {
            return null;
        }
    }

    public final String encryptBase64(String str) {
        return encryptBase64(str, strDefaultKey);
    }

    public final String encryptBase64(String str, String key) {
        Cipher cipher = getEncrypt(key);
        try {
            byte[] encrypted = cipher.doFinal(str.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeBase64String(encrypted);
        } catch (Exception e) {
            return null;
        }
    }

    public String decrypt(String str) {
        return decrypt(str, strDefaultKey);
    }

    public String decrypt(String str, String key) {
        Cipher cipher = getDecrypt(key);
        try {
            byte[] original = cipher.doFinal(Hex.decodeHex(str));
            return new String(original, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return str;
        }
    }

    public String decryptBase64(String str) {
        return decryptBase64(str, strDefaultKey);
    }

    public String decryptBase64(String str, String key) {
        if (str == null) {
            return null;
        }
        if (key == null || key.length() < 8) {
            return null;
        }
        Cipher cipher = getDecrypt(key);
        try {
            byte[] original = cipher.doFinal(Base64.decodeBase64(str));
            return new String(original, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return str;
        }
    }

    private Cipher getEncrypt(String key) {
        Cipher encryptCipher = this.encryptMap.get(key);
        if (encryptCipher != null) {
            return encryptCipher;
        }
        try {
            encryptCipher = Cipher.getInstance(CIPHERMODE);
            encryptCipher.init(Cipher.ENCRYPT_MODE, this.getKey(key));
            this.encryptMap.put(key, encryptCipher);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return encryptCipher;
    }

    private Cipher getDecrypt(String key) {
        Cipher decryptCipher = this.decryptMap.get(key);
        if (decryptCipher != null) {
            return decryptCipher;
        }
        try {
            decryptCipher = Cipher.getInstance(CIPHERMODE);
            decryptCipher.init(Cipher.DECRYPT_MODE, this.getKey(key));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return decryptCipher;
    }

    public static void main(String[] args) {
        // KhEHVbXML55azFoL7SnnYQ==
        String pwd = "abc@12345";

        String encryptBase64 = DESedeUtil.getInstance().encryptBase64(pwd, "cap_user");
        System.out.println("1:" + encryptBase64);
        System.out.println("2:KhEHVbXML55azFoL7SnnYQ==");
        System.out.println("3:" + DESedeUtil.getInstance().decryptBase64(encryptBase64, "cap_user"));

        String encryptHex = DESedeUtil.getInstance().encrypt(pwd, "cap_user");
        System.out.println("11:" + encryptHex);
        System.out.println("22:" + DESedeUtil.getInstance().decryptBase64(encryptHex, "cap_user"));
    }
}
