package jz.carbon.tomcat.sesssion.store;

import java.net.URI;
import java.util.List;

/**
 * Created by jack on 2016/12/21.
 */
public interface IFCacheClient {
    void addNode(URI uri);

    void addNode(IFCacheNode node);

    void removeAllNode();

    int getNodeSize();

    byte[] get(String key) throws CacheClientIOException;

    byte[] getAndTouch(String key, int expiration) throws CacheClientIOException;

    boolean set(String key, byte[] value, int expiration) throws CacheClientIOException;

    boolean delete(String key) throws CacheClientIOException;

    int getSize() throws CacheClientIOException;

    void clean() throws CacheClientIOException;

    void backgroundWork();

    List<String> getKeys() throws CacheClientIOException;

    AbstractCacheStore getStore();

    void setStore(AbstractCacheStore store);

    String getUriScheme();

    String getUriSslScheme();

    int getDefaultPort();

    void setKeyPrefix(String keyPrefix);

}
