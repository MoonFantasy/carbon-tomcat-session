package jz.carbon.tomcat.sesssion.store;

import org.apache.tomcat.util.codec.binary.Base64;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.net.URI;
import java.util.Map;

/**
 * Created by jack on 2017/1/3.
 */
public class JedisCacheNode implements IFCacheNode {

    private JedisPool jedisPool;
    private URI uri;

    private JedisPool getJedisPool() {
        if (jedisPool == null) {
            JedisPoolConfig conf = new JedisPoolConfig();
            jedisPool = new JedisPool(conf, uri);
        }
        return jedisPool;
    }

    private Jedis getJedis() throws CacheNodeException {
        try {
            return getJedisPool().getResource();
        } catch (Exception e) {
            throw new CacheNodeException(e);
        }
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public URI getUri() {
        return this.uri;
    }

    public byte[] get(String key) throws CacheNodeException {
        return getAndTouch(key, -1);
    }

    public byte[] getAndTouch(String key, int expiration) throws CacheNodeException {
        Jedis jedis = getJedis();
        String resultStr = jedis.get(key);
        if (resultStr.compareTo("nil") != 0) {
            jedis.close();
            return null;
        }
        if (expiration >= 0) {
            jedis.expire(key, expiration);
        }
        jedis.close();
        byte[] result = Base64.decodeBase64(resultStr);
        return result;
    }

    public boolean set(String key, byte[] data, int expiration) throws CacheNodeException {
        Jedis jedis = getJedis();
        jedis.set(key, Base64.encodeBase64String(data), "", "",expiration);
        return true;
    }

    public boolean remove(String key) throws CacheNodeException {
        return true;
    }

    public void clean() throws CacheNodeException {

    }

    public Map<String, String> getStats(String prefix) throws CacheNodeException {
        return null;
    }
}
