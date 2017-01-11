package jz.carbon.tomcat.sesssion.store;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jack on 2017/1/4.
 */
abstract public class AbstractCacheClient implements IFCacheClient {
    private static final Log log = LogFactory.getLog(AbstractCacheClient.class);
    private ArrayList<IFCacheNode> nodes = new ArrayList<IFCacheNode>();
    private Map<Integer, Long> suspendedNodeIndex = new ConcurrentHashMap<Integer, Long>();
    private AbstractCacheStore store;
    private String keyPrefix = "";

    public AbstractCacheClient(AbstractCacheStore store) {
        this.setStore(store);
    }

    abstract public String getDriverName();

    abstract protected IFCacheNode createNewCacheNode(URI uri);

    public AbstractCacheStore getStore() {
        return store;
    }

    public void setStore(AbstractCacheStore store) {
        this.store = store;
    }

    public void addNode(URI uri) {
        if (!isValidUriScheme(uri)) {
            log.warn("Invalid uri scheme " + uri);
            return;
        }
        if (uri.getPort() < 0) {
            try {
                URI newUri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), getDefaultPort(), uri.getPath(), uri.getQuery(), uri.getFragment());
                uri = newUri;
            } catch (URISyntaxException e) {
                log.error("Add node create URI fail ", e);
                return;
            }
        }
        addNode(createNewCacheNode(uri));
    }

    public void addNode(IFCacheNode node) {
        log.debug("add Node " + node.getUri().getHost() + ":" + node.getUri().getPort());
        this.nodes.add(node);
    }

    public boolean isSusspended(int index) {
        return suspendedNodeIndex.containsKey(index);
    }

    public void suspendNode(int index) {
        synchronized (suspendedNodeIndex) {
            log.warn("Suspending Node : " + index + " " + getNode(index).toString());
            long timestamp = System.currentTimeMillis() / 1000;
            suspendedNodeIndex.put(index, timestamp);
        }
    }

    public int hashKey(String key) {
        if (key == null)
            return 0;
        return Math.abs(key.hashCode());
    }


    public void resumeNode(int index) {
        synchronized (suspendedNodeIndex) {
            log.warn("Resuming Node : " + index + " " + getNode(index).toString());
            suspendedNodeIndex.remove(index);
        }
    }

    public boolean isAllSusspended() {
        return suspendedNodeIndex.size() >= nodes.size();
    }

    public int getHashAveilableNodeIndex(String key) {
        if (isAllSusspended())
            return -1;

        int index = -1;
        int size = nodes.size();
        int hashKey = hashKey(key);
        for (int i = 0; i < size; i++, hashKey--) {
            index = Math.abs(hashKey % size);
            if (!isSusspended(index))
                break;
        }
        return index;
    }

    public IFCacheNode getNode(int index) {
        return nodes.get(index);
    }

    public int getNodeSize() {
        return nodes.size();
    }

    public void backgroundWork() {
        synchronized (suspendedNodeIndex) {
            String key = "___TestAlive___";
            String value = "alive";
            for (int index : suspendedNodeIndex.keySet()) {
                log.debug("Check node " + index + " ...");
                try {
                    getNode(index).set(key, value.getBytes(), 10);
                } catch (Exception e) {
                    continue;
                }
                try {
                    if ((new String(getNode(index).get(key))).compareTo(value) == 0)
                        resumeNode(index);
                } catch (Exception e) {
                    continue;
                }
            }
        }
    }


    public byte[] get(String key) throws CacheClientIOException {
        return getAndTouch(key, -1);
    }

    public byte[] getAndTouch(String key, int expiration) throws CacheClientIOException {
        if (key == null || key.length() == 0)
            throw new IllegalArgumentException("parameter key must not be null or empty");

        String storeKey = getStoreKey(key);

        do {
            int index = getHashAveilableNodeIndex(key);
            if (index == -1) {
                throw new CacheClientIOException("No available " + getDriverName() + " node to get");
            }
            IFCacheNode node = getNode(index);
            try {
                if (expiration > 0) {
                    log.debug("getAndTouch '" + key + "' from " + index + " " + node.getUri().getHost() + ":" + node.getUri().getPort());
                    return node.getAntTouch(storeKey, expiration);
                } else {
                    log.debug("get '" + key + "' from " + index + " " + node.getUri().getHost() + ":" + node.getUri().getPort());
                    return node.get(storeKey);
                }
            } catch (Exception e) {
                log.error("Fail get data from Node " + index + " " + node.getUri().getHost() + ":" + node.getUri().getPort() + " is not available.", e);
                if (e instanceof CacheNodeException)
                    suspendNode(index);
                else
                    return null;
            }
        } while (true);
    }

    public boolean set(String key, byte[] value, int expiration) throws CacheClientIOException {
        if (key == null || key.length() == 0)
            throw new IllegalArgumentException("parameter key must not be null or empty");

        String storeKey = getStoreKey(key);

        do {
            int index = getHashAveilableNodeIndex(key);
            if (index == -1) {
                throw new CacheClientIOException("No available " + getDriverName() + " node to store");
            }
            IFCacheNode node = getNode(index);
            try {
                log.debug("setting '" + key + "' length:" + value.length + " to " + index + " " + node.getUri().getHost() + ":" + node.getUri().getPort());
                return node.set(storeKey, value, expiration);
            } catch (Exception e) {
                log.error("set failt Node " + index + " " + node.getUri().getHost() + ":" + node.getUri().getPort() + " is not available.", e);
                if (e instanceof CacheNodeException)
                    suspendNode(index);
                else
                    return false;
            }
        } while (true);
    }

    public boolean delete(String key) throws CacheClientIOException {
        if (key == null || key.length() == 0)
            throw new IllegalArgumentException("parameter key must not be null or empty");
        String storeKey = getStoreKey(key);
        do {
            int index = getHashAveilableNodeIndex(key);
            if (index == -1) {
                throw new CacheClientIOException("No available " + getDriverName() + " node to remove");
            }
            IFCacheNode node = getNode(index);
            try {
                log.debug("deleting '" + key + "' from " + index + " " + node.getUri().getHost() + ":" + node.getUri().getPort());
                return node.remove(storeKey);
            } catch (Exception e) {
                log.error("delete fail Node " + index + " " + node.getUri().getHost() + ":" + node.getUri().getPort() + " is not available.", e);
                if (e instanceof CacheNodeException)
                    suspendNode(index);
                else
                    return false;
            }
        } while (true);
    }

    public void clean() throws CacheClientIOException {
        if (isAllSusspended())
            throw new CacheClientIOException("No available " + getDriverName() + " node to clean");

        int size = getNodeSize();
        log.debug("Cleaning ... ");
        for (int i = 0; i < size; i++) {
            IFCacheNode node = getNode(i);
            try {
                node.clean();
                node.getStats("reset");
            } catch (Exception e) {
                log.error("Clean fail Node " + i + " " + node.getUri().getHost() + ":" + node.getUri().getPort() + " is not available.", e);
            }
        }
    }

    public int getSize() throws CacheClientIOException {
        if (isAllSusspended())
            throw new CacheClientIOException("No available " + getDriverName() + " node to get size");

        int size = getNodeSize();
        int result = 0;
        for (int i = 0; i < size; i++) {
            IFCacheNode node = getNode(i);
            try {
                result += node.getKeyCount(keyPrefix);
            } catch (Exception e) {
                log.error("getSize fail Node " + i + " " + node.getUri().getHost() + ":" + node.getUri().getPort() + " is not available.", e);
            }
        }
        return result;
    }

    public List<String> getKeys() throws CacheClientIOException {
        if (isAllSusspended())
            throw new CacheClientIOException("No available " + getDriverName() + " node to get keys");

        int size = getNodeSize();
        ArrayList<String> keys = new ArrayList<String>();
        for (int i = 0; i < size; i++) {
            IFCacheNode node = getNode(i);
            try {
                keys.addAll(node.getKeys(keyPrefix));
            } catch (Exception e) {
                log.error("getSize fail Node " + i + " " + node.getUri().getHost() + ":" + node.getUri().getPort() + " is not available.", e);
            }
        }
        return keys;
    }

    public boolean isValidUriScheme(URI uri) {
        if (uri == null || uri.getScheme() == null)
            return false;

        String scheme = uri.getScheme().toLowerCase();
        return !(scheme.compareTo(getUriScheme().toLowerCase()) != 0
                && scheme.compareTo(getUriSslScheme().toLowerCase()) != 0);
    }

    private String getStoreKey(String key) {
        return keyPrefix == null ? key : keyPrefix + key;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public void removeAllNode() {
        for (IFCacheNode node : nodes)
            node.close();
        nodes.clear();
    }
}
