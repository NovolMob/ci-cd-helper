package ru.novolmob.cicdhelper.models

import kotlinx.serialization.Serializable

@Serializable
data class YandexScenarioActionsResponse(
    val request_id: String,
    val status: String
)

interface YandexScenario {
    val yandexScenarioId: String
    val yandexDelay: Long?
}