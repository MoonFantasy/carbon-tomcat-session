package session;

import jz.carbon.tomcat.sesssion.CTSession;
import jz.carbon.tomcat.sesssion.CTSessionHandlerValve;
import jz.carbon.tomcat.sesssion.CTSessionIdGenerator;
import jz.carbon.tomcat.sesssion.CTSessionPersistentManager;
import mock.jz.DummyRequest;
import mock.jz.store.MockCacheClient;
import mock.jz.store.MockCacheStore;
import mock.session.TesterStore;
import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.apache.catalina.Session;
import org.apache.catalina.Valve;
import org.apache.catalina.util.StandardSessionIdGenerator;
import org.apache.commons.codec.binary.Base64;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import utils.TestUtils;
import utils.tomcat.unittest.TesterContext;
import utils.tomcat.unittest.TesterHost;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.concurrent.Callable;

import static org.junit.Assert.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)

public class TestCTSessionPersistentManager {

    private CTSessionPersistentManager getNewManager() throws Exception {
        CTSessionPersistentManager manager = new CTSessionPersistentManager(-1234);
        manager.setStore(new TesterStore());
        Host host = new TesterHost();
        Context context = new TesterContext();
        context.setParent(host);
        manager.setContainer(context);

        return manager;
    }



    @Test
    public void testMinIdleSwap() throws Exception {
        CTSessionPersistentManager manager = getNewManager();

        manager.setMaxActiveSessions(2);
        manager.setMinIdleSwap(0);

        manager.start();

        // Create the maximum number of sessions
        manager.createSession(null);
        manager.createSession(null);

        // Given the minIdleSwap settings, this should swap one out to get below
        // the limit
        manager.processPersistenceChecks();
        assertEquals(0, manager.getActiveSessions());
        assertTrue(manager.getActiveSessionsFull() >= 2);

        manager.createSession(null);
        assertEquals(0, manager.getActiveSessions());
        assertTrue(manager.getActiveSessionsFull() >= 3);
        manager.backgroundProcess();
    }

    @Test
    public void testMinIdleSwap2() throws Exception {
        CTSessionPersistentManager manager = getNewManager();

        manager.setMaxActiveSessions(2);
        manager.setMinIdleSwap(0);


        manager.start();

        // Create the maximum number of sessions
        manager.createSession(null);
        manager.createSession(null);

        // Given the minIdleSwap settings, this should swap one out to get below
        // the limit
        manager.processPersistenceChecks();
        assertEquals(0, manager.getActiveSessions());
        assertTrue(manager.getActiveSessionsFull() >= 2);

        manager.createSession(null);
        assertEquals(0, manager.getActiveSessions());
        assertTrue(manager.getActiveSessionsFull() >= 3);
        manager.backgroundProcess();
    }


    @Test
    public void testSetSessionIdGeneratorClassName() throws Exception {
        CTSessionPersistentManager manager = getNewManager();
        manager.start();

        manager.setSessionIdGeneratorClassName("not.exist.class.name");
        assertEquals(StandardSessionIdGenerator.class.getName(), manager.getSessionIdGenerator().getClass().getName());

        manager.setSessionIdGeneratorClassName(CTSessionIdGenerator.class.getName());
        assertEquals(CTSessionIdGenerator.class.getName(), manager.getSessionIdGenerator().getClass().getName());

        manager.setSessionIdGeneratorClassName("not.exist.class.name");
        assertEquals(CTSessionIdGenerator.class.getName(), manager.getSessionIdGenerator().getClass().getName());

        manager.setSessionIdGeneratorClassName(StandardSessionIdGenerator.class.getName());
        assertEquals(StandardSessionIdGenerator.class.getName(), manager.getSessionIdGenerator().getClass().getName());

        manager.setSessionIdGeneratorClassName("not.exist.class.name");
        assertEquals(StandardSessionIdGenerator.class.getName(), manager.getSessionIdGenerator().getClass().getName());

        manager.setSessionIdGeneratorClassName(ExcptionSessionIdGenerator.class.getName());
        assertEquals(StandardSessionIdGenerator.class.getName(), manager.getSessionIdGenerator().getClass().getName());
    }


