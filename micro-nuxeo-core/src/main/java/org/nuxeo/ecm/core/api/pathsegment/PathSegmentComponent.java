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
 *     Florent Guillaume
 */
package org.nuxeo.ecm.core.api.pathsegment;

import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoException;

/**
 * Central service for the generation of a path segment for a document.
 */
public class PathSegmentComponent implements PathSegmentService {

    private static final Log log = LogFactory.getLog(PathSegmentComponent.class);

    public static final String XP = "pathSegmentService";

    protected LinkedList<Class<? extends PathSegmentService>> contribs;

    protected PathSegmentService service;

    protected boolean recompute;

    public PathSegmentComponent() {
        contribs = new LinkedList<Class<? extends PathSegmentService>>();
        recompute = true;
        service = null;
    }

    @Override
    public String generatePathSegment(DocumentModel doc) {
        if (recompute) {
            recompute();
            recompute = false;
        }
        return service.generatePathSegment(doc);
    }

    protected void recompute() {
        Class<? extends PathSegmentService> klass;
        if (contribs.isEmpty()) {
            klass = PathSegmentServiceDefault.class;
        } else {
            klass = contribs.getLast();
        }
        if (service == null || klass != service.getClass()) {
            try {
                service = klass.newInstance();
            } catch (ReflectiveOperationException e) {
                throw new NuxeoException(e);
            }
        } // else keep old service instance
    }

    @Override
    public String generatePathSegment(String s) {
        if (recompute) {
            recompute();
            recompute = false;
        }
        return service.generatePathSegment(s);
    }

    @Override
    public int getMaxSize() {
        if (recompute) {
            recompute();
            recompute = false;
        }
        return service.getMaxSize();
    }
}
