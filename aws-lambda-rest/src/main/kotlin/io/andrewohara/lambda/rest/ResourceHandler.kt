package io.andrewohara.lambda.rest

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import com.beust.klaxon.Klaxon

/**
 * Base Class to implement a series of CRUD endpoints for a resource when invoked by API Gateway.
 *
 * By default, all CRUD operations are unimplemented, and will return a 405 when invoked.
 * To implement them, extend this class and override the CRUD methods.
 * Your lambda handler should point to the extension of this class.
 */
open class ResourceHandler<out T: Any>(
        private val resourcePathParameter: String, enableCors: Boolean = true
): RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    protected val mapper = Klaxon()
    private val headers = if(enableCors) {
        mapOf(
                "Content-Type" to "application/json",
                "Access-Control-Allow-Origin" to "*",
                "Access-Control-Allow-Headers" to "Content-Type,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token"
        )
    } else {
        mapOf("Content-Type" to "application/json")
    }

    override fun handleRequest(event: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent {
        context.logger.log(event.toString())

        val resourceId = event.pathParameters?.get(resourcePathParameter)
        val restMethod = try {
            RestMethod.valueOf(event.httpMethod)
        } catch (e: IllegalArgumentException) {
            return UnsupportedResourceOperation(event).asResponse(headers)
        }

        return try {
            val result: Any = when {
                restMethod.isOptions() -> return options(event, context)
                restMethod.isGet() && resourceId != null -> get(resourceId, event, context) ?: throw ResourceNotFoundException(resourceId)
                restMethod.isGet() && resourceId == null -> list(event, context)
                restMethod.isPost() && resourceId == null -> create(event, context)
                restMethod.isPost() && resourceId != null -> createWithId(resourceId, event, context)
                restMethod.isPut() && resourceId != null -> update(resourceId, event, context) ?: throw ResourceNotFoundException(resourceId)
                restMethod.isDelete() && resourceId != null -> delete(resourceId, event, context) ?: throw ResourceNotFoundException(resourceId)
                restMethod.isDelete() && resourceId == null -> deleteAll(event, context)
                else -> return default(event, context)
            }

            APIGatewayProxyResponseEvent()
                    .withHeaders(headers)
                    .withStatusCode(200)
                    .withBody(mapper.toJsonString(result))
        } catch (e: RestException) {
            e.asResponse(headers)
        } catch (e: Exception) {
            e.printStackTrace()
            InternalServerError(cause=e).asResponse(headers)
        }
    }

    /**
     * Invoked on OPTIONS /<resourcePath>/\*
     *
     * You control all attributes of the response object, including the headers and status code.
     */
    @Throws(RestException::class)
    protected open fun options(event: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent = APIGatewayProxyResponseEvent()
            .withHeaders(headers)
            .withStatusCode(200)
            .withBody("{}")

    /**
     * Invoked on GET /<resourcePath>/
     *
     * Returns all the resources as a list
     */
    @Throws(RestException::class)
    protected open fun list(event: APIGatewayProxyRequestEvent, context: Context): List<T> = throw UnsupportedResourceOperation(event)

    /**
     * Invoked on GET /<resourcePath>/{resourceId>/
     *
     * Gets a resource by id, and returns a 404 if not found
     */
    @Throws(RestException::class)
    protected open fun get(resourceId: String, event: APIGatewayProxyRequestEvent, context: Context): T? = throw UnsupportedResourceOperation(event)

    /**
     * Invoked on POST /<resourcePath>/
     *
     * Creates a resource and returns it
     */
    @Throws(RestException::class)
    protected open fun create(event: APIGatewayProxyRequestEvent, context: Context): T = throw UnsupportedResourceOperation(event)

    /**
     * Invoked on POST /<resourcePath>/<resourceId>/
     *
     * Creates a resource with the specified id and returns in
     */
    @Throws(RestException::class)
    protected open fun createWithId(resourceId: String, event: APIGatewayProxyRequestEvent, context: Context): T = throw UnsupportedResourceOperation(event)

    /**
     * Invoked on DELETE /<resourcePath>/<resourceId>/
     *
     * Deletes the resource and returns it if it existed.  Otherwise, returns a 404
     */
    @Throws(RestException::class)
    protected open fun delete(resourceId: String, event: APIGatewayProxyRequestEvent, context: Context): T? = throw UnsupportedResourceOperation(event)

    /**
     * Invoked on DELETE /<resourcePath>/
     *
     * Deletes all resources, and returns the ones deleted.
     */
    @Throws(RestException::class)
    protected open fun deleteAll(event: APIGatewayProxyRequestEvent, context: Context): List<T> = throw UnsupportedResourceOperation(event)

    /**
     * Invoked on PUT /<resourcePath>/<resourceId>/
     *
     * Updates the resource and returns the updated version.  Otherwise, returns a 404
     */
    @Throws(RestException::class)
    protected open fun update(resourceId: String, event: APIGatewayProxyRequestEvent, context: Context): T? = throw UnsupportedResourceOperation(event)

    /**
     * Invoked on all other events that don't match a standard CRUD operation.
     *
     * You control all attributes of the response object, including the headers and status code.
     */
    @Throws(RestException::class)
    protected open fun default(event: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent = throw UnsupportedResourceOperation(event)

    protected inline fun <reified T> APIGatewayProxyRequestEvent.maybeParseBody(): T? {
        try {
            return mapper.parse(this.body)
        } catch (e: Exception) {
            throw DataValidationException(this)
        }
    }

    protected inline fun <reified T> APIGatewayProxyRequestEvent.parseBody(): T = maybeParseBody() ?: throw DataValidationException(this)
}