package ru.novolmob.cicdhelper.job

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ru.novolmob.cicdhelper.models.NotifiedJob
import org.apache.logging.log4j.LogManager

class JobPool(
    private val mutableMap: MutableMap<Long, NotifiedJob> = mutableMapOf(),
    private val currentStatuses: MutableMap<Long, String> = mutableMapOf()
) {
    private val logger = LogManager.getLogger(this::class)
    private val mutex = Mutex()
    val jobs: List<NotifiedJob>
        get() = mutableMap.values.toList()

    suspend fun putAll(collection: Collection<NotifiedJob>) =
        forcePutAll(collection = collection.filter { !containsJob(jobId = it.id) })

    suspend fun forcePutAll(collection: Collection<NotifiedJob>) =
        mutex.withLock {
            mutableMap.putAll(collection.associateBy { it.id })
        }

    suspend fun remove(notifiedJob: NotifiedJob) =
        mutex.withLock {
            mutableMap.remove(notifiedJob.id)
        }

    fun containsJob(jobId: Long): Boolean =
        currentStatuses.contains(key = jobId)

    suspend fun updateJobStatus(jobId: Long, newStatus: String): Boolean =
        mutex.withLock {
            val savedStatus = currentStatuses.put(key = jobId, value = newStatus)
            if (savedStatus != newStatus) {
                logger.info("Job $jobId change status from $savedStatus to $newStatus.")
                true
            } else false
        }

}