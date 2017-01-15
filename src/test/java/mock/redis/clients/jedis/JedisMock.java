package mock.redis.clients.jedis;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisDataException;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by jack on 2017/1/6.
 */
public class JedisMock extends Jedis {
    private  HashMap<String, Pair<byte[], Long>> store = new HashMap<String, Pair<byte[], Long>>();
    private int sw = 0;
    public JedisMock(){

    }

    public void setSw(int sw) {
        this.sw = sw;
    }

    private void throwExcetpion(int sw) {
        switch (sw) {
            case JedisPoolMock.DATA_EXCEPTION:
                throw new JedisDataException("mock data exception");
            case JedisPoolMock.OTHER_EXCEPTION:
                throw new RuntimeException("mock run time exception");
        }
    }

    @Override
    public byte[] get(byte[] key) {
        throwExcetpion(sw);
        long now = System.currentTimeMillis() / 1000;
        String storeKey = String.valueOf(Hex.encodeHex(key));
        Pair<byte[], Long> pair = store.get(storeKey);
        if (pair == null)
            return null;

        if (now > pair.getValue()) {
            store.remove(key);
            return null;
        }
        return pair.getKey();
    }


    @Override
    public Set<String> keys(final String pattern) {
        throwExcetpion(sw);
        Set<String> keys;
        synchronized (store) {
            keys = store.keySet();
        }
        return keys;
    }

    @Override
    public Object eval(String script) {
        throwExcetpion(sw);
        switch (sw) {
            case JedisPoolMock.EVAL_NOT_NUMBER_STRING:
                return new Object();
            case JedisPoolMock.EVAL_NUMBER_STRING:
                return ((Integer)store.size()).toString();
        }
        return store.size();
    }

    @Override
    public Long expire(final byte[] key, final int seconds) {
        throwExcetpion(sw);
        long now = System.currentTimeMillis() / 1000;
        String storeKey = String.valueOf(Hex.encodeHex(key));
        Pair<byte[], Long> pair = store.get(storeKey);
        if (pair == null)
            return 0L;
        synchronized (store) {
            store.remove(key);
            store.put(storeKey, new MutablePair<byte[], Long>(pair.getKey(), now + seconds));
        }

        return 1L;
    }

    @Override
    public String setex(final byte[] key, final int seconds, final byte[] value) {
        throwExcetpion(sw);
        String storeKey = String.valueOf(Hex.encodeHex(key));
        long now = System.currentTimeMillis() / 1000;
        MutablePair<byte[], Long> pair = new MutablePair<byte[], Long>(value, now + seconds);
        synchronized (store) {
            store.put(storeKey, pair);
        }
        return "OK";
    }

    @Override
    public Long del(final byte[] key) {
        throwExcetpion(sw);
        String storeKey = String.valueOf(Hex.encodeHex(key));
        synchronized (store) {
            if (store.containsKey(storeKey)) {
                store.remove(storeKey);
                return 1L;
            }
        }
        return 0L;
    }

    @Override
    public String flushDB() {
        throwExcetpion(sw);
        synchronized (store) {
            store.clear();
        }
        return "OK";
    }

    @Override
    public String info() {
        return info(null);
    }

    @Override
    public String info(final String section) {
        throwExcetpion(sw);
        return "dummy:123\ndummy2:0";
    }
}
