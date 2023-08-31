package ru.novolmob.cicdhelper

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.joinAll
import ru.novolmob.cicdhelper.job.JobHandler
import ru.novolmob.cicdhelper.job.JobPool
import ru.novolmob.cicdhelper.job.ProjectJobsHandler
import ru.novolmob.cicdhelper.notification.NotificationService
import ru.novolmob.cicdhelper.settings.SettingsService


suspend fun main() {
    val scope = CoroutineScope(SupervisorJob())
    val settingsService = SettingsService()
    val jobPool = JobPool()
    val notificationService = NotificationService(
        settingsService = settingsService
    )

    val projectJobsHandler = ProjectJobsHandler(
        settingsService = settingsService,
        jobPool = jobPool,
        scope = scope
    ).start()

    val jobHandler = JobHandler(
        settingsService = settingsService,
        jobPool = jobPool,
        notificationService = notificationService,
        scope = scope
    ).start()

    joinAll(projectJobsHandler, jobHandler)
}

