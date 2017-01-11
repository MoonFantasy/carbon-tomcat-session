package mock.redis.clients.jedis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.JedisPool;

import java.net.URI;

/**
 * Created by jack on 2017/1/8.
 */
public class JedisPoolExceptionMock extends JedisPool {
    public JedisPoolExceptionMock(final GenericObjectPoolConfig poolConfig, final URI uri, final int timeout) {
        throw new RuntimeException("JedisPoolExceptionMock");
    }
}