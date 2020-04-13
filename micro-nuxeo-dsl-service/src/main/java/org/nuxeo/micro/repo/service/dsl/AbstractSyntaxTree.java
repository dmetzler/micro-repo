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
package org.nuxeo.micro.repo.service.dsl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

@DataObject
public class AbstractSyntaxTree implements Map<String, Object> {

    private Map<String, Object> ast;

    public AbstractSyntaxTree(Map<String, Object> ast) {
        this.ast = ast;
    }

    public AbstractSyntaxTree(JsonObject json) {

        ast = new HashMap<>();

        byte[] data = Base64.getDecoder().decode(json.getString("value"));

        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
                ObjectInputStream ois = new ObjectInputStream(bais)) {
            ast = (Map<String, Object>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalArgumentException("Unable to deserialize JSON", e);
        }
    }

    public JsonObject toJson() {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(baos)) {

            out.writeObject(ast);
            String b64sm = Base64.getEncoder().encodeToString(baos.toByteArray());
            return new JsonObject().put("type", ast.getClass().getCanonicalName()).put("value", b64sm);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to serialize AST", e);
        }
    }

    public int size() {
        return ast.size();
    }

    public boolean isEmpty() {
        return ast.isEmpty();
    }

    public boolean containsKey(Object key) {
        return ast.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return ast.containsValue(value);
    }

    public Object get(Object key) {
        return ast.get(key);
    }

    public Object put(String key, Object value) {
        return ast.put(key, value);
    }

    public Object remove(Object key) {
        return ast.remove(key);
    }

    public void putAll(Map<? extends String, ? extends Object> m) {
        ast.putAll(m);
    }

    public void clear() {
        ast.clear();
    }

    public Set<String> keySet() {
        return ast.keySet();
    }

    public Collection<Object> values() {
        return ast.values();
    }

    public Set<Entry<String, Object>> entrySet() {
        return ast.entrySet();
    }

    public boolean equals(Object o) {
        return ast.equals(o);
    }

    public int hashCode() {
        return ast.hashCode();
    }

    public Object getOrDefault(Object key, Object defaultValue) {
        return ast.getOrDefault(key, defaultValue);
    }

    public void forEach(BiConsumer<? super String, ? super Object> action) {
        ast.forEach(action);
    }

    public void replaceAll(BiFunction<? super String, ? super Object, ? extends Object> function) {
        ast.replaceAll(function);
    }

    public Object putIfAbsent(String key, Object value) {
        return ast.putIfAbsent(key, value);
    }

    public boolean remove(Object key, Object value) {
        return ast.remove(key, value);
    }

    public boolean replace(String key, Object oldValue, Object newValue) {
        return ast.replace(key, oldValue, newValue);
    }

    public Object replace(String key, Object value) {
        return ast.replace(key, value);
    }

    public Object computeIfAbsent(String key, Function<? super String, ? extends Object> mappingFunction) {
        return ast.computeIfAbsent(key, mappingFunction);
    }

    public Object computeIfPresent(String key,
            BiFunction<? super String, ? super Object, ? extends Object> remappingFunction) {
        return ast.computeIfPresent(key, remappingFunction);
    }

    public Object compute(String key, BiFunction<? super String, ? super Object, ? extends Object> remappingFunction) {
        return ast.compute(key, remappingFunction);
    }

    public Object merge(String key, Object value,
            BiFunction<? super Object, ? super Object, ? extends Object> remappingFunction) {
        return ast.merge(key, value, remappingFunction);
    }

}
