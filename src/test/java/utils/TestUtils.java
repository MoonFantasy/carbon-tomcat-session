package utils;


import java.security.SecureRandom;
import java.util.Random;

/**
 * Created by jack on 2016/12/26.
 */
public class TestUtils {
    private static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz+_=-`~!@#$%^&*()[]{};':\",./<>?\\|";
    private static SecureRandom rnd = new SecureRandom();

    public static String randomString(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));
        return sb.toString();
    }
    public static byte[] randomBytes(int len) {
        byte [] result = new byte[len];
        new Random().nextBytes(result);
        return result;
    }
}
