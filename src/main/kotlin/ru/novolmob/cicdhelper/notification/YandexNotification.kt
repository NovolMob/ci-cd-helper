package ru.novolmob.cicdhelper.notification

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.novolmob.cicdhelper.models.YandexScenario
import org.apache.logging.log4j.LogManager
import ru.novolmob.cicdhelper.yandex.YandexRepository

class YandexNotification(
    private val repository: YandexRepository,
    private val notificationQueue: MutableList<YandexScenario> = mutableListOf()
) {
    private val logger = LogManager.getLogger(this::class)
    private val mutex = Mutex()
    private val scope = CoroutineScope(SupervisorJob())
    private var job: Job = scope.launch {
        val iterator = notificationQueue.iterator()
        while (iterator.hasNext()) {
            val scenario = iterator.next()
            repository.scenarioActions(scenarioId = scenario.yandexScenarioId)?.let {
                if (it.status.lowercase() == "ok") {
                    logger.info("Started ${scenario.yandexScenarioId} Yandex scenario!")
                } else null
            } ?: logger.warn("Failed to start ${scenario.yandexScenarioId} Yandex scenario!")
            scenario.yandexDelay?.let {
                delay(timeMillis = it)
            }
        }
        mutex.withLock {
            notificationQueue.clear()
            startSendingNotification()
        }
    }

    suspend fun addToQueue(scenario: YandexScenario) {
        mutex.withLock {
            notificationQueue.add(scenario)
            startSendingNotification()
        }
    }

    private fun startSendingNotification() {
        if (notificationQueue.isNotEmpty()) {
            job.start()
        }
    }

}