package session;

import jz.carbon.tomcat.sesssion.CTSessionHandlerValve;
import jz.carbon.tomcat.sesssion.CTSessionIdGenerator;
import jz.carbon.tomcat.sesssion.CTSessionPersistentManager;
import mock.jz.DummyRequest;
import mock.jz.DummyValve;
import org.apache.catalina.Context;
import org.apache.catalina.Host;
import org.junit.Test;
import utils.tomcat.unittest.TesterContext;
import utils.tomcat.unittest.TesterHost;

import static org.junit.Assert.*;


/**
 * Created by jack on 2017/1/10.
 */
public class TestCTSessionHandlerValve {

    private CTSessionPersistentManager getManager() throws Exception {
        CTSessionPersistentManager manager = new CTSessionPersistentManager(-1234);
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
    public void testInvoke() throws Exception {
        CTSessionHandlerValve valve = new CTSessionHandlerValve();
        CTSessionPersistentManager manager = getManager();
        valve.setRequestUriIgnorePattern("abc");
        valve.setNext(new DummyValve());

        valve.invoke(new DummyRequest(null), null);
        assertNull(valve.getCTSessionPersistentManager());

        valve.setCTSessionPersistentManager(manager);
        assertEquals(manager, valve.getCTSessionPersistentManager());

        valve.invoke(new DummyRequest(null), null);
        assertFalse(manager.isIgnoreRequest());

        valve.invoke(new DummyRequest(""), null);
        assertFalse(manager.isIgnoreRequest());

        valve.invoke(new DummyRequest("cba"), null);
        assertFalse(manager.isIgnoreRequest());

        valve.invoke(new DummyRequest("abc"), null);
        assertTrue(manager.isIgnoreRequest());

        valve.setRequestUriIgnorePattern("[abc");
        valve.invoke(new DummyRequest("cba"), null);
        assertFalse(manager.isIgnoreRequest());

    }

}
