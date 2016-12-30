package session.store;

import jz.carbon.tomcat.sesssion.store.SpyMemcachedClient;
import jz.carbon.tomcat.sesssion.store.SpyMemcachedNode;
import mock.net.spy.memcached.MockMemcachedClient;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import utils.TestUtils;

import java.net.URI;

import static org.junit.Assert.*;

/**
 * Created by jack on 2016/12/26.
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestSpyMemcachedClient {

    @Before
    public void setUp() throws Exception {
        SpyMemcachedNode.setMemcachedClientClass(MockMemcachedClient.class);
    }
    private SpyMemcachedNode getNodeWithClient(String host, int port){
        SpyMemcachedNode node = new SpyMemcachedNode(host, port);
        return node;
    }
    @Test
    public void testSetAndGet() throws Exception {
        SpyMemcachedClient client = new SpyMemcachedClient();
        client.addNode(getNodeWithClient("127.0.0.1", 32768));
        client.addNode(getNodeWithClient("127.0.0.1", 32769));

        String key = TestUtils.randomString(100);
        String value = TestUtils.randomString(300);
        assertTrue(client.set(key, value.getBytes(), 100));
        assertEquals(value, new String(client.get(key)));
    }


    @Test
    public void testFailover1() throws Exception {
        SpyMemcachedClient client = new SpyMemcachedClient();
        client.addNode(getNodeWithClient("127.0.0.1", 32769));
        client.addNode(getNodeWithClient("127.0.0.1", 1));
        client.addNode(getNodeWithClient("127.0.0.1", 2));
        client.addNode(getNodeWithClient("127.0.0.1", 3));
        client.addNode(getNodeWithClient("127.0.0.1", 4));
        client.addNode(getNodeWithClient("127.0.0.1", 5));
        for (int i = 0; i < 40; i++) {
            String value = TestUtils.randomString(300);
            assertTrue(client.set(Integer.toString(i), value.getBytes(), 100));
            assertEquals(value, new String(client.get(Integer.toString(i))));
        }
    }

    @Test
    public void testFailover2() throws Exception {
        SpyMemcachedClient client = new SpyMemcachedClient();
        client.addNode(getNodeWithClient("127.0.0.1", 32769));
        client.addNode(getNodeWithClient("127.0.0.1", 1));
        client.addNode(getNodeWithClient("127.0.0.1", 2));
        client.addNode(getNodeWithClient("127.0.0.1", 3));
        client.addNode(getNodeWithClient("127.0.0.1", 4));
        client.addNode(getNodeWithClient("127.0.0.1", 5));
        for (int i = 0; i < 40; i++) {
            String value = TestUtils.randomString(300);
            assertTrue(client.set(Integer.toString(i), value.getBytes(), 100));
            assertNotNull(client.getAndTouch(Integer.toString(i), 100));
        }
    }



    @Test
    public void testFailover3() throws Exception {
        SpyMemcachedClient client = new SpyMemcachedClient();
        client.addNode(getNodeWithClient("127.0.0.1", 32769));
        client.addNode(getNodeWithClient("127.0.0.1", 1));
        client.addNode(getNodeWithClient("127.0.0.1", 2));
        client.addNode(getNodeWithClient("127.0.0.1", 3));
        client.addNode(getNodeWithClient("127.0.0.1", 4));
        client.addNode(getNodeWithClient("127.0.0.1", 5));
        for (int i = 0; i < 40; i++) {
            String value = TestUtils.randomString(300);
            assertTrue(client.set(Integer.toString(i), value.getBytes(), 100));
            assertTrue(client.delete(Integer.toString(i)));
        }
    }

    @Test
    public void testFailover4() throws Exception {
        SpyMemcachedClient client = new SpyMemcachedClient();
        client.addNode(getNodeWithClient("127.0.0.1", 32769));
        client.addNode(getNodeWithClient("127.0.0.1", 1));
        client.addNode(getNodeWithClient("127.0.0.1", 2));
        client.addNode(getNodeWithClient("127.0.0.1", 3));
        client.addNode(getNodeWithClient("127.0.0.1", 4));
        client.addNode(getNodeWithClient("127.0.0.1", 5));
        for (int i = 0; i < 40; i++) {
            String value = TestUtils.randomString(300);
            assertTrue(client.set(Integer.toString(i), value.getBytes(), 100));
        }
        client.clean();
        for (int i = 0; i < 40; i++) {
            assertNull(client.get(Integer.toString(i)));
        }

    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetNullKey() throws Exception {
        SpyMemcachedClient client = new SpyMemcachedClient();
        client.addNode(getNodeWithClient("127.0.0.1", 32769));
        client.get(null);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetAndTouchNullKey() throws Exception {
        SpyMemcachedClient client = new SpyMemcachedClient();
        client.addNode(getNodeWithClient("127.0.0.1", 32769));
        client.getAndTouch(null, 123);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetNullKey() throws Exception {
        SpyMemcachedClient client = new SpyMemcachedClient();
        client.addNode(getNodeWithClient("127.0.0.1", 32769));
        client.set(null, null, 123);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteNullKey() throws Exception {
        SpyMemcachedClient client = new SpyMemcachedClient();
        client.addNode(getNodeWithClient("127.0.0.1", 32769));
        client.delete(null);

    }


    @Test(expected = IllegalArgumentException.class)
    public void testGetEmptyKey() throws Exception {
        SpyMemcachedClient client = new SpyMemcachedClient();
        client.addNode(getNodeWithClient("127.0.0.1", 32769));
        client.get("");

    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetAndTouchEmptyKey() throws Exception {
        SpyMemcachedClient client = new SpyMemcachedClient();
        client.addNode(getNodeWithClient("127.0.0.1", 32769));
        client.getAndTouch("", 123);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetEmptyKey() throws Exception {
        SpyMemcachedClient client = new SpyMemcachedClient();
        client.addNode(getNodeWithClient("127.0.0.1", 32769));
        client.set("", null, 123);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteEmptyKey() throws Exception {
        SpyMemcachedClient client = new SpyMemcachedClient();
        client.addNode(getNodeWithClient("127.0.0.1", 32769));
        client.delete("");

    }

    @Test
    public void testSetNoAvailableNode() throws Exception {
        SpyMemcachedClient client = new SpyMemcachedClient();
        client.addNode(getNodeWithClient("127.0.0.1", 1));

        String value = TestUtils.randomString(300);
        String key = TestUtils.randomString(200);
        assertFalse(client.set(key, value.getBytes(), 100));
        assertNull(client.getAndTouch(key, 100));
        assertNull(client.get(key));
        assertFalse(client.delete(key));

    }

    @Test
    public void testHashkey() throws Exception {
        SpyMemcachedClient client = new SpyMemcachedClient();
        client.addNode(getNodeWithClient("127.0.0.1", 32769));
        assertEquals(0, client.hashKey(null));
        assertEquals(0, client.hashKey(""));
    }

    @Test
    public void testResumingNode() throws Exception {
        SpyMemcachedClient client = new SpyMemcachedClient();
        client.addNode(getNodeWithClient("127.0.0.1", 32769));
        client.addNode(getNodeWithClient("127.0.0.1", 32769));
        client.addNode(getNodeWithClient("127.0.0.1", 3));
        client.addNode(getNodeWithClient("127.0.0.1", 4));
        for (int i = 0; i < 10; i++) {
            String value = TestUtils.randomString(300);
            assertTrue(client.set(Integer.toString(i), value.getBytes(), 100));
            assertEquals(value, new String(client.get(Integer.toString(i))));
        }
        client.resumeNode(2);
        for (int i = 0; i < 10; i++) {
            String value = TestUtils.randomString(300);
            assertTrue(client.set(Integer.toString(i), value.getBytes(), 100));
            assertEquals(value, new String(client.get(Integer.toString(i))));
        }

    }


}
