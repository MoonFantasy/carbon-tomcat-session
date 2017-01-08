package session.store;

import jz.carbon.tomcat.sesssion.store.CacheNodeException;
import jz.carbon.tomcat.sesssion.store.SpyMemcachedNode;
import mock.net.spy.memcached.MockMemcachedClient;
import org.junit.Before;
import org.junit.Test;
import utils.TestUtils;

import java.io.IOException;
import java.net.URI;
import java.nio.channels.UnresolvedAddressException;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by jack on 2016/12/22.
 */
public class TestSpyMemcachedNode {

    private SpyMemcachedNode spyMemcachedNode;


    @Before
    public void setUp() throws Exception {
        SpyMemcachedNode.setMemcachedClientClass(MockMemcachedClient.class);
        spyMemcachedNode = new SpyMemcachedNode(new URI("memcached://localhost:32768"));

    }


    private SpyMemcachedNode getSpyMemcachedNode(String host, int port, Class mcClass) throws Exception {
        SpyMemcachedNode client = new SpyMemcachedNode(new URI("memcached://" + host + ":" + port));
        if (mcClass != null) {
            SpyMemcachedNode.setMemcachedClientClass(mcClass);
        }
        return client;
    }

    @Test
    public void testPortAndHost() throws Exception {
        assertEquals("localhost", spyMemcachedNode.getHost());
        assertEquals(32768, spyMemcachedNode.getPort());
    }

    @Test(expected = CacheNodeException.class)
    public void testSetExcption1() throws Exception {
        String key = TestUtils.randomString(250);
        String value = TestUtils.randomString(300);
        SpyMemcachedNode mcnode = getSpyMemcachedNode("mock", 1, MockMemcachedClient.class);
        mcnode.set(key, value.getBytes(), 5);
    }

    @Test(expected = CacheNodeException.class)
    public void testSetExcption2() throws Exception {
        String key = TestUtils.randomString(250);
        String value = TestUtils.randomString(300);
        SpyMemcachedNode mcnode = getSpyMemcachedNode("mock", 2, MockMemcachedClient.class);
        mcnode.set(key, value.getBytes(), 5);
    }

    @Test(expected = CacheNodeException.class)
    public void testSetExcption3() throws Exception {
        String key = TestUtils.randomString(250);
        String value = TestUtils.randomString(300);
        SpyMemcachedNode mcnode = getSpyMemcachedNode("mock", 3, MockMemcachedClient.class);
        mcnode.set(key, value.getBytes(), 5);
    }

    @Test(expected = CacheNodeException.class)
    public void testSetExcption4() throws Exception {
        String key = TestUtils.randomString(250);
        String value = TestUtils.randomString(300);
        SpyMemcachedNode mcnode = getSpyMemcachedNode("mock", 4, MockMemcachedClient.class);
        mcnode.set(key, value.getBytes(), 5);
    }

    @Test(expected = CacheNodeException.class)
    public void testSetExcption5() throws Exception {
        String key = TestUtils.randomString(250);
        String value = TestUtils.randomString(300);
        SpyMemcachedNode mcnode = getSpyMemcachedNode("mock", 5, MockMemcachedClient.class);
        mcnode.set(key, value.getBytes(), 5);
    }

    @Test
    public void testSetExcption6() throws Exception {
        String key = TestUtils.randomString(250);
        String value = TestUtils.randomString(300);
        SpyMemcachedNode mcnode = getSpyMemcachedNode("mock", 6, MockMemcachedClient.class);
        assertFalse(mcnode.set(key, value.getBytes(), 5));
    }

    @Test(expected = CacheNodeException.class)
    public void testRemoveExcption1() throws Exception {
        String key = TestUtils.randomString(250);
        SpyMemcachedNode mcnode = getSpyMemcachedNode("mock", 1, MockMemcachedClient.class);
        mcnode.remove(key);
    }

    @Test(expected = CacheNodeException.class)
    public void testRemoveExcption2() throws Exception {
        String key = TestUtils.randomString(250);
        SpyMemcachedNode mcnode = getSpyMemcachedNode("mock", 2, MockMemcachedClient.class);
        mcnode.remove(key);
    }

    @Test(expected = CacheNodeException.class)
    public void testRemoveExcption3() throws Exception {
        String key = TestUtils.randomString(250);
        SpyMemcachedNode mcnode = getSpyMemcachedNode("mock", 3, MockMemcachedClient.class);
        mcnode.remove(key);
    }

