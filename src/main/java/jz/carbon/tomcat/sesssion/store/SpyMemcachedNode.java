package jz.carbon.tomcat.sesssion.store;

import net.spy.memcached.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.nio.channels.UnresolvedAddressException;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by jack on 2016/12/22.
 */
public class SpyMemcachedNode implements IFCacheNode {

    private static final Log log = LogFactory.getLog(SpyMemcachedNode.class);
    private URI uri;
    private MemcachedClientIF memcachedClient = null;
    static private Class memcachedClientClass = MemcachedClient.class;
    private long timeout = 2000;

    static {
        Properties systemProperties = System.getProperties();
        systemProperties.put("net.spy.log.LoggerImpl", "net.spy.memcached.compat.log.Log4JLogger");
        System.setProperties(systemProperties);
    }

    public SpyMemcachedNode(URI uri) {
        this.setUri(uri);
    }

    public void setMemcachedClient(MemcachedClientIF memcachedClient) {
        close();
        this.memcachedClient = memcachedClient;
    }

    static public void setMemcachedClientClass(Class memcachedClientClass) {
        if (!Arrays.asList(memcachedClientClass.getInterfaces()).contains(MemcachedClientIF.class))
            throw new IllegalArgumentException(memcachedClientClass.getName() + " is not MemcachedClientIF implementation");
        synchronized (SpyMemcachedNode.memcachedClientClass) {
            SpyMemcachedNode.memcachedClientClass = memcachedClientClass;
        }
    }

    public MemcachedClientIF getNewMemcachedClient() throws IOException {
        ArrayList<InetSocketAddress> addrs = new ArrayList<InetSocketAddress>();
        addrs.add(new InetSocketAddress(uri.getHost(), uri.getPort()));
        try {
            return (MemcachedClientIF) memcachedClientClass
                    .getConstructor(ConnectionFactory.class, List.class)
                    .newInstance(new BinaryConnectionFactory(), addrs);
        } catch (Exception e) {
            log.error("Create Spy Memcached client fail " + e.getCause().getMessage() + " " + e.getCause().getCause());
            if (e.getCause() instanceof IOException)
                throw (IOException) e.getCause();
            else if (e.getCause() instanceof UnresolvedAddressException)
                throw (UnresolvedAddressException) e.getCause();
            return null;
        }
    }

    public MemcachedClientIF getMemcachedClient() throws CacheNodeException {
        if (memcachedClient == null) {
            try {
                memcachedClient = getNewMemcachedClient();
            } catch (IOException e) {
                throw new CacheNodeException("IO Error " + e.getMessage() + " " + e.getCause(), e);
            } catch (UnresolvedAddressException e) {
                throw new CacheNodeException("Unresolved Address " + e.getMessage() + " " + e.getCause(), e);
            }
        }
        return memcachedClient;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public URI getUri() {
        return this.uri;
    }

    public String getHost() {
        return uri.getHost();
    }

    public int getPort() {
        return uri.getPort();
    }

    public byte[] get(String key) throws CacheNodeException {
        MemcachedClientIF client = getMemcachedClient();
        Future<Object> future = null;
        try {
            future = client.asyncGet(key);
            return (byte[]) future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (CancellationException e) {
            log.error("Cancellation " + e.getCause());
            return null;
        } catch (Exception e) {
            throw new CacheNodeException(e.getMessage(), e);
        }
    }

    public byte[] getAntTouch(String key, int expiration) throws CacheNodeException {
        MemcachedClientIF client = getMemcachedClient();
        Future<CASValue<Object>> future = null;
        try {
            future = client.asyncGetAndTouch(key, expiration);
            CASValue<Object> cas = future.get(timeout, TimeUnit.MILLISECONDS);
            if (cas == null)
                return null;
            return (byte[]) cas.getValue();
        } catch (CancellationException e) {
            log.error("Cancellation " + e.getCause());
            return null;
        } catch (Exception e) {
            throw new CacheNodeException(e.getMessage(), e);
        }

    }

    public boolean set(String key, byte[] data, int expiration) throws CacheNodeException {
        MemcachedClientIF client = getMemcachedClient();
        Future<Boolean> future = null;
        try {
            future = client.set(key, expiration, data);
            boolean result = future.get(timeout, TimeUnit.MILLISECONDS);
            log.debug("Set "+ key + " data length :"+ data.length);
            return result;
        } catch (CancellationException e) {
            log.error("Cancellation " + e.getCause());
            return false;
        } catch (Exception e) {
            throw new CacheNodeException(e.getMessage(), e);
        }
    }

    public boolean remove(String key) throws CacheNodeException {
        MemcachedClientIF client = getMemcachedClient();
        Future<Boolean> future = null;
        try {
            future = client.delete(key);
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (CancellationException e) {
            log.error("Cancellation " + e.getCause());
            return false;
        } catch (Exception e) {
            throw new CacheNodeException(e.getMessage(), e);
        }
    }

    public void clean() throws CacheNodeException {
        MemcachedClientIF client = getMemcachedClient();
        Future<Boolean> future = null;
        try {
            future = client.flush();
            future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (CancellationException e) {
            log.error("Cancellation " + e.getCause());
        } catch (Exception e) {
            throw new CacheNodeException(e.getMessage(), e);
        }
    }

    public Map<String, String> getStats(String prefix) throws CacheNodeException {
        MemcachedClientIF client = getMemcachedClient();

        try {
            Map<SocketAddress, Map<String, String>> stats = client.getStats(prefix);
            if (stats == null)
                return new HashMap<String, String>();

            for (SocketAddress address : stats.keySet()) {
                return stats.get(address);
            }
        } catch (Exception e) {
            throw new CacheNodeException(e.getClass().getName() + " Get State Fail: " + e.getMessage() + " " + e.getCause(), e);
        }
        return null;
    }

    public int getKeyCount(String keyPrefix) throws CacheNodeException {
        Map<String, String> stats = getStats(null);
        int result = 0;
        result += Integer.parseInt(stats.get("total_items"));
        if (stats.get("total_items") != null) {
            try {
                result += Integer.parseInt(stats.get("total_items"));
            } catch (IllegalArgumentException e) {
            }
        }
        if (stats.get("evicted_unfetched") != null) {
            try {
                result -= Integer.parseInt(stats.get("evicted_unfetched"));
            } catch (IllegalArgumentException e) {
            }
        }
        return result;
    }
    public List<String> getKeys(String prefix) throws CacheNodeException {
        return new ArrayList<String>();
    }

    public void close() {
        if (memcachedClient != null) {
            memcachedClient.shutdown();
            memcachedClient = null;
        }
    }
}
