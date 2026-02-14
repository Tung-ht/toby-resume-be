package com.tobyresume.backend.config;

import com.tobyresume.backend.common.exception.ResourceNotFoundException;
import com.tobyresume.backend.common.exception.ValidationException;
import graphql.GraphqlErrorBuilder;
import graphql.GraphQLError;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Maps exceptions from GraphQL resolvers to GraphQL errors. No stack traces or internal class names.
 *
 * @see docs/ai/design/phase1-mvp.md §9.3, api-design §8.4
 */
@Component
public class GraphQLExceptionResolver extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(@NonNull Throwable ex, @NonNull DataFetchingEnvironment env) {
        String code;
        String message;
        if (ex instanceof ResourceNotFoundException) {
            code = "NOT_FOUND";
            message = ex.getMessage() != null ? ex.getMessage() : "Resource not found";
        } else if (ex instanceof ValidationException) {
            code = "VALIDATION_ERROR";
            message = ex.getMessage() != null ? ex.getMessage() : "Validation failed";
        } else {
            code = "INTERNAL_ERROR";
            message = "An unexpected error occurred";
        }
        return GraphqlErrorBuilder.newError(env)
                .message(message)
                .extensions(Map.of("code", code))
                .build();
    }
}
