package jz.carbon.tomcat.sesssion.store;

import java.io.IOException;

/**
 * Created by jack on 2016/12/21.
 */
public class SpyMemcachedStore extends AbstractCacheStore {

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
        return getClass().getSimpleName();
    }

    @Override
    public String[] keys() throws IOException {
        return new String[0];
    }
}
