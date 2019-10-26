package org.nuxeo.micro.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SharedMetricRegistries;

public class MetricsRegistry {

    protected static final MetricRegistry registry = SharedMetricRegistries
            .getOrCreate(MetricsRegistry.class.getName());


    public static final MetricRegistry get() {
        return registry;
    }

    private MetricsRegistry() {
        // TODO Auto-generated constructor stub
    }
}