    @Test
    @SuppressWarnings("deprecation")
    public void testSetSessionIdLength() throws Exception {
        CTSessionPersistentManager manager = getNewManager();
        manager.setSessionIdGeneratorClassName(Object.class.getName());
        manager.setMaxActiveSessions(2);
        manager.setMinIdleSwap(0);
        manager.setSessionIdLength(128);
        manager.start();

        manager.setSessionIdGeneratorClassName(CTSessionIdGenerator.class.getName());
        Session session = manager.createSession(null);
        assertTrue(Base64.isArrayByteBase64(session.getId().getBytes()));
        assertTrue(session.getId().length() <= manager.getSessionIdLength());

    }

    @Test
    public void testWillNotThrowSecurityException() throws Exception {
        CTSessionPersistentManager manager = getNewManager();
        manager.setSessionIdGeneratorClassName(Object.class.getName());
        manager.setMaxActiveSessions(2);
        manager.setMinIdleSwap(0);
        manager.setSessionIdLength(128);
        manager.start();

        manager.getMaxActive();
        manager.getActiveSessions();
        manager.getExpiredSessions();
        manager.getRejectedSessions();
        manager.getMaxInactiveInterval();
        manager.setSessionMaxAliveTime(300);
        manager.getSessionAverageAliveTime();
        manager.findSessions();

        assertEquals(CTSessionPersistentManager.class.getSimpleName(), manager.getName());
        assertEquals(CTSessionPersistentManager.class.getSimpleName() + "/1.0", manager.getInfo());
    }

    @Test
    public void testSetRequestUriIgnorePattern() throws Exception {
        String pattern = "abc";
        CTSessionPersistentManager manager = getNewManager();
        manager.setSessionIdGeneratorClassName(CTSessionIdGenerator.class.getName());
        manager.setMaxActiveSessions(2);
        manager.setMinIdleSwap(0);
        manager.setSessionIdLength(128);
        manager.setRequestUriIgnorePattern(pattern);
        manager.start();

        Field fd = CTSessionPersistentManager.class.getDeclaredField("requestUriIgnorePattern");
        fd.setAccessible(true);
        assertEquals(pattern, fd.get(manager));
    }

    @Test
    public void testFindSession() throws Exception {
        CTSessionPersistentManager manager = getNewManager();
        manager.setStore(new MockCacheStore());
        manager.setMaxActiveSessions(2);
        manager.setMinIdleSwap(0);
        manager.setSessionIdLength(128);
        manager.setRequestUriIgnorePattern("abc");
        manager.start();

        assertNull(manager.findSession(null));
    }

    @Test
    public void testFindSessionAndAdd() throws Exception {

        String nodes = "dummy:1234, dummy2:1234";

        CTSessionPersistentManager manager1 = getNewManager();
        MockCacheStore store1 = new MockCacheStore();
        store1.setNodes(nodes);
        manager1.setStore(store1);
        manager1.start();

        CTSessionPersistentManager manager2 = getNewManager();
        MockCacheStore store2 = new MockCacheStore();
        store2.setNodes(nodes);
        manager2.setStore(store2);
        manager2.start();

        String sessionId = manager1.getSessionIdGenerator().generateSessionId();

        assertNull(manager1.findSession(sessionId));
        assertNull(manager2.findSession(sessionId));

        Session session1 = manager1.createSession(sessionId);
        manager1.add(session1);

        Session session2 = manager2.findSession(sessionId);

        assertNotNull(session2);
        assertEquals(session1.getId(), session2.getId());

        Session session3 = manager2.findSession(sessionId);

        assertNotNull(session3);
        assertEquals(session1.getId(), session3.getId());

    }

    @Test
    public void testIsIgnoreRequest() throws Exception {
        String nodes = "dummy:1234, dummy2:1234";

        CTSessionPersistentManager manager = getNewManager();
        MockCacheStore store = new MockCacheStore();
        store.setNodes(nodes);
        manager.setStore(store);
        assertFalse(manager.isIgnoreRequest());
        manager.start();

        Valve[] valves = manager.getContainer().getPipeline().getValves();
        try {
            valves[0].invoke(new DummyRequest("abc.jpg"), null);
        } catch (NullPointerException e) {
        }
        assertTrue(manager.isIgnoreRequest());
        try {
            valves[0].invoke(new DummyRequest("abc"), null);
        } catch (NullPointerException e) {
        }
        assertFalse(manager.isIgnoreRequest());
    }

