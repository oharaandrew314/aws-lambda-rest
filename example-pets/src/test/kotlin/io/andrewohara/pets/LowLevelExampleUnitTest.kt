package io.andrewohara.pets

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import io.andrewohara.lambda.rest.LowLevelExample
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test

class LowLevelExampleUnitTest {

    private val testObj = LowLevelExample()
    private val context: Context = Contexts.testContext

    @Test
    fun list() {
        val event = APIGatewayProxyRequestEvent().withHttpMethod("GET")
        val response = testObj.handleRequest(event, context)
        Assert.assertThat(response.statusCode, CoreMatchers.equalTo(200))
        Assert.assertThat(response.body, CoreMatchers.equalTo("listing stuff..."))
    }

    @Test
    fun get() {
        val event = APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPathParamters(mapOf("id" to "123"))
                .withBody("foo")

        val response = testObj.handleRequest(event, context)
        Assert.assertThat(response.statusCode, CoreMatchers.equalTo(200))
        Assert.assertThat(response.body, CoreMatchers.equalTo("foo"))
    }

    @Test
    fun deleteAll() {
        val event = APIGatewayProxyRequestEvent()
                .withHttpMethod("DELETE")
                .withBody("bar")

        val response = testObj.handleRequest(event, context)
        Assert.assertThat(response.statusCode, CoreMatchers.equalTo(200))
        Assert.assertThat(response.body, CoreMatchers.equalTo("bar"))
    }
}