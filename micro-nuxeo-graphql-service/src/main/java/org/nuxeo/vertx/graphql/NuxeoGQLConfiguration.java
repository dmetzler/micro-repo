package org.nuxeo.vertx.graphql;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.nuxeo.micro.repo.service.graphql.NuxeoContext;

import graphql.GraphQL;
import graphql.execution.AsyncExecutionStrategy;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.vertx.core.Promise;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.handler.graphql.VertxDataFetcher;

public class NuxeoGQLConfiguration {

    protected static final Logger log = LoggerFactory.getLogger(NuxeoGQLConfiguration.class);

    private NuxeoGQLConfiguration() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private List<Class<?>> configurationClasses = new ArrayList<>();
        private Object configuration;
        private graphql.schema.idl.RuntimeWiring.Builder runtimeWiring;

        public Builder() {
            runtimeWiring = newRuntimeWiring();
        }

        public Builder runtimeWiring(graphql.schema.idl.RuntimeWiring.Builder runtimeWiring) {
            this.runtimeWiring = runtimeWiring;
            return this;
        }

        public Builder configuration(Class<?> configurationClass) {
            this.configurationClasses.add(configurationClass);
            return this;
        }

        public GraphQLSchema getGraphQLSchema() {
            TypeDefinitionRegistry typeDefinitionRegistry = null;
            SchemaParser schemaParser = new SchemaParser();

            for (Class<?> configurationClass : configurationClasses) {

                try {
                    this.configuration = configurationClass.newInstance();
                } catch (ReflectiveOperationException e) {
                    log.error("Unable to instantiate GQL configuration file", e);
                    continue;
                }

                Schema schemaAnnotation = configurationClass.getAnnotation(Schema.class);
                if (schemaAnnotation != null) {
                    try {
                        URL schemaUrl = this.configuration.getClass().getResource(schemaAnnotation.value());

                        String objectSchema = IOUtils.toString(schemaUrl.openStream(), Charset.defaultCharset());

                        TypeDefinitionRegistry tdr = schemaParser.parse(objectSchema);
                        if (typeDefinitionRegistry != null) {
                            typeDefinitionRegistry.merge(tdr);
                        } else {
                            typeDefinitionRegistry = tdr;
                        }
                    } catch (IOException e) {
                        log.error("Unable to read GQL schema file", e);
                    }
                }

                for (Method method : getMethodsAnnotatedWith(configurationClass, Query.class)) {
                    configureMethod(runtimeWiring, method, Query.class);
                }

                for (Method method : getMethodsAnnotatedWith(configurationClass, Mutation.class)) {
                    configureMethod(runtimeWiring, method, Mutation.class);
                }
            }

            SchemaGenerator schemaGenerator = new SchemaGenerator();
            return schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring.build()); // (4)

        }

        public TypeDefinitionRegistry getTypeDefinitionRegistry() {
            SchemaParser schemaParser = new SchemaParser();
            TypeDefinitionRegistry typeDefinitionRegistry = null;

            for (Class<?> configurationClass : configurationClasses) {

                try {
                    this.configuration = configurationClass.newInstance();
                } catch (ReflectiveOperationException e) {
                    log.error("Unable to instantiate GQL configuration file", e);
                    continue;
                }

                Schema schemaAnnotation = configurationClass.getAnnotation(Schema.class);
                if (schemaAnnotation != null) {
                    try {
                        URL schemaUrl = this.configuration.getClass().getResource(schemaAnnotation.value());

                        String objectSchema = IOUtils.toString(schemaUrl.openStream(), Charset.defaultCharset());

                        TypeDefinitionRegistry tdr = schemaParser.parse(objectSchema);
                        if (typeDefinitionRegistry != null) {
                            typeDefinitionRegistry.merge(tdr);
                        } else {
                            typeDefinitionRegistry = tdr;
                        }
                    } catch (IOException e) {
                        log.error("Unable to read GQL schema file", e);
                    }
                }

                for (Method method : getMethodsAnnotatedWith(configurationClass, Query.class)) {
                    configureMethod(runtimeWiring, method, Query.class);
                }

                for (Method method : getMethodsAnnotatedWith(configurationClass, Mutation.class)) {
                    configureMethod(runtimeWiring, method, Mutation.class);
                }
            }

            return typeDefinitionRegistry;

        }

        public GraphQL build() {

            return GraphQL.newGraphQL(getGraphQLSchema())//
                    .mutationExecutionStrategy(new AsyncExecutionStrategy(new NuxeoDataFetcherExceptionHandler()))//
                    .build();
        }

        private <T> void configureMethod(graphql.schema.idl.RuntimeWiring.Builder runtimeWiring, Method method,
                Class<? extends Annotation> annotation) {
            String type = "Query";
            String queryName = "";

            if (annotation.equals(Mutation.class)) {
                queryName = ((Mutation) method.getAnnotation(annotation)).value();
                type = "Mutation";
            } else {
                queryName = ((Query) method.getAnnotation(annotation)).value();
            }

            if (StringUtils.isBlank(queryName)) {
                queryName = method.getName();
            }

            final String name = queryName;


            runtimeWiring.type(type, builder -> builder.dataFetcher(name,
                    new VertxDataFetcher<T>((DataFetchingEnvironment env, Promise<T> fut) -> {

                        NuxeoContext nc = env.getContext();
                        nc.session(ar -> {
                            if (ar.succeeded()) {
                                try {
                                    method.invoke(this.configuration, env, ar.result(), fut);
                                } catch (InvocationTargetException e) {
                                    fut.fail(e.getTargetException());
                                } catch (ReflectiveOperationException e) {
                                    fut.fail(e);
                                }
                            } else {
                                fut.fail(ar.cause());
                            }
                        });

                    })));
        }

        private List<Method> getMethodsAnnotatedWith(final Class<?> type,
                final Class<? extends Annotation> annotation) {
            final List<Method> methods = new ArrayList<>();
            Class<?> klass = type;
            while (klass != Object.class) {
                final List<Method> allMethods = new ArrayList<>(Arrays.asList(klass.getDeclaredMethods()));
                for (final Method method : allMethods) {
                    if (method.isAnnotationPresent(annotation)) {
                        methods.add(method);
                    }
                }
                // move to the upper class in the hierarchy in search for more methods
                klass = klass.getSuperclass();
            }
            return methods;
        }

    }

}
