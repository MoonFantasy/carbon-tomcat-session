package jz.carbon.tomcat.sesssion;

import org.apache.catalina.*;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.session.StandardSession;
import org.apache.catalina.util.SessionIdGeneratorBase;
import org.apache.catalina.util.StandardSessionIdGenerator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.webapp.mgt.CarbonTomcatSessionPersistentManager;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
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
            log.fatal(e.getMessage());
            throw new RuntimeException(e);
        }
    }



    public CTSessionPersistentManager(int owenTenantId) {
        super(owenTenantId);
    }

    public CTSessionPersistentManager() {
    }

    public void setRequestUriIgnorePattern(String ignoreSavePattern) {
        this.requestUriIgnorePattern = ignoreSavePattern;
    }

    private boolean isSessionIdGeneratorBaseSubClass(Class subclass) {
        if (subclass == null)
            return false;
        if (subclass.getName().compareTo(SessionIdGeneratorBase.class.getName()) == 0)
            return true;
        return isSessionIdGeneratorBaseSubClass(subclass.getSuperclass());
    }

    public void setSessionIdGeneratorClassName(String className) throws Exception {
        Class<?> sessionIdGeneratorClasss = null;
        try {
            sessionIdGeneratorClasss = Class.forName(className, false, getClass().getClassLoader());
            if (!isSessionIdGeneratorBaseSubClass(sessionIdGeneratorClasss)) {
                log.error("Class '" + sessionIdGeneratorClasss.getName() + "' is not SessionIdGeneratorBase sub class");
            } else {
                this.sessionIdGeneratorClass = (Class<? extends SessionIdGenerator>) sessionIdGeneratorClasss;
                this.sessionIdGenerator = null;
            }
        } catch (ClassNotFoundException execp) {
            log.error("Could not load session id generator class '" + className + "' " + execp.getMessage());
        } finally {
            if (this.sessionIdGeneratorClass == null)
                this.sessionIdGeneratorClass = StandardSessionIdGenerator.class;
        }
    }

    /**
     * Assume session id will not duplicate
     * make sure session id length is enough
     * @return
     */
    @Override
    protected String generateSessionId() {
        String result = null;
        result = getSessionIdGenerator().generateSessionId();
        return result;
    }

    @Override
    public SessionIdGenerator getSessionIdGenerator() {
        if (sessionIdGenerator != null) {
            return sessionIdGenerator;
        } else if (sessionIdGeneratorClass != null) {
            try {
                sessionIdGenerator = sessionIdGeneratorClass.newInstance();
                return sessionIdGenerator;
            } catch(Exception ex) {
                log.error("Create new session id generator instance fail ", ex);
                sessionIdGenerator = new StandardSessionIdGenerator();
            } finally {
                if (sessionIdLength != SESSION_ID_LENGTH_UNSET)
                    sessionIdGenerator.setSessionIdLength(sessionIdLength);
                sessionIdGenerator.setJvmRoute(getJvmRoute());
            }
        }
        return sessionIdGenerator;
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
        return getName() + "/1.0";
    }

    public String getName() {
        return getClass().getSimpleName();
    }

    public void backgroundProcess() {
        for (Callable<Void> work : backgroundWorks) {
            try {
                work.call();
            } catch (Exception e) {
                log.error("Invoke Work fail " + e.getMessage() + " " + e.getCause());
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
        if (isIgnoreRequest()) {
            Session session = createEmptySession();
            session.setNew(true);
            session.setValid(true);
            session.setCreationTime(System.currentTimeMillis());
            session.setMaxInactiveInterval(((Context) getContainer()).getSessionTimeout() * 60);
            session.setId(id);
            log.debug("ignore new session" + " thread id: " + Thread.currentThread().getId());
            return session;
        }
        log.debug("not ignore new session" + " thread id: " + Thread.currentThread().getId());
        return super.swapIn(id);
    }

    protected void writeSession(Session session) throws IOException {
        if (!isIgnoreRequest())
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
                    log.debug("After request Write session done " + " thread id: " + Thread.currentThread().getId());
                }
            } catch (IOException e) {
                log.error("After Request write session fail " + " thread id: " + Thread.currentThread().getId(), e);
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
        Pipeline pipeline = getContainer().getParent().getPipeline();
        Valve[] valves = pipeline.getValves();
        for (Valve valve : valves) {
            if (valve instanceof CTSessionHandlerValve) {
                this.handlerValve = (CTSessionHandlerValve) valve;
                this.handlerValve.setRequestUriIgnorePattern(requestUriIgnorePattern);
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
            this.handlerValve.setRequestUriIgnorePattern(requestUriIgnorePattern);
            this.handlerValve.setCTSessionPersistentManager(this);
            pipeline.addValve(this.handlerValve);
            log.info("Session Manager start internal attached to CTSessionHandlerValve  after the request auto save session");
            getContainer().getPipeline().addValve(this.handlerValve);
        }
    }

}
