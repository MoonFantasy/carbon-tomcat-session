package mock.jz.store;

import jz.carbon.tomcat.sesssion.store.CacheNodeException;
import jz.carbon.tomcat.sesssion.store.IFCacheNode;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jack on 2017/1/8.
 */
public class MockCacheNode implements IFCacheNode {

    private URI uri;

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

        return null;
    }

    public byte[] getAntTouch(String key, int expiration) throws CacheNodeException {

        return null;
    }

    public boolean set(String key, byte[] data, int expiration) throws CacheNodeException {

        return true;
    }

    public boolean remove(String key) throws CacheNodeException {
        return true;
    }

    public void clean() throws CacheNodeException {

    }

    public int getKeyCount(String prefix) throws CacheNodeException {
        return 0;
    }

    public Map<String, String> getStats(String prefix) throws CacheNodeException {
        return new HashMap<String, String>();
    }

    public List<String> getKeys(String prefix) throws CacheNodeException {
        return new ArrayList<String>();
    }

    public void close() {

    }
}
