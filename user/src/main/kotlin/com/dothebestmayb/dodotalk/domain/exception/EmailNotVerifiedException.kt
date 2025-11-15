package com.dothebestmayb.dodotalk.domain.exception

import java.lang.RuntimeException

class EmailNotVerifiedException : RuntimeException(
    "email is not verified."
)