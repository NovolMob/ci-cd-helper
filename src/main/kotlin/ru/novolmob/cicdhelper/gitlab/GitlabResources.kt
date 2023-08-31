package ru.novolmob.cicdhelper.gitlab

import io.ktor.resources.*
import kotlinx.serialization.Serializable

@Serializable
@Resource("/projects")
class Projects() {

    @Serializable
    @Resource("/{id}")
    class ID(
        val id: String,
        val projects: Projects = Projects()
    ) {

        @Serializable
        @Resource("/jobs")
        class Jobs(
            val id: Projects.ID,
            val scope: String? = null
        ) {

            @Serializable
            @Resource("/{jobId}")
            class ID(
                val jobId: Long,
                val jobs: Jobs
            )

        }

    }

}