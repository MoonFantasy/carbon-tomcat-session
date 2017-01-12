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
    protected String requestUriIgnorePattern = ".*\\.(ico|png|gif|jpg|css|js)$";
    protected String requestIgnoreExcludePattern = "csrfPrevention";
    protected String requestMethodIgnoreExcludePattern = "POST";

    public void setCTSessionPersistentManager(CTSessionPersistentManager manager) {
        this.manager = manager;
    }

    public void setRequestUriIgnorePattern(String pattern) {
        this.requestUriIgnorePattern = pattern;
    }

    public void setRequestIgnoreExcludePattern(String pattern) {
        this.requestIgnoreExcludePattern = pattern;
    }

    public void setRequestMethodIgnoreExcludePattern(String pattern) {
        this.requestMethodIgnoreExcludePattern = pattern;
    }

    public CTSessionPersistentManager getCTSessionPersistentManager() {
        return manager;
    }

    public void invoke(Request request, Response response) throws IOException, ServletException {
        try {
            if (manager != null) {
                if (requestUriIgnorePattern != null && requestUriIgnorePattern.length() > 0) {
                    boolean isIgnore = false;
                    try {
                        Pattern pattern = Pattern.compile(requestUriIgnorePattern, Pattern.CASE_INSENSITIVE);
                        Matcher matcher = pattern.matcher(request.getRequestURI());
                        if (matcher.find())
                            isIgnore = true;

                        if (isIgnore && requestIgnoreExcludePattern != null && requestIgnoreExcludePattern.length() > 0) {
                            Pattern ignoreExcludePattern = Pattern.compile(requestIgnoreExcludePattern, Pattern.CASE_INSENSITIVE);
                            Matcher ignoreExcludeMatcher = ignoreExcludePattern.matcher(request.getRequestURI());
                            if (ignoreExcludeMatcher.find())
                                isIgnore = false;
                        }

                        if (isIgnore && requestMethodIgnoreExcludePattern != null && requestMethodIgnoreExcludePattern.length() > 0) {
                            Pattern methodIgnoreExcludePattern = Pattern.compile(requestMethodIgnoreExcludePattern, Pattern.CASE_INSENSITIVE);
                            Matcher methodIgnoreExcludeMatcher = methodIgnoreExcludePattern.matcher(request.getMethod());
                            if (methodIgnoreExcludeMatcher.find())
                                isIgnore = false;
                        }

                    } catch (IllegalArgumentException e) {
                        log.warn("Wrong pattern format ", e);
                    } catch (Exception e) {
                        log.warn(e);
                    } finally {
                        if (isIgnore && log.isDebugEnabled())
                            log.debug("Ignore Session Request " + request.getRequestURI() + " thread id: " + Thread.currentThread().getId());
                        manager.setCurrentIgnore(isIgnore);
                    }
                }
            }
            if(getNext() != null)
                getNext().invoke(request, response);
        } finally {
            if (manager != null) {
                manager.afterRequest(request, response);
            }

        }
    }
}
