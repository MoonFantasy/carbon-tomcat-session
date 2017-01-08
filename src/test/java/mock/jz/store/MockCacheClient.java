package mock.jz.store;

import jz.carbon.tomcat.sesssion.store.AbstractCacheClient;
import jz.carbon.tomcat.sesssion.store.AbstractCacheStore;
import jz.carbon.tomcat.sesssion.store.IFCacheNode;

import java.net.URI;

/**
 * Created by jack on 2017/1/8.
 */
public class MockCacheClient extends AbstractCacheClient {
    public static final int DEFAULT_PORT = 202020;

    public MockCacheClient() {
        super(null);
    }

    public MockCacheClient(AbstractCacheStore store) {
        super(store);
    }

    public String getDriverName() {
        return getClass().getSimpleName();
    }

    protected IFCacheNode createNewCacheNode(URI uri) {
        return new MockCacheNode(uri);
    }

    public String getUriScheme() {
        return "mockcache";
    }

    public String getUriSslScheme() {
        return "mockcaches";
    }

    public int getDefaultPort() {
        return DEFAULT_PORT;
    }
}
