package ru.novolmob.cicdhelper.models

import kotlinx.serialization.Serializable

@Serializable
data class NotifiedJob(
    val id: Long,
    val stage: String,
    val name: String,
    val projectId: String,
    val notifications: List<Settings.Notification>
)