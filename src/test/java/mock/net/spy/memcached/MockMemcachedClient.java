package mock.net.spy.memcached;

import net.spy.memcached.*;
import net.spy.memcached.internal.BulkFuture;
import net.spy.memcached.internal.OperationFuture;
import net.spy.memcached.transcoders.Transcoder;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.UnresolvedAddressException;
import java.util.*;
import java.util.concurrent.*;

/**
 * Created by jack on 2016/12/28.
 */
public class MockMemcachedClient implements MemcachedClientIF {
    public static MockMemcachedClient getNewInstance(String host, int port) throws IOException {
        ArrayList<InetSocketAddress> addrs = new ArrayList<InetSocketAddress>();
        addrs.add(new InetSocketAddress(host, port));
        return new MockMemcachedClient(null, addrs);
    }
    private int sw = 0;
    private ConcurrentHashMap<String, MutablePair<Object, Long>> mockMemcached = new ConcurrentHashMap<String, MutablePair<Object, Long>>();
    public void setSw(int sw) {
        this.sw = sw;
    }

    public MockMemcachedClient(ConnectionFactory cf, List<InetSocketAddress> addrs) throws IOException {
        if (addrs == null || addrs.isEmpty())
            return;
        if (addrs.get(0).getPort() == 1)
            throw new IOException("test IOException");
        if (addrs.get(0).getPort() == 2)
            throw new UnresolvedAddressException();

        this.sw = addrs.get(0).getPort();
    }

    public Collection<SocketAddress> getAvailableServers() {
        return null;
    }

    public Collection<SocketAddress> getUnavailableServers() {
        return null;
    }

    public Transcoder<Object> getTranscoder() {
        return null;
    }

    public NodeLocator getNodeLocator() {
        return null;
    }

    public Future<Boolean> append(long cas, String key, Object val) {
        return null;
    }

    public Future<Boolean> append(String key, Object val) {
        return null;
    }

    public <T> Future<Boolean> append(long cas, String key, T val, Transcoder<T> tc) {
        return null;
    }

    public <T> Future<Boolean> append(String key, T val, Transcoder<T> tc) {
        return null;
    }

    public Future<Boolean> prepend(long cas, String key, Object val) {
        return null;
    }

    public Future<Boolean> prepend(String key, Object val) {
        return null;
    }

    public <T> Future<Boolean> prepend(long cas, String key, T val, Transcoder<T> tc) {
        return null;
    }

    public <T> Future<Boolean> prepend(String key, T val, Transcoder<T> tc) {
        return null;
    }

    public <T> Future<CASResponse> asyncCAS(String key, long casId, T value,
                                            Transcoder<T> tc) {
        return null;
    }

    public Future<CASResponse> asyncCAS(String key, long casId, Object value) {
        return null;
    }

    public Future<CASResponse> asyncCAS(String key, long casId, int exp, Object value) {
        return null;
    }

    public <T> OperationFuture<CASResponse> asyncCAS(String key, long casId, int exp,
                                                     T value, Transcoder<T> tc) {
        return null;
    }

    public <T> CASResponse cas(String key, long casId, int exp, T value,
                               Transcoder<T> tc) {
        return null;
    }

    public CASResponse cas(String key, long casId, Object value) {
        return null;
    }

    public CASResponse cas(String key, long casId, int exp, Object value) {
        return null;
    }

    public <T> CASResponse cas(String key, long casId, T value, Transcoder<T> tc) {
        return null;
    }

    public <T> Future<Boolean> add(String key, int exp, T o, Transcoder<T> tc) {
        return null;
    }

    public Future<Boolean> add(String key, int exp, Object o) {
        return null;
    }

    public <T> Future<Boolean> set(String key, int exp, T o, Transcoder<T> tc) {
        synchronized (mockMemcached) {
            mockMemcached.put(key, new MutablePair<Object, Long>(o, exp > 0 ? (System.currentTimeMillis() / 1000) + exp : 0));
        }
        return new MockFuture(true, sw);
    }

    public Future<Boolean> set(String key, int exp, Object o) {

        synchronized (mockMemcached){
            mockMemcached.put(key, new MutablePair<Object, Long>(o, exp > 0 ? (System.currentTimeMillis() / 1000) + exp : 0));
        }
        return new MockFuture(true, sw);
    }

