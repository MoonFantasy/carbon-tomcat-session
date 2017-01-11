package session.store;

import jz.carbon.tomcat.sesssion.store.CacheNodeException;
import jz.carbon.tomcat.sesssion.store.SpyMemcachedNode;
import mock.net.spy.memcached.MockMemcachedClient;
import net.spy.memcached.BinaryConnectionFactory;
import net.spy.memcached.MemcachedClientIF;
import org.junit.Before;
import org.junit.Test;
import utils.TestUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.nio.channels.UnresolvedAddressException;
import java.util.ArrayList;
import java.util.HashMap;
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
        SpyMemcachedNode mcNode = getSpyMemcachedNode("mock", 1, MockMemcachedClient.class);
        mcNode.set(key, value.getBytes(), 5);
    }

    @Test(expected = CacheNodeException.class)
    public void testSetExcption2() throws Exception {
        String key = TestUtils.randomString(250);
        String value = TestUtils.randomString(300);
        SpyMemcachedNode mcNode = getSpyMemcachedNode("mock", 2, MockMemcachedClient.class);
        mcNode.set(key, value.getBytes(), 5);
    }

    @Test(expected = CacheNodeException.class)
    public void testSetExcption3() throws Exception {
        String key = TestUtils.randomString(250);
        String value = TestUtils.randomString(300);
        SpyMemcachedNode mcNode = getSpyMemcachedNode("mock", 3, MockMemcachedClient.class);
        mcNode.set(key, value.getBytes(), 5);
    }

    @Test(expected = CacheNodeException.class)
    public void testSetExcption4() throws Exception {
        String key = TestUtils.randomString(250);
        String value = TestUtils.randomString(300);
        SpyMemcachedNode mcNode = getSpyMemcachedNode("mock", 4, MockMemcachedClient.class);
        mcNode.set(key, value.getBytes(), 5);
    }

    @Test(expected = CacheNodeException.class)
    public void testSetExcption5() throws Exception {
        String key = TestUtils.randomString(250);
        String value = TestUtils.randomString(300);
        SpyMemcachedNode mcNode = getSpyMemcachedNode("mock", 5, MockMemcachedClient.class);
        mcNode.set(key, value.getBytes(), 5);
    }

    @Test
    public void testSetExcption6() throws Exception {
        String key = TestUtils.randomString(250);
        String value = TestUtils.randomString(300);
        SpyMemcachedNode mcNode = getSpyMemcachedNode("mock", 6, MockMemcachedClient.class);
        assertFalse(mcNode.set(key, value.getBytes(), 5));
    }

    @Test(expected = CacheNodeException.class)
    public void testRemoveExcption1() throws Exception {
        String key = TestUtils.randomString(250);
        SpyMemcachedNode mcNode = getSpyMemcachedNode("mock", 1, MockMemcachedClient.class);
        mcNode.remove(key);
    }

    @Test(expected = CacheNodeException.class)
    public void testRemoveExcption2() throws Exception {
        String key = TestUtils.randomString(250);
        SpyMemcachedNode mcNode = getSpyMemcachedNode("mock", 2, MockMemcachedClient.class);
        mcNode.remove(key);
    }

    @Test(expected = CacheNodeException.class)
    public void testRemoveExcption3() throws Exception {
        String key = TestUtils.randomString(250);
        SpyMemcachedNode mcNode = getSpyMemcachedNode("mock", 3, MockMemcachedClient.class);
        mcNode.remove(key);
    }

    @Test(expected = CacheNodeException.class)
    public void testRemoveExcption4() throws Exception {
        String key = TestUtils.randomString(250);
        SpyMemcachedNode mcNode = getSpyMemcachedNode("mock", 4, MockMemcachedClient.class);
        mcNode.remove(key);
    }

    @Test(expected = CacheNodeException.class)
    public void testRemoveExcption5() throws Exception {
        String key = TestUtils.randomString(250);
        SpyMemcachedNode mcNode = getSpyMemcachedNode("mock", 5, MockMemcachedClient.class);
        mcNode.remove(key);
    }

    @Test
    public void testRemoveExcption6() throws Exception {
        String key = TestUtils.randomString(250);
        SpyMemcachedNode mcNode = getSpyMemcachedNode("mock", 6, MockMemcachedClient.class);
        assertFalse(mcNode.remove(key));
    }


    @Test(expected = CacheNodeException.class)
    public void testCleanExcption1() throws Exception {
        SpyMemcachedNode mcNode = getSpyMemcachedNode("mock", 1, MockMemcachedClient.class);
        mcNode.clean();
    }

    @Test(expected = CacheNodeException.class)
    public void testCleanExcption2() throws Exception {
        SpyMemcachedNode mcNode = getSpyMemcachedNode("mock", 2, MockMemcachedClient.class);
        mcNode.clean();
    }

    @Test(expected = CacheNodeException.class)
    public void testCleanExcption3() throws Exception {
        SpyMemcachedNode mcNode = getSpyMemcachedNode("mock", 3, MockMemcachedClient.class);
        mcNode.clean();
    }

    @Test(expected = CacheNodeException.class)
    public void testCleanExcption4() throws Exception {
        SpyMemcachedNode mcNode = getSpyMemcachedNode("mock", 4, MockMemcachedClient.class);
        mcNode.clean();
    }

    @Test(expected = CacheNodeException.class)
    public void testCleanExcption5() throws Exception {
        SpyMemcachedNode mcNode = getSpyMemcachedNode("mock", 5, MockMemcachedClient.class);
        mcNode.clean();

        mcNode = getSpyMemcachedNode("mock", 6, MockMemcachedClient.class);
        mcNode.clean();
    }

    @Test
    public void testCleanExcption6() throws Exception {
        SpyMemcachedNode mcNode = getSpyMemcachedNode("mock", 6, MockMemcachedClient.class);
        mcNode.clean();
    }

    @Test(expected = CacheNodeException.class)
    public void testGetExcption1() throws Exception {
        String key = TestUtils.randomString(250);
        SpyMemcachedNode mcNode = getSpyMemcachedNode("mock", 1, MockMemcachedClient.class);
        mcNode.get(key);
    }

    @Test(expected = CacheNodeException.class)
    public void testGetExcption2() throws Exception {
        String key = TestUtils.randomString(250);
        SpyMemcachedNode mcNode = getSpyMemcachedNode("mock", 2, MockMemcachedClient.class);
        mcNode.get(key);
    }

    @Test(expected = CacheNodeException.class)
    public void testGetExcption3() throws Exception {
        String key = TestUtils.randomString(250);
        SpyMemcachedNode mcNode = getSpyMemcachedNode("mock", 3, MockMemcachedClient.class);
        mcNode.get(key);
    }

    @Test(expected = CacheNodeException.class)
    public void testGetExcption4() throws Exception {
        String key = TestUtils.randomString(250);
        SpyMemcachedNode mcNode = getSpyMemcachedNode("mock", 4, MockMemcachedClient.class);
        mcNode.get(key);
    }

    @Test(expected = CacheNodeException.class)
    public void testGetExcption5() throws Exception {
        String key = TestUtils.randomString(250);
        SpyMemcachedNode mcNode = getSpyMemcachedNode("mock", 5, MockMemcachedClient.class);
        mcNode.get(key);
    }

    @Test
    public void testGetExcption6() throws Exception {
        String key = TestUtils.randomString(250);
        SpyMemcachedNode mcNode = getSpyMemcachedNode("mock", 6, MockMemcachedClient.class);
        assertNull(mcNode.get(key));
    }

    @Test
    public void testGetAndTouch() throws Exception {
        String key = TestUtils.randomString(250);
        SpyMemcachedNode mcNode = getSpyMemcachedNode("127.0.0.1", 32769, MockMemcachedClient.class);
        assertNull(mcNode.getAntTouch(key, 5));
    }

    @Test
    public void testGet() throws Exception {
        String key = TestUtils.randomString(250);
        SpyMemcachedNode mcNode = getSpyMemcachedNode("127.0.0.1", 32769, MockMemcachedClient.class);
        assertNull(mcNode.get(key));
    }


    @Test(expected = CacheNodeException.class)
    public void testGetAndTouchExcption1() throws Exception {
        String key = TestUtils.randomString(250);
        SpyMemcachedNode mcNode = getSpyMemcachedNode("mock", 1, MockMemcachedClient.class);
        mcNode.getAntTouch(key, 5);
    }


    @Test(expected = CacheNodeException.class)
    public void testGetAndTouchExcption2() throws Exception {
        String key = TestUtils.randomString(250);
        SpyMemcachedNode mcNode = getSpyMemcachedNode("mock", 2, MockMemcachedClient.class);
        mcNode.getAntTouch(key, 5);
    }

    @Test(expected = CacheNodeException.class)
    public void testGetAndTouchExcption3() throws Exception {
        String key = TestUtils.randomString(250);
        SpyMemcachedNode mcNode = getSpyMemcachedNode("mock", 3, MockMemcachedClient.class);
        mcNode.getAntTouch(key, 5);
    }

    @Test(expected = CacheNodeException.class)
    public void testGetAndTouchExcption4() throws Exception {
        String key = TestUtils.randomString(250);
        SpyMemcachedNode mcNode = getSpyMemcachedNode("mock", 4, MockMemcachedClient.class);
        mcNode.getAntTouch(key, 5);
    }

    @Test(expected = CacheNodeException.class)
    public void testGetAndTouchExcption5() throws Exception {
        String key = TestUtils.randomString(250);
        SpyMemcachedNode mcNode = getSpyMemcachedNode("mock", 5, MockMemcachedClient.class);
        mcNode.getAntTouch(key, 5);
    }

    @Test
    public void testGetAndTouchExcption6() throws Exception {
        String key = TestUtils.randomString(250);
        SpyMemcachedNode mcNode = getSpyMemcachedNode("mock", 6, MockMemcachedClient.class);
        assertNull(mcNode.getAntTouch(key, 5));
    }


    @Test(expected = UnresolvedAddressException.class)
    public void testUnresolvedAddressException() throws Exception {
        SpyMemcachedNode mcNode = getSpyMemcachedNode("mock", 2, MockMemcachedClient.class);
        mcNode.getNewMemcachedClient();
    }

    @Test(expected = IOException.class)
    public void testIOException() throws Exception {
        SpyMemcachedNode mcNode = getSpyMemcachedNode("mock", 1, MockMemcachedClient.class);
        mcNode.getNewMemcachedClient();
    }

    @Test(expected = CacheNodeException.class)
    public void testGetStatsExcption1() throws Exception {
        SpyMemcachedNode mcNode = getSpyMemcachedNode("mock", 1, MockMemcachedClient.class);
        mcNode.getStats(null);
    }

    @Test(expected = CacheNodeException.class)
    public void testGetStatsExcption2() throws Exception {
        SpyMemcachedNode mcNode = getSpyMemcachedNode("mock", 2, MockMemcachedClient.class);
        mcNode.getStats(null);
    }

    @Test(expected = CacheNodeException.class)
    public void testGetStatsExcption3() throws Exception {
        SpyMemcachedNode mcNode = getSpyMemcachedNode("mock", 3, MockMemcachedClient.class);
        mcNode.getStats(null);
    }

    @Test
    public void testGetStats() throws Exception {
        SpyMemcachedNode mcNode = getSpyMemcachedNode("mock", 11211, MockMemcachedClient.class);
        Map<String, String> stats = mcNode.getStats(null);
        assertNotNull(stats);
    }

    @Test
    public void testGetStatsNull() throws Exception {
        SpyMemcachedNode mcNode = getSpyMemcachedNode("mock", 11211, MockMemcachedClient.class);

        ArrayList<InetSocketAddress> addrs = new ArrayList<InetSocketAddress>();
        addrs.add(new InetSocketAddress("localhost", 11211));
        MockMemcachedClient client = new MockMemcachedClient(new BinaryConnectionFactory(), addrs) {
            @Override
            public Map<SocketAddress, Map<String, String>> getStats(String prefix) {
                return null;
            }

            @Override
            public Map<SocketAddress, Map<String, String>> getStats() {
                return null;
            }
        };

        mcNode.setMemcachedClient(client);
        Map<String, String> stats = mcNode.getStats(null);
        assertNotNull(stats);
    }


    @Test
    public void testOperation() throws Exception {
        SpyMemcachedNode mcNode = getSpyMemcachedNode("mock", 11211, MockMemcachedClient.class);
        String key = TestUtils.randomString(100);
        String value = TestUtils.randomString(300);
        assertTrue(mcNode.set(key, value.getBytes(), 100));
        assertEquals(value, new String(mcNode.get(key)));
        assertEquals(value, new String(mcNode.getAntTouch(key, 100)));
        assertTrue(mcNode.remove(key));
        mcNode.clean();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetMemcachedClientClassException() throws Exception {
        SpyMemcachedNode mcNode = getSpyMemcachedNode("mock", 11211, MockMemcachedClient.class);
        SpyMemcachedNode.setMemcachedClientClass(Object.class);
    }

    @Test
    public void testSetMemcachedClient() throws Exception {
        SpyMemcachedNode mcNode = getSpyMemcachedNode("mock", 11211, MockMemcachedClient.class);
        mcNode.setMemcachedClient(MockMemcachedClient.getNewInstance("mock", 11211));
        assertTrue(mcNode.getNewMemcachedClient() instanceof MockMemcachedClient);
    }

    @Test
    public void testClose() throws Exception {
        SpyMemcachedNode mcNode = getSpyMemcachedNode("mock", 11211, MockMemcachedClient.class);
        mcNode.get("123");
        Field fd = SpyMemcachedNode.class.getDeclaredField("memcachedClient");
        fd.setAccessible(true);

        assertNotNull(fd.get(mcNode));
        assertTrue(fd.get(mcNode) instanceof MemcachedClientIF);
        mcNode.close();
        assertNull(fd.get(mcNode));
    }

    @Test
    public void testGetKeyCount() throws Exception {
        SpyMemcachedNode mcNode = getSpyMemcachedNode("mock", 11211, MockMemcachedClient.class);
        assertEquals(0, mcNode.getKeyCount("prefix"));

        ArrayList<InetSocketAddress> addrs = new ArrayList<InetSocketAddress>();
        addrs.add(new InetSocketAddress("localhost", 11211));
        MockMemcachedClient client = new MockMemcachedClient(new BinaryConnectionFactory(), addrs) {
            @Override
            public Map<SocketAddress, Map<String, String>> getStats(String prefix) {

                Map<SocketAddress, Map<String, String>> map = new HashMap<SocketAddress, Map<String, String>>();
                Map<String, String> innerMap = new HashMap<String, String>();
                innerMap.put("total_items", "abc");
                innerMap.put("evicted_unfetched", "abc");
                map.put(null, innerMap);
                return map;
            }
        };
        mcNode.setMemcachedClient(client);
        assertEquals(0, mcNode.getKeyCount("prefix"));
    }

    @Test
    public void testGetKeys() throws Exception {
        SpyMemcachedNode mcNode = getSpyMemcachedNode("mock", 11211, MockMemcachedClient.class);
        assertEquals(0, mcNode.getKeys("prefix").size());
    }

}
