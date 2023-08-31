package ru.novolmob.cicdhelper.notification

import ru.novolmob.cicdhelper.models.Settings
import ru.novolmob.cicdhelper.settings.SettingsService
import ru.novolmob.cicdhelper.yandex.YandexRepository

class NotificationService(
    private val settingsService: SettingsService
) {

    private suspend fun getSettings() = settingsService.getSettings()

    suspend fun notify(list: List<Settings.Notification>) {
        val settings = getSettings()
        val scenarios = settings.getScenarios(names = list.getScenarioNames())
        list.sortedByDescending { it.priority }
            .mapNotNull { scenarios[it.scenario] }
            .forEach { startScenario(settings = settings, scenario = it) }
    }

    suspend fun notify(notification: Settings.Notification) {
        val settings = getSettings()
        val scenario = settings.getScenario(name = notification.scenario) ?: return
        startScenario(settings = settings, scenario = scenario)
    }

    private suspend fun startScenario(settings: Settings, scenario: Settings.Scenario) {
        val yandexNotification = settings.getYandexNotification()
        if (scenario.isYandex()) yandexNotification.addToQueue(scenario = scenario)
    }

    private fun Settings.Scenario.isYandex(): Boolean =
        yandexScenarioId.isNotBlank()

    private fun List<Settings.Notification>.getScenarioNames(): List<String> =
        map { it.scenario }.distinct()

    private fun Settings.getScenario(name: String): Settings.Scenario? =
        scenarios.find { it.name == name }

    private fun Settings.getScenarios(names: List<String>): Map<String, Settings.Scenario> =
        scenarios
            .filter { it.name in names }
            .associateBy { it.name }

    private fun Settings.getYandexNotification(): YandexNotification =
        YandexNotification(
            repository = YandexRepository(
                yandexApiUrl = yandexApiUrl,
                yandexKey = yandexKey
            )
        )

}