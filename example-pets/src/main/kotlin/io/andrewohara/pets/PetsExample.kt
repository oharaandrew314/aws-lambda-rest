package io.andrewohara.pets

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import io.andrewohara.lambda.rest.ResourceHandler
import io.andrewohara.lambda.rest.RestException
import java.util.*

data class Pet(val id: String=UUID.randomUUID().toString(), val name: String, val type: PetType)
data class CreateUpdatePetData(val name: String, val type: PetType)
enum class PetType { Cat, Dog }

/**
 * CRUD resource for pets
 *
 * The ResourceHandler base class accepts a "resourcePath" parameter.
 * This must be the name of the API Gateway event pathParameter that
 * holds the id of the resource you are working with.
 */
class PetsResource : ResourceHandler<Pet>(resourcePathParameter="petId", enableCors=true) {

    private val pets = mutableMapOf<String, Pet>()

    /**
     * List all the pets (200)
     */
    @Throws(RestException::class)
    override fun list(event: APIGatewayProxyRequestEvent, context: Context): List<Pet> {
        return pets.values.toList()
    }

    /**
     * Get the pet by its id, or null (404)
     */
    @Throws(RestException::class)
    override fun get(resourceId: String, event: APIGatewayProxyRequestEvent, context: Context): Pet? {
        return pets[resourceId]
    }

    /**
     * Create a pet if the request is valid (200), otherwise throw DataValidationException (400)
     */
    @Throws(RestException::class)
    override fun create(event: APIGatewayProxyRequestEvent, context: Context): Pet {
        return event.parseBody<CreateUpdatePetData>()
                .let { Pet(name=it.name, type=it.type) }
                .apply { pets[id] = this }
    }

    /**
     * Create a pet with the given id id the request is valid (200)
     * Otherwise, throw DataValidationException (400)
     */
    @Throws(RestException::class)
    override fun createWithId(resourceId: String, event: APIGatewayProxyRequestEvent, context: Context): Pet {
        return super.createWithId(resourceId, event, context)
    }

    /**
     * delete a pet if it exists (200), otherwise return null (404)
     */
    @Throws(RestException::class)
    override fun delete(resourceId: String, event: APIGatewayProxyRequestEvent, context: Context): Pet? {
        return pets.remove(resourceId)
    }

    /**
     * update a pet if it exists (200)
     * If request is invalid, throws DataValidationException (400)
     * if pet does not exist, returns null (404)
     */
    @Throws(RestException::class)
    override fun update(resourceId: String, event: APIGatewayProxyRequestEvent, context: Context): Pet? {
        if (resourceId !in pets) return null
        return event.parseBody<CreateUpdatePetData>()
                .let { Pet(resourceId, it.name, it.type) }
                .apply { pets[resourceId] = this }
    }

    /**
     * Delete and return all the pets (200)
     */
    @Throws(RestException::class)
    override fun deleteAll(event: APIGatewayProxyRequestEvent, context: Context): List<Pet> {
        return pets
                .keys
                .mapNotNull { pets.remove(it) }
                .toList()
    }
}