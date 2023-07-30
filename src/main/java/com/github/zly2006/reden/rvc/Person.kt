package com.github.zly2006.reden.rvc

import java.util.*

data class Person(
    @Id
    val id: String,
    @Id
    val email: String,
    @Id
    val mcUuid: UUID?,
    val displayName: String,
) {
    /**
     * Annotate that this field is used to identify a person
     */
    annotation class Id
}
