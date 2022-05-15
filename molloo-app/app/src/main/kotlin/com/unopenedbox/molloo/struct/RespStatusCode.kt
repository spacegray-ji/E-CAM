package com.unopenedbox.molloo.struct

enum class RespStatusCode(val code: Int) {
    OK(200),
    NOT_FOUND(404),
    INTERNAL_SERVER_ERROR(500),
}