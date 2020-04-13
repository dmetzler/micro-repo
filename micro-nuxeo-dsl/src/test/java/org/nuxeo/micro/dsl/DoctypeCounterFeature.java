/*
 * (C) Copyright 2020 Nuxeo (http://nuxeo.com/) and others.
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
 *     dmetzler
 */
package org.nuxeo.micro.dsl;

import java.util.List;
import java.util.Map;

import org.nuxeo.micro.dsl.features.DslFeature;

public class DoctypeCounterFeature implements DslFeature {

    int count = 0;

    @Override
    public void visit(DslModel model, Map<String, Object> ast) {
        if (ast.get("doctypes") != null) {
            count = ((List<Object>) ast.get("doctypes")).size();
        }
    }

    public int getCount() {
        return count;
    }

}
