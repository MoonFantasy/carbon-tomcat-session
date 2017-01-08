package session.store;

import jz.carbon.tomcat.sesssion.store.CacheNodeException;
import jz.carbon.tomcat.sesssion.store.RedisCacheNode;
import mock.redis.clients.jedis.JedisPoolExceptionMock;
import mock.redis.clients.jedis.JedisPoolMock;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.Test;
import utils.TestUtils;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Created by jack on 2017/1/5.
 */
public class TestJedisCacheNode {
    public static RedisCacheNode getRedisCacheNodeWithMock(String uriStr, int sw) throws Exception {
        RedisCacheNode redisNode = new RedisCacheNode(new URI(uriStr));
        if (sw == JedisPoolMock.NONE)
            redisNode.setPoolClass(JedisPoolMock.class);
        else {
            JedisPoolMock jedisPool = new JedisPoolMock(new GenericObjectPoolConfig(), new URI(uriStr), RedisCacheNode.DEFAULT_TIMEOUT);
            jedisPool.setSw(sw);
            redisNode.setJedisPool(jedisPool);
        }
        return redisNode;
    }


    @Test
    public void testSetPoolClass() throws Exception {
        RedisCacheNode node = new RedisCacheNode(new URI("redis://dummy:123"));
        node.setPoolClass(JedisPoolMock.class);
        Field fd = node.getClass().getDeclaredField("poolClass");
        fd.setAccessible(true);
        assertEquals(((Class)fd.get(node)).getName(), JedisPoolMock.class.getName());
    }

    @Test
    public void testSetPoolClassFail() throws Exception {
        RedisCacheNode node = new RedisCacheNode(new URI("redis://dummy:123"));
        Field fd = node.getClass().getDeclaredField("poolClass");
        fd.setAccessible(true);
        String oldClassName =((Class)fd.get(node)).getName();
        node.setPoolClass(Object.class);
        assertEquals(((Class)fd.get(node)).getName(), oldClassName);

    }

    @Test(expected = CacheNodeException.class)
    public void testNewPoolException() throws Exception {
        RedisCacheNode node = new RedisCacheNode(new URI("redis://dummy:123"));
        node.setPoolClass(JedisPoolExceptionMock.class);
        node.get("321");

    }

    @Test
    public void testSetUri() throws Exception {
        String uriStr1 = "redis://dummy:1234";
        String uriStr2 = "redis://dummy2:4321";
        RedisCacheNode node = new RedisCacheNode(new URI(uriStr1));
        assertEquals(uriStr1, node.getUri().toString());
        node.setUri(new URI(uriStr2));
        assertEquals(uriStr2, node.getUri().toString());
    }

    @Test
    public void testSetMaxPoolSetTimeout() throws Exception {
        int timeout = RedisCacheNode.DEFAULT_MAX_POOL + 10;
        int maxPool = RedisCacheNode.DEFAULT_TIMEOUT + 10;
        RedisCacheNode node = new RedisCacheNode(new URI("redis://dummy:1234"));
        Field feildMaxPool = node.getClass().getDeclaredField("maxPool");
        feildMaxPool.setAccessible(true);
        Field feildTimeout = node.getClass().getDeclaredField("timeout");
        feildTimeout.setAccessible(true);

        assertEquals(RedisCacheNode.DEFAULT_MAX_POOL , feildMaxPool.get(node));
        assertEquals(RedisCacheNode.DEFAULT_TIMEOUT , feildTimeout.get(node));

        node.setTimeout(timeout);
        assertEquals(timeout , feildTimeout.get(node));

        node.setMaxPool(maxPool);
        assertEquals(maxPool , feildMaxPool.get(node));

        node.setMaxPool(0);
        assertEquals(RedisCacheNode.DEFAULT_MAX_POOL , feildMaxPool.get(node));

        node.setTimeout(0);
        assertEquals(RedisCacheNode.DEFAULT_TIMEOUT , feildTimeout.get(node));
    }

    @Test
    public void testSet() throws Exception {
        RedisCacheNode node = getRedisCacheNodeWithMock("redis://dummy:123", JedisPoolMock.NONE);
        byte[] data = TestUtils.randomBytes(100);
        String key = TestUtils.randomString(40);
        assertTrue(node.set(key, data, 123));
        assertArrayEquals(data, node.get(key));
    }

