package io.andrewohara.lambda.rest

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent

/**
 * Minimal example.
 *
 * The only operations defined are options (by default) and get.
 *
 * OPTIONS /... -> 200
 * GET /.../{id} -> 200
 * ANY /... -> 405
 */
class MinimalExample: RestResource("id", cors=false) {

    data class Person(val id: String)

    override fun getHandler() = object: BasicHandlerWithResource("id") {
        override fun handleResource(resourceId: String, event: APIGatewayProxyRequestEvent, context: Context?): Person {
            return Person(resourceId)
        }
    }
}