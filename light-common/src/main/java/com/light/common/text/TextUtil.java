package com.light.common.text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 处理文本里面敏感信息，如手机号、邮箱等，部分信息模糊掉
 */
public class TextUtil {

    public static final char SHELTER = '*';

    private static final String EMAIL_REGEX = "[\\w[.-]]+@[\\w[.-]]+\\.[\\w]+";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    private static final String MOBILE_REGEX =
        "(?<!\\d)(?:(?:(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\\d{8}))(?!\\d)";
    private static final Pattern MOBILE_PATTERN = Pattern.compile(MOBILE_REGEX);

    private static final String ID_REGEX = "(?<!\\d)((\\d{15})|(\\d{18}))(?!(\\d))|(\\d{17}(X|x))";
    private static final Pattern ID_PATTERN = Pattern.compile(ID_REGEX);

    public static String desensitize(String text) {
        StringBuilder builder = new StringBuilder(text);
        desensitizeEmail(text, builder);
        desensitizeMobile(text, builder);
        desensitizeID(text, builder);
        return builder.toString();
    }

    /**
     * 保留前三@后 13800138000@163.com => 138********@163.com
     *
     * @param text
     * @param builder
     */
    private static void desensitizeEmail(String text, final StringBuilder builder) {
        int pos = text.indexOf("@");
        if (pos == -1) {
            return;
        }
        try {
            Matcher matcher = EMAIL_PATTERN.matcher(text);
            while (matcher.find()) {
                int start = matcher.start();
                int middle = (pos - start) / 2;
                if (middle < pos) {
                    for (int i = start + middle; i < pos; i++) {
                        builder.setCharAt(i, SHELTER);
                    }
                }
                pos = text.indexOf("@", pos + 1);
            }
        } catch (Exception e) {
            // ignore
        }
    }

    /**
     * <ul>
     * <li>15位 保留前6后3</li>
     * <li>18位 保留前6后4</li>
     * </ul>
     * 18位 前6后4 500200187109160297 => 500200********0297
     * 
     * @param text
     * @param builder
     */
    private static void desensitizeMobile(String text, final StringBuilder builder) {
        try {
            Matcher matcher = MOBILE_PATTERN.matcher(text);
            while (matcher.find()) {
                int pos = matcher.start();
                for (int i = pos + 3; i < pos + 7; i++) {
                    builder.setCharAt(i, SHELTER);
                }
            }
        } catch (Exception e) {
            // ignore
        }
    }

    /**
     * 保留前3后4 13800138000 => 138****8000
     *
     * @param text
     * @param builder
     */
    private static void desensitizeID(String text, final StringBuilder builder) {
        try {
            Matcher matcher = ID_PATTERN.matcher(text);
            while (matcher.find()) {
                int pos = matcher.start();
                int end = matcher.end();
                int len = (end - pos) == 15 ? 12 : 14;
                for (int i = pos + 6; i < pos + len; i++) {
                    builder.setCharAt(i, SHELTER);
                }
            }
        } catch (Exception e) {
            // ignore
        }
    }

    public static void main(String[] args) {
        System.out.println(TextUtil.desensitize("500200187109160  1380000@163.com dfsdf9212121@qq.comds"));
    }
}
