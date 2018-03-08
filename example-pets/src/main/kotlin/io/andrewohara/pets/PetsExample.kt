package io.andrewohara.pets

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.beust.klaxon.Klaxon
import io.andrewohara.lambda.rest.DataValidationException
import io.andrewohara.lambda.rest.ResourceHandler
import io.andrewohara.lambda.rest.RestException
import java.util.*

data class Pet(val id: String, val name: String, val type: PetType)
data class CreateUpdatePetData(val name: String, val type: PetType)
enum class PetType { Cat, Dog }

/**
 * CRUD resource for pets
 *
 * The ResourceHandler base class accepts a "resourcePath" parameter.
 * This must be the name of the API Gateway event pathParameter that
 * holds the id of the resource you are working with.
 */
class PetsResource : ResourceHandler<Pet>("petId") {

    private val mapper = Klaxon()
    private val pets = mutableMapOf<String, Pet>()

    private fun getData(event: APIGatewayProxyRequestEvent): CreateUpdatePetData {
        return try {
            event.body?.let { mapper.parse<CreateUpdatePetData>(it) } ?: throw DataValidationException(event)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            throw DataValidationException(event)
        }
    }

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
        return getData(event)
                .let { Pet(UUID.randomUUID().toString(), it.name, it.type) }
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
        return getData(event)
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