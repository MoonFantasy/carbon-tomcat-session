package jz.carbon.tomcat.sesssion.store;

import net.spy.memcached.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.UnresolvedAddressException;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by jack on 2016/12/22.
 */
public class SpyMemcachedNode implements ICacheNode {

    private static final Log log = LogFactory.getLog(SpyMemcachedNode.class);
    private String host;
    private int port;
    private MemcachedClientIF memcachedClient = null;
    static private Class memcachedClientClass = MemcachedClient.class;
    private long timeout = 2000;

    static {
        Properties systemProperties = System.getProperties();
        systemProperties.put("net.spy.log.LoggerImpl", "net.spy.memcached.compat.log.Log4JLogger");
        System.setProperties(systemProperties);
    }

    public SpyMemcachedNode(String host, int port) {
        setHost(host);
        setPort(port);
    }

    public void setMemcachedClient(MemcachedClientIF memcachedClient) {
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
        addrs.add(new InetSocketAddress(host, port));
        try {
            return (MemcachedClientIF) memcachedClientClass.getConstructor(ConnectionFactory.class, List.class).newInstance(new BinaryConnectionFactory(), addrs);
        } catch (InvocationTargetException e) {
            log.error("Create Spy Memcached client fail " + e.getCause().getMessage() + " " + e.getCause().getCause());
            if (e.getCause() instanceof IOException)
                throw (IOException) e.getCause();
            else if (e.getCause() instanceof UnresolvedAddressException)
                throw (UnresolvedAddressException) e.getCause();
            return null;
        } catch (Exception e) {
            log.error("Create Spy Memcached client fail " + e.getMessage() + " " + e.getCause());
            return null;
        }
    }