    public <T> Future<Boolean> replace(String key, int exp, T o, Transcoder<T> tc) {
        return null;
    }

    public Future<Boolean> replace(String key, int exp, Object o) {
        return null;
    }

    public <T> Future<T> asyncGet(String key, Transcoder<T> tc) {
        Pair<Object, Long> pair = mockMemcached.get(key);
        Object o = null;
        if (pair != null && (pair.getValue() == 0 || pair.getValue() >= System.currentTimeMillis() / 1000)) {
            o = pair.getKey();
        }
        return new MockFuture<T>((T) o, sw);
    }

    public Future<Object> asyncGet(String key) {
        return asyncGet(key, null);
    }

    public Future<CASValue<Object>> asyncGetAndTouch(final String key, final int exp) {

        return asyncGetAndTouch(key, exp, null);
    }

    public <T> Future<CASValue<T>> asyncGetAndTouch(final String key, final int exp,
                                                    final Transcoder<T> tc) {
        Pair<Object, Long> pair = mockMemcached.get(key);
        Object o = null;
        if (pair != null && (pair.getValue() == 0 || pair.getValue() >= System.currentTimeMillis() / 1000)) {
            o = pair.getKey();
            synchronized (mockMemcached) {
                mockMemcached.remove(key);
                mockMemcached.put(key, new MutablePair<Object, Long>(o, exp > 0 ? (System.currentTimeMillis() / 1000) + exp : 0));
            }
        }
        return new MockFuture<CASValue<T>>(o == null ? null : new CASValue<T>(0, (T) o), sw);
    }

    public CASValue<Object> getAndTouch(String key, int exp) {

        return getAndTouch(key, exp, null);
    }

    public <T> CASValue<T> getAndTouch(String key, int exp, Transcoder<T> tc) {
        try {
            return asyncGetAndTouch(key, exp, tc).get();
        } catch (InterruptedException e) {
        } catch (ExecutionException e) {
        }
        return null;
    }

    public <T> Future<CASValue<T>> asyncGets(String key, Transcoder<T> tc) {
        return null;
    }

    public Future<CASValue<Object>> asyncGets(String key) {
        return null;
    }

    public <T> CASValue<T> gets(String key, Transcoder<T> tc) {
        return null;
    }

    public CASValue<Object> gets(String key) {
        return null;
    }

    public <T> T get(String key, Transcoder<T> tc) {
        return null;
    }

    public Object get(String key) {
        return null;
    }

    public <T> BulkFuture<Map<String, T>> asyncGetBulk(Iterator<String> keys,
                                                       Iterator<Transcoder<T>> tcs) {
        return null;
    }

    public <T> BulkFuture<Map<String, T>> asyncGetBulk(Collection<String> keys,
                                                       Iterator<Transcoder<T>> tcs) {
        return null;
    }

    public <T> BulkFuture<Map<String, T>> asyncGetBulk(Iterator<String> keys,
                                                       Transcoder<T> tc) {
        return null;
    }

    public <T> BulkFuture<Map<String, T>> asyncGetBulk(Collection<String> keys,
                                                       Transcoder<T> tc) {
        return null;
    }

    public BulkFuture<Map<String, Object>> asyncGetBulk(Iterator<String> keys) {
        return null;
    }

    public BulkFuture<Map<String, Object>> asyncGetBulk(Collection<String> keys) {
        return null;
    }

    public <T> BulkFuture<Map<String, T>> asyncGetBulk(Transcoder<T> tc, String... keys) {
        return null;
    }

    public BulkFuture<Map<String, Object>> asyncGetBulk(String... keys) {
        return null;
    }

    public <T> Map<String, T> getBulk(Iterator<String> keys, Transcoder<T> tc) {
        return null;
    }

    public <T> Map<String, T> getBulk(Collection<String> keys, Transcoder<T> tc) {
        return null;
    }

    public Map<String, Object> getBulk(Iterator<String> keys) {
        return null;
    }

    public Map<String, Object> getBulk(Collection<String> keys) {
        return null;
    }

    public <T> Map<String, T> getBulk(Transcoder<T> tc, String... keys) {
        return null;
    }

    public Map<String, Object> getBulk(String... keys) {
        return null;
    }

