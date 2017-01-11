package jz.carbon.tomcat.sesssion.store;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.URI;

/**
 * Created by jack on 2016/12/21.
 */
public class SpyMemcachedClient extends AbstractCacheClient {

    private static final Log log = LogFactory.getLog(SpyMemcachedClient.class);
    public static final int DEFAULT_PORT = 11211;

    public SpyMemcachedClient() {
        super(null);
    }

    public SpyMemcachedClient(AbstractCacheStore store) {
        super(store);
    }

    protected IFCacheNode createNewCacheNode(URI uri) {
        SpyMemcachedNode node = new SpyMemcachedNode(uri);
        return node;
    }

    public String getDriverName() {
        return "Memcached";
    }

    public String getUriScheme() {
        return "memcached";
    }

    public String getUriSslScheme() {
        return "memcached";
    }

    public int getDefaultPort() {
        return DEFAULT_PORT;
    }

}
