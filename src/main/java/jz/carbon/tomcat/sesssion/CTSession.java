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
}
