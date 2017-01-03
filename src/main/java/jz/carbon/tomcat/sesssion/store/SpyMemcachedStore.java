package jz.carbon.tomcat.sesssion.store;

import jz.carbon.tomcat.sesssion.CTSession;
import jz.carbon.tomcat.sesssion.CTSessionPersistentManager;
import org.apache.catalina.Session;
import org.apache.catalina.session.StoreBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jack on 2016/12/21.
 */
public class SpyMemcachedStore extends StoreBase {

    private static final Log log = LogFactory.getLog(SpyMemcachedStore.class);
    private ArrayList<String> nodes = new ArrayList<String>();
    private ICacheClient memcachedClient = null;
    private Class<?> memcachedClass = SpyMemcachedClient.class;
    private long backgroundInterval = 60;
    static public final int DEFAULT_MAX_INACTIVE_INTERVAL = 900;

    public void setMemcachedClassName(String className) {
        Class<?> mcClass = null;
        try {
            mcClass = Class.forName(className, false, manager.getContainer().getLoader().getClassLoader());
        } catch (Exception e) {
            log.warn("Could not load memcached class '" + className + "' " + e.getCause());
            try {
                mcClass = Class.forName(className, false, getClass().getClassLoader());
            } catch (ClassNotFoundException execp) {
                log.error("Still could not load memcached class '" + className + "' " + execp.getCause());
            }
        }
        if (!Arrays.asList(mcClass.getInterfaces()).contains(ICacheClient.class))
            log.error("Class '" + mcClass.getName() + "' is not ICacheClient implementation");
        else
            memcachedClass =  mcClass;
    }

    public void setMemcachedClient(ICacheClient memcachedClient) {
        this.memcachedClient = memcachedClient;
    }

    private URI createMemcachedUri(String uriStr) throws URISyntaxException {
        String uriString = uriStr;
        Pattern pattern = Pattern.compile("^\\s*(\\w+)://");
        Matcher matcher = pattern.matcher(uriString);
        if (!matcher.find()) {
            uriString = "memcached://" + uriString;
        }
        URI uri = new URI(uriString);
        if (uri.getHost() == null || uri.getHost().length() == 0)
            throw new URISyntaxException("Invalid uri " + uri.toString(), "host can not empty");

        if (uri.getScheme().length() != 0 && uri.getScheme().toLowerCase().compareTo("memcached") != 0)
            throw new URISyntaxException("Invalid scheme " + uri.toString(), "scheme cannot be " + uri.getScheme());

        return uri;
    }

    public ICacheClient getMemcachedClient() {
        if (memcachedClient == null) {
            try {
                memcachedClient = (ICacheClient) memcachedClass.newInstance();
            } catch (Exception e) {
                log.error("Instantiation memcached client fail " + e.getMessage() + " " + e.getCause());
            }
            for (String node : nodes) {
                try {
                    memcachedClient.addNode(createMemcachedUri(node));
                } catch (URISyntaxException e) {
                    log.error("Invalid format '" +node + "' " + e.getMessage() + " " + e.getCause());
                }
            }
        }
        return memcachedClient;
    }

    @SuppressWarnings( "deprecation" )
    public int getMaxInactiveInterval() {
        if (manager == null)
            return DEFAULT_MAX_INACTIVE_INTERVAL;
        return manager.getMaxInactiveInterval();
    }

    public void setNodes(String nodesString) {
        log.debug("Set nodes :" + nodesString);
        String[] nodeList = nodesString.split(",");
        Pattern pattern = Pattern.compile("^\\s*((\\w+://)*[\\w\\d_\\-]+(\\.[\\w\\d_\\-]+)*):(\\d*)\\s*$|^\\s*((\\w+://)*[\\w\\d_\\-]+(\\.[\\w\\d_\\-]+)*)\\s*$");
        String nodeStr;
        for (String node : nodeList) {
            if (node.trim().length() == 0)
                continue;
            Matcher matcher = pattern.matcher(node);
            if (!matcher.find()) {
                log.error(new IllegalArgumentException("Invalid node format " + node));
                continue;
            }
            if (matcher.group(1) != null) {
                nodeStr = matcher.group(1);
                nodeStr += matcher.group(4) == null || matcher.group(4).length() == 0
                        ? ""
                        : ":" + matcher.group(4);
            } else if (matcher.group(5) != null && matcher.group(5).length() > 0){
                nodeStr = matcher.group(5);
            } else {
                log.error(new IllegalArgumentException("Invalid node format " + node));
                continue;
            }

            nodes.add(nodeStr);
        }
    }

