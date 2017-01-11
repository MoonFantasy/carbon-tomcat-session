package mock.jz;

import org.apache.catalina.Valve;
import org.apache.catalina.comet.CometEvent;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * Created by jack on 2017/1/11.
 */
public class DummyValve implements Valve {
    public String getInfo() {
        return getClass().getSimpleName() + "/1.0";
    }

    public Valve getNext() {
        return null;
    }

    public void setNext(Valve valve) {
    }

    public void backgroundProcess() {
    }

    public void invoke(Request request, Response response)
            throws IOException, ServletException {
    }

    public void event(Request request, Response response, CometEvent event)
            throws IOException, ServletException {
    }

    public boolean isAsyncSupported() {
        return false;
    }
}