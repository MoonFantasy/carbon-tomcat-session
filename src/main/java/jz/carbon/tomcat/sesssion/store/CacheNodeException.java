package jz.carbon.tomcat.sesssion.store;

/**
 * Created by jack on 2016/12/26.
 */
public class CacheNodeException extends Exception {
    public CacheNodeException(String message, Throwable cause) {
        super(message, cause);
    }

    public CacheNodeException(Throwable cause) {
        super(cause);
    }
}
