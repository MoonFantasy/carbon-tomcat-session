package session.store;

import jz.carbon.tomcat.sesssion.store.RedisCacheClient;
import jz.carbon.tomcat.sesssion.store.RedisStore;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertEquals;


/**
 * Created by jack on 2017/1/8.
 */
public class TestJedisCacheClient {

    @Test
    public void testGetDriverName() throws Exception {
        RedisCacheClient client = new RedisCacheClient();
        assertEquals("Redis", client.getDriverName());
    }

    @Test
    public void testAddNodeUri() throws Exception {
        RedisStore store = new RedisStore();
        RedisCacheClient client = new RedisCacheClient(store);
        client.addNode(new URI("redis://127.0.0.1:32767"));
        client.addNode(new URI("redis://127.0.0.1:32768"));
        client.addNode(new URI("rediss://127.0.0.1:32769"));
        client.addNode(new URI("dummy://127.0.0.1:32769"));
        assertEquals(3, client.getNodeSize());
    }
}
