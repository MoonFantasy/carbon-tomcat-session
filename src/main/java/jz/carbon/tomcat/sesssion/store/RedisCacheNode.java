package jz.carbon.tomcat.sesssion.store;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisDataException;

import java.net.URI;
import java.util.*;

/**
 * Created by jack on 2017/1/3.
 */
public class RedisCacheNode implements IFCacheNode {

    private static final Log log = LogFactory.getLog(RedisCacheNode.class);

    public final static int DEFAULT_MAX_POOL = 15;
    public final static int DEFAULT_TIMEOUT = 3000;
    private URI uri;
    private JedisPool jedisPool;
    private int maxPool = DEFAULT_MAX_POOL;
    /**
     * milliseconds
     */
    private int timeout = DEFAULT_TIMEOUT;
    private Class poolClass = JedisPool.class;

    protected JedisPool getJedisPool() {
        if (jedisPool == null) {
            try {
                JedisPoolConfig poolConfig = new JedisPoolConfig();
                poolConfig.setMaxTotal(maxPool);
                jedisPool = (JedisPool) poolClass
                        .getConstructor(GenericObjectPoolConfig.class, URI.class, int.class)
                        .newInstance(poolConfig, uri, timeout);
            } catch (Exception e) {
                log.error(e);
            }
        }
        return jedisPool;
    }

    private boolean isJedisPoolClass(Class poolClass) {
        if (poolClass == null)
            return false;

        if (poolClass.getName().compareTo(JedisPool.class.getName()) == 0)
            return true;

        return isJedisPoolClass(poolClass.getSuperclass());
    }

    public void setPoolClass(Class poolClass) {
        if (isJedisPoolClass(poolClass))
            this.poolClass = poolClass;
        log.error("Invalid JedisPool class " + poolClass.getName());
    }

    public void setJedisPool(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    public RedisCacheNode(URI uri) {
        this.uri = uri;
    }

    public void setMaxPool(int maxPool) {
        this.maxPool = maxPool > 0 ? maxPool : DEFAULT_MAX_POOL;
    }

    public void setTimeout(int connTimeout) {
        this.timeout = connTimeout > 0 ? connTimeout : DEFAULT_TIMEOUT;
    }

    protected Jedis getJedis() throws CacheNodeException {
        try {
            return getJedisPool().getResource();
        } catch (Exception e) {
            throw new CacheNodeException(e);
        }
    }
    private void closeJedis(Jedis jedis) {
        if (jedis == null)
            return;
        jedis.close();
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    public URI getUri() {
        return this.uri;
    }

    public byte[] get(String key) throws CacheNodeException {
        return getAntTouch(key, -1);
    }

    public byte[] getAntTouch(String key, int expiration) throws CacheNodeException {
        Jedis jedis = getJedis();
        byte[] result = null;
        try {
            result = jedis.get(key.getBytes());
            if (expiration > 0) {
                jedis.expire(key.getBytes(), expiration);
            }
        } catch (JedisDataException e) {
            log.error("Jedis Data Exception '" + key + "' " + e.getMessage(), e);
        } catch (Exception e) {
            throw new CacheNodeException(e);
        } finally {
            closeJedis(jedis);
        }
        return result;
    }

    public boolean set(String key, byte[] data, int expiration) throws CacheNodeException {
        Jedis jedis = getJedis();
        boolean result = false;
        String resultStr;
        try {
            resultStr = jedis.setex(key.getBytes(), expiration, data);
            if (resultStr != null && resultStr.compareTo("OK") == 0) {
                result = true;
            }
        } catch (JedisDataException e) {
            log.error("Jedis Data Exception on set '" + key + "' " + e.getMessage(), e);
        } catch (Exception e) {
            throw new CacheNodeException(e);
        } finally {
            closeJedis(jedis);
        }
        return result;
    }

    public boolean remove(String key) throws CacheNodeException {
        Jedis jedis = getJedis();
        boolean result = false;
        try {
            result = jedis.del(key.getBytes()) == 1;
        } catch (JedisDataException e) {
            log.error("Jedis Data Exception on remove " + e.getMessage(), e);
        } catch (Exception e) {
            throw new CacheNodeException(e);
        } finally {
            closeJedis(jedis);
        }
        return result;
    }

    public void clean() throws CacheNodeException {
        Jedis jedis = getJedis();
        String resultStr;
        try {
            resultStr = jedis.flushDB();
            log.debug("Flush DB result " + resultStr);
        } catch (JedisDataException e) {
            log.error("Jedis Data Exception on dlean " + e.getMessage(), e);
        } catch (Exception e) {
            throw new CacheNodeException(e);
        } finally {
            closeJedis(jedis);
        }
    }

    public Map<String, String> getStats(String section) throws CacheNodeException {
        Jedis jedis = getJedis();
        String info = "";
        try {
            if (section == null)
                info = jedis.info();
            else
                info = jedis.info(section);
        } catch (JedisDataException e) {
            log.error("Jedis Data Exception on getStats " + e.getMessage(), e);
        } catch (Exception e) {
            throw new CacheNodeException(e);
        } finally {
            closeJedis(jedis);
        }
        HashMap<String, String> map = new HashMap<String, String>();
        String[] infos = info.split("\n");
        for (int i = 0; i < infos.length; i++) {
            int index = infos[i].trim().indexOf(":");
            if (index > 0) {
                map.put(infos[i].trim().substring(0, index), infos[i].trim().substring(index + 1));
            }
        }
        return map;
    }

    public int getKeyCount(String keyPrefix) throws CacheNodeException {
        Jedis jedis = getJedis();
        String script = "return table.getn(redis.call(\"keys\", \"" + (keyPrefix == null ? "" : keyPrefix) + "*\"))";
        Object result = null;
        try {
            result = jedis.eval(script);
        } catch (JedisDataException e) {
            log.error("Jedis Data Exception on getStats " + e.getMessage(), e);
        } catch (Exception e) {
            throw new CacheNodeException(e);
        } finally {
            closeJedis(jedis);
        }
        if (result == null)
            return 0;

        if (result instanceof Number)
            return (Integer) result;

        try {
            return new Integer(result.toString());
        } catch (Exception e) {
            log.error("getKeyCount convert to integer fail ", e);
        }
        return 0;
    }

    public List<String> getKeys(String keyPrefix) throws CacheNodeException {
        Jedis jedis = getJedis();
        Set<String> keys = null;
        try {
            keys = jedis.keys(((keyPrefix == null ? "" : keyPrefix) + "*"));
        } catch (JedisDataException e) {
            log.error("Jedis Data Exception on getStats " + e.getMessage(), e);
        } catch (Exception e) {
            throw new CacheNodeException(e);
        } finally {
            closeJedis(jedis);
        }
        ArrayList<String> result = new ArrayList<String>();
        if (keys != null)
            result.addAll(keys);
        return result;
    }

    public String toString() {
        return getClass().getSimpleName() + "[" + uri.getHost() + ":" + uri.getPort() + "]";
    }

    public void close() {
        if (jedisPool != null) {
            jedisPool.close();
            jedisPool = null;
        }
    }

}