    @Test(expected = CacheNodeException.class)
    public void testRemoveExcption4() throws Exception {
        String key = TestUtils.randomString(250);
        SpyMemcachedNode mcnode = getSpyMemcachedNode("mock", 4, MockMemcachedClient.class);
        mcnode.remove(key);
    }

    @Test(expected = CacheNodeException.class)
    public void testRemoveExcption5() throws Exception {
        String key = TestUtils.randomString(250);
        SpyMemcachedNode mcnode = getSpyMemcachedNode("mock", 5, MockMemcachedClient.class);
        mcnode.remove(key);
    }

    @Test
    public void testRemoveExcption6() throws Exception {
        String key = TestUtils.randomString(250);
        SpyMemcachedNode mcnode = getSpyMemcachedNode("mock", 6, MockMemcachedClient.class);
        assertFalse(mcnode.remove(key));
    }


    @Test(expected = CacheNodeException.class)
    public void testCleanExcption1() throws Exception {
        SpyMemcachedNode mcnode = getSpyMemcachedNode("mock", 1, MockMemcachedClient.class);
        mcnode.clean();
    }

    @Test(expected = CacheNodeException.class)
    public void testCleanExcption2() throws Exception {
        SpyMemcachedNode mcnode = getSpyMemcachedNode("mock", 2, MockMemcachedClient.class);
        mcnode.clean();
    }

    @Test(expected = CacheNodeException.class)
    public void testCleanExcption3() throws Exception {
        SpyMemcachedNode mcnode = getSpyMemcachedNode("mock", 3, MockMemcachedClient.class);
        mcnode.clean();
    }

    @Test(expected = CacheNodeException.class)
    public void testCleanExcption4() throws Exception {
        SpyMemcachedNode mcnode = getSpyMemcachedNode("mock", 4, MockMemcachedClient.class);
        mcnode.clean();
    }

    @Test(expected = CacheNodeException.class)
    public void testCleanExcption5() throws Exception {
        SpyMemcachedNode mcnode = getSpyMemcachedNode("mock", 5, MockMemcachedClient.class);
        mcnode.clean();

        mcnode = getSpyMemcachedNode("mock", 6, MockMemcachedClient.class);
        mcnode.clean();
    }

    @Test(expected = CacheNodeException.class)
    public void testGetExcption1() throws Exception {
        String key = TestUtils.randomString(250);
        SpyMemcachedNode mcnode = getSpyMemcachedNode("mock", 1, MockMemcachedClient.class);
        mcnode.get(key);
    }

    @Test(expected = CacheNodeException.class)
    public void testGetExcption2() throws Exception {
        String key = TestUtils.randomString(250);
        SpyMemcachedNode mcnode = getSpyMemcachedNode("mock", 2, MockMemcachedClient.class);
        mcnode.get(key);
    }

    @Test(expected = CacheNodeException.class)
    public void testGetExcption3() throws Exception {
        String key = TestUtils.randomString(250);
        SpyMemcachedNode mcnode = getSpyMemcachedNode("mock", 3, MockMemcachedClient.class);
        mcnode.get(key);
    }

    @Test(expected = CacheNodeException.class)
    public void testGetExcption4() throws Exception {
        String key = TestUtils.randomString(250);
        SpyMemcachedNode mcnode = getSpyMemcachedNode("mock", 4, MockMemcachedClient.class);
        mcnode.get(key);
    }

    @Test(expected = CacheNodeException.class)
    public void testGetExcption5() throws Exception {
        String key = TestUtils.randomString(250);
        SpyMemcachedNode mcnode = getSpyMemcachedNode("mock", 5, MockMemcachedClient.class);
        mcnode.get(key);
    }

    @Test
    public void testGetExcption6() throws Exception {
        String key = TestUtils.randomString(250);
        SpyMemcachedNode mcnode = getSpyMemcachedNode("mock", 6, MockMemcachedClient.class);
        assertNull(mcnode.get(key));
    }

    @Test
    public void testGetAndTouch() throws Exception {
        String key = TestUtils.randomString(250);
        SpyMemcachedNode mcnode = getSpyMemcachedNode("127.0.0.1", 32769, MockMemcachedClient.class);
        assertNull(mcnode.getAntTouch(key, 5));
    }

    @Test
    public void testGet() throws Exception {
        String key = TestUtils.randomString(250);
        SpyMemcachedNode mcnode = getSpyMemcachedNode("127.0.0.1", 32769, MockMemcachedClient.class);
        assertNull(mcnode.get(key));
    }


