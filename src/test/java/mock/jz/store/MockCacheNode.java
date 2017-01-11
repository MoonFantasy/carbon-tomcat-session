package mock.jz.store;

import jz.carbon.tomcat.sesssion.store.CacheNodeException;
import jz.carbon.tomcat.sesssion.store.IFCacheNode;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jack on 2017/1/8.
 */
public class MockCacheNode implements IFCacheNode {

    private URI uri;
    private static Map<String, byte[]> innerStore = new ConcurrentHashMap<String, byte[]>();
    public MockCacheNode(URI uri) {
        setUri(uri);
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public URI getUri() {
        return uri;
    }

    public byte[] get(String key) throws CacheNodeException {
        return innerStore.get(key);
    }

    public byte[] getAntTouch(String key, int expiration) throws CacheNodeException {

        return innerStore.get(key);
    }

    public boolean set(String key, byte[] data, int expiration) throws CacheNodeException {

        synchronized (innerStore) {
            innerStore.put(key, data);
        }
        return true;
    }

    public boolean remove(String key) throws CacheNodeException {
        synchronized (innerStore) {
            innerStore.remove(key);
        }
        return true;
    }

    public void clean() throws CacheNodeException {
        synchronized (innerStore) {
            innerStore.clear();
        }
    }

    public int getKeyCount(String prefix) throws CacheNodeException {
        return innerStore.size();
    }

    public Map<String, String> getStats(String prefix) throws CacheNodeException {
        return new HashMap<String, String>();
    }

    public List<String> getKeys(String prefix) throws CacheNodeException {
        ArrayList<String> list = new ArrayList<String>();
        list.addAll(innerStore.keySet());
        return list;
    }

    public void close() {

    }
}
