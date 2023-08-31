package ru.novolmob.cicdhelper.yandex

import io.ktor.resources.*
import kotlinx.serialization.Serializable

@Serializable
@Resource("/scenarios")
class Scenarios() {

    @Serializable
    @Resource("/{id}")
    class ID(val id: String, val scenarios: Scenarios = Scenarios()) {

        @Serializable
        @Resource("/actions")
        class Actions(val id: ID)

    }

}