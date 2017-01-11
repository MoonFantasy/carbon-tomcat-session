package session.store;

import jz.carbon.tomcat.sesssion.store.IFCacheClient;
import jz.carbon.tomcat.sesssion.store.RedisCacheClient;
import jz.carbon.tomcat.sesssion.store.RedisStore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by jack on 2017/1/8.
 */
public class TestRedisStore {

    @Test
    public void testInfo() throws Exception {
        RedisStore store = new RedisStore();
        assertEquals(RedisStore.class.getSimpleName()+"/1.0", store.getInfo());
    }

    @Test
    public void testSetMaxPoolTimeout() throws Exception {
        RedisStore store = new RedisStore();
        assertEquals(0, store.getMaxPool());
        assertEquals(0, store.getTimeout());
        store.setMaxPool(3);
        assertEquals(3, store.getMaxPool());
        store.setTimeout(5);
        assertEquals(5, store.getTimeout());
    }

    @Test
    public void testSetNodes() throws Exception {
        RedisStore store = new RedisStore();
        store.setNodes("127.0.0.1:1234, redis://127.0.0.1:21315, redis://abc, redis://abc:efg@hijk, redis://!@#$, abc. ,");
        assertEquals(4,store.getNodeSize());
        IFCacheClient client = store.getCacheClient();
        assertTrue(client instanceof RedisCacheClient);

    }
}
