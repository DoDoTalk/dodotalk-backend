package com.dothebestmayb.dodotalk.domain.exception

import java.lang.RuntimeException

class UserAlreadyExistsException: RuntimeException(
    "이미 사용 중인 닉네임 혹은 이메일입니다."
)