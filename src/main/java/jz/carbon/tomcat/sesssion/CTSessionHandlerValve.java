package jz.carbon.tomcat.sesssion;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by jack on 2016/12/30.
 */
public class CTSessionHandlerValve extends ValveBase {
    private static final Log log = LogFactory.getLog(CTSessionHandlerValve.class);
    protected CTSessionPersistentManager manager = null;
    protected String requestUriIgnorePattern = null;

    public void setCTSessionPersistentManager(CTSessionPersistentManager manager) {
        this.manager = manager;

    }

    public void setSequestUriIgnorePattern(String pattern) {
        this.requestUriIgnorePattern = pattern;
    }

    public CTSessionPersistentManager getCTSessionPersistentManager() {
        return manager;
    }

    public void invoke(Request request, Response response) throws IOException, ServletException {
        try {
            if (manager != null) {
                if (requestUriIgnorePattern != null && requestUriIgnorePattern.length() > 0) {
                    boolean isMatch = false;
                    try {
                        Pattern pattern = Pattern.compile(requestUriIgnorePattern, Pattern.CASE_INSENSITIVE);
                        Matcher matcher = pattern.matcher(request.getRequestURI());
                        if (matcher.find())
                            isMatch = true;
                    } catch (IllegalArgumentException e) {
                        log.warn("Wrong requestUriIgnorePattern format ", e);
                    } catch (Exception e) {
                        log.warn(e);
                    } finally {
                        manager.setCurrentIgnore(isMatch);
                    }
                }
            }
            getNext().invoke(request, response);
        } finally {
            if (manager != null) {
                manager.afterRequest(request, response);
            }

        }
    }
}
