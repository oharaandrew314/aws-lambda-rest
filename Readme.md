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