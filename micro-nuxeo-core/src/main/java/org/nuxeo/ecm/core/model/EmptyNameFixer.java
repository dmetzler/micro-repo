/*
 * (C) Copyright 2013 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     Stephane Lacoin
 */
package org.nuxeo.ecm.core.model;

import org.nuxeo.common.utils.IdUtils;
import org.nuxeo.ecm.core.api.event.CoreEventConstants;
import org.nuxeo.micro.event.DocumentEventContext;
import org.nuxeo.micro.event.Event;
import org.nuxeo.micro.event.EventListener;

public class EmptyNameFixer implements EventListener {

    @Override
    public void handleEvent(Event event) {
        DocumentEventContext context = (DocumentEventContext) event.getContext();
        String name = (String) context.getProperty(CoreEventConstants.DESTINATION_NAME);
        if (name != null && name.length() > 0) {
            return;
        }
        context.setProperty(CoreEventConstants.DESTINATION_NAME, IdUtils.generateStringId());
    }

}
