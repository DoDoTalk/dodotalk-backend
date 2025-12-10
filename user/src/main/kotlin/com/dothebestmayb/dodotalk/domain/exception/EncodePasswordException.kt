package com.dothebestmayb.dodotalk.domain.exception

// TODO : should map to HTTP 500 Internal Server Error
class EncodePasswordException: RuntimeException(
    "Internal Server Error"
)