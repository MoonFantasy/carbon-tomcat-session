package session.store;

import jz.carbon.tomcat.sesssion.CTSession;
import jz.carbon.tomcat.sesssion.CTSessionIdGenerator;
import jz.carbon.tomcat.sesssion.CTSessionPersistentManager;
import jz.carbon.tomcat.sesssion.store.AbstractCacheStore;
import jz.carbon.tomcat.sesssion.store.CacheClientIOException;
import jz.carbon.tomcat.sesssion.store.IFCacheClient;
import mock.jz.store.MockCacheClient;
import mock.jz.store.MockCacheStore;
import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.Session;
import org.junit.Test;
import utils.TestUtils;
import utils.tomcat.unittest.TesterContext;
import utils.tomcat.unittest.TesterHost;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by jack on 2017/1/10.
 */
public class TestAbstractCacheStore {

    private CTSessionPersistentManager getManager(AbstractCacheStore store) throws Exception {
        CTSessionPersistentManager manager = new CTSessionPersistentManager(-1234);
        manager.setStore(store);
        manager.setSessionIdGeneratorClassName(CTSessionIdGenerator.class.getName());
        manager.setMaxActiveSessions(2);
        manager.setMinIdleSwap(0);
        manager.setSessionIdLength(128);
        Host host = new TesterHost();
        Context context = new TesterContext();
        context.setParent(host);
        manager.setContainer(context);
        manager.start();
        return manager;
    }
    @Test
    public void testSetCacheClient() throws Exception {
        AbstractCacheStore store = new MockCacheStore();
        assertEquals("MockCacheClient", store.getCacheClient().getDriverName());
        store.setCacheClient(new MockCacheClient(){
            @Override
            public String getDriverName() {
                return "Dummy";
            }
        });
        assertEquals("Dummy", store.getCacheClient().getDriverName());
    }

    @Test
    public void testGetMaxInactiveInterval() throws Exception {
        AbstractCacheStore store = new MockCacheStore();
        assertEquals(AbstractCacheStore.DEFAULT_MAX_INACTIVE_INTERVAL, store.getMaxInactiveInterval());
        store.setManager(new CTSessionPersistentManager(){
            @Override
            public int getMaxInactiveInterval() {
                return 123456;
            }
        });
        assertEquals(123456, store.getMaxInactiveInterval());
    }

    @Test
    public void testSetNodes() {
        AbstractCacheStore store = new MockCacheStore();
        MockCacheClient client = (MockCacheClient)store.getCacheClient();

        store.setNodes("123.123.123.123");
        assertEquals(1, store.getNodeSize());

        store.setNodes(" 1275.243.5.1:1234");
        assertEquals(0, store.getNodeSize());

        store.setNodes(" 127.243.5.1:1234");
        assertEquals(1, store.getNodeSize());

        store.setNodes("," + client.getUriScheme() + "://");
        assertEquals(0, store.getNodeSize());

        store.setNodes("," + client.getUriScheme() + "localhost");
        assertEquals(1, store.getNodeSize());

        store.setNodes("localhost," + client.getUriScheme() + "://abc@somewhere:1234");
        assertEquals(2, store.getNodeSize());

        store.setNodes(client.getUriScheme() + "://localhost," + client.getUriScheme() + "://@!$#$:1234");
        assertEquals(1, store.getNodeSize());

    }

    @Test
    public void testSetNodesURISyntaxException() throws Exception {
        AbstractCacheStore store = new MockCacheStore(){
            @Override
            protected String transfromToUriString(String str) {
                return str;
            }
        };
        MockCacheClient client = (MockCacheClient)store.getCacheClient();

        store.setNodes(client.getUriScheme() + "://:1234");
        assertEquals(0, client.getNodeSize());

        store.setNodes("://localhost");
        assertEquals(0, client.getNodeSize());

    }

    @Test
    public void testInit() throws Exception {
        AbstractCacheStore store = new MockCacheStore();
        CTSessionPersistentManager manager = new CTSessionPersistentManager();
        store.setManager(manager);
        Field fd = manager.getClass().getDeclaredField("backgroundWorks");
        fd.setAccessible(true);
        assertEquals(0, ((List)fd.get(manager)).size());
        store.init();
        assertEquals(1, ((List)fd.get(manager)).size());
    }

    @Test
    public void testGetSize() throws Exception {
        AbstractCacheStore store = new MockCacheStore();
        store.setNodes(store.getCacheClient().getUriScheme() + "://127.0.0.1:1234");
        assertEquals(0, store.getSize());
    }

    @Test(expected = IOException.class)
    public void testGetSizeIOException() throws Exception {
        AbstractCacheStore store = new MockCacheStore();
        store.setNodes(store.getCacheClient().getUriScheme() + "://:1234");
        assertEquals(0, store.getSize());
    }

