package org.nuxeo.ecm.core.api;

public interface CoreSessionProvider {

    /**
     * Gets an existing open session for the given session id.
     * <p>
     * The returned {@link CoreSession} must not be closed, as it is owned by someone else.
     *
     * @param sessionId the session id
     * @return the session, which must not be closed
     */
    CoreSession getCoreSession(String sessionId);
}
