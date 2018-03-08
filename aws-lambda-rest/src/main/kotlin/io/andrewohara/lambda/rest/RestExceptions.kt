package io.andrewohara.lambda.rest

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent

open class RestException(val statusCode: Int, message: String? = null, cause: Exception? = null): Exception(message, cause) {
    fun asResponse(headers: Map<String, String>): APIGatewayProxyResponseEvent = APIGatewayProxyResponseEvent()
            .withStatusCode(statusCode)
            .withHeaders(headers)
            .withBody("{ \"message\": \"$message\"}")
}

class ResourceNotFoundException(resourceId: String): RestException(
        404,
        "Resource not found: $resourceId"
)

class DataValidationException(event: APIGatewayProxyRequestEvent, message: String? = null): RestException(
        400,
        message ?: event.body
)

class UnsupportedResourceOperation(event: APIGatewayProxyRequestEvent, message: String? = null): RestException(
        405,
        message ?: "Cannot perform ${event.httpMethod} ${event.resource}"
)

class InternalServerError(message: String? = null, cause: Exception? = null): RestException(500, message, cause)