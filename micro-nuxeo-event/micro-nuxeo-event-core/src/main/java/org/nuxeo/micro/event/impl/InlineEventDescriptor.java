package org.nuxeo.micro.event.impl;

import java.util.Arrays;
import java.util.HashSet;

import org.nuxeo.micro.event.EventListenerDescriptor;

public class InlineEventDescriptor extends EventListenerDescriptor {

    public static Builder builder(Class<?> klass) {
        return new Builder(klass);
    }

    public static class Builder {

        private HashSet<String> events = new HashSet<>();
        private int priority;
        private Class<?> klass;

        public Builder(Class<?> klass) {
            this.klass = klass;

        }

        public Builder on(String... eventNames) {
            events.addAll(Arrays.asList(eventNames));
            return this;
        }

        public Builder withPriority(int priority) {
            this.priority = priority;
            return this;
        }

        public InlineEventDescriptor build() {
            InlineEventDescriptor desc = new InlineEventDescriptor();

            desc.className = klass.getName();
            desc.name = klass.getCanonicalName();
            desc.setEvents(events);
            desc.priority = priority;
            return desc;
        }
    }
}
