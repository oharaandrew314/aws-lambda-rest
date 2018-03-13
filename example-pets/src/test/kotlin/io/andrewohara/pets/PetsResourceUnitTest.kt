package io.andrewohara.pets

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import io.andrewohara.lambda.rest.ResourceHandlerUnitTest
import org.hamcrest.CoreMatchers
import org.junit.Assert
import org.junit.Test

class PetsResourceUnitTest {

    private val testObj = PetsResource()
    private val context = ResourceHandlerUnitTest.context
    private val mapper = Klaxon()

    private val listEvent = APIGatewayProxyRequestEvent()
            .withHttpMethod("GET")
    private fun getEvent(petId: String) = APIGatewayProxyRequestEvent()
            .withHttpMethod("GET")
            .withPathParamters(mapOf("petId" to petId))
    private fun createEvent(name: String, type: Pet.Type) = APIGatewayProxyRequestEvent()
            .withHttpMethod("POST")
            .withBody(mapper.toJsonString(mapOf(
                    "name" to name,
                    "type" to type.toString()
            )))
    private fun updateEvent(petId: String, name: String, type: Pet.Type) = APIGatewayProxyRequestEvent()
            .withHttpMethod("PUT")
            .withPathParamters(mapOf("petId" to petId))
            .withBody(mapper.toJsonString(mapOf(
                    "name" to name,
                    "type" to type.toString()
            )))
    private fun deleteEvent(petId: String) = APIGatewayProxyRequestEvent()
            .withHttpMethod("DELETE")
            .withPathParamters(mapOf("petId" to petId))

    @Test
    fun listEmpty() {
        val response = testObj.handleRequest(listEvent, context)
        Assert.assertThat(response.statusCode, CoreMatchers.equalTo(200))
        Assert.assertThat(response.body, CoreMatchers.equalTo("[]"))
    }

    @Test
    fun getNotFound() {
        val response = testObj.handleRequest(getEvent("123"), context)
        Assert.assertThat(response.statusCode, CoreMatchers.equalTo(404))

        Assert.assertThat(
                mapper.parseJsonObject(response.body.reader()).toMap(),
                CoreMatchers.equalTo(mapOf<String, Any?>("message" to "Resource not found: 123"))
        )
    }

    @Test
    fun createListAndGet() {
        val createResp = testObj.handleRequest(createEvent("Tigger", Pet.Type.Cat), context)
        Assert.assertThat(createResp.statusCode, CoreMatchers.equalTo(200))
        val created = mapper.parseJsonObject(createResp.body.reader())
        Assert.assertThat(created["name"].toString(), CoreMatchers.equalTo("Tigger"))

        val getResp = testObj.handleRequest(getEvent(created["id"].toString()), context)
        Assert.assertThat(getResp.statusCode, CoreMatchers.equalTo(200))
        val getted = mapper.parseJsonObject(getResp.body.reader())
        Assert.assertThat(getted, CoreMatchers.equalTo(created))

        val listResp = testObj.handleRequest(listEvent, context)
        Assert.assertThat(listResp.statusCode, CoreMatchers.equalTo(200))
        val listed = mapper.parseJsonArray(listResp.body.reader()).map { it as JsonObject }
        Assert.assertThat(listed, CoreMatchers.equalTo(listOf(created)))
    }

    @Test
    fun createWithEmptyBody() {
        val event = APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
        val response = testObj.handleRequest(event, context)
        Assert.assertThat(response.statusCode, CoreMatchers.equalTo(400))
    }

    @Test
    fun createWithMalformedBody() {
        val event = APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withBody("add pet please?")
        val response = testObj.handleRequest(event, context)
        Assert.assertThat(response.statusCode, CoreMatchers.equalTo(400))
    }

    @Test
    fun createWithMissingParams() {
        val event = APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withBody(mapper.toJsonString(mapOf("name" to "Tigger")))

        val response = testObj.handleRequest(event, context)
        Assert.assertThat(response.statusCode, CoreMatchers.equalTo(400))
    }

    @Test
    fun updateNotFound() {
        val response = testObj.handleRequest(updateEvent("123", "Tigger", Pet.Type.Cat), context)
        Assert.assertThat(response.statusCode, CoreMatchers.equalTo(404))
    }

    @Test
    fun updateAndGet() {
        val createResp = testObj.handleRequest(createEvent("Tigger", Pet.Type.Cat), context)
        val created = mapper.parse<Pet>(createResp.body)!!

        val updateResp = testObj.handleRequest(updateEvent(created.id, "Tiggles", Pet.Type.Cat), context)
        Assert.assertThat(updateResp.statusCode, CoreMatchers.equalTo(200))
        val updated = mapper.parse<Pet>(updateResp.body)!!
        Assert.assertThat(updated.id, CoreMatchers.equalTo(created.id))
        Assert.assertThat(updated.name, CoreMatchers.equalTo("Tiggles"))

        val getResp = testObj.handleRequest(getEvent(created.id), context)
        Assert.assertThat(getResp.statusCode, CoreMatchers.equalTo(200))
        val getted = mapper.parse<Pet>(getResp.body)!!
        Assert.assertThat(getted.name, CoreMatchers.equalTo("Tiggles"))
    }

    @Test
    fun deleteNotFound() {
        val response = testObj.handleRequest(deleteEvent("123"), context)
        Assert.assertThat(response.statusCode, CoreMatchers.equalTo(404))
    }

    @Test
    fun deleteAndGet() {
        val createResp = testObj.handleRequest(createEvent("Tigger", Pet.Type.Cat), context)
        val created = mapper.parse<Pet>(createResp.body)!!

        val deleteResp = testObj.handleRequest(deleteEvent(created.id), context)
        Assert.assertThat(deleteResp.statusCode, CoreMatchers.equalTo(200))
        val deleted = mapper.parse<Pet>(deleteResp.body)!!
        Assert.assertThat(deleted, CoreMatchers.equalTo(created))

        val getResp = testObj.handleRequest(getEvent(created.id), context)
        Assert.assertThat(getResp.statusCode, CoreMatchers.equalTo(404))
    }
}