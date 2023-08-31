package ru.novolmob.cicdhelper.gitlab

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import ru.novolmob.cicdhelper.models.GitlabJob
import ru.novolmob.cicdhelper.models.GitlabProject

class GitlabRepository(private val client: HttpClient) {

    constructor(
        gitlabApiUrl: String,
        gitlabToken: String,
        json: Json = Json {
            encodeDefaults = false
            ignoreUnknownKeys = true
            isLenient = true
        },
    ) : this(
        client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(json = json)
            }
            install(Resources)
            install(Logging) {
                logger = Logger.EMPTY
                level = LogLevel.NONE
            }
            defaultRequest {
                url("$gitlabApiUrl/")
                header(key = HttpHeaders.Authorization, value = "Bearer $gitlabToken")
            }
        }
    )

    suspend fun getProject(projectId: String): GitlabProject? =
        try {
            client
                .get(
                    resource = Projects.ID(
                        id = projectId
                    )
                ).body()
        } catch (e: Exception) {
            null
        }

    suspend fun getProjectJobs(projectId: String, status: String): List<GitlabJob> =
        try {
            client
                .get(
                    resource = Projects.ID.Jobs(
                        id = Projects.ID(
                            id = projectId
                        ),
                        scope = status
                    )
                ).body()
        } catch (e: Exception) {
            emptyList()
        }

    suspend fun getJob(projectId: String, jobId: Long): GitlabJob? =
        try {
            client
                .get(
                    resource = Projects.ID.Jobs.ID(
                        jobId = jobId,
                        jobs = Projects.ID.Jobs(
                            id = Projects.ID(
                                id = projectId
                            )
                        )
                    )
                ).body()
        } catch (e: Exception) {
            null
        }

}