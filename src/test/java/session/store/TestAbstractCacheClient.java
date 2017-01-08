package session.store;

import jz.carbon.tomcat.sesssion.store.AbstractCacheClient;
import jz.carbon.tomcat.sesssion.store.AbstractCacheStore;
import jz.carbon.tomcat.sesssion.store.CacheClientIOException;
import jz.carbon.tomcat.sesssion.store.CacheNodeException;
import mock.jz.store.MockCacheClient;
import mock.jz.store.MockCacheNode;
import mock.jz.store.MockStore;
import org.junit.Test;
import utils.TestUtils;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by jack on 2017/1/8.
 */
public class TestAbstractCacheClient {


    private AbstractCacheClient getCacheClient(AbstractCacheStore store) {
        return new MockCacheClient(store);
    }

    @Test
    public void testAddNode() throws Exception {
        AbstractCacheClient client = getCacheClient(null);
        client.addNode(new URI(null,
                null, "localnost", 1234,
        null, null, null));
        assertEquals(0, client.getNodeSize());

        client.addNode(new URI("abc",
                null, "localnost", 1234,
                null, null, null));
        assertEquals(0, client.getNodeSize());

        client.addNode(new URI(client.getUriScheme(),
                null, "localnost", -1,
                null, null, null));
        assertEquals(1, client.getNodeSize());
        assertEquals(client.getDefaultPort(), client.getNode(0).getUri().getPort());

        client.addNode(new URI(client.getUriSslScheme(),
                null, "localnost", -1,
                null, null, null));
        assertEquals(2, client.getNodeSize());
        assertEquals(client.getDefaultPort(), client.getNode(1).getUri().getPort());
    }

    @Test
    public void testAddNodeURIException() throws Exception {
        AbstractCacheClient client = new MockCacheClient(null) {
            public String getUriScheme() {
                return "";
            }
            public String getUriSslScheme() {
                return "";
            }
        };
        URI uri = new URI("dummy://localhost");
        Field schemeField = uri.getClass().getDeclaredField("scheme");
        schemeField.setAccessible(true);
        schemeField.set(uri, "");
        Field schemeField2 = uri.getClass().getDeclaredField("port");
        schemeField2.setAccessible(true);
        schemeField2.set(uri, -1);
        assertEquals(0,client.getNodeSize());
        client.addNode(uri);
        assertEquals(0,client.getNodeSize());
    }

    @Test
    public void testHashKey() throws Exception {
        AbstractCacheClient client = getCacheClient(null);
        assertEquals(0, client.hashKey(null));
        assertEquals(0, client.hashKey(TestUtils.randomString(0)));
        assertTrue(client.hashKey(TestUtils.randomString(1)) > 0);
        assertTrue(client.hashKey(TestUtils.randomString(10)) > 0);
        assertTrue(client.hashKey(TestUtils.randomString(150)) > 0);
        assertTrue(client.hashKey(TestUtils.randomString(222)) > 0);
    }


    @Test
    public void testSetAndGetStore() throws Exception {
        AbstractCacheClient client = getCacheClient(null);
        assertNull(client.getStore());
        client.setStore(new MockStore());
        assertTrue(client.getStore() instanceof MockStore);
    }

    @Test
    public void testGet() throws Exception {
        AbstractCacheClient client = getCacheClient(null);
        client.addNode(new URI(client.getUriScheme()+"://127.0.0.1:1234"));
        client.addNode(new URI(client.getUriScheme()+"://127.0.0.1:1235"));
        assertEquals(2, client.getNodeSize());
        assertNull(client.get("mykey"));
    }

    @Test
    public void testGetStoreKey() throws Exception {
        AbstractCacheClient client = getCacheClient(null);
        client.addNode(new URI(client.getUriScheme()+"://127.0.0.1:1235"));
        Field keyPrefixField = AbstractCacheClient.class.getDeclaredField("keyPrefix");
        keyPrefixField.setAccessible(true);
        String prefix = "prefix____";
        assertEquals("", keyPrefixField.get(client));
        assertNull(client.get("mykey"));
        client.setKeyPrefix(prefix);
        assertNull(client.get("mykey"));
        assertEquals(prefix, keyPrefixField.get(client));
    }