    @Test
    public void testAfterRequest() throws Exception {
        String nodes = "dummy:1234, dummy2:1234";

        CTSessionPersistentManager manager = getNewManager();
        MockCacheStore store = new MockCacheStore();
        store.setNodes(nodes);
        manager.setStore(store);
        manager.start();

        CTSessionPersistentManager manager2 = getNewManager();
        MockCacheStore store2 = new MockCacheStore();
        store2.setNodes(nodes);
        manager2.setStore(store2);
        manager2.start();

        String attrName = TestUtils.randomString(10);
        String attrValue = TestUtils.randomString(50);
        CTSession session = (CTSession)manager.createSession(null);
        session.setAttribute(attrName, attrValue);

        assertEquals(attrValue, session.getAttribute(attrName));
        CTSession session2 =  (CTSession)manager2.getStore().load(session.getId());
        assertNull(session2.getAttribute(attrName));

        //Run after request via valve
        Valve[] valves = manager.getContainer().getPipeline().getValves();
        try {
            valves[0].invoke(new DummyRequest("abc"), null);
        } catch (NullPointerException e) {
        }

        session2 =  (CTSession)manager2.getStore().load(session.getId());
        assertEquals(attrValue, session2.getAttribute(attrName));

    }

    @Test
    public void testAfterRequestIOException() throws Exception {
        String nodes = "dummy:1234";

        CTSessionPersistentManager manager = getNewManager();
        MockCacheStore store = new MockCacheStore();
        store.setNodes(nodes);
        manager.setStore(store);
        manager.start();
        CTSession session = (CTSession)manager.createSession(null);
        //Suspending Node
        ((MockCacheClient)store.getCacheClient()).suspendNode(0);

        String attrName = TestUtils.randomString(10);
        String attrValue = TestUtils.randomString(50);
        session.setAttribute(attrName, attrValue);

        //Run after request via valve afterRequest will throws IOExcepton
        Valve[] valves = manager.getContainer().getPipeline().getValves();
        try {
            valves[0].invoke(new DummyRequest("abc"), null);
        } catch (NullPointerException e) {
        }
        //Resuming Node
        ((MockCacheClient)store.getCacheClient()).resumeNode(0);
        CTSession session2 = (CTSession)manager.getStore().load(session.getId());
        assertNull(attrValue, session2.getAttribute(attrName));
    }

    @Test
    public void testStartInternal() throws Exception {
        String nodes = "dummy:1234";
        CTSessionPersistentManager manager = getNewManager();
        MockCacheStore store = new MockCacheStore();
        store.setNodes(nodes);
        manager.setStore(store);
        int valveSize = manager.getContainer().getPipeline().getValves().length;
        manager.getContainer().getPipeline().addValve(new CTSessionHandlerValve());
        assertEquals(valveSize + 1, manager.getContainer().getPipeline().getValves().length);

        CTSessionHandlerValve myValve = (CTSessionHandlerValve)manager.getContainer().getPipeline().getValves()[valveSize];
        assertNull(myValve.getCTSessionPersistentManager());

        manager.start();

//        assertTrue(myValve.getCTSessionPersistentManager() instanceof CTSessionPersistentManager);

    }

    @Test
    public void testBackgroundProcess() throws Exception {
        String nodes = "dummy:1234";
        CTSessionPersistentManager manager = getNewManager();
        MockCacheStore store = new MockCacheStore();
        store.setNodes(nodes);
        manager.setStore(store);
        manager.start();
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        manager.addBackgroundWork(new CallableWork(map));
        assertTrue(map.isEmpty());
        manager.backgroundProcess();
        assertFalse(map.isEmpty());

        //CallableWork will throw Exception
        manager.backgroundProcess();
        assertTrue(map.isEmpty());
    }


    private class CallableWork implements Callable<Void> {
        private HashMap<String, Integer> map;
        public CallableWork(HashMap<String, Integer> map) {
            this.map = map;
        }
        public Void call() throws Exception {
            if (map.isEmpty())
                map.put("abc", 123);
            else {
                map.clear();
                throw new Exception("CallableWork");
            }
            return null;
        }
    }

    private class ExcptionSessionIdGenerator extends StandardSessionIdGenerator {
        public ExcptionSessionIdGenerator() {
            throw new RuntimeException("ExcptionSessionIdGenerator");
        }
    }
}
