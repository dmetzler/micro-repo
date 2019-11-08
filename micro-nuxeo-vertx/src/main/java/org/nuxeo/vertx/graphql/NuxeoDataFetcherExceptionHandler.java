package org.nuxeo.vertx.graphql;

import org.nuxeo.ecm.core.api.DocumentSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graphql.ExceptionWhileDataFetching;
import graphql.execution.DataFetcherExceptionHandler;
import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherExceptionHandlerResult;
import graphql.execution.ExecutionPath;
import graphql.execution.SimpleDataFetcherExceptionHandler;
import graphql.language.SourceLocation;

public class NuxeoDataFetcherExceptionHandler implements DataFetcherExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(SimpleDataFetcherExceptionHandler.class);

    @Override
    public DataFetcherExceptionHandlerResult onException(DataFetcherExceptionHandlerParameters handlerParameters) {
        Throwable exception = handlerParameters.getException();
        SourceLocation sourceLocation = handlerParameters.getSourceLocation();
        ExecutionPath path = handlerParameters.getPath();

        ExceptionWhileDataFetching error = new ExceptionWhileDataFetching(path, exception, sourceLocation);
        if (!(exception instanceof DocumentSecurityException)) {
            log.warn(error.getMessage(), exception);
        } else {
            log.warn(error.getMessage());
        }

        return DataFetcherExceptionHandlerResult.newResult().error(error).build();
    }

}
