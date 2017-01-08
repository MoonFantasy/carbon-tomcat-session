package jz.carbon.tomcat.sesssion.store;

import java.net.URI;

/**
 * Created by jack on 2016/12/31.
 */
public class JedisCacheClient implements IFCacheClient {
    public void addNode(URI uri) {

    }
    public void addNode(IFCacheNode node) {

    }
    public byte[] get(String key) {
        return null;
    }

    public byte[] getAndTouch(String key, int expiration) {
        return null;
    }
    public boolean set(String key, byte[] value, int expiration) {
        return false;
    }
    public boolean delete(String key) {
        return false;
    }
    public int getSize() {
        return 0;
    }
    public void clean() {

    }
    public void backgroundWork() {

    }
}
