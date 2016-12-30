package jz.carbon.tomcat.sesssion.store;

import java.util.Map;

/**
 * Created by jack on 2016/12/21.
 */
public interface ICacheNode {
    void setHost(String host);

    String getHost();

    void setPort(int port);

    int getPort();

    byte[] get(String key) throws CacheNodeException;

    byte[] getAntTouch(String key, int expiration) throws CacheNodeException;

    boolean set(String key, byte[] data, int expiration) throws CacheNodeException;

    boolean remove(String key) throws CacheNodeException;

    void clean() throws CacheNodeException;

    Map<String, String> getStats(String prefix) throws CacheNodeException;

}
