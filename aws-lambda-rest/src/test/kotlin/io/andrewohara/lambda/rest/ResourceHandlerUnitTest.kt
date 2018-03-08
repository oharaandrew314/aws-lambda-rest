package io.andrewohara.lambda.rest

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.LambdaLogger
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test

class ResourceHandlerUnitTest {

    private val context = object: Context {
        override fun getAwsRequestId() = throw UnsupportedOperationException()
        override fun getLogGroupName() = throw UnsupportedOperationException()
        override fun getLogStreamName() = throw UnsupportedOperationException()
        override fun getClientContext() = throw UnsupportedOperationException()
        override fun getFunctionName() = throw UnsupportedOperationException()
        override fun getFunctionVersion() = throw UnsupportedOperationException()
        override fun getIdentity() = throw UnsupportedOperationException()
        override fun getInvokedFunctionArn() = throw UnsupportedOperationException()
        override fun getMemoryLimitInMB() = throw UnsupportedOperationException()
        override fun getRemainingTimeInMillis() = throw UnsupportedOperationException()
        override fun getLogger() = object: LambdaLogger {
            override fun log(message: String?) { }
            override fun log(message: ByteArray?) { }
        }
    }

    private val optionsEvent = APIGatewayProxyRequestEvent()
            .withHttpMethod("OPTIONS")
    private val listEvent = APIGatewayProxyRequestEvent()
            .withHttpMethod("GET")
    private val getEvent = APIGatewayProxyRequestEvent()
            .withHttpMethod("GET")
            .withPathParamters(mapOf("id" to "123"))
    private val deleteEvent = APIGatewayProxyRequestEvent()
            .withHttpMethod("DELETE")
            .withPathParamters(mapOf("id" to "123"))
    private val updateEvent = APIGatewayProxyRequestEvent()
            .withHttpMethod("PUT")
            .withPathParamters(mapOf("id" to "123"))
            .withBody("def")
    private val createEvent = APIGatewayProxyRequestEvent()
            .withHttpMethod("POST")
            .withBody("def")

    @Test
    fun unsupportedHttpMethod() {
        val event = APIGatewayProxyRequestEvent().withHttpMethod("FOO")
        val testObj = ResourceHandler<String>("id")
        test(testObj, event, 405)
    }

    @Test
    fun optionsWithCors() {
        val testObj = ResourceHandler<String>("id")
        val response = test(testObj, optionsEvent, 200)
        Assert.assertThat(response.headers["Access-Control-Allow-Origin"], CoreMatchers.equalTo("*"))
    }

    @Test
    fun optionsWithoutCors() {
        val testObj = ResourceHandler<String>("id", false)
        val response = test(testObj, optionsEvent, 200)
        Assert.assertThat(response.headers["Access-Control-Allow-Origin"], CoreMatchers.nullValue())
    }

    @Test
    fun listUnsupported() {
        val testObj = ResourceHandler<String>("id")
        test(testObj, listEvent, 405)
    }

    @Test
    fun list() {
        val testObj = object: ResourceHandler<String>("id") {
            override fun list(event: APIGatewayProxyRequestEvent, context: Context) = listOf("abc")
        }
        test(testObj, listEvent)
    }

    @Test
    fun getUnsupported() {
        val testObj = object: ResourceHandler<String>("id") {}
        test(testObj, getEvent, 405)
    }

    @Test
    fun getNotFound() {
        val testObj = object: ResourceHandler<String>("id") {
            override fun get(resourceId: String, event: APIGatewayProxyRequestEvent, context: Context): Nothing? = null
        }
         test(testObj, getEvent, 404)

        val testOb2 = object: ResourceHandler<String>("id") {
            override fun get(resourceId: String, event: APIGatewayProxyRequestEvent, context: Context) = throw ResourceNotFoundException(resourceId)
        }
        test(testOb2, getEvent, 404)
    }

    @Test
    fun get() {
        val testObj = object: ResourceHandler<String>("id") {
            override fun get(resourceId: String, event: APIGatewayProxyRequestEvent, context: Context) = "abc"
        }
        val response = test(testObj, getEvent)
        Assert.assertThat(response.body, CoreMatchers.equalTo("\"abc\""))
    }

    @Test
    fun deleteUnsupported() {
        val testObj = object: ResourceHandler<String>("id") {}
        test(testObj, deleteEvent, 405)
    }

