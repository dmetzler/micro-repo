
/*
 * (C) Copyright 2006-2011 Nuxeo SA (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Nuxeo - initial API and implementation
 *
 * $Id$
 */
package org.nuxeo.micro.event;

/**
 * Interface for EventService administration
 *
 * @author Thierry Delprat
 */
public interface EventServiceAdmin {

    int getEventsInQueueCount();

    int getActiveThreadsCount();

    boolean isBlockAsyncHandlers();

    void setBlockAsyncHandlers(boolean blockAsyncHandlers);

    boolean isBlockSyncPostCommitHandlers();

    void setBlockSyncPostCommitHandlers(boolean blockSyncPostCommitHandlers);

    EventListenerList getListenerList();

    void setListenerEnabledFlag(String listenerName, boolean enabled);

    boolean isBulkModeEnabled();

    void setBulkModeEnabled(boolean bulkModeEnabled);

}
