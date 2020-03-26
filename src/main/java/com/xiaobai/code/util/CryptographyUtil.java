package com.xiaobai.code.util;

import org.apache.shiro.crypto.hash.Md5Hash;

/**
 * 加密工具类
 */
public class CryptographyUtil {
    public static final String SALT="code";
    /**
     * md5加密
     */
    public static String md5(String str,String salt){
        return new Md5Hash(str,salt).toString();
    }

    public static void main(String[] args) {
        String password ="123456";
        System.out.println(CryptographyUtil.md5(password,SALT));
    }
}