    @Test
    fun deleteNotFound() {
        val testObj = object: ResourceHandler<String>("id") {
            override fun delete(resourceId: String, event: APIGatewayProxyRequestEvent, context: Context): Nothing? = null
        }
        test(testObj, deleteEvent, 404)

        val testOb2 = object: ResourceHandler<String>("id") {
            override fun delete(resourceId: String, event: APIGatewayProxyRequestEvent, context: Context) = throw ResourceNotFoundException(resourceId)
        }
        test(testOb2, deleteEvent, 404)
    }

    @Test
    fun delete() {
        val testObj = object: ResourceHandler<String>("id") {
            override fun delete(resourceId: String, event: APIGatewayProxyRequestEvent, context: Context) = "def"
        }
        test(testObj, deleteEvent)
    }

    @Test
    fun deleteAllUnsupported() {
        val event = APIGatewayProxyRequestEvent().withHttpMethod("DELETE")
        val testObj = object: ResourceHandler<String>("id") {}
        test(testObj, event, 405)
    }

    @Test
    fun updateUnsupported() {
        val testObj = object: ResourceHandler<String>("id") {}
        test(testObj, updateEvent, 405)
    }

    @Test
    fun updateNotFound() {
        val testObj = object: ResourceHandler<String>("id") {
            override fun update(resourceId: String, event: APIGatewayProxyRequestEvent, context: Context): Nothing? = null
        }
        test(testObj, updateEvent, 404)
    }

    @Test
    fun update() {
        val testObj = object: ResourceHandler<String>("id") {
            override fun update(resourceId: String, event: APIGatewayProxyRequestEvent, context: Context) = event.body
        }
        test(testObj, updateEvent)
    }

    @Test
    fun updateWithInvalidData() {
        val testObj = object: ResourceHandler<String>("id") {
            override fun update(resourceId: String, event: APIGatewayProxyRequestEvent, context: Context) = throw DataValidationException(event)
        }
        test(testObj, updateEvent, 400)
    }

    @Test
    fun updateWithoutResource() {
        val event = APIGatewayProxyRequestEvent().withHttpMethod("PUT").withBody("def")
        val testObj = object: ResourceHandler<String>("id") {
            override fun update(resourceId: String, event: APIGatewayProxyRequestEvent, context: Context) = event.body
        }
        test(testObj, event, 405)
    }

    @Test
    fun createUnsupported() {
        val testObj = object: ResourceHandler<String>("id") {}
        test(testObj, createEvent, 405)
    }

    @Test
    fun create() {
        val testObj = object: ResourceHandler<String>("id") {
            override fun create(event: APIGatewayProxyRequestEvent, context: Context) = event.body
        }
        test(testObj, createEvent)
    }

    @Test
    fun createInvalidData() {
        val testObj = object: ResourceHandler<String>("id") {
            override fun create(event: APIGatewayProxyRequestEvent, context: Context) = throw DataValidationException(event)
        }
        test(testObj, createEvent, 400)
    }

    @Test
    fun createWithSpecificIdUnsupported() {
        val event = APIGatewayProxyRequestEvent().withHttpMethod("POST").withBody("456").withPathParamters(mapOf("id" to "123"))
        val testObj = object: ResourceHandler<String>("id") {
            override fun create(event: APIGatewayProxyRequestEvent, context: Context) = throw DataValidationException(event)
        }
        test(testObj, event, 405)
    }

    @Test
    fun overrideDefault() {
        val event = APIGatewayProxyRequestEvent().withHttpMethod("PUT")
        val testObj = object: ResourceHandler<String>("id") {
            override fun default(event: APIGatewayProxyRequestEvent, context: Context): APIGatewayProxyResponseEvent {
                return APIGatewayProxyResponseEvent().withStatusCode(1337)
            }
        }

        test(testObj, event, 1337)
    }

    private fun test(testObj: ResourceHandler<String>, event: APIGatewayProxyRequestEvent, statusCode: Int = 200, body: String? = ""): APIGatewayProxyResponseEvent {
        val response = testObj.handleRequest(event, context)
        Assert.assertThat(response.statusCode, CoreMatchers.equalTo(statusCode))
//        Assert.assertThat(response.body, CoreMatchers.equalTo(body))
        return response
    }
}