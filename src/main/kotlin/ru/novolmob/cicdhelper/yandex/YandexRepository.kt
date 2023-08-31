package ru.novolmob.cicdhelper.yandex

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import ru.novolmob.cicdhelper.models.YandexScenarioActionsResponse

class YandexRepository(private val client: HttpClient) {

    constructor(
        yandexApiUrl: String,
        yandexKey: String,
        json: Json = Json {
            encodeDefaults = false
            ignoreUnknownKeys = true
            isLenient = true
        }
    ): this(
        client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(json = json)
            }
            install(Resources)
            defaultRequest {
                url("$yandexApiUrl/")
                header(key = HttpHeaders.Authorization, value = "Bearer $yandexKey")
            }
        }
    )

    suspend fun scenarioActions(scenarioId: String): YandexScenarioActionsResponse? =
        try {
            client.post(
                resource = Scenarios.ID.Actions(
                    id = Scenarios.ID(id = scenarioId)
                )
            ).body()
        } catch (e: Exception) { null }
}