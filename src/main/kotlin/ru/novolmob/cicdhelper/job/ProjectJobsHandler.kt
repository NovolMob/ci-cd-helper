package ru.novolmob.cicdhelper.job

import kotlinx.coroutines.*
import ru.novolmob.cicdhelper.gitlab.GitlabRepository
import ru.novolmob.cicdhelper.models.GitlabJob
import ru.novolmob.cicdhelper.models.NotifiedJob
import ru.novolmob.cicdhelper.models.Settings
import org.apache.logging.log4j.LogManager
import ru.novolmob.cicdhelper.settings.SettingsService
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class ProjectJobsHandler(
    private val settingsService: SettingsService,
    private val jobPool: JobPool,
    private val delay: Duration = 1.seconds,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob())
) {
    private val logger = LogManager.getLogger(this::class)

    suspend fun start() = scope.launch(
        context = newSingleThreadContext("ProjectJobsHandler")
    ) {
        logger.info("Project jobs handler is started!")
        while (true) {
            val settings = getSettings()
            val gitlabRepository = GitlabRepository(
                gitlabApiUrl = settings.gitlabApiUrl,
                gitlabToken = settings.gitlabToken
            )
            settings.projects.forEach { project ->
                resolveProject(
                    project = project,
                    gitlabRepository = gitlabRepository,
                    generalNotifications = settings.notifications
                )
            }
            delay(duration = delay)
        }
    }

    private suspend fun getSettings() = settingsService.getSettings()

    private suspend fun resolveProject(
        project: Settings.Project,
        gitlabRepository: GitlabRepository,
        generalNotifications: List<Settings.Notification>
    ) {
        val jobs = gitlabRepository
            .getProjectJobs(projectId = project.projectId, status = project.jobFilter)
        putJobs(
            projectId = project.projectId,
            list = jobs,
            projectNotifications = (project.notifications ?: generalNotifications)
        )
    }

    private suspend fun putJobs(projectId: String, list: List<GitlabJob>, projectNotifications: List<Settings.Notification>) {
        val collection = list.mapNotNull { job ->
            if (jobPool.containsJob(jobId = job.id)) return@mapNotNull null
            val jobNotifications = projectNotifications.filterByJob(job = job)
            NotifiedJob(
                id = job.id,
                name = job.name,
                stage = job.stage,
                projectId = projectId,
                notifications = jobNotifications
            )
        }
        if (collection.isNotEmpty()) {
            logger.info("Handle ${collection.size} jobs for Projects $projectId.")
            jobPool.forcePutAll(collection = collection)
        }
    }

    private fun List<Settings.Notification>.filterByJob(job: GitlabJob) =
        filter { (it.jobName == job.name || it.jobName == null) && (it.jobStage == job.stage || it.jobStage == null) }

}