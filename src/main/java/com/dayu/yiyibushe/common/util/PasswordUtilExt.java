package com.dayu.yiyibushe.common.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

/**
 * 密码摘要工具：使用 JDK 自带 PBKDF2WithHmacSHA256 对密码做加盐慢哈希。
 * <p>
 * 每个密码使用独立随机盐，数据库中看不到明文，也无法用彩虹表批量反推。
 * 存储格式：{@code pbkdf2$迭代次数$盐Base64$哈希Base64}。
 * 同时兼容旧版固定盐 SHA-256 摘要（64 位十六进制串），保证历史用户可登录，
 * 建议登录后通过修改密码升级为新格式。
 *
 * @author Witty·Kid Fisher
 * @version 0.0.2
 */
public final class PasswordUtilExt {

    /** PBKDF2 算法名 */
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";

    /** 存储格式前缀（对外暴露，供登录后识别并升级旧密码格式） */
    public static final String PBKDF2_PREFIX = "pbkdf2$";

    /** PBKDF2 迭代次数（越大越安全但越慢） */
    private static final int ITERATIONS = 120_000;

    /** 派生密钥位数 */
    private static final int KEY_LENGTH_BITS = 256;

    /** 盐长度（字节） */
    private static final int SALT_LENGTH_BYTES = 16;

    /** 旧版固定盐值（仅用于兼容历史 SHA-256 数据） */
    private static final String LEGACY_SALT = "yiyibushe-learning-salt:";

    /** 密码学安全随机数生成器 */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * 私有构造器：工具类不允许实例化
     */
    private PasswordUtilExt() {
    }

    /**
     * 对明文密码生成加盐 PBKDF2 摘要
     *
     * @param rawPassword
     *                    明文密码
     * @return 可直接存储的摘要串
     */
    public static String encode(String rawPassword) {
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        SECURE_RANDOM.nextBytes(salt);
        byte[] hash = pbkdf2Hash(rawPassword, salt, ITERATIONS);
        Base64.Encoder encoder = Base64.getEncoder();
        return PBKDF2_PREFIX + ITERATIONS + "$" + encoder.encodeToString(salt)
                + "$" + encoder.encodeToString(hash);
    }

    /**
     * 校验明文密码是否与已存储摘要匹配（支持新 PBKDF2 与旧 SHA-256 两种格式）
     *
     * @param rawPassword
     *                       明文密码
     * @param storedPassword
     *                       已存储的摘要
     * @return true 匹配
     */
    public static boolean matches(String rawPassword, String storedPassword) {
        if (StringUtilExt.isBlank(rawPassword) || StringUtilExt.isBlank(storedPassword)) {
            return false;
        }
        if (storedPassword.startsWith(PBKDF2_PREFIX)) {
            return matchesPbkdf2(rawPassword, storedPassword);
        }
        // 兼容旧版固定盐 SHA-256（64 位十六进制）
        return MessageDigest.isEqual(
                legacySha256(rawPassword).getBytes(StandardCharsets.UTF_8),
                storedPassword.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 校验 PBKDF2 格式摘要
     *
     * @param rawPassword
     *                       明文密码
     * @param storedPassword
     *                       PBKDF2 摘要串
     * @return true 匹配
     */
    private static boolean matchesPbkdf2(String rawPassword, String storedPassword) {
        String[] parts = StringUtilExt.split(storedPassword, "$");
        if (parts.length != 4) {
            return false;
        }
        int iterations;
        try {
            iterations = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return false;
        }
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] salt;
        byte[] expectedHash;
        try {
            salt = decoder.decode(parts[2]);
            expectedHash = decoder.decode(parts[3]);
        } catch (IllegalArgumentException e) {
            return false;
        }
        byte[] actualHash = pbkdf2Hash(rawPassword, salt, iterations);
        // 常量时间比较，避免计时侧信道
        return MessageDigest.isEqual(actualHash, expectedHash);
    }

    /**
     * 计算 PBKDF2 哈希
     *
     * @param rawPassword
     *                    明文密码
     * @param salt
     *                    盐
     * @param iterations
     *                    迭代次数
     * @return 哈希字节
     */
    private static byte[] pbkdf2Hash(String rawPassword, byte[] salt, int iterations) {
        PBEKeySpec keySpec = new PBEKeySpec(rawPassword.toCharArray(), salt, iterations, KEY_LENGTH_BITS);
        try {
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            return keyFactory.generateSecret(keySpec).getEncoded();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("当前 JDK 不支持 " + PBKDF2_ALGORITHM, e);
        } catch (InvalidKeySpecException e) {
            throw new IllegalStateException("PBKDF2 密钥规格无效", e);
        } finally {
            keySpec.clearPassword();
        }
    }

    /**
     * 旧版固定盐 SHA-256 摘要（仅用于兼容历史数据）
     *
     * @param rawPassword
     *                    明文密码
     * @return 十六进制摘要串
     */
    private static String legacySha256(String rawPassword) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = messageDigest.digest((LEGACY_SALT + rawPassword).getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("当前 JDK 不支持 SHA-256", e);
        }
    }
}
