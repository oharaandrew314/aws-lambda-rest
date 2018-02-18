# aws-lambda-rest (JVM)

A JVM micro-framework for creating REST API resources on AWS Lambda, written in Kotlin.

## Features

- standard CRUD operations for a single resource
  - `POST /pets/` (create)
  - `GET /pets/{petId}` (get)
  - `GET /pets/` (list)
  - `PUT /pets/{petId}` (update)
  - `DELETE /pets/{petId}` (delete)
- CORS

## Planned Features

- Nested resources `/clients/{clientId}/pets/{petId}`
  - e.g. `/clients/{clientId}/pets` would list the pets for the given client