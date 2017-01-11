package mock.jz.store;

import jz.carbon.tomcat.sesssion.store.AbstractCacheClient;
import jz.carbon.tomcat.sesssion.store.AbstractCacheStore;

/**
 * Created by jack on 2017/1/8.
 */
public class MockStore extends AbstractCacheStore {
    public AbstractCacheClient createNewCacheClient() {
        return new MockCacheClient();
    }
}
