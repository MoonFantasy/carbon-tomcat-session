package jz.carbon.tomcat.sesssion.store;

import java.io.IOException;

/**
 * Created by jack on 2017/1/8.
 */
public class CacheClientIOException extends IOException {
    public CacheClientIOException(String message) {
        super(message);
    }
}