    @Test
    public void testKeys() throws Exception {
        AbstractCacheStore store = new MockCacheStore();
        store.setNodes(store.getCacheClient().getUriScheme() + "://127.0.0.1:1234");
        assertEquals(0, store.keys().length);
    }

    @Test(expected = IOException.class)
    public void testKeyIOException() throws Exception {
        AbstractCacheStore store = new MockCacheStore();
        store.setNodes(store.getCacheClient().getUriScheme() + "://:1234");
        store.keys();
    }

    @Test
    public void testRemove() throws Exception {
        AbstractCacheStore store = new MockCacheStore();
        CTSessionPersistentManager manager = getManager(store);
        store.setManager(manager);
        store.setNodes(store.getCacheClient().getUriScheme() + "://127.0.0.1:1234");
        Session session = manager.createSession(null);
        store.remove(session.getId());
    }

    @Test(expected = IOException.class)
    public void testRemoveIOException() throws Exception {
        AbstractCacheStore store = new MockCacheStore();
        CTSessionPersistentManager manager = getManager(store);
        store.setManager(manager);
        store.setNodes(store.getCacheClient().getUriScheme() + "://:1234");
        Session session = manager.createSession(null);
        store.remove(session.getId());
    }

    @Test
    public void testClean() throws Exception {
        AbstractCacheStore store = new MockCacheStore();
        store.setNodes(store.getCacheClient().getUriScheme() + "://127.0.0.1:1234");
        store.clear();
    }

    @Test(expected = IOException.class)
    public void testCleanIOException() throws Exception {
        AbstractCacheStore store = new MockCacheStore();
        store.setNodes(store.getCacheClient().getUriScheme() + "://:1234");
        store.clear();
    }

    @Test
    public void testSaveAndLoad() throws Exception {
        AbstractCacheStore store = new MockCacheStore();
        CTSessionPersistentManager manager = getManager(store);
        manager.setStore(store);
        MockCacheClient client = new MockCacheClient() {
            private Map<String, byte[]> map = new HashMap<String, byte[]>();
            @Override
            public byte[] getAndTouch(String key, int expiration) throws CacheClientIOException {
                return map.get(key);
            }

            @Override
            public boolean set(String key, byte[] value, int expiration) throws CacheClientIOException {
                map.put(key, value);
                return true;
            }
        };
        store.setCacheClient(client);
        store.setNodes(store.getCacheClient().getUriScheme() + "://:1234");
        String sessionId = TestUtils.randomString(200);
        CTSession session = (CTSession) manager.createSession(sessionId);
        session.setAttribute("123", "abc");
        session.setAttribute("234", new ArrayList<String>());
        store.save(session);

        CTSession gotSession = (CTSession)store.load(sessionId);

        assertEquals(sessionId, gotSession.getId());
        assertEquals(session.getAttribute("123"), gotSession.getAttribute("123"));
        assertEquals(session.getAttribute("234"), gotSession.getAttribute("234"));
    }

    @Test(expected = IOException.class)
    public void testSaveIOException() throws Exception {
        AbstractCacheStore store = new MockCacheStore();
        CTSessionPersistentManager manager = getManager(store);

        manager.setStore(store);
        store.setNodes(store.getCacheClient().getUriScheme() + "://:1234");
        CTSession session = (CTSession) manager.createSession(null);
        store.save(session);
    }

    @Test(expected = IOException.class)
    public void testLoadIOException() throws Exception {
        AbstractCacheStore store = new MockCacheStore();
        CTSessionPersistentManager manager = getManager(store);
        manager.setStore(store);
        store.setNodes(store.getCacheClient().getUriScheme() + "://:1234");
        store.load("abcd");
    }

    @Test
    public void testSetPrefix() throws Exception {
        AbstractCacheStore store = new MockCacheStore();
        Field fd = AbstractCacheStore.class.getDeclaredField("keyPrefix");
        fd.setAccessible(true);
        String old = (String)fd.get(store);
        String prefix = TestUtils.randomString(10);
        store.setKeyPrefix(prefix);
        assertNotEquals(old, fd.get(store));
        assertEquals(prefix, fd.get(store));
    }

    @Test
    public void testWork() throws Exception {
        MockCacheStoreWork store = new MockCacheStoreWork();
        MockCacheStoreWork.NWork work = store.getWork();
        work.call();
        //2rd call
        work.call();
        store = new MockCacheStoreWork() {
            @Override
            public IFCacheClient getCacheClient() {
                return null;
            }
        };

        MockCacheStoreWork.NWork nwork = store.getWork();
        nwork.call();

    }

    private class MockCacheStoreWork extends MockCacheStore {
        public NWork getWork() {
            return new NWork();
        }
        public class NWork extends Work {

        }
    }

}
