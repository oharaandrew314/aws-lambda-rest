# aws-lambda-rest (JVM)

[ ![Download](https://api.bintray.com/packages/oharaandrew314/maven/aws-lambda-rest/images/download.svg) ](https://bintray.com/oharaandrew314/maven/aws-lambda-rest/_latestVersion)

A JVM micro-framework for creating REST API resources on AWS Lambda, written in Kotlin.

## Features

- standard CRUD operations for a single resource
  - create - `POST /pets/`
  - createWithId - `POST /pets/{petId}`
  - get - `GET /pets/{petId}`
  - list - `GET /pets/`
  - update - `PUT /pets/{petId}`
  - delete - `DELETE /pets/{petId}`
  - deleteAll - `DELETE /pets/`
- CORS

## Planned Features

- Nested resources `/clients/{clientId}/pets/{petId}`
  - e.g. `/clients/{clientId}/pets` would list the pets for the given client
- Custom Authorizer Helpers
- Pass deserialized request body into handler methods
- Accept ScheduledEvent object (for keeping lambda warm)

## Installation

```groovy
repositories {
    jcenter()
    maven {
        url 'https://dl.bintray.com/oharaandrew314/maven'
    }
}

dependencies {
    compile 'io.andrewohara:aws-lambda-rest:<latest_version>'
}
```

## Minimal Example

```kotlin
// MinimalExample.kt
package io.andrewohara.minimal

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import io.andrewohara.lambda.rest.ResourceHandler

data class Person(val id: String)

/**
 * Minimal implementation of a ResourceHandler.
 * The only available operations are options and get.
 * 
 * CORS is enabled by default.
 */
class MinimalResource: ResourceHandler<Person>("id") {

    override fun get(resourceId: String, event: APIGatewayProxyRequestEvent, context: Context): Person? {
        return Person(resourceId)
    }
}
```

```yml
# service.yml
AWSTemplateFormatVersion: 2010-09-09
Transform: AWS::Serverless-2016-10-31
Description: aws-lambda-rest example for pets endpoints

Resources:
  MinimalResource:
      Type: AWS::Serverless::Function
      Properties:
        Handler: io.andrewohara.minimal.MinimalResource
        Runtime: java8
        CodeUri: ./kobaltBuild/libs/example-pets-0.0.1.jar
        Timeout: 30
        MemorySize: 1024
        Events:
          GetPerson:
            Type: Api
            Properties:
              Path: /people/{id}
              Method: get
```

## Full CRUD example

```kotlin
// PetsExample.kt
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
```

Example SAM Template to deploy your resource lambda

```yml
# service.yml
AWSTemplateFormatVersion: 2010-09-09
Transform: AWS::Serverless-2016-10-31
Description: aws-lambda-rest example for pets endpoints

Resources:
  PetsResource:
    Type: AWS::Serverless::Function
    Properties:
      Handler: io.andrewohara.pets.PetsResource
      Runtime: java8
      CodeUri: ./kobaltBuild/libs/example-pets-0.0.1.jar
      Timeout: 30
      MemorySize: 1024
      Events:
        PetsOptions:
          Type: Api
          Properties:
            Path: /pets
            Method: options
        ListPets:
          Type: Api
          Properties:
            Path: /pets
            Method: get
        CreatePet:
          Type: Api
          Properties:
            Path: /pets
            Method: post
        PetOptions:
          Type: Api
          Properties:
            Path: /pets/{petId}
            Method: options
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
        DeletePet:
          Type: Api
          Properties:
            Path: /pets/{petId}
            Method: delete
```

