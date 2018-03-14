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

## Getting Started

```kotlin
package io.andrewohara.lambda.rest

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent

class MinimalExample: RestResource("id", cors=false) {

    data class Person(val id: String)

    override fun getHandler() = object: BasicHandlerWithResource("id") {
        override fun handleResource(resourceId: String, event: APIGatewayProxyRequestEvent, context: Context?): Person {
            return Person(resourceId)
        }
    }
}
```

```yml
# service.yml
AWSTemplateFormatVersion: 2010-09-09
Transform: AWS::Serverless-2016-10-31
Description: aws-lambda-rest example

Resources:
  MinimalResource:
      Type: AWS::Serverless::Function
      Properties:
        Handler: io.andrewohara.lambda.rest.MinimalExample
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