    public <T> Future<Boolean> touch(final String key, final int exp,
                              final Transcoder<T> tc) {
        return null;
    }

    public <T> Future<Boolean> touch(final String key, final int exp) {
        return null;
    }

    public Map<SocketAddress, String> getVersions() {
        return null;
    }

    public Map<SocketAddress, Map<String, String>> getStats() {

        return getStats(null);
    }

    public Map<SocketAddress, Map<String, String>> getStats(String prefix) {
        switch (sw) {
            case 3:
                throw new IllegalArgumentException("For Jack Test case ExecutionException");
        }
        Map<SocketAddress, Map<String, String>> map = new HashMap<SocketAddress, Map<String, String>>();
        Map<String, String> innerMap = new HashMap<String, String>();
        innerMap.put("total_items", Integer.toString(mockMemcached.size()));
        innerMap.put("evicted_unfetched", "0");
        map.put(null, innerMap);
        return map;
    }

    public long incr(String key, long by) {
        return 0;
    }

    public long incr(String key, int by) {
        return 0;
    }

    public long decr(String key, long by) {
        return 0;
    }

    public long decr(String key, int by) {
        return 0;
    }

    public Future<Long> asyncIncr(String key, long by) {
        return null;
    }

    public Future<Long> asyncIncr(String key, int by) {
        return null;
    }

    public Future<Long> asyncDecr(String key, long by) {
        return null;
    }

    public Future<Long> asyncDecr(String key, int by) {
        return null;
    }

    public long incr(String key, long by, long def, int exp) {
        return 0;
    }

    public long incr(String key, int by, long def, int exp) {
        return 0;
    }

    public long decr(String key, long by, long def, int exp) {
        return 0;
    }

    public long decr(String key, int by, long def, int exp) {
        return 0;
    }

    public Future<Long> asyncIncr(String key, long by, long def, int exp) {
        return null;
    }

    public Future<Long> asyncIncr(String key, int by, long def, int exp) {
        return null;
    }

    public Future<Long> asyncDecr(String key, long by, long def, int exp) {
        return null;
    }

    public Future<Long> asyncDecr(String key, int by, long def, int exp) {
        return null;
    }

    public long incr(String key, long by, long def) {
        return 0;
    }

    public long incr(String key, int by, long def) {
        return 0;
    }

    public long decr(String key, long by, long def) {
        return 0;
    }

    public long decr(String key, int by, long def) {
        return 0;
    }

    public Future<Long> asyncIncr(String key, long by, long def) {
        return null;
    }

    public Future<Long> asyncIncr(String key, int by, long def) {
        return null;
    }

    public Future<Long> asyncDecr(String key, long by, long def) {
        return null;
    }

    public Future<Long> asyncDecr(String key, int by, long def) {
        return null;
    }

    public Future<Boolean> delete(String key) {
        return delete(key, 0);
    }

    public Future<Boolean> delete(String key, long cas) {
        boolean result = false;
        synchronized (mockMemcached) {
            if (mockMemcached.containsKey(key)) {
                mockMemcached.remove(key);
                result = true;
            }
        }
        return new MockFuture<Boolean>(result, sw);
    }

    public Future<Boolean> flush(int delay) {
        synchronized (mockMemcached) {
            mockMemcached.clear();
        }
        return new MockFuture<Boolean>(true, sw);
    }

    public Future<Boolean> flush() {
        return flush(0);
    }

    public void shutdown() {

    }

    public boolean shutdown(long timeout, TimeUnit unit) {
        return true;
    }

    public boolean waitForQueues(long timeout, TimeUnit unit) {
        return true;
    }

    public boolean addObserver(ConnectionObserver obs) {
        return true;
    }

    public boolean removeObserver(ConnectionObserver obs) {
        return true;
    }

    public CountDownLatch broadcastOp(final BroadcastOpFactory of) {
        return null;
    }

    public CountDownLatch broadcastOp(final BroadcastOpFactory of,
                               Collection<MemcachedNode> nodes) {
        return null;
    }

    /**
     * Get the set of SASL mechanisms supported by the servers.
     *
     * @return the union of all SASL mechanisms supported by the servers.
     */
    public Set<String> listSaslMechanisms() {
        return null;
    }

}