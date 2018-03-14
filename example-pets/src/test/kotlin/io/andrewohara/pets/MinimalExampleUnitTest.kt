package io.andrewohara.pets

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import io.andrewohara.lambda.rest.MinimalExample
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test

class MinimalExampleUnitTest {

    private val testObj = MinimalExample()
    private val context = Contexts.testContext

    @Test // 200
    fun options() {
        val event = APIGatewayProxyRequestEvent().withHttpMethod("OPTIONS")
        val response = testObj.handleRequest(event, context)
        Assert.assertThat(response.statusCode, CoreMatchers.equalTo(200))
    }

    @Test // 405
    fun list() {
        val event = APIGatewayProxyRequestEvent().withHttpMethod("GET")
        val response = testObj.handleRequest(event, context)
        Assert.assertThat(response.statusCode, CoreMatchers.equalTo(405))
    }

    @Test // 200
    fun get() {
        val event = APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPathParamters(mapOf("id" to "123"))
        val response = testObj.handleRequest(event, context)
        Assert.assertThat(response.statusCode, CoreMatchers.equalTo(200))
    }

    @Test // 405
    fun create() {
        val event = APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
        val response = testObj.handleRequest(event, context)
        Assert.assertThat(response.statusCode, CoreMatchers.equalTo(405))
    }
}