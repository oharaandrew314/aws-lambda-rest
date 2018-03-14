package io.andrewohara.lambda.rest

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent

open class RestResource(private val resourcePathParameter: String, cors: Boolean = false): Handler {

    private val corsHeaders = if(cors) {
        mapOf(
                "Content-Type" to "application/json",
                "Access-Control-Allow-Origin" to "*",
                "Access-Control-Allow-Headers" to "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token"
        )
    } else {
        mapOf("Content-Type" to "application/json")
    }

    override fun handleRequest(event: APIGatewayProxyRequestEvent, context: Context?): APIGatewayProxyResponseEvent {
        val hasResource = resourcePathParameter in (event.pathParameters ?: emptyMap())

        val handler: Handler = when(event.httpMethod.toUpperCase()) {
            "OPTIONS" -> optionsHandler()
            "GET" -> if (hasResource) getHandler() else listHandler()
            "POST" -> if (hasResource) createWithIdHandler() else createHandler()
            "PUT" -> if (hasResource) updateHandler() else null
            "DELETE" -> if (hasResource) deleteHandler() else deleteAllHandler()
            else -> null
        } ?: defaultHandler()

        return handler.handleRequest(event, context).apply {
            headers = (headers ?: emptyMap()) + corsHeaders
        }
    }

    open fun optionsHandler(): Handler? = Handler { _, _ -> APIGatewayProxyResponseEvent().withStatusCode(200) }
    open fun listHandler(): Handler? = null
    open fun getHandler(): Handler? = null
    open fun createHandler(): Handler? = null
    open fun createWithIdHandler(): Handler? = null
    open fun updateHandler(): Handler? = null
    open fun deleteHandler(): Handler? = null
    open fun deleteAllHandler(): Handler? = null
    open fun defaultHandler(): Handler = object: BasicHandler {
        override fun handle(event: APIGatewayProxyRequestEvent, context: Context?) = throw UnsupportedResourceOperation(event)
    }
}
