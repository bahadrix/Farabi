package org.farabiproject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Farabi
 * User: Bahadir
 * Date: 04.02.2014
 * Time: 17:04
 */
public class Util {

    public static String getMD5(String original) {
        StringBuffer sb = null;
        try {


            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(original.getBytes());

            byte byteData[] = md.digest();

            sb = new StringBuffer();
            for (int i = 0; i < byteData.length; i++) {
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
            }

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return sb.toString();

    }
}
