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
import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by jack on 2016/12/28.
 */
public class TestSpyMemcachedStore {

    @Before
    public void setUp() throws Exception {
        SpyMemcachedNode.setMemcachedClientClass(MockMemcachedClient.class);
    }

    private SpyMemcachedStore getSpyMemcachedStore(String nodes, String mcClientClassName) {
        SpyMemcachedStore memcachedStore = new SpyMemcachedStore();
        if (mcClientClassName != null && mcClientClassName.trim().length() != 0) {
            memcachedStore.setMemcachedClassName(mcClientClassName);
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
    public void testSetMemcachedClassName() throws Exception {
        SpyMemcachedStore store = getSpyMemcachedStore(null, null);
        Field fd = store.getClass().getDeclaredField("memcachedClass");
        fd.setAccessible(true);

        assertEquals("jz.carbon.tomcat.sesssion.store.SpyMemcachedClient",
                ((Class)fd.get(store)).getName());

        store.setMemcachedClassName(Object.class.getName());

        assertEquals("jz.carbon.tomcat.sesssion.store.SpyMemcachedClient",
                ((Class)fd.get(store)).getName());

        store.setMemcachedClassName(TestISpyMcClient.class.getName());

        assertEquals(TestISpyMcClient.class.getName(),
                ((Class)fd.get(store)).getName());
    }

    @Test
    public void testGetMemcachedClient() throws Exception {
        SpyMemcachedStore store = getSpyMemcachedStore("127.0.0.1:11211, 127.0.0.1:11212", null);
        assertTrue(store.getMemcachedClient() instanceof SpyMemcachedClient);
    }

    @Test
    public void testGetMaxInactiveInterval() throws Exception {
        SpyMemcachedStore store = getSpyMemcachedStore("127.0.0.1:11211, 127.0.0.1:11212", null);
        assertEquals(store.DEFAULT_MAX_INACTIVE_INTERVAL, store.getMaxInactiveInterval());
        CTSessionPersistentManager manager = new CTSessionPersistentManager(-1234);
        manager.setStore(store);
        assertEquals(manager.getMaxInactiveInterval(), store.getMaxInactiveInterval());
    }

    @Test
    public void testSetNode() throws Exception {
        SpyMemcachedStore store = getSpyMemcachedStore(null, null);
        store.setNodes("127.0.0.1:11211, memcached://127.0.0.1:11212, nono:nono, 127.0.0.1:abc, abc. ,memcached://");
        Field fd = store.getClass().getDeclaredField("nodes");
        fd.setAccessible(true);
        assertEquals(2,((ArrayList<String>)fd.get(store)).size());
    }
    @Test
    public void testClear() throws Exception {
        SpyMemcachedStore store = getSpyMemcachedStore("127.0.0.1:11211, 127.0.0.1:11212", null);
        store.clear();
        store = getSpyMemcachedStore("127.0.0.1:11211, 127.0.0.1:11212", null);
        store.setMemcachedClassName(TestISpyMcClient.class.getName());
        store.clear();
    }


    @Test
    public void testOperation() throws Exception {
        String nodes = "127.0.0.1:11211, 127.0.0.1:11212, memcached://127.0.0.1:1, memcached://127.0.0.1:2, memcached://127.0.0.1:3, memcached://127.0.0.1:4, memcached://127.0.0.1:5";
        SpyMemcachedStore store = getSpyMemcachedStore(nodes, null);
        CTSessionPersistentManager manager = getManager(store);

        CTSession session1 = (CTSession)manager.createSession(null);
        CTSession session2 = (CTSession)manager.createSession(null);
        CTSession session3 = (CTSession)manager.createSession(null);
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
        store.setMemcachedClassName(TestISpyMcClient.class.getName());
        CTSessionPersistentManager manager = getManager(store);

        CTSession session1 = (CTSession)manager.createSession(null);
        CTSession session2 = (CTSession)manager.createSession(null);
        CTSession session3 = (CTSession)manager.createSession(null);
        //assertEquals(0, store.getSize());

        store.save(session1);
        store.save(session2);
        store.save(session3);

        assertEquals(session1.getId(), store.load(session1.getId()).getId());
        store.remove(session1.getId());
        assertNull(store.load(session1.getId()));
        assertEquals(session2.getId(), store.load(session2.getId()).getId());
        store.clear();
        assertNull(store.load(session2.getId()));


    }

    private class TestISpyMcClient implements ICacheClient {
        public TestISpyMcClient() throws Exception {
            throw new Exception("for test");
        }
        public void addNode(URI uri) {}
        public void addNode(ICacheNode node) {}
        public byte[] get(String key) {return null;}
        public byte[] getAndTouch(String key, int expiration) {return null;}
        public boolean set(String key, byte[] value, int expiration) {return false;}
        public boolean delete(String key) {return false;}
        public int getSize() {return 0;}
        public void clean() {}
        public void backgroundWork() {}
    }



}
