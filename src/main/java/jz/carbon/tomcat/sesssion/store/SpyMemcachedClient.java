package jz.carbon.tomcat.sesssion.store;

import java.net.URI;

/**
 * Created by jack on 2016/12/21.
 */
public class SpyMemcachedClient extends AbstractCacheClient {

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