    @Test
    public void testSetDataException() throws Exception {
        RedisCacheNode node = getRedisCacheNodeWithMock("redis://dummy:123", JedisPoolMock.DATA_EXCEPTION);
        byte[] data = TestUtils.randomBytes(100);
        String key = TestUtils.randomString(40);
        assertFalse(node.set(key, data, 123));
    }

    @Test(expected = CacheNodeException.class)
    public void testSetException() throws Exception {
        RedisCacheNode node = getRedisCacheNodeWithMock("redis://dummy:123", JedisPoolMock.CONN_EXCEPTION);
        byte[] data = TestUtils.randomBytes(100);
        String key = TestUtils.randomString(40);
        node.set(key, data, 123);
    }


    @Test(expected = CacheNodeException.class)
    public void testSetOtherException() throws Exception {
        RedisCacheNode node = getRedisCacheNodeWithMock("redis://dummy:123", JedisPoolMock.OTHER_EXCEPTION);
        byte[] data = TestUtils.randomBytes(100);
        String key = TestUtils.randomString(40);
        node.set(key, data, 123);
    }



    @Test
    public void testGetAndTuch() throws Exception {
        RedisCacheNode node = getRedisCacheNodeWithMock("redis://dummy:123", JedisPoolMock.NONE);
        byte[] data = TestUtils.randomBytes(100);
        String key = TestUtils.randomString(40);
        assertTrue(node.set(key, data, 123));
        assertArrayEquals(data, node.getAntTouch(key, 40));
    }



    @Test
    public void testGet() throws Exception {
        RedisCacheNode node = getRedisCacheNodeWithMock("redis://dummy:123", JedisPoolMock.NONE);
        assertNull(node.get("123"));
    }

    @Test(expected = CacheNodeException.class)
    public void testGetException() throws Exception {
        RedisCacheNode node = getRedisCacheNodeWithMock("redis://dummy:123", JedisPoolMock.CONN_EXCEPTION);
        node.get("123");
    }

    @Test
    public void testGetDataException() throws Exception {
        RedisCacheNode node = getRedisCacheNodeWithMock("redis://dummy:123", JedisPoolMock.DATA_EXCEPTION);
        assertNull(node.get("123"));
    }


    @Test(expected = CacheNodeException.class)
    public void testGetRuntimeException() throws Exception {
        RedisCacheNode node = getRedisCacheNodeWithMock("redis://dummy:123", JedisPoolMock.OTHER_EXCEPTION);
        node.get("123");
    }


    @Test
    public void testRemove() throws Exception {
        RedisCacheNode node = getRedisCacheNodeWithMock("redis://dummy:123", JedisPoolMock.NONE);
        assertFalse(node.remove("123"));
    }

    @Test(expected = CacheNodeException.class)
    public void testRemoveException() throws Exception {
        RedisCacheNode node = getRedisCacheNodeWithMock("redis://dummy:123", JedisPoolMock.CONN_EXCEPTION);
        node.remove("123");
    }

    @Test
    public void testRemoveDataException() throws Exception {
        RedisCacheNode node = getRedisCacheNodeWithMock("redis://dummy:123", JedisPoolMock.DATA_EXCEPTION);
        assertFalse(node.remove("123"));
    }


    @Test(expected = CacheNodeException.class)
    public void testRemoveRuntimeException() throws Exception {
        RedisCacheNode node = getRedisCacheNodeWithMock("redis://dummy:123", JedisPoolMock.OTHER_EXCEPTION);
        node.remove("123");
    }


    @Test
    public void testClean() throws Exception {
        RedisCacheNode node = getRedisCacheNodeWithMock("redis://dummy:123", JedisPoolMock.NONE);
        node.clean();
    }

    @Test(expected = CacheNodeException.class)
    public void testCleanException() throws Exception {
        RedisCacheNode node = getRedisCacheNodeWithMock("redis://dummy:123", JedisPoolMock.CONN_EXCEPTION);
        node.clean();
    }

    @Test
    public void testCleanDataException() throws Exception {
        RedisCacheNode node = getRedisCacheNodeWithMock("redis://dummy:123", JedisPoolMock.DATA_EXCEPTION);
        node.clean();
    }


    @Test(expected = CacheNodeException.class)
    public void testCleanRuntimeException() throws Exception {
        RedisCacheNode node = getRedisCacheNodeWithMock("redis://dummy:123", JedisPoolMock.OTHER_EXCEPTION);
        node.clean();
    }


