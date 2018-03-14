package io.andrewohara.lambda.rest

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent

class LowLevelExample: RestResource("id") {

    override fun listHandler() = Handler { _, context ->
        context.logger.log("list")
        APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withHeaders(mapOf("Content-Type" to "text/plain"))
                .withBody("listing stuff...")
    }

    override fun defaultHandler() = Handler { event, context ->
        context.logger.log("default")
        APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withHeaders(mapOf("Content-Type" to "text/plain"))
                .withBody(event.body)
    }
}