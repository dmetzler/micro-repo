package org.nuxeo.micro.event;

/**
 * Event stats service. Montoring / management frameworks may implement this interface and register it as a nuxeo
 * service to log event stats.
 *
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 */
public interface EventStats {

    void logAsyncExec(EventListenerDescriptor listener, long delta);

    void logSyncExec(EventListenerDescriptor listener, long delta);

}
