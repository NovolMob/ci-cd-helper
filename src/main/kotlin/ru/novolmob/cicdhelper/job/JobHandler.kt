package ru.novolmob.cicdhelper.job

import ru.novolmob.cicdhelper.gitlab.GitlabRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.novolmob.cicdhelper.models.NotifiedJob
import ru.novolmob.cicdhelper.models.Settings
import ru.novolmob.cicdhelper.notification.NotificationService
import org.apache.logging.log4j.LogManager
import ru.novolmob.cicdhelper.models.GitlabJob
import ru.novolmob.cicdhelper.settings.SettingsService
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class JobHandler(
    private val settingsService: SettingsService,
    private val jobPool: JobPool,
    private val notificationService: NotificationService,
    private val delay: Duration = 1.seconds,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob())
) {
    private val logger = LogManager.getLogger(this::class)

    suspend fun start() = scope.launch {
        logger.info("Job handler is started!")
        while (true) {
            val settings = getSettings()
            val gitlabRepository = GitlabRepository(
                gitlabApiUrl = settings.gitlabApiUrl,
                gitlabToken = settings.gitlabToken
            )
            jobPool.jobs.forEach { notifiedJob ->
                scope.launch {
                    resolveJob(gitlabRepository = gitlabRepository, job = notifiedJob)
                }
            }
            delay(duration = delay)
        }
    }

    private suspend fun getSettings() = settingsService.getSettings()

    private suspend fun resolveJob(gitlabRepository: GitlabRepository, job: NotifiedJob) {
        val gitlabJob = gitlabRepository.getGitlabJob(job = job) ?: return
        val newStatus = gitlabJob.status
        val notifications = if (updateJobStatus(jobId = job.id, newStatus = newStatus)) {
            job.notifications.filterByStatus(status = newStatus)
        } else return
        if (notifications.isNotEmpty()) {
            logger.info("Sending ${notifications.size} notifications by ${job.projectId} Project!")
            notify(notifications = notifications)
        }
        if (gitlabJob.finished_at != null) {
            jobPool.remove(notifiedJob = job)
            logger.info("Removed [${job.id}] ${job.name}{${job.stage}} Job from pool!")
        }
    }

    private suspend fun notify(notifications: List<Settings.Notification>) {
        notificationService.notify(list = notifications)
    }

    private suspend fun GitlabRepository.getGitlabJob(job: NotifiedJob): GitlabJob? =
        getJob(projectId = job.projectId, jobId = job.id)

    private suspend fun updateJobStatus(jobId: Long, newStatus: String): Boolean =
        jobPool.updateJobStatus(jobId = jobId, newStatus = newStatus)

    private fun List<Settings.Notification>.filterByStatus(status: String) =
        filter { it.jobStatus == status || it.jobStatus == null }

}