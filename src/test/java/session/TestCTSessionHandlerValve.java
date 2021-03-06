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
        return manager;
    }

    @Test
    public void testSetCTSessionPersistentManager() throws Exception {
        CTSessionHandlerValve valve = new CTSessionHandlerValve();
        CTSessionPersistentManager manager = getManager();
        manager.getContainer().getParent().getPipeline().addValve(valve);
        manager.getContainer().getParent().getPipeline().addValve(new DummyValve());

        valve.invoke(new DummyRequest(null), null);
        assertNull(valve.getCTSessionPersistentManager());
        manager.start();

        valve.setCTSessionPersistentManager(manager);
        assertEquals(manager, valve.getCTSessionPersistentManager());

    }

    @Test
    public void testPatternIllegal() throws Exception {
        CTSessionHandlerValve valve = new CTSessionHandlerValve();
        CTSessionPersistentManager manager = getManager();
        manager.getContainer().getParent().getPipeline().addValve(valve);
        manager.getContainer().getParent().getPipeline().addValve(new DummyValve());
        valve.setRequestUriIgnorePattern("[abc");
        manager.start();
        valve.invoke(new DummyRequest("abc"), null);
        assertFalse(manager.isIgnoreRequest());
    }

    @Test
    public void testIgnoreExclude() throws Exception {
        CTSessionHandlerValve valve = new CTSessionHandlerValve();
        CTSessionPersistentManager manager = getManager();
        manager.getContainer().getParent().getPipeline().addValve(valve);
        manager.getContainer().getParent().getPipeline().addValve(new DummyValve());

        manager.start();

        valve.setRequestUriIgnorePattern("abc.*");

        valve.invoke(new DummyRequest("abcdef"), null);
        assertTrue(manager.isIgnoreRequest());

        valve.setRequestIgnoreExcludePattern("def");

        valve.invoke(new DummyRequest("abcdef"), null);
        assertFalse(manager.isIgnoreRequest());

    }

    @Test
    public void testMethodIgnoreExclude() throws Exception {
        CTSessionHandlerValve valve = new CTSessionHandlerValve();
        CTSessionPersistentManager manager = getManager();
        manager.getContainer().getParent().getPipeline().addValve(valve);
        manager.getContainer().getParent().getPipeline().addValve(new DummyValve());

        manager.start();

        valve.setRequestUriIgnorePattern("abc.*");

        valve.invoke(new DummyRequest("abcdef"), null);
        assertTrue(manager.isIgnoreRequest());

        valve.setRequestMethodIgnoreExcludePattern("GET|POST");

        valve.invoke(new DummyRequest("abcdef", "GET"), null);
        assertFalse(manager.isIgnoreRequest());

        valve.invoke(new DummyRequest("abcdef", "POST"), null);
        assertFalse(manager.isIgnoreRequest());

    }

    @Test
    public void testInvoke() throws Exception {
        CTSessionHandlerValve valve = new CTSessionHandlerValve();
        CTSessionPersistentManager manager = getManager();
        manager.getContainer().getParent().getPipeline().addValve(valve);
        manager.getContainer().getParent().getPipeline().addValve(new DummyValve());
        valve.setRequestUriIgnorePattern("abc.*");

        manager.start();

        valve.invoke(new DummyRequest(null), null);
        assertFalse(manager.isIgnoreRequest());

        valve.invoke(new DummyRequest(""), null);
        assertFalse(manager.isIgnoreRequest());

        valve.invoke(new DummyRequest("cba"), null);
        assertFalse(manager.isIgnoreRequest());

        valve.invoke(new DummyRequest("abc"), null);
        assertTrue(manager.isIgnoreRequest());

        valve.invoke(new DummyRequest("cba"), null);
        assertFalse(manager.isIgnoreRequest());

    }

}
