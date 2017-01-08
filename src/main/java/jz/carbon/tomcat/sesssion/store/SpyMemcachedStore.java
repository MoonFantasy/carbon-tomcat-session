package jz.carbon.tomcat.sesssion.store;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * Created by jack on 2016/12/21.
 */
public class SpyMemcachedStore extends AbstractCacheStore {

    private static final Log log = LogFactory.getLog(SpyMemcachedStore.class);

    public SpyMemcachedStore() {
    }

    protected IFCacheClient createNewCacheClient() {
        SpyMemcachedClient cacheClient = new SpyMemcachedClient(this);
        return cacheClient;
    }

    public String getInfo() {
        return getStoreName() + "/1.0";

    }

    public String getStoreName() {
        return SpyMemcachedStore.class.getSimpleName();
    }

    @Override
    public String[] keys() throws IOException {
        return new String[0];
    }
}
