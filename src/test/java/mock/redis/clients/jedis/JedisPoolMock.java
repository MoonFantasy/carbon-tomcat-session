package mock.redis.clients.jedis;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.net.URI;

/**
 * Created by jack on 2017/1/6.
 */
public class JedisPoolMock extends JedisPool {
    public static final int EVAL_NOT_NUMBER_STRING = 5;
    public static final int EVAL_NUMBER_STRING = 4;
    public static final int OTHER_EXCEPTION = 3;
    public static final int DATA_EXCEPTION = 2;
    public static final int CONN_EXCEPTION = 1;
    public static final int NONE = 0;
    private int sw = NONE;
    private JedisMock jedis = null;
    public JedisPoolMock(final GenericObjectPoolConfig poolConfig, final URI uri, final int timeout) {

    }

    public void setSw(int sw) {
        this.sw = sw;
    }

    public Jedis getResource() {
        if (sw == CONN_EXCEPTION)
            throw new JedisConnectionException("JedisConnectionException");

        if (jedis == null) {
            jedis = new JedisMock();
            jedis.setSw(sw);
        }

        return jedis;
    }
}
