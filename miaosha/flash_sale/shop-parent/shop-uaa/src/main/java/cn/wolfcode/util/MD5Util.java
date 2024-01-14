package cn.wolfcode.util;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * Created by lanxw
 * 登录加密算法
 */
public class MD5Util {
    public static String encode(String password,String salt){
        return DigestUtils.md5Hex(""+salt.charAt(0)+salt.charAt(2)+password+salt.charAt(4)+salt.charAt(5));
    }
}