    @Test(expected = CacheNodeException.class)
    public void testGetAndTouchExcption1() throws Exception {
        String key = TestUtils.randomString(250);
        SpyMemcachedNode mcnode = getSpyMemcachedNode("mock", 1, MockMemcachedClient.class);
        mcnode.getAntTouch(key, 5);
    }


    @Test(expected = CacheNodeException.class)
    public void testGetAndTouchExcption2() throws Exception {
        String key = TestUtils.randomString(250);
        SpyMemcachedNode mcnode = getSpyMemcachedNode("mock", 2, MockMemcachedClient.class);
        mcnode.getAntTouch(key, 5);
    }

    @Test(expected = CacheNodeException.class)
    public void testGetAndTouchExcption3() throws Exception {
        String key = TestUtils.randomString(250);
        SpyMemcachedNode mcnode = getSpyMemcachedNode("mock", 3, MockMemcachedClient.class);
        mcnode.getAntTouch(key, 5);
    }

    @Test(expected = CacheNodeException.class)
    public void testGetAndTouchExcption4() throws Exception {
        String key = TestUtils.randomString(250);
        SpyMemcachedNode mcnode = getSpyMemcachedNode("mock", 4, MockMemcachedClient.class);
        mcnode.getAntTouch(key, 5);
    }

    @Test(expected = CacheNodeException.class)
    public void testGetAndTouchExcption5() throws Exception {
        String key = TestUtils.randomString(250);
        SpyMemcachedNode mcnode = getSpyMemcachedNode("mock", 5, MockMemcachedClient.class);
        mcnode.getAntTouch(key, 5);
    }

    @Test
    public void testGetAndTouchExcption6() throws Exception {
        String key = TestUtils.randomString(250);
        SpyMemcachedNode mcnode = getSpyMemcachedNode("mock", 6, MockMemcachedClient.class);
        assertNull(mcnode.getAntTouch(key, 5));
    }


    @Test(expected = UnresolvedAddressException.class)
    public void testUnresolvedAddressException() throws Exception {
        SpyMemcachedNode mcnode = getSpyMemcachedNode("mock", 2, MockMemcachedClient.class);
        mcnode.getNewMemcachedClient();
    }

    @Test(expected = IOException.class)
    public void testIOException() throws Exception {
        SpyMemcachedNode mcnode = getSpyMemcachedNode("mock", 1, MockMemcachedClient.class);
        mcnode.getNewMemcachedClient();
    }

    @Test(expected = CacheNodeException.class)
    public void testGetStatsExcption1() throws Exception {
        SpyMemcachedNode mcnode = getSpyMemcachedNode("mock", 1, MockMemcachedClient.class);
        mcnode.getStats(null);
    }

    @Test(expected = CacheNodeException.class)
    public void testGetStatsExcption2() throws Exception {
        SpyMemcachedNode mcnode = getSpyMemcachedNode("mock", 2, MockMemcachedClient.class);
        mcnode.getStats(null);
    }

    @Test(expected = CacheNodeException.class)
    public void testGetStatsExcption3() throws Exception {
        SpyMemcachedNode mcnode = getSpyMemcachedNode("mock", 3, MockMemcachedClient.class);
        mcnode.getStats(null);
    }

    @Test
    public void testGetStats() throws Exception {
        SpyMemcachedNode mcnode = getSpyMemcachedNode("mock", 11211, MockMemcachedClient.class);
        Map<String, String> stats = mcnode.getStats(null);
        assertNotNull(stats);
    }


    @Test
    public void testOperation() throws Exception {
        SpyMemcachedNode mcnode = getSpyMemcachedNode("mock", 11211, MockMemcachedClient.class);
        String key = TestUtils.randomString(100);
        String value = TestUtils.randomString(300);
        assertTrue(mcnode.set(key, value.getBytes(), 100));
        assertEquals(value, new String(mcnode.get(key)));
        assertEquals(value, new String(mcnode.getAntTouch(key, 100)));
        assertTrue(mcnode.remove(key));
        mcnode.clean();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetMemcachedClientClassException() throws Exception {
        SpyMemcachedNode mcnode = getSpyMemcachedNode("mock", 11211, MockMemcachedClient.class);
        SpyMemcachedNode.setMemcachedClientClass(Object.class);
    }

    @Test
    public void testSetMemcachedClient() throws Exception {
        SpyMemcachedNode mcnode = getSpyMemcachedNode("mock", 11211, MockMemcachedClient.class);
        mcnode.setMemcachedClient(MockMemcachedClient.getNewInstance("mock", 11211));
        assertTrue(mcnode.getNewMemcachedClient() instanceof MockMemcachedClient);
    }
}
