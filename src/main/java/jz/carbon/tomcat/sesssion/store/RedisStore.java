package jz.carbon.tomcat.sesssion.store;

/**
 * Created by jack on 2016/12/31.
 */
public class RedisStore extends AbstractCacheStore {
    private int maxPool = 0;

    private int timeout = 0;

    public RedisStore() {
    }

    public String getInfo() {
        return getStoreName() + "/1.0";
    }

    public String getStoreName() {
        return getClass().getSimpleName();
    }

    public void setMaxPool(int maxPool) {
        this.maxPool = maxPool;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getMaxPool() {
        return this.maxPool;
    }

    public int getTimeout() {
        return this.timeout;
    }

    protected IFCacheClient createNewCacheClient() {
        RedisCacheClient cacheClient = new RedisCacheClient(this);
        return cacheClient;
    }

}
