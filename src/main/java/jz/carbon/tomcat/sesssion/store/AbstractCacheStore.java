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
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jack on 2016/12/31.
 */
abstract public class AbstractCacheStore extends StoreBase {
    private static final Log log = LogFactory.getLog(AbstractCacheStore.class);
    private long backgroundInterval = 60;
    private ArrayList<URI> nodes = new ArrayList<URI>();
    private IFCacheClient cacheClient;
    private String keyPrefix = "";
    static public final int DEFAULT_MAX_INACTIVE_INTERVAL = 900;


    abstract protected IFCacheClient createNewCacheClient();

    public void setCacheClient(IFCacheClient cacheClient) {
        if (cacheClient != null)
            cacheClient.removeAllNode();

        this.cacheClient = cacheClient;
    }

    @SuppressWarnings("deprecation")
    public int getMaxInactiveInterval() {
        if (manager == null)
            return DEFAULT_MAX_INACTIVE_INTERVAL;
        return manager.getMaxInactiveInterval();
    }

    public IFCacheClient getCacheClient() {
        if (cacheClient == null) {
            cacheClient = createNewCacheClient();
            cacheClient.setKeyPrefix(keyPrefix);
        }
        return cacheClient;
    }

    public void setKeyPrefix(String prefix) {
        this.keyPrefix = prefix;
    }

    protected String transfromToUriString(String str) {
        IFCacheClient client = getCacheClient();
        String schemePatternStr = "[A-Za-z][A-Za-z0-9+\\-.]*";
        String ipParttenStr = "(?:(?:[0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}(?:[0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])";
        String hostNameParttenStr ="(?:[A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9]\\.)*(?:[A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\\-]*[A-Za-z0-9])";
        String ipv6ParttenStr = "\\[(?:(?:[0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|(?:[0-9a-fA-F]{1,4}:){1,7}:|(?:[0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|(?:[0-9a-fA-F]{1,4}:){1,5}(?::[0-9a-fA-F]{1,4}){1,2}|(?:[0-9a-fA-F]{1,4}:){1,4}(?::[0-9a-fA-F]{1,4}){1,3}|(?:[0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|(?:[0-9a-fA-F]{1,4}:){1,2}(?::[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:(?:(?::[0-9a-fA-F]{1,4}){1,6})|:(?:(?::[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(?::[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(?:ffff(?::0{1,4}){0,1}:){0,1}(?:(?:25[0-5]|(?:2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(?:25[0-5]|(?:2[0-4]|1{0,1}[0-9]){0,1}[0-9])|(?:[0-9a-fA-F]{1,4}:){1,4}:(?:(?:25[0-5]|(?:2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(?:25[0-5]|(?:2[0-4]|1{0,1}[0-9]){0,1}[0-9]))\\]";
        String hostPatternStr = "" + ipParttenStr + "|" +hostNameParttenStr + "|" +ipv6ParttenStr + "";
        String portPatternStr = "([0-9]+)";
        String userinfoPatternStr ="((?:[A-Za-z0-9\\-._~!$&'()*+,;=:]|%[0-9A-Fa-f]{2})*)";
        Pattern hostPortPattern = Pattern.compile("^\\s*(" + hostPatternStr + ")(?::("+ portPatternStr +"))?\\s*$");
        Pattern urlPattern = Pattern.compile("^\\s*("+ schemePatternStr +")://(((?:" + userinfoPatternStr + "@){0,1})(" + hostPatternStr + ")(?::("+ portPatternStr +")){0,1})");

        Matcher matcherPathPort = hostPortPattern.matcher(str);
        if (matcherPathPort.find()
                && matcherPathPort.group(1) != null
                && matcherPathPort.group(1).length() > 0) {

            if (matcherPathPort.group(3) == null)
                return client.getUriScheme()+ "://"+matcherPathPort.group(1)+":" + client.getDefaultPort();
            return client.getUriScheme()+ "://"+str.trim();
        }

        Matcher matcherUrl = urlPattern.matcher(str);
        if (matcherUrl.find()
                && matcherUrl.group(1) != null && matcherUrl.group(1).length() > 0
                && matcherUrl.group(2) != null && matcherUrl.group(2).length() > 0) {
            return str.trim();
        }
        return null;
    }

    public void setNodes(String nodesString) {
        log.debug("Set nodes :" + nodesString);
        IFCacheClient client = getCacheClient();
        client.removeAllNode();
        String[] nodeList = nodesString.split("\\s*,\\s*");
        for (String node : nodeList) {
            String urlStr = transfromToUriString(node);
            if (urlStr == null) {
                log.error("Invalid format '"+ node +"'");
                continue;
            }
            URI uri = null;
            try {
                uri = new URI(urlStr);
                if (uri.getHost() == null || uri.getHost().length() == 0)
                    throw new URISyntaxException(node.trim(), "HostName is empty");
            } catch (URISyntaxException e) {
                log.warn("Invalid format ", e);
                continue;
            }
            client.addNode(uri);
        }
        log.debug(client.getNodeSize() + " nodes added");
    }

    public int getNodeSize() {
        return getCacheClient().getNodeSize();
    }

    @Override
    protected void initInternal() {
        if (manager instanceof CTSessionPersistentManager) {
            ((CTSessionPersistentManager) manager).addBackgroundWork(new Work());
        }
    }

    protected Session bytesToSession(byte[] data) throws ClassNotFoundException, IOException {

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

    protected byte[] sessionToBytes(Session session) throws IOException {
        ObjectOutputStream oos = null;
        ByteArrayOutputStream bos = null;
        bos = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(new BufferedOutputStream(bos));
        ((CTSession) session).writeObjectData(oos);
        oos.close();
        return bos.toByteArray();
    }

    public int getSize() throws IOException {
        IFCacheClient client = getCacheClient();
        return client.getSize();
    }

    public String[] keys() throws IOException {
        IFCacheClient client = getCacheClient();
        ArrayList<String> keys = new ArrayList<String>();
        keys.addAll(client.getKeys());
        return keys.toArray(new String[keys.size()]);
    }

    public Session load(String id) throws ClassNotFoundException, IOException {
        IFCacheClient client = getCacheClient();
        byte[] sessionData = null;
        sessionData = client.get(id);
        return sessionData == null ? null : bytesToSession(sessionData);
    }

    public void remove(String id) throws IOException {
        IFCacheClient client = getCacheClient();
        client.delete(id);
    }

    public void clear() throws IOException {
        IFCacheClient client = getCacheClient();
        client.clean();
    }

    public void save(Session session) throws IOException {
        IFCacheClient client = getCacheClient();
        byte[] sessionData = sessionToBytes(session);
        client.set(session.getId(),
                sessionData,
                session.getMaxInactiveInterval() == 0
                        ? getMaxInactiveInterval()
                        : session.getMaxInactiveInterval());
    }

    protected class Work implements Callable<Void> {
        long lastRunTimeStemp = 0;

        public Void call() throws Exception {
            long now = System.currentTimeMillis() / 1000;
            if (now - backgroundInterval < lastRunTimeStemp)
                return null;
            IFCacheClient cachedClient = getCacheClient();
            if (cachedClient != null) {
                log.trace("Start store background works Timestamp:" + now + " ThreadId:" + Thread.currentThread().getId());
                cachedClient.backgroundWork();
            }
            lastRunTimeStemp = System.currentTimeMillis() / 1000;
            return null;
        }
    }
}
