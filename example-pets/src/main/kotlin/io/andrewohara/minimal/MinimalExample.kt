package io.andrewohara.minimal

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import io.andrewohara.lambda.rest.ResourceHandler

data class Person(val id: String)

/**
 * Minimal implementation of a ResourceHandler.
 * The only available operations are options and get.
 *
 * CORS is enabled by default.
 */
class MinimalResource: ResourceHandler<Person>("id") {

    override fun get(resourceId: String, event: APIGatewayProxyRequestEvent, context: Context): Person? {
        return Person(resourceId)
    }
}