    public String getInfo() {
        return getStoreName() + "/1.0";

    }

    public String getStoreName() {
        return SpyMemcachedStore.class.getSimpleName();
    }

    public void clear() {
        try {
            getMemcachedClient().clean();
        } catch (NullPointerException e) {
            log.error("Clear fail memcache client is null ");
        }
    }

    public int getSize() throws IOException {
        try {
            return getMemcachedClient().getSize();
        } catch (NullPointerException e) {
            log.error("getSize fail memcache client is null ");
        }
        return 0;
    }

    public void remove(String id) throws IOException {
        try {
            getMemcachedClient().delete(id);
        } catch (NullPointerException e) {
            log.error("remove " + id + " fail memcache client is null ");
            throw new IOException("memcache client is null");
        }
    }

    public Session load(String id) throws ClassNotFoundException, IOException {
        byte[] sessionData = null;
        try {
            sessionData = getMemcachedClient().get(id);
        } catch (NullPointerException e) {
            log.error("load " + id + " fail memcache client is null ");
            throw new IOException("memcache client is null");
        }
        return sessionData == null ? null : bytesToSession(sessionData);
    }

    public void save(Session session) throws IOException {
        try {
            byte[] sessionData = sessionToBytes(session);
            getMemcachedClient().set(session.getId(),
                    sessionData,
                    session.getMaxInactiveInterval() == 0 ? getMaxInactiveInterval() : session.getMaxInactiveInterval());
        } catch (NullPointerException e) {
            log.error("save " + session.getId() + " fail memcache client is null ");
            throw new IOException("memcache client is null");
        }
    }

    private Session bytesToSession(byte[] data) throws ClassNotFoundException, IOException {

        CTSession restoreSession = null;
        ObjectInputStream ois = null;
        ByteArrayInputStream bis = null;
        bis = new ByteArrayInputStream(data);
        ois = getObjectInputStream(bis);

        restoreSession = (CTSession) manager.createEmptySession();
        restoreSession.readObjectData(ois);
        restoreSession.setManager(manager);

        return restoreSession;
    }
    private byte[] sessionToBytes(Session session)  throws IOException {
        ObjectOutputStream oos = null;
        ByteArrayOutputStream bos = null;
        bos = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(new BufferedOutputStream(bos));
        ((CTSession) session).writeObjectData(oos);
        oos.close();
        return  bos.toByteArray();
    }

    public String[] keys() {
        if (log.isDebugEnabled()) {
            StackTraceElement[] trace = Thread.currentThread().getStackTrace();
            String traceString = "";
            for (int i = 3 ;i < 5 && i < trace.length; i++)
                traceString += "\n" + trace[i].getClassName() + " :: " + trace[i].getMethodName();
            log.debug("Get Keys caller " + traceString);
        }
        return new String[0];
    }

    @Override
    protected void initInternal() {
        if (manager instanceof CTSessionPersistentManager) {
            ((CTSessionPersistentManager) manager).addBackgroundWork(new work());
        }
    }

    private class work implements Callable<Void> {
        long lastRunTimeStemp = 0;

        public Void call() throws Exception {
            long now = System.currentTimeMillis() / 1000;
            if (now - backgroundInterval < lastRunTimeStemp)
                return null;
            ICacheClient memcachedClient = getMemcachedClient();
            if (memcachedClient != null) {
                log.debug("Start store background works " + now);
                memcachedClient.backgroundWork();
            }
            lastRunTimeStemp = System.currentTimeMillis() / 1000;
            return null;
        }
    }
}
