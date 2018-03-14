package io.andrewohara.lambda.rest

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import com.beust.klaxon.Klaxon
import kotlin.reflect.KClass

typealias Handler = RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent>

interface BasicHandler: Handler {

    companion object {
        private val mapper = Klaxon()
    }

    override fun handleRequest(event: APIGatewayProxyRequestEvent, context: Context?): APIGatewayProxyResponseEvent {
        val responseObject = try {
            handle(event, context)
        } catch(e: RestException) {
            return e.asResponse(mapOf("Content-Type" to "application/json"))
        } catch (e: Exception) {
            e.printStackTrace()
            return InternalServerError().asResponse(emptyMap())  // TODO headers
        }

        return APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withHeaders(mapOf("Content-Type" to "application/json"))
                .withBody(mapper.toJsonString(responseObject))
    }

    @Throws(RestException::class)
    fun handle(event: APIGatewayProxyRequestEvent, context: Context?): Any
}

abstract class BasicHandlerWithBody<in Body: Any>(private val bodyType: KClass<Body>): BasicHandler {

    companion object {
        private val mapper = Klaxon()
    }

    @Throws(RestException::class)
    override fun handle(event: APIGatewayProxyRequestEvent, context: Context?): Any {
        if (event.body == null) throw DataValidationException(event, "Request body required, but was empty")

        val body = try {
            val json = mapper.parseJsonObject(event.body.reader())
            mapper.fromJsonObject(json, bodyType.java, bodyType) as Body
        } catch (e: RuntimeException) {
            e.printStackTrace()
            throw DataValidationException(event, "Request body was invalid")
        }

        return handleBody(body, event, context)
    }

    abstract fun handleBody(body: Body, event: APIGatewayProxyRequestEvent, context: Context?): Any
}

abstract class BasicHandlerWithResource(private val resourcePathParameter: String): BasicHandler {

    @Throws(RestException::class)
    override fun handle(event: APIGatewayProxyRequestEvent, context: Context?): Any {
        val resourceId = event.pathParameters.getValue(resourcePathParameter)
        return handleResource(resourceId, event, context)
    }

    @Throws(RestException::class)
    abstract fun handleResource(resourceId: String, event: APIGatewayProxyRequestEvent, context: Context?): Any
}

abstract class BasicHandlerWithBodyAndResource<in Body: Any>(private val resourcePathParameter: String, bodyType: KClass<Body>): BasicHandlerWithBody<Body>(bodyType) {

    override fun handleBody(body: Body, event: APIGatewayProxyRequestEvent, context: Context?): Any {
        val resourceId = event.pathParameters.getValue(resourcePathParameter)
        return handleResourceAndBody(resourceId, body, event, context)
    }

    @Throws(RestException::class)
    abstract fun handleResourceAndBody(resourceId: String, body: Body, event: APIGatewayProxyRequestEvent, context: Context?): Any
}
