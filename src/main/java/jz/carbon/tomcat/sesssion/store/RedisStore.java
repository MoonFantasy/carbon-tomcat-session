package jz.carbon.tomcat.sesssion.store;

import org.apache.catalina.Session;
import org.apache.catalina.session.StoreBase;

import java.io.IOException;

/**
 * Created by jack on 2016/12/31.
 */
public class JedisStore extends AbstractCacheStore {

    public JedisStore() {

    }

    protected IFCacheClient createNewCacheClient() {
        return null;
    }

    public int getSize() throws IOException {
        return 0;
    }

    public String[] keys() throws IOException {
        return new String[0];
    }

    public Session load(String id)
            throws ClassNotFoundException, IOException {
        return null;
    }

    public void remove(String id) throws IOException {

    }

    public void clear() throws IOException {

    }

    public void save(Session session) throws IOException {

    }

}
