package io.andrewohara.lambda.rest

enum class RestMethod {
    OPTIONS, GET, PUT, POST, DELETE;

    fun isOptions() = this == OPTIONS
    fun isGet() = this == GET
    fun isPut() = this == PUT
    fun isPost() = this == POST
    fun isDelete() = this == DELETE
}