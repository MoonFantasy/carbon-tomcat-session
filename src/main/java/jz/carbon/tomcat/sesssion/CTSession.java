package jz.carbon.tomcat.sesssion;

import org.apache.catalina.Manager;
import org.apache.catalina.session.StandardSession;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by jack on 2016/12/26.
 */
public class CTSession extends StandardSession {
    private static final Log log = LogFactory.getLog(CTSession.class);

    public CTSession(Manager manager) {
        super(manager);
    }

    public String getInfo() {
        return getClass().getSimpleName() + "/1.0";
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append("[");
        sb.append(id);
        sb.append("]");
        return (sb.toString());
    }

    protected void writeObject(ObjectOutputStream stream)
            throws IOException {
        String[] keys = keys();
        for (String key : keys) {
            if (attributes.get(key) instanceof Map) {
                stripMapNonSerilizable((Map) attributes.get(key));
            } else if (attributes.get(key) instanceof List) {
                stripListNonSerilizable((List) attributes.get(key));
            }
        }
        super.writeObject(stream);
    }

    private boolean isSerializable(Object obj) {
        if ((obj instanceof String)) {
            return true;
        }
        if (obj.getClass().isArray()) {
            return true;
        }
        if ((obj instanceof Enum)) {
            return true;
        }
        return (obj instanceof Serializable);
    }

    private void stripMapNonSerilizable(Map map) {
        Object[] keys = map.keySet().toArray();
        for (Object key : keys) {
            if (!isSerializable(key)) {
                if (log.isDebugEnabled())
                    log.debug("Remove from map " + key);
                map.remove(key);
            } else if ((map.get(key) instanceof Map)) {
                stripMapNonSerilizable((Map) map.get(key));
            } else if ((map.get(key) instanceof List)) {
                stripListNonSerilizable((List) map.get(key));
            } else if (!isSerializable(map.get(key))) {
                if (log.isDebugEnabled())
                    log.debug("Remove from map " + key + " " + map.get(key));
                map.remove(key);
            }
        }


    }

    private void stripListNonSerilizable(List list) {
        Object[] objs = list.toArray();
        for (Object obj : objs) {
            if ((obj instanceof Map)) {
                stripMapNonSerilizable((Map) obj);
            } else if ((obj instanceof List)) {
                stripListNonSerilizable((List) obj);
            } else if (!isSerializable(obj)) {
                if (log.isDebugEnabled())
                    log.debug("Remove from list " + obj);
                list.remove(obj);
            }
        }
    }
}
