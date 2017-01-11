package jz.carbon.tomcat.sesssion.store;

import org.apache.catalina.session.StoreBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.URI;

/**
 * Created by jack on 2016/12/31.
 */
public class RedisCacheClient extends AbstractCacheClient {
    private static final Log log = LogFactory.getLog(RedisCacheClient.class);
    public static final int DEFAULT_PORT = 6379;
    public RedisCacheClient() {
        super(null);
    }

    public RedisCacheClient(AbstractCacheStore store) {
        super(store);
    }

    protected IFCacheNode createNewCacheNode(URI uri) {
        RedisCacheNode node = new RedisCacheNode(uri);
        StoreBase store = getStore();
        if (store != null && store instanceof RedisStore) {
            node.setMaxPool(((RedisStore) store).getMaxPool());
            node.setTimeout(((RedisStore) store).getTimeout());
        }
        return node;
    }

    public String getUriScheme() {
        return "redis";
    }

    public String getUriSslScheme() {
        return "rediss";
    }

    public String getDriverName() {
        return "Redis";
    }

    public int getDefaultPort() {
        return DEFAULT_PORT;
    }
}