    @Test
    public void testGetState() throws Exception {
        RedisCacheNode node = getRedisCacheNodeWithMock("redis://dummy:123", JedisPoolMock.NONE);
        Map<String, String> stat = node.getStats(null);
        Map<String, String> stat2 = node.getStats("server");
        assertTrue(stat.containsKey("dummy"));
        assertTrue(stat2.containsKey("dummy"));
        assertEquals("123", stat.get("dummy"));
        assertEquals("123", stat2.get("dummy"));
    }

    @Test(expected = CacheNodeException.class)
    public void testGetStateException() throws Exception {
        RedisCacheNode node = getRedisCacheNodeWithMock("redis://dummy:123", JedisPoolMock.CONN_EXCEPTION);
        Map<String, String> stat = node.getStats(null);
    }

    @Test
    public void testGetStateDataException() throws Exception {
        RedisCacheNode node = getRedisCacheNodeWithMock("redis://dummy:123", JedisPoolMock.DATA_EXCEPTION);
        assertTrue(node.getStats(null).isEmpty());
    }


    @Test(expected = CacheNodeException.class)
    public void testGetStateRuntimeException() throws Exception {
        RedisCacheNode node = getRedisCacheNodeWithMock("redis://dummy:123", JedisPoolMock.OTHER_EXCEPTION);
        Map<String, String> stat = node.getStats(null);
    }



    @Test
    public void testGetKeyCount() throws Exception {
        RedisCacheNode node = getRedisCacheNodeWithMock("redis://dummy:123", JedisPoolMock.NONE);
        assertEquals(0, node.getKeyCount("123"));
    }

    @Test
    public void testGetKeyCountNumString() throws Exception {
        RedisCacheNode node = getRedisCacheNodeWithMock("redis://dummy:123", JedisPoolMock.EVAL_NUMBER_STRING);
        assertEquals(0, node.getKeyCount("123"));
    }

    @Test
    public void testGetKeyCountNotNumString() throws Exception {
        RedisCacheNode node = getRedisCacheNodeWithMock("redis://dummy:123", JedisPoolMock.EVAL_NOT_NUMBER_STRING);
        assertEquals(0, node.getKeyCount("123"));
    }


    @Test(expected = CacheNodeException.class)
    public void testGetKeyCountException() throws Exception {
        RedisCacheNode node = getRedisCacheNodeWithMock("redis://dummy:123", JedisPoolMock.CONN_EXCEPTION);
        node.getKeyCount("123");
    }

    @Test
    public void testGetKeyCountDataException() throws Exception {
        RedisCacheNode node = getRedisCacheNodeWithMock("redis://dummy:123", JedisPoolMock.DATA_EXCEPTION);
        assertEquals(0, node.getKeyCount("123"));
    }


    @Test(expected = CacheNodeException.class)
    public void testGetKeyCountRuntimeException() throws Exception {
        RedisCacheNode node = getRedisCacheNodeWithMock("redis://dummy:123", JedisPoolMock.OTHER_EXCEPTION);
        node.getKeyCount("123");
    }

    @Test
    public void testGetKey() throws Exception {
        RedisCacheNode node = getRedisCacheNodeWithMock("redis://dummy:123", JedisPoolMock.NONE);
        assertTrue(node.getKeys("123").isEmpty());
    }

    @Test(expected = CacheNodeException.class)
    public void testGetKeyException() throws Exception {
        RedisCacheNode node = getRedisCacheNodeWithMock("redis://dummy:123", JedisPoolMock.CONN_EXCEPTION);
        node.getKeys("123");
    }

    @Test
    public void testGetKeyDataException() throws Exception {
        RedisCacheNode node = getRedisCacheNodeWithMock("redis://dummy:123", JedisPoolMock.DATA_EXCEPTION);
        assertTrue(node.getKeys("123").isEmpty());
    }


    @Test(expected = CacheNodeException.class)
    public void testGetKeyRuntimeException() throws Exception {
        RedisCacheNode node = getRedisCacheNodeWithMock("redis://dummy:123", JedisPoolMock.OTHER_EXCEPTION);
        node.getKeys("123");
    }

    @Test
    public void testClose() throws Exception {
        RedisCacheNode node = getRedisCacheNodeWithMock("redis://dummy:123", JedisPoolMock.NONE);
        node.getKeys("123");
        Field poolField = RedisCacheNode.class.getDeclaredField("jedisPool");
        poolField.setAccessible(true);
        assertNotNull(poolField.get(node));
        assertTrue(poolField.get(node) instanceof JedisPoolMock);
        node.close();
        assertNull(poolField.get(node));
    }
}
