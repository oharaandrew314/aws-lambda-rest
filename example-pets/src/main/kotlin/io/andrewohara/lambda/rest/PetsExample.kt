package io.andrewohara.lambda.rest

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import java.util.*

class PetsExample: RestResource("petId") {

    data class Pet(val id: String=UUID.randomUUID().toString(), val name: String, val type: Type) {
        enum class Type { Cat, Dog }
    }
    data class PetData(val name: String, val type: Pet.Type)

    private val pets = mutableMapOf<String, Pet>()

    @Throws(RestException::class)
    override fun listHandler() = object: BasicHandler {
        override fun handle(event: APIGatewayProxyRequestEvent, context: Context?): Collection<Pet> = pets.values
    }

    @Throws(RestException::class)
    override fun getHandler() = object: BasicHandlerWithResource("petId") {
        override fun handleResource(resourceId: String, event: APIGatewayProxyRequestEvent, context: Context?): Pet {
            return pets[resourceId] ?: throw ResourceNotFoundException(resourceId)
        }
    }

    @Throws(RestException::class)
    override fun createHandler() = object: BasicHandlerWithBody<PetData>(PetData::class) {
        override fun handleBody(body: PetData, event: APIGatewayProxyRequestEvent, context: Context?): Pet {
            val newPet = Pet(name = body.name, type = body.type)
            pets[newPet.id] = newPet
            return newPet
        }
    }

    override fun createWithIdHandler() = object: BasicHandlerWithBodyAndResource<PetData>("petId", PetData::class) {
        override fun handleResourceAndBody(resourceId: String, body: PetData, event: APIGatewayProxyRequestEvent, context: Context?): Pet {
            if (resourceId in pets) throw DataValidationException(event, "Id already exists: $resourceId")
            val newPet = Pet(resourceId, body.name, body.type)
            pets[resourceId] = newPet
            return newPet
        }
    }

    override fun updateHandler() = object: BasicHandlerWithBodyAndResource<PetData>("petId", PetData::class) {
        override fun handleResourceAndBody(resourceId: String, body: PetData, event: APIGatewayProxyRequestEvent, context: Context?): Pet {
            if (resourceId !in pets) throw ResourceNotFoundException(resourceId)
            val updated = pets.getValue(resourceId).copy(name=body.name, type=body.type)
            pets[resourceId] = updated
            return updated
        }
    }

    @Throws(RestException::class)
    override fun deleteAllHandler() = object: BasicHandler {
        override fun handle(event: APIGatewayProxyRequestEvent, context: Context?) {
            pets.clear()
        }
    }

    @Throws(RestException::class)
    override fun deleteHandler() = object: BasicHandlerWithResource("petId") {
        override fun handleResource(resourceId: String, event: APIGatewayProxyRequestEvent, context: Context?): Pet {
            return pets.remove(resourceId) ?: throw ResourceNotFoundException(resourceId)
        }
    }
}