package jz.carbon.tomcat.sesssion;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.Session;
import org.apache.catalina.SessionIdGenerator;
import org.apache.catalina.Valve;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.session.StandardSession;
import org.apache.catalina.util.SessionIdGeneratorBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.webapp.mgt.CarbonTomcatSessionPersistentManager;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;


/**
 * Created by jack on 2016/12/21.
 */
public class CTSessionPersistentManager extends CarbonTomcatSessionPersistentManager {

    private static final Log log = LogFactory.getLog(CTSessionPersistentManager.class);
    private List<Callable<Void>> backgroundWorks = new ArrayList<Callable<Void>>();

    protected String requestUriIgnorePattern = ".*\\.(ico|png|gif|jpg|css|js)$";
    protected ThreadLocal<Session> currentSession = new ThreadLocal<Session>();
    protected ThreadLocal<String> currentSessionId = new ThreadLocal<String>();
    protected ThreadLocal<Boolean> currentSessionIsPersisted = new ThreadLocal<Boolean>();
    protected ThreadLocal<Boolean> currentIgnore = new ThreadLocal<Boolean>();
    protected CTSessionHandlerValve handlerValve;

    static {
        try {
            Field fd = CarbonTomcatSessionPersistentManager.class.getDeclaredField("allowedClasses");
            fd.setAccessible(true);
            @SuppressWarnings("unchecked")
            List<String> allowedClasses = (List<String>) fd.get(ArrayList.class);
            allowedClasses.add(CTSessionPersistentManager.class.getName());
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void setRequestUriIgnorePattern(String ignoreSavePattern) {
        this.requestUriIgnorePattern = ignoreSavePattern;
    }

    public void setSessionIdGeneratorClassName(String className) throws Exception {
        Class<?> sessionIdGeneratorClasss = null;
        try {
            sessionIdGeneratorClasss = Class.forName(className, false, getContainer().getLoader().getClassLoader()).asSubclass(SessionIdGeneratorBase.class);
        } catch (Exception e) {
            log.warn("Could not load memcached class '" + className + "' " + e.getCause());
            try {
                sessionIdGeneratorClasss = Class.forName(className, false, getClass().getClassLoader()).asSubclass(SessionIdGeneratorBase.class);
            } catch (ClassNotFoundException execp) {
                log.error("Still could not load memcached class '" + className + "' " + execp.getCause());
                throw execp;
            }
        }
        if (!Arrays.asList(sessionIdGeneratorClasss.getSuperclass()).contains(SessionIdGeneratorBase.class)) {
            log.error("Class '" + sessionIdGeneratorClasss.getName() + "' is not SessionIdGeneratorBase sub class");
        } else {
            this.sessionIdGeneratorClass = (Class<? extends SessionIdGenerator>) sessionIdGeneratorClasss;
        }
    }

    public CTSessionPersistentManager(int owenTenantId) {
        super(owenTenantId);
    }

    public CTSessionPersistentManager() {
    }

    public void addBackgroundWork(Callable<Void> work) {
        synchronized (backgroundWorks) {
            backgroundWorks.add(work);
        }
    }

    @Override
    protected StandardSession getNewSession() {
        return new CTSession(this);
    }

    public String getInfo() {
        return "CTSessionPersistentManager/1.0";
    }

    public String getName() {
        return "CTSessionPersistentManager";
    }

    public void backgroundProcess() {
        for (Callable<Void> work : backgroundWorks) {
            try {
                work.call();
            } catch (Exception e) {
                log.error("Invoke work fail " + e.getMessage() + " " + e.getCause());
            }
        }
        super.backgroundProcess();
    }

    public int getMaxActive() {
        return super.getMaxActive();
    }

    public long getExpiredSessions() {
        return super.getExpiredSessions();
    }

    public int getRejectedSessions() {
        return super.getRejectedSessions();
    }

    public int getMaxInactiveInterval() {
        return super.getMaxInactiveInterval();
    }

    public void setSessionMaxAliveTime(int sessionMaxAliveTime) {
        super.setSessionMaxAliveTime(sessionMaxAliveTime);
    }

    public int getSessionAverageAliveTime() {
        return super.getSessionAverageAliveTime();
    }

    public Session[] findSessions() {
        return super.findSessions();
    }

    @Override
    public int getActiveSessions() {
        return super.getActiveSessions();
    }

    @Override
    public int getActiveSessionsFull() {
        return super.getActiveSessionsFull();
    }

    @Override
    public void add(Session session) {
        try {
            writeSession(session);
        } catch (IOException e) {
            log.error("Adding session fail", e);
        }
    }

    @Override
    public Session createSession(String sessionId) {
        Session session = super.createSession(sessionId);
        if (session != null) {
            currentSession.set(session);
            currentSessionId.set(sessionId);
            currentSessionIsPersisted.set(false);
        }
        return session;
    }

    @Override
    public Session findSession(String id) throws IOException {
        Session session = null;
        if (null == id) {
            currentSessionIsPersisted.set(false);
            currentSession.set(null);
            currentSessionId.set(null);
        } else if (id.equals(currentSessionId.get())) {
            session = currentSession.get();
        } else {
            session = swapIn(id);
            if (session == null) {
                currentSessionIsPersisted.set(false);
                currentSession.set(null);
                currentSessionId.set(null);
            } else {
                currentSession.set(session);
                currentSessionIsPersisted.set(true);
                currentSessionId.set(id);
            }
        }
        return session;
    }

    protected Session swapIn(String id) throws IOException {
        return super.swapIn(id);
    }

    protected void writeSession(Session session) throws IOException {
        super.writeSession(session);
    }

    public void setCurrentIgnore(boolean ignore) {
        currentIgnore.set(ignore);
    }

    public boolean isIgnoreRequest() {
        if (currentIgnore.get() == null)
            return false;
        return currentIgnore.get();
    }

    public void afterRequest(Request request, Response response) {
        Session session = currentSession.get();
        if (session != null) {
            try {
                if (!isIgnoreRequest()) {
                    writeSession(session);
                    log.debug("After request Write session done ");
                }
            } catch (IOException e) {
                log.error("After Request write session fail ", e);
            } finally {
                currentSession.remove();
                currentSessionId.remove();
                currentSessionIsPersisted.remove();
                setCurrentIgnore(false);
            }
        }
    }

    protected synchronized void startInternal() throws LifecycleException {
        super.startInternal();
        boolean attachedToValve = false;
        currentIgnore.set(false);
        for (Valve valve : getContainer().getPipeline().getValves()) {
            if (valve instanceof CTSessionHandlerValve) {
                this.handlerValve = (CTSessionHandlerValve) valve;
                this.handlerValve.setSequestUriIgnorePattern(requestUriIgnorePattern);
                if (this.handlerValve.getCTSessionPersistentManager() == null) {
                    this.handlerValve.setCTSessionPersistentManager(this);
                    log.info("Attached to CTSessionHandlerValve after the request auto save session");
                }
                attachedToValve = true;
                break;
            }
        }
        if (!attachedToValve) {
            this.handlerValve = new CTSessionHandlerValve();
            this.handlerValve.setSequestUriIgnorePattern(requestUriIgnorePattern);
            this.handlerValve.setCTSessionPersistentManager(this);
            log.info("Session Manager start internal attached to CTSessionHandlerValve  after the request auto save session");
            getContainer().getPipeline().addValve(this.handlerValve);
        }
    }

}