    @Test
    public void testGetAndtouch() throws Exception {
        AbstractCacheClient client = getCacheClient(null);
        client.addNode(new URI(client.getUriScheme()+"://127.0.0.1:1234"));
        client.addNode(new URI(client.getUriScheme()+"://127.0.0.1:1235"));
        assertEquals(2, client.getNodeSize());
        assertNull(client.getAndTouch("mykey", 10));
    }


    @Test(expected = CacheClientIOException.class)
    public void testGetIOException() throws Exception {
        AbstractCacheClient client = getCacheClient(null);
        client.addNode(new URI(client.getUriScheme()+"://127.0.0.1:1234"));
        client.addNode(new URI(client.getUriScheme()+"://127.0.0.1:1235"));
        assertEquals(2, client.getNodeSize());
        client.suspendNode(0);
        client.suspendNode(1);
        assertNull(client.get("mykey"));
    }

    @Test
    public void testGetCacheNodeException() throws Exception {
        AbstractCacheClient client = getCacheClient(null);
        client.addNode(new URI(client.getUriScheme()+"://127.0.0.1:1233"));
        client.addNode(new URI(client.getUriScheme()+"://127.0.0.1:1234"));
        client.addNode(new MockCacheNode(new URI(client.getUriScheme()+"://127.0.0.1:1235")){
            @Override
            public byte[] get(String key) throws CacheNodeException {
                throw new CacheNodeException("CacheNodeException", null);
            }
        });
        assertEquals(3, client.getNodeSize());
        String key = null;
        while (true) {
            key = TestUtils.randomString(10);
            if (client.getHashAveilableNodeIndex(key) == 2)
                break;
        }
        assertNull(client.get(key));
        assertTrue(client.isSusspended(2));
    }

