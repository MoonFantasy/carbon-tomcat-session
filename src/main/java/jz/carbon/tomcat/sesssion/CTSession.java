package jz.carbon.tomcat.sesssion;

import org.apache.catalina.Manager;
import org.apache.catalina.session.StandardSession;

/**
 * Created by jack on 2016/12/26.
 */
public class CTSession extends StandardSession {
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
}
