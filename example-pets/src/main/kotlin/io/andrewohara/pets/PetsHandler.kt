package io.andrewohara.pets

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.beust.klaxon.Klaxon
import io.andrewohara.lambda.rest.DataValidationException
import io.andrewohara.lambda.rest.ResourceHandler
import java.util.*

class PetsHandler : ResourceHandler<PetsHandler.Pet>("petId") {

    data class Pet(val id: String, val name: String, val type: PetType)
    data class CreateUpdatePetRequest(val name: String, val type: PetType)
    enum class PetType { Cat, Dog }

    private val mapper = Klaxon()
    private val pets = mutableMapOf<String, Pet>()

    private fun getRequest(event: APIGatewayProxyRequestEvent): CreateUpdatePetRequest {
        return try {
            event.body?.let { mapper.parse<CreateUpdatePetRequest>(it) } ?: throw DataValidationException(event)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            throw DataValidationException(event)
        }
    }

    override fun list(event: APIGatewayProxyRequestEvent, context: Context) = pets.values.toList()

    override fun get(resourceId: String, event: APIGatewayProxyRequestEvent, context: Context) = pets[resourceId]

    override fun create(event: APIGatewayProxyRequestEvent, context: Context): Pet {
        return getRequest(event)
                .let { Pet(UUID.randomUUID().toString(), it.name, it.type) }
                .apply { pets[id] = this }
    }

    override fun delete(resourceId: String, event: APIGatewayProxyRequestEvent, context: Context) = pets.remove(resourceId)

    override fun update(resourceId: String, event: APIGatewayProxyRequestEvent, context: Context): Pet? {
        if (resourceId !in pets) return null
        return getRequest(event)
                .let { Pet(resourceId, it.name, it.type) }
                .apply { pets[resourceId] = this }
    }
}