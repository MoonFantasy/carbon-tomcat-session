package jz.carbon.tomcat.sesssion.store;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by jack on 2016/12/21.
 */
public class SpyMemcachedClient implements ICacheClient {

    private static final Log log = LogFactory.getLog(SpyMemcachedClient.class);
    private ArrayList<ICacheNode> nodes = new ArrayList<ICacheNode>();
    private Map<Integer, Long> suspendedNodeIndex = new ConcurrentHashMap<Integer, Long>();

    public SpyMemcachedClient() {
    }

    public void addNode(URI uri) {
        addNode(uri.getHost(), uri.getPort() >= 0 ? uri.getPort() : 11211);
    }

    public void addNode(String host, int port) {
        SpyMemcachedNode node = new SpyMemcachedNode(host, port);
        addNode(node);
    }

    public int hashKey(String key) {
        if (key == null)
            return 0;
        return Math.abs(key.hashCode());
    }

    public void addNode(ICacheNode node) {
        log.debug("add Node " + node.getHost() + ":"+node.getPort());
        this.nodes.add(node);
    }

    public boolean isSusspended(int index) {
        return suspendedNodeIndex.containsKey(index);
    }

    public void suspendNode(int index) {
        synchronized (suspendedNodeIndex) {
            log.warn("Suspending Node : " + index);
            long timestamp = System.currentTimeMillis() / 1000;
            suspendedNodeIndex.put(index, timestamp);
        }
    }

    public void resumeNode(int index) {
        synchronized (suspendedNodeIndex) {
            log.warn("Resuming Node : " + index);
            suspendedNodeIndex.remove(index);
        }
    }

    public int getHashAveilableNodeIndex(String key) {
        int size = nodes.size();
        if (suspendedNodeIndex.size() >= size)
            return -1;
        int hashKey = hashKey(key);
        for (int i = 0; i < size; i++, hashKey--) {
            int index = Math.abs(hashKey % size);
            if (!isSusspended(index))
                return index;
        }
        return -1;
    }

    public byte[] get(String key) {
        if (key == null || key.length() == 0)
            throw new IllegalArgumentException("parameter key must not be null or empty");
        do {
            int index = getHashAveilableNodeIndex(key);
            if (index == -1) {
                log.error("No available memcached node to get");
                break;
            }
            try {
                ICacheNode node = nodes.get(index);
                log.debug("get '" + key + "' from " + index + " " + node.getHost() + ":" + node.getPort());
                return node.get(key);
            } catch (Exception e) {
                log.error("get fail Node " + index + " " + nodes.get(index).getHost() + ":" + nodes.get(index).getPort() + " is not available.", e);
                if (e instanceof CacheNodeException)
                    suspendNode(index);
            }
        } while (true);
        return null;
    }

    public byte[] getAndTouch(String key, int expiration) {
        if (key == null || key.length() == 0)
            throw new IllegalArgumentException("parameter key must not be null or empty");

        do {
            int index = getHashAveilableNodeIndex(key);
            if (index == -1) {
                log.error("No available memcached node to get");
                break;
            }
            try {
                ICacheNode node = nodes.get(index);
                log.debug("getAndTouch '" + key + "' from " + index + " " + node.getHost() + ":" + node.getPort());
                return node.getAntTouch(key, expiration);
            } catch (Exception e) {
                log.error("getAndTouch failt Node " + index + " " + nodes.get(index).getHost() + ":" + nodes.get(index).getPort() + " is not available.", e);
                if (e instanceof CacheNodeException)
                    suspendNode(index);
            }
        } while (true);
        return null;
    }

    public boolean set(String key, byte[] value, int expiration) {
        if (key == null || key.length() == 0)
            throw new IllegalArgumentException("parameter key must not be null or empty");

        do {
            int index = getHashAveilableNodeIndex(key);
            if (index == -1) {
                log.error("No available memcached node to store");
                break;
            }
            try {
                ICacheNode node = nodes.get(index);
                log.debug("setting '" + key +  "' length:" + value.length + " to " + index + " " + node.getHost() + ":" + node.getPort());
                return node.set(key, value, expiration);
            } catch (Exception e) {
                log.error("set failt Node " + index + " " + nodes.get(index).getHost() + ":" + nodes.get(index).getPort() + " is not available.", e);
                if (e instanceof CacheNodeException)
                    suspendNode(index);
            }
        } while (true);
        return false;
    }

    public boolean delete(String key) {
        if (key == null || key.length() == 0)
            throw new IllegalArgumentException("parameter key must not be null or empty");

        do {
            int index = getHashAveilableNodeIndex(key);
            if (index == -1) {
                log.error("No available memcached node  to remove");
                break;
            }
            try {
                ICacheNode node = nodes.get(index);
                log.debug("deleting '" + key +  "' from " + index + " " + node.getHost() + ":" + node.getPort());
                return node.remove(key);
            } catch (Exception e) {
                log.error("delete fail Node " + index + " " + nodes.get(index).getHost() + ":" + nodes.get(index).getPort() + " is not available.", e);
                if (e instanceof CacheNodeException)
                    suspendNode(index);
            }
        } while (true);
        return false;
    }

    public void clean() {
        int size = nodes.size();
        log.debug("Cleaning ... ");
        for (int i = 0; i < size; i++) {
            try {
                nodes.get(i).clean();
                nodes.get(i).getStats("reset");
            } catch (Exception e) {
                log.error("Clean fail Node " + i + " " + nodes.get(i).getHost() + ":" + nodes.get(i).getPort() + " is not available.", e);
            }
        }
    }

    public int getSize() {
        int size = nodes.size();
        int result = 0;
        for (int i = 0; i < size; i++) {
            try {
                Map<String, String> stats = nodes.get(i).getStats(null);
                if (stats.get("total_items") != null) {
                    try {
                        result += Integer.parseInt(stats.get("total_items"));
                    } catch (IllegalArgumentException e) {
                        continue;
                    }
                }
                if (stats.get("evicted_unfetched") != null) {
                    try {
                        result -= Integer.parseInt(stats.get("evicted_unfetched"));
                    } catch (IllegalArgumentException e) {
                        continue;
                    }
                }
            } catch (Exception e) {
                log.error("getSize fail Node " + i + " " + nodes.get(i).getHost() + ":" + nodes.get(i).getPort() + " is not available.", e);
            }
        }
        return result;
    }

    public void backgroundWork() {
        synchronized (suspendedNodeIndex) {
            String key = "___TestAlive___";
            String value = "alive";
            for (int index : suspendedNodeIndex.keySet()) {
                try {
                    nodes.get(index).set(key, value.getBytes(), 10);
                } catch (Exception e) {
                    continue;
                }
                try {
                    if ((new String(nodes.get(index).get(key))).compareTo(value) == 0)
                        resumeNode(index);
                } catch (Exception e) {
                    continue;
                }
            }
        }
    }
}
