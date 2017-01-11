package mock.jz.store;

import jz.carbon.tomcat.sesssion.store.AbstractCacheStore;
import jz.carbon.tomcat.sesssion.store.IFCacheClient;

/**
 * Created by jack on 2017/1/10.
 */
public class MockCacheStore extends AbstractCacheStore {
    public MockCacheStore() {
    }

    protected IFCacheClient createNewCacheClient() {
        MockCacheClient cacheClient = new MockCacheClient(this);
        return cacheClient;
    }

    public String getInfo() {
        return getStoreName() + "/1.0";

    }

    public String getStoreName() {
        return getClass().getSimpleName();
    }

}
