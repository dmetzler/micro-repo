/*
 * (C) Copyright 2006-2016 Nuxeo SA (http://nuxeo.com/) and others.
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
 */
package org.nuxeo.ecm.core.api.security;

/**
 * @author Bogdan Stefanescu
 */
// TODO: make it a constant utility class instead of an interface.
public interface SecurityConstants {

    String SYSTEM_USERNAME = "System";

    /**
     * @deprecated since 5.3.1 administrator user names are configurable on user manager
     * Too many references to this constant, no clean for LTS 2017
     */
    @Deprecated
    String ADMINISTRATOR = "Administrator";

    /**
     * @deprecated since 5.3.1 anonymous user name is configurable on user manager
     * Too many references to this constant, no clean for LTS 2017
     */
    @Deprecated
    String ANONYMOUS = "anonymous";

    /**
     * @deprecated since 5.3.1 administrators groups are configurable on user manager
     * Too many references to this constant, no clean for LTS 2017
     */
    @Deprecated
    String ADMINISTRATORS = "administrators";

    /**
     * @deprecated since 5.3.1 default group is configurable on user manager
     * Too many references to this constant, no clean for LTS 2017
     */
    @Deprecated
    String MEMBERS = "members";

    String EVERYONE = "Everyone";

    String EVERYTHING = "Everything";

    String RESTRICTED_READ = "RestrictedRead";

    String READ = "Read";

    String WRITE = "Write";

    String READ_WRITE = "ReadWrite";

    String REMOVE = "Remove";

    String VERSION = "Version";

    String READ_VERSION = "ReadVersion";

    String WRITE_VERSION = "WriteVersion";

    String BROWSE = "Browse";

    String WRITE_SECURITY = "WriteSecurity";

    String READ_SECURITY = "ReadSecurity";

    String READ_PROPERTIES = "ReadProperties";

    String WRITE_PROPERTIES = "WriteProperties";

    String READ_CHILDREN = "ReadChildren";

    String ADD_CHILDREN = "AddChildren";

    String REMOVE_CHILDREN = "RemoveChildren";

    String READ_LIFE_CYCLE = "ReadLifeCycle";

    String WRITE_LIFE_CYCLE = "WriteLifeCycle";

    String MANAGE_WORKFLOWS = "ManageWorkflows";

    String VIEW_WORKLFOW = "ReviewParticipant";

    String UNLOCK = "Unlock";

    /**
     * Flag that can be used as principal to mark an unsupported ACL.
     */
    String UNSUPPORTED_ACL = "_UNSUPPORTED_ACL_";

}
