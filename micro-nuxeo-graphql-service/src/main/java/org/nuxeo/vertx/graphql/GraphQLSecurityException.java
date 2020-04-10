package org.nuxeo.vertx.graphql;

import graphql.ExceptionWhileDataFetching;
import graphql.execution.ExecutionPath;
import graphql.language.SourceLocation;

public class GraphQLSecurityException extends ExceptionWhileDataFetching{

	public GraphQLSecurityException(ExecutionPath path, Throwable exception, SourceLocation sourceLocation) {
		super(path, exception, sourceLocation);		
	}

}
