package com.hezhujun.shopping.common;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by hezhujun on 2017/6/24.
 * MD5编码
 */
public class MD5Coder {

    /**
     * MD5编码
     * @param str
     * @return
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    public static String encode(String str) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        String newStr = HexCoder.encode(md5.digest(str.getBytes("utf-8")));
        return newStr;
    }

    /**
     * 简单测试
     * @param args
     */
    public static void main(String[] args) {
        try {
            System.out.println(encode("123456"));
            System.out.println(encode("123456").length());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

}
