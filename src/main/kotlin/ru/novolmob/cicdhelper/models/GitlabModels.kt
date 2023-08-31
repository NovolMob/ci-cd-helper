package ru.novolmob.cicdhelper.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class GitlabJob(
    val id: Long,
    val name: String,
    val stage: String,
    val finished_at: String? = null,
    val status: String,
    val runner: JsonElement? = null
)

@Serializable
data class GitlabProject(
    val id: Long,
    val name: String,
    val path_with_namespace: String
)