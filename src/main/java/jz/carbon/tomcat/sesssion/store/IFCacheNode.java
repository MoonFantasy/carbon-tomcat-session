package jz.carbon.tomcat.sesssion.store;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Created by jack on 2016/12/21.
 */
public interface IFCacheNode {
    void setUri(URI uri);

    URI getUri();

    byte[] get(String key) throws CacheNodeException;

    byte[] getAntTouch(String key, int expiration) throws CacheNodeException;

    boolean set(String key, byte[] data, int expiration) throws CacheNodeException;

    boolean remove(String key) throws CacheNodeException;

    void clean() throws CacheNodeException;

    int getKeyCount(String prefix) throws CacheNodeException;

    Map<String, String> getStats(String prefix) throws CacheNodeException;

    List<String> getKeys(String prefix) throws CacheNodeException;

    void close();
}
