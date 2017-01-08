package jz.carbon.tomcat.sesssion.store;

import java.net.URI;

/**
 * Created by jack on 2016/12/21.
 */
public interface ICacheClient {
    void addNode(URI uri);
    void addNode(ICacheNode node);
    byte[] get(String key);
    byte[] getAndTouch(String key, int expiration);
    boolean set(String key, byte[] value, int expiration);
    boolean delete(String key);
    int getSize();
    void clean();
    void backgroundWork();
}
