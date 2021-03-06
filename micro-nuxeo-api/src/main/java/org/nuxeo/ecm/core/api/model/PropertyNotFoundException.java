/*
 * (C) Copyright 2006-2015 Nuxeo SA (http://nuxeo.com/) and others.
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
 *     Bogdan Stefanescu
 *     Florent Guillaume
 */
package org.nuxeo.ecm.core.api.model;

/**
 * Exception indicating a property not found.
 */
public class PropertyNotFoundException extends PropertyException {

    private static final long serialVersionUID = 1L;

    protected final String detail;

    public PropertyNotFoundException(String path) {
        super(path);
        detail = null;
    }

    public PropertyNotFoundException(String path, String detail) {
        super(path);
        addInfo(detail);
        this.detail = detail;
    }

    public String getPath() {
        return getOriginalMessage();
    }

    public String getDetail() {
        return detail;
    }

}
