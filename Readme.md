# aws-lambda-rest (JVM)

A JVM micro-framework for creating REST API resources on AWS Lambda, written in Kotlin.

## Features

- standard CRUD operations for a single resource
  - create -`POST /pets/`
  - get - `GET /pets/{petId}`
  - list - `GET /pets/`
  - update - `PUT /pets/{petId}`
  - delete - `DELETE /pets/{petId}`
- CORS

## Planned Features

- Nested resources `/clients/{clientId}/pets/{petId}`
  - e.g. `/clients/{clientId}/pets` would list the pets for the given client
- Custom Authorizer Helpers
- Pass deserialized request body into handler methods

## Installation

My repo and jcenter are required

```groovy
repositories {
    jcenter()
    maven {
        url 'https://dl.bintray.com/oharaandrew314/maven'
    }
}

dependencies {
    compile 'io.andrewohara:aws-lambda-rest:0.1.0'
}
```

## Usage

```kotlin
package io.andrewohara.pets

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.beust.klaxon.Klaxon
import io.andrewohara.lambda.rest.DataValidationException
import io.andrewohara.lambda.rest.ResourceHandler
import java.util.*

/**
 * CRUD resource for pets
 * 
 * The ResourceHandler base class accepts a "resourcePath" parameter.
 * This must be the name of the API Gateway event pathParameter that
 * holds the id of the resource you are working with.
 */
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

    /**
     * List all the pets (200)
     */
    override fun list(event: APIGatewayProxyRequestEvent, context: Context) = pets.values.toList()

    /**
     * Get the pet by its id, or null (404)
     */
    override fun get(resourceId: String, event: APIGatewayProxyRequestEvent, context: Context) = pets[resourceId]

    /**
     * Create a pet if the request is valid (200), otherwise throw DataValidationException (400)
     */
    override fun create(event: APIGatewayProxyRequestEvent, context: Context): Pet {
        return getRequest(event)
                .let { Pet(UUID.randomUUID().toString(), it.name, it.type) }
                .apply { pets[id] = this }
    }

    /**
     * delete a pet if it exists (200), otherwise return null (404)
     */
    override fun delete(resourceId: String, event: APIGatewayProxyRequestEvent, context: Context) = pets.remove(resourceId)

    /**
     * update a pet if it exists (200)
     * If request is invalid, throws DataValidationException (400)
     * if pet does not exist, returns null (404)
     */
    override fun update(resourceId: String, event: APIGatewayProxyRequestEvent, context: Context): Pet? {
        if (resourceId !in pets) return null
        return getRequest(event)
                .let { Pet(resourceId, it.name, it.type) }
                .apply { pets[resourceId] = this }
    }
}
```

Example SAM Template to deploy your resource lambda

```yml
AWSTemplateFormatVersion: 2010-09-09
Transform: AWS::Serverless-2016-10-31
Description: aws-lambda-rest example for pets endpoints

Resources:
  PetsResource:
    Type: AWS::Serverless::Function
    Properties:
      Handler: io.andrewohara.pets.PetsHandler
      Runtime: java8
      CodeUri: ./kobaltBuild/libs/example-pets-0.0.1.jar
      Timeout: 30
      MemorySize: 1024
      Events:
        ListPets:
          Type: Api
          Properties:
            Path: /pets/
            Method: get
        GetPet:
          Type: Api
          Properties:
            Path: /pets/{petId}
            Method: get
        UpdatePet:
          Type: Api
          Properties:
            Path: /pets/{petId}
            Method: put
        CreatePet:
          Type: Api
          Properties:
            Path: /pets/
            Method: post
        DeletePet:
          Type: Api
          Properties:
            Path: /pets/{petId}
            Method: delete
```

