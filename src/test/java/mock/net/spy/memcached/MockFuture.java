package mock.net.spy.memcached;

import java.util.concurrent.*;

/**
 * Created by jack on 2016/12/28.
 */
public class MockFuture<T> implements Future<T>{
    private T obj;
    private int sw = 0;
    public MockFuture(T obj, int sw) {
        this.obj = obj;
        this.sw = sw;
    }
    public boolean cancel(boolean mayInterruptIfRunning){
        return true;
    }
    public boolean isCancelled() {
        return false;
    }
    public boolean isDone() {
        return true;

    }
    public T get() throws InterruptedException, ExecutionException {

        try {
            return get(0, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            return null;
        }

    }

    public T get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException{
        switch (sw) {
            case 3:
                throw new ExecutionException("For Jack Test case ExecutionException", new Exception("for test"));
            case 4:
                throw new InterruptedException("For Jack Test case InterruptedException");
            case 5:
                throw new TimeoutException("For Jack Test case TimeoutException");
            case 6:
                throw new CancellationException("For Jack Test case CancellationException");
        }
        return obj;
    }
}
