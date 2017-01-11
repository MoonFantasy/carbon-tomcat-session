package session.store;

import jz.carbon.tomcat.sesssion.CTSession;
import jz.carbon.tomcat.sesssion.CTSessionPersistentManager;
import jz.carbon.tomcat.sesssion.store.*;
import mock.net.spy.memcached.MockMemcachedClient;
import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.Store;
import org.junit.Before;
import org.junit.Test;
import utils.tomcat.unittest.TesterContext;
import utils.tomcat.unittest.TesterHost;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by jack on 2016/12/28.
 */
public class TestSpyMemcachedStore {

    @Before
    public void setUp() throws Exception {
        SpyMemcachedNode.setMemcachedClientClass(MockMemcachedClient.class);
    }

    private SpyMemcachedStore getSpyMemcachedStore(String nodes, Class mcClientClass) {
        SpyMemcachedStore memcachedStore = new SpyMemcachedStore();
        IFCacheClient client = null;
        if (mcClientClass != null) {
            try {
                mcClientClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            memcachedStore.setCacheClient(client);
        }

        if (nodes != null)
            memcachedStore.setNodes(nodes);

        return memcachedStore;
    }

    private CTSessionPersistentManager getManager(Store store) throws Exception {
        CTSessionPersistentManager manager = new CTSessionPersistentManager(-1234);
        manager.setStore(store);

        Host host = new TesterHost();
        Context context = new TesterContext();
        context.setParent(host);

        manager.setContainer(context);

        manager.setMaxActiveSessions(10);
        manager.setMinIdleSwap(0);

        manager.start();
        return manager;
    }


    @Test
    public void testGetMemcachedClient() throws Exception {
        SpyMemcachedStore store = getSpyMemcachedStore("127.0.0.1:11211, 127.0.0.1:11212", null);
        assertTrue(store.getCacheClient() instanceof SpyMemcachedClient);
    }

    @Test
    public void testGetMaxInactiveInterval() throws Exception {
        SpyMemcachedStore store = getSpyMemcachedStore("127.0.0.1:11211, 127.0.0.1:11212", null);
        assertEquals(AbstractCacheStore.DEFAULT_MAX_INACTIVE_INTERVAL, store.getMaxInactiveInterval());
        CTSessionPersistentManager manager = new CTSessionPersistentManager(-1234);
        manager.setStore(store);
        assertEquals(manager.getMaxInactiveInterval(), store.getMaxInactiveInterval());
    }

    @Test
    public void testSetNodes() throws Exception {
        SpyMemcachedStore store = getSpyMemcachedStore(null, null);
        store.setNodes("122.0.0.1, localhost, 127.0.0.1:11211, abc://localhost, memcached://localhost:11211");
        assertEquals(4, store.getNodeSize());
    }

    @Test
    public void testClear() throws Exception {
        SpyMemcachedStore store = getSpyMemcachedStore("127.0.0.1:11211, 127.0.0.1:11212", null);
        store.clear();
        store = getSpyMemcachedStore("127.0.0.1:11211, 127.0.0.1:11212", null);
//        store.setCacheClientClassName(TestIFCacheClient.class.getName());
        store.clear();
    }


    @Test
    public void testOperation() throws Exception {
        String nodes = "127.0.0.1:11211, 127.0.0.1:11212, memcached://127.0.0.1:1, memcached://127.0.0.1:2, memcached://127.0.0.1:3, memcached://127.0.0.1:4, memcached://127.0.0.1:5";
        SpyMemcachedStore store = getSpyMemcachedStore(nodes, null);
        CTSessionPersistentManager manager = getManager(store);

        CTSession session1 = (CTSession) manager.createSession(null);
        CTSession session2 = (CTSession) manager.createSession(null);
        CTSession session3 = (CTSession) manager.createSession(null);
        //assertEquals( 0, store.getSize());
        store.save(session1);
        store.save(session2);
        store.save(session3);
        assertEquals(session1.getId(), store.load(session1.getId()).getId());
        store.remove(session1.getId());
        //assertEquals( 2, store.getSize());
        assertNull(store.load(session1.getId()));
        assertEquals(session2.getId(), store.load(session2.getId()).getId());
        store.clear();
        assertNull(store.load(session2.getId()));
        manager.backgroundProcess();

    }

    @Test(expected = IOException.class)
    public void testOperationException() throws Exception {

        String nodes = "127.0.0.1:11211, 127.0.0.1:11212, memcached://127.0.0.1:1, memcached://127.0.0.1:2, memcached://127.0.0.1:3, memcached://127.0.0.1:4, memcached://127.0.0.1:5";
        SpyMemcachedStore store = getSpyMemcachedStore(nodes, null);
        store.setCacheClient(new TestIFCacheClient());
        CTSessionPersistentManager manager = getManager(store);

        CTSession session1 = (CTSession) manager.createSession(null);

        store.save(session1);

    }

    @Test
    public void testGetInfo() throws Exception {
        SpyMemcachedStore store = getSpyMemcachedStore("127.0.0.1", null);
        assertEquals(store.getClass().getSimpleName()+"/1.0", store.getInfo());

    }

    @Test
    public void testKeys() throws Exception {
        SpyMemcachedStore store = getSpyMemcachedStore("127.0.0.1", null);
        assertEquals(0, store.keys().length);
    }
    private class TestIFCacheClient implements IFCacheClient {

        public String getDriverName() {return "TestIFCacheClient";}
        public String getUriScheme() {
            return "memcached";
        }

        public String getUriSslScheme() {
            return "memcached";
        }

        public void removeAllNode() {

        }

        public void addNode(URI uri) {
        }

        public void addNode(IFCacheNode node) {
        }

        public int getNodeSize() {
            return 0;
        }

        public byte[] get(String key) throws CacheClientIOException {
            throw new CacheClientIOException("TestIFCacheClient throws");
        }

        public byte[] getAndTouch(String key, int expiration) throws CacheClientIOException {
            throw new CacheClientIOException("TestIFCacheClient throws");
        }

        public boolean set(String key, byte[] value, int expiration) throws CacheClientIOException {
            throw new CacheClientIOException("TestIFCacheClient throws");
        }

        public boolean delete(String key) throws CacheClientIOException {
            throw new CacheClientIOException("TestIFCacheClient throws");
        }

        public int getSize() throws CacheClientIOException {
            throw new CacheClientIOException("TestIFCacheClient throws");
        }

        public void clean() throws CacheClientIOException {
            throw new CacheClientIOException("TestIFCacheClient throws");
        }

        public void backgroundWork() {

        }

        public List<String> getKeys() throws CacheClientIOException {
            throw new CacheClientIOException("TestIFCacheClient throws");
        }

        public AbstractCacheStore getStore() {
            return null;
        }

        public void setStore(AbstractCacheStore store) {
        }

        public void setKeyPrefix(String keyPrefix) {
        }

        public TestIFCacheClient() throws Exception {

        }

        public int getDefaultPort() {
            return 0;
        }
    }
}
