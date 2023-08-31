package ru.novolmob.cicdhelper.models

import kotlinx.serialization.Serializable

@Serializable
data class Settings(
    val gitlabToken: String = "",
    val yandexKey: String = "",
    val gitlabApiUrl: String = "",
    val yandexApiUrl: String = "https://api.iot.yandex.net",
    val projects: List<Project> = emptyList(),
    val notifications: List<Notification> = emptyList(),
    val scenarios: List<Scenario> = emptyList()
) {
    @Serializable
    data class Project(
        val projectId: String = "",
        val jobFilter: String = "created",
        val notifications: List<Notification>? = null
    )

    @Serializable
    data class Notification(
        val jobName: String? = null,
        val jobStage: String? = null,
        val jobStatus: String? = null,
        val priority: Int = 0,
        val scenario: String = ""
    )

    @Serializable
    data class Scenario(
        val name: String,
        override val yandexScenarioId: String = "",
        override val yandexDelay: Long?
    ): YandexScenario
}

