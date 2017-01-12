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
import java.util.Set;

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
        Set<? extends Map.Entry> entrySet = this.attributes.entrySet();
        for (Map.Entry entry : entrySet) {
            if ((entry.getValue() instanceof Map)) {
                stripMapNonSerilizable((Map) entry.getValue());
            } else if ((entry.getValue() instanceof List)) {
                stripListNonSerilizable((List) entry.getValue());
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
        Set<? extends Map.Entry> entrySet = map.entrySet();
        for (Map.Entry entry : entrySet) {
            if (!isSerializable(entry.getKey())) {
                map.remove(entry.getKey());
                log.debug("Remove from map " + entry.getKey());
            } else if ((entry.getValue() instanceof Map)) {
                stripMapNonSerilizable((Map) entry.getValue());
            } else if ((entry.getValue() instanceof List)) {
                stripListNonSerilizable((List) entry.getValue());
            } else if (!isSerializable(entry.getValue())) {
                map.remove(entry.getKey());
                log.debug("Remove from map " + entry.getKey() + " " + entry.getValue());
            }
        }
    }

    private void stripListNonSerilizable(List list) {
        for (Object obj : list) {
            if ((obj instanceof Map)) {
                stripMapNonSerilizable((Map) obj);
            } else if ((obj instanceof List)) {
                stripListNonSerilizable((List) obj);
            } else if (!isSerializable(obj)) {
                list.remove(obj);
                log.debug("Remove from list " + obj);
            }
        }
    }
}