    public MemcachedClientIF getMemcachedClient() throws IOException {
        if (memcachedClient == null) {
            memcachedClient = getNewMemcachedClient();
        }
        return memcachedClient;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getHost() {
        return this.host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return this.port;
    }

    public byte[] get(String key) throws CacheNodeException {
        MemcachedClientIF client = null;
        try {
            client = getMemcachedClient();
        } catch (IOException e) {
            throw new CacheNodeException("IO Error " + e.getMessage() + " " + e.getCause(), e);
        } catch (UnresolvedAddressException e) {
            throw new CacheNodeException("Unresolved Address " + e.getMessage() + " " + e.getCause(), e);
        }
        Future<Object> future = null;
        try {
            future = client.asyncGet(key);
            return (byte[]) future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (CancellationException e) {
            log.error("Cancellation " + e.getCause());
            return null;
        } catch (InterruptedException e) {
            throw new CacheNodeException("Interrupted waiting for value" + e.getMessage() + " " + e.getCause(), e);
        } catch (ExecutionException e) {
            throw new CacheNodeException("Execution Exception " + e.getMessage() + " " + e.getCause(), e);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new CacheNodeException("Timeout waiting for value: " + e.getMessage() + " " + e.getCause(), e);
        } catch (Exception e) {
            throw new CacheNodeException(e.getMessage() + " " + e.getCause(), e);
        }
    }

    public byte[] getAntTouch(String key, int expiration) throws CacheNodeException {
        MemcachedClientIF client = null;
        try {
            client = getMemcachedClient();
        } catch (IOException e) {
            throw new CacheNodeException("IO Error " + e.getMessage() + " " + e.getCause(), e);
        } catch (UnresolvedAddressException e) {
            throw new CacheNodeException("Unresolved Address " + e.getMessage() + " " + e.getCause(), e);
        }
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
        } catch (InterruptedException e) {
            throw new CacheNodeException("Interrupted waiting for value" + e.getMessage() + " " + e.getCause(), e);
        } catch (ExecutionException e) {
            throw new CacheNodeException("Execution Exception " + e.getMessage() + " " + e.getCause(), e);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new CacheNodeException("Timeout waiting for value: " + e.getMessage() + " " + e.getCause(), e);
        } catch (Exception e) {
            throw new CacheNodeException(e.getMessage() + " " + e.getCause(), e);
        }

    }

    public boolean set(String key, byte[] data, int expiration) throws CacheNodeException {
        MemcachedClientIF client = null;
        try {
            client = getMemcachedClient();
        } catch (IOException e) {
            throw new CacheNodeException("IO Error " + e.getMessage() + " " + e.getCause(), e);
        } catch (UnresolvedAddressException e) {
            throw new CacheNodeException("Unresolved Address " + e.getMessage() + " " + e.getCause(), e);
        }
        Future<Boolean> future = null;
        try {
            future = client.set(key, expiration, data);
            boolean result = future.get(timeout, TimeUnit.MILLISECONDS);
            log.debug("Set "+ key + " data length :"+ data.length);
            return result;
        } catch (CancellationException e) {
            log.error("Cancellation " + e.getCause());
            return false;
        } catch (InterruptedException e) {
            throw new CacheNodeException("Interrupted waiting for value " + e.getMessage() + " " + e.getCause(), e);
        } catch (ExecutionException e) {
            throw new CacheNodeException("Execution Exception " + e.getMessage() + " " + e.getCause(), e);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new CacheNodeException("Timeout waiting for value: " + e.getMessage() + " " + e.getCause(), e);
        } catch (Exception e) {
            throw new CacheNodeException(e.getMessage() + " " + e.getCause(), e);
        }
    }

    public boolean remove(String key) throws CacheNodeException {
        MemcachedClientIF client = null;
        try {
            client = getMemcachedClient();
        } catch (IOException e) {
            throw new CacheNodeException("IO Error " + e.getMessage() + " " + e.getCause(), e);
        } catch (UnresolvedAddressException e) {
            throw new CacheNodeException("Unresolved Address " + e.getMessage() + " " + e.getCause(), e);
        }
        Future<Boolean> future = null;
        try {
            future = client.delete(key);
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (CancellationException e) {
            log.error("Cancellation " + e.getCause());
            return false;
        } catch (InterruptedException e) {
            throw new CacheNodeException("Interrupted waiting for value " + e.getMessage() + " " + e.getCause(), e);
        } catch (ExecutionException e) {
            throw new CacheNodeException("Execution Exception " + e.getMessage() + " " + e.getCause(), e);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new CacheNodeException("Timeout waiting for value: " + e.getMessage() + " " + e.getCause(), e);
        } catch (Exception e) {
            throw new CacheNodeException(e.getMessage() + " " + e.getCause(), e);
        }
    }

    public void clean() throws CacheNodeException {
        MemcachedClientIF client = null;
        try {
            client = getMemcachedClient();
        } catch (IOException e) {
            throw new CacheNodeException("IO Error " + e.getMessage() + " " + e.getCause(), e);
        } catch (UnresolvedAddressException e) {
            throw new CacheNodeException("Unresolved Address " + e.getMessage() + " " + e.getCause(), e);
        }
        Future<Boolean> future = null;
        try {
            future = client.flush();
            future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (CancellationException e) {
            log.error("Cancellation " + e.getCause());
        } catch (InterruptedException e) {
            throw new CacheNodeException("Interrupted waiting for value" + e.getMessage() + " " + e.getCause(), e);
        } catch (ExecutionException e) {
            throw new CacheNodeException("Execution Exception " + e.getMessage() + " " + e.getCause(), e);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new CacheNodeException("Timeout waiting for value: " + e.getMessage() + " " + e.getCause(), e);
        } catch (Exception e) {
            throw new CacheNodeException(e.getMessage() + " " + e.getCause(), e);
        }
    }

    public Map<String, String> getStats(String prefix) throws CacheNodeException {
        MemcachedClientIF client = null;
        try {
            client = getMemcachedClient();
        } catch (IOException e) {
            throw new CacheNodeException("IO Error " + e.getMessage() + " " + e.getCause(), e);
        } catch (UnresolvedAddressException e) {
            throw new CacheNodeException("Unresolved Address " + e.getMessage() + " " + e.getCause(), e);
        }

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
}