    @Test
    public void testGetException() throws Exception {
        AbstractCacheClient client = getCacheClient(null);
        client.addNode(new URI(client.getUriScheme()+"://127.0.0.1:1233"));
        client.addNode(new URI(client.getUriScheme()+"://127.0.0.1:1234"));
        client.addNode(new MockCacheNode(new URI(client.getUriScheme()+"://127.0.0.1:1235")){
            @Override
            public byte[] get(String key) throws CacheNodeException {
                throw new RuntimeException("Exception");
            }
        });
        assertEquals(3, client.getNodeSize());
        String key = null;
        while (true) {
            key = TestUtils.randomString(10);
            if (client.getHashAveilableNodeIndex(key) == 2)
                break;
        }
        assertNull(client.get(key));
        assertFalse(client.isSusspended(2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetNullKey() throws Exception {
        AbstractCacheClient client = getCacheClient(null);
        client.addNode(new URI(client.getUriScheme()+"://127.0.0.1:1233"));
        String key = null;
        client.get(key);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetEmptyKey() throws Exception {
        AbstractCacheClient client = getCacheClient(null);
        client.addNode(new URI(client.getUriScheme()+"://127.0.0.1:1233"));
        String key = "";
        client.get(key);
    }

    @Test
    public void testSet() throws Exception {
        AbstractCacheClient client = getCacheClient(null);
        client.addNode(new URI(client.getUriScheme()+"://127.0.0.1:1233"));
        client.addNode(new URI(client.getUriScheme()+"://127.0.0.1:1234"));
        client.addNode(new MockCacheNode(new URI(client.getUriScheme()+"://127.0.0.1:1235")){
            @Override
            public boolean set(String key, byte[] data, int expiration) throws CacheNodeException {
                throw new CacheNodeException("CacheNodeException", null);
            }
        });
        assertEquals(3, client.getNodeSize());
        String key = null;
        while (true) {
            key = TestUtils.randomString(10);
            if (client.getHashAveilableNodeIndex(key) == 2)
                break;
        }
        assertTrue(client.set(key, "testdata".getBytes(), 10));

    }

    @Test
    public void testSetRuntimExcepton() throws Exception {
        AbstractCacheClient client = getCacheClient(null);
        client.addNode(new URI(client.getUriScheme()+"://127.0.0.1:1233"));
        client.addNode(new URI(client.getUriScheme()+"://127.0.0.1:1234"));
        client.addNode(new MockCacheNode(new URI(client.getUriScheme()+"://127.0.0.1:1235")){
            @Override
            public boolean set(String key, byte[] data, int expiration) throws CacheNodeException {
                throw new RuntimeException("CacheNodeException");
            }
        });
        assertEquals(3, client.getNodeSize());
        String key = null;
        while (true) {
            key = TestUtils.randomString(10);
            if (client.getHashAveilableNodeIndex(key) == 2)
                break;
        }
        assertFalse(client.set(key, "testdata".getBytes(), 10));
    }

    @Test(expected = CacheClientIOException.class)
    public void testSetCacheClientIOException() throws Exception {
        AbstractCacheClient client = getCacheClient(null);
        client.addNode(new URI(client.getUriScheme()+"://127.0.0.1:1233"));
        client.addNode(new URI(client.getUriScheme()+"://127.0.0.1:1234"));
        assertEquals(2, client.getNodeSize());
        client.suspendNode(0);
        client.suspendNode(1);
        assertFalse(client.set("mykey", "testdata".getBytes(), 10));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetNullKey() throws Exception {
        AbstractCacheClient client = getCacheClient(null);
        client.addNode(new URI(client.getUriScheme()+"://127.0.0.1:1233"));
        String key = null;
        client.set(key, "123".getBytes(), 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetEmptyKey() throws Exception {
        AbstractCacheClient client = getCacheClient(null);
        client.addNode(new URI(client.getUriScheme()+"://127.0.0.1:1233"));
        String key = "";
        client.set(key, "123".getBytes(), 10);
    }


    @Test
    public void testDelete() throws Exception {
        AbstractCacheClient client = getCacheClient(null);
        client.addNode(new URI(client.getUriScheme()+"://127.0.0.1:1233"));
        client.addNode(new URI(client.getUriScheme()+"://127.0.0.1:1234"));
        client.addNode(new MockCacheNode(new URI(client.getUriScheme()+"://127.0.0.1:1235")){
            @Override
            public boolean remove(String key) throws CacheNodeException {
                throw new CacheNodeException("CacheNodeException", null);
            }
        });
        assertEquals(3, client.getNodeSize());
        String key = null;
        while (true) {
            key = TestUtils.randomString(10);
            if (client.getHashAveilableNodeIndex(key) == 2)
                break;
        }
        assertTrue(client.delete(key));

    }

    @Test
    public void testDeleteRuntimExcepton() throws Exception {
        AbstractCacheClient client = getCacheClient(null);
        client.addNode(new URI(client.getUriScheme()+"://127.0.0.1:1233"));
        client.addNode(new URI(client.getUriScheme()+"://127.0.0.1:1234"));
        client.addNode(new MockCacheNode(new URI(client.getUriScheme()+"://127.0.0.1:1235")){
            @Override
            public boolean remove(String key) throws CacheNodeException {
                throw new RuntimeException("CacheNodeException");
            }
        });
        assertEquals(3, client.getNodeSize());
        String key = null;
        while (true) {
            key = TestUtils.randomString(10);
            if (client.getHashAveilableNodeIndex(key) == 2)
                break;
        }
        assertFalse(client.delete(key));
    }

    @Test(expected = CacheClientIOException.class)
    public void testDeleteCacheClientIOException() throws Exception {
        AbstractCacheClient client = getCacheClient(null);
        client.addNode(new URI(client.getUriScheme()+"://127.0.0.1:1233"));
        client.addNode(new URI(client.getUriScheme()+"://127.0.0.1:1234"));
        assertEquals(2, client.getNodeSize());
        client.suspendNode(0);
        client.suspendNode(1);
        assertFalse(client.delete("mykey"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteNullKey() throws Exception {
        AbstractCacheClient client = getCacheClient(null);
        client.addNode(new URI(client.getUriScheme()+"://127.0.0.1:1233"));
        String key = null;
        client.delete(key);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDeleteEmptyKey() throws Exception {
        AbstractCacheClient client = getCacheClient(null);
        client.addNode(new URI(client.getUriScheme()+"://127.0.0.1:1233"));
        String key = "";
        client.delete(key);
    }


    @Test
    public void testClean() throws Exception {
        AbstractCacheClient client = getCacheClient(null);
        client.addNode(new URI(client.getUriScheme()+"://127.0.0.1:1233"));
        client.addNode(new URI(client.getUriScheme()+"://127.0.0.1:1234"));
        client.addNode(new MockCacheNode(new URI(client.getUriScheme()+"://127.0.0.1:1235")){
            @Override
            public void clean() throws CacheNodeException {
                throw new CacheNodeException("CacheNodeException", null);
            }
        });
        assertEquals(3, client.getNodeSize());
        client.clean();
    }

    @Test
    public void testCleanRuntimException() throws Exception {
        AbstractCacheClient client = getCacheClient(null);
        client.addNode(new URI(client.getUriScheme()+"://127.0.0.1:1233"));
        client.addNode(new URI(client.getUriScheme()+"://127.0.0.1:1234"));
        client.addNode(new MockCacheNode(new URI(client.getUriScheme()+"://127.0.0.1:1235")){
            @Override
            public void clean() throws CacheNodeException {
                throw new RuntimeException("RuntimeException");
            }
        });
        assertEquals(3, client.getNodeSize());
        client.clean();
    }

    @Test(expected = CacheClientIOException.class)
    public void testCleanCacheClientIOException() throws Exception {
        AbstractCacheClient client = getCacheClient(null);
        client.addNode(new URI(client.getUriScheme()+"://127.0.0.1:1233"));
        client.addNode(new URI(client.getUriScheme()+"://127.0.0.1:1234"));
        assertEquals(2, client.getNodeSize());
        client.suspendNode(0);
        client.suspendNode(1);
        client.clean();
    }

    @Test
    public void testGetSize() throws Exception {
        AbstractCacheClient client = getCacheClient(null);
        client.addNode(new URI(client.getUriScheme()+"://127.0.0.1:1233"));
        client.addNode(new MockCacheNode(new URI(client.getUriScheme()+"://127.0.0.1:1234")){
            @Override
            public int getKeyCount(String prefix) throws CacheNodeException {
                throw new CacheNodeException("CacheNodeException", null);
            }
        });
        client.addNode(new MockCacheNode(new URI(client.getUriScheme()+"://127.0.0.1:1235")){
            @Override
            public int getKeyCount(String prefix) throws CacheNodeException {
                throw new RuntimeException("RuntimeException");
            }
        });

        assertEquals(3, client.getNodeSize());
        assertEquals(0, client.getSize());

    }

    @Test(expected = CacheClientIOException.class)
    public void testGetSizeCacheClientIOException() throws Exception {
        AbstractCacheClient client = getCacheClient(null);
        client.addNode(new URI(client.getUriScheme()+"://127.0.0.1:1233"));
        client.addNode(new URI(client.getUriScheme()+"://127.0.0.1:1234"));
        assertEquals(2, client.getNodeSize());
        client.suspendNode(0);
        client.suspendNode(1);
        client.getSize();
    }


    @Test
    public void testGetKeys() throws Exception {
        AbstractCacheClient client = getCacheClient(null);
        client.addNode(new URI(client.getUriScheme()+"://127.0.0.1:1233"));
        client.addNode(new MockCacheNode(new URI(client.getUriScheme()+"://127.0.0.1:1234")){
            @Override
            public List<String> getKeys(String prefix) throws CacheNodeException {
                throw new CacheNodeException("CacheNodeException", null);
            }
        });
        client.addNode(new MockCacheNode(new URI(client.getUriScheme()+"://127.0.0.1:1235")){
            @Override
            public List<String> getKeys(String prefix) throws CacheNodeException {
                throw new RuntimeException("RuntimeException");
            }
        });
        assertEquals(3, client.getNodeSize());
        assertEquals(0, client.getKeys().size());

    }

    @Test(expected = CacheClientIOException.class)
    public void testGetKeysCacheClientIOException() throws Exception {
        AbstractCacheClient client = getCacheClient(null);
        client.addNode(new URI(client.getUriScheme()+"://127.0.0.1:1233"));
        client.addNode(new URI(client.getUriScheme()+"://127.0.0.1:1234"));
        assertEquals(2, client.getNodeSize());
        client.suspendNode(0);
        client.suspendNode(1);
        client.getKeys();
    }

    @Test
    public void testRemoveAllNode() throws Exception {
        AbstractCacheClient client = getCacheClient(null);
        client.addNode(new URI(client.getUriScheme()+"://127.0.0.1:1233"));
        client.addNode(new URI(client.getUriScheme()+"://127.0.0.1:1234"));
        assertEquals(2, client.getNodeSize());
        client.removeAllNode();
        assertEquals(0, client.getNodeSize());
    }

    @Test
    public void testResumeNode() throws Exception {
        AbstractCacheClient client = getCacheClient(null);
        client.addNode(new URI(client.getUriScheme()+"://127.0.0.1:1233"));
        client.addNode(new URI(client.getUriScheme()+"://127.0.0.1:1234"));
        client.addNode(new URI(client.getUriScheme()+"://127.0.0.1:1235"));
        client.addNode(new URI(client.getUriScheme()+"://127.0.0.1:1236"));
        assertEquals(4, client.getNodeSize());
        String key = null;
        int suspendedIndex = 2;
        while (true) {
            key = TestUtils.randomString(10);
            if (client.getHashAveilableNodeIndex(key) == suspendedIndex)
                break;
        }
        client.suspendNode(suspendedIndex);
        assertNotEquals(2, client.getHashAveilableNodeIndex(key));
        client.resumeNode(suspendedIndex);
        assertEquals(2, client.getHashAveilableNodeIndex(key));
    }

    @Test
    public void testBackgroundWork() throws Exception {
        AbstractCacheClient client = getCacheClient(null);
        client.addNode(new MockCacheNode(new URI(client.getUriScheme()+"://127.0.0.1:1234")){
            @Override
            public byte[] get(String key) throws CacheNodeException {
                return null;
            }

            @Override
            public boolean set(String key, byte[] data, int expiration) throws CacheNodeException {
                throw new RuntimeException("RuntimeException");
            }
        });
        client.addNode(new MockCacheNode(new URI(client.getUriScheme()+"://127.0.0.1:1235")){
            @Override
            public byte[] get(String key) throws CacheNodeException {
                throw new RuntimeException("RuntimeException");
            }
            @Override
            public boolean set(String key, byte[] data, int expiration) throws CacheNodeException {
                return true;
            }
        });

        client.addNode(new MockCacheNode(new URI(client.getUriScheme()+"://127.0.0.1:1236")){
            private HashMap<String, byte[]> store = new HashMap<String, byte[]>();
            @Override
            public byte[] get(String key) throws CacheNodeException {
                return store.get(key);
            }

            @Override
            public boolean set(String key, byte[] data, int expiration) throws CacheNodeException {
                store.put(key, data);
                return true;
            }
        });
        String key = TestUtils.randomString(100);
        assertEquals(3, client.getNodeSize());
        client.suspendNode(0);
        client.suspendNode(1);
        client.suspendNode(2);
        assertEquals(-1, client.getHashAveilableNodeIndex(key));
        client.backgroundWork();
        assertEquals(2, client.getHashAveilableNodeIndex(key));
    }
}
