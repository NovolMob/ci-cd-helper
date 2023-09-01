@file:OptIn(ExperimentalSerializationApi::class)

package ru.novolmob.cicdhelper.settings

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import ru.novolmob.cicdhelper.models.Settings
import org.apache.logging.log4j.LogManager
import java.io.File
import kotlin.io.use

class SettingsService(
    private val settingsFile: File = File("settings.json"),
    private val json: Json = Json {
        encodeDefaults = true
        prettyPrint = true
        ignoreUnknownKeys = true
        isLenient = true
    }
) {
    @OptIn(InternalCoroutinesApi::class)
    private val context = Dispatchers.IO.newCoroutineContext(addedContext = newFixedThreadPoolContext(nThreads = 5, name = "SettingsService"))
    private val logger = LogManager.getLogger(this::class)
    private val mutex = Mutex()
    private var lastSettingUpdate: Long = 0
    private lateinit var settings: Settings

    private fun loadSettings(file: File = settingsFile): Settings =
        file.inputStream().use {
            json.decodeFromStream(stream = it)
        }

    private fun loadOrCreateSettings(file: File = settingsFile): Settings {
        return if (file.exists()) loadSettings(file = file) else {
            getAndCreateSettings(file = file)
        }
    }

    private fun getAndCreateSettings(file: File = settingsFile, settings: Settings = DEFAULT_SETTINGS): Settings {
        file.createNewFile()
        file.outputStream().use {
            json.encodeToStream(value = settings, stream = it)
        }
        return settings
    }

    suspend fun getSettings(): Settings {
        mutex.withLock {
            if (!settingsFile.exists()) {
                withContext(context) {
                    settings = loadOrCreateSettings()
                    lastSettingUpdate = settingsFile.lastModified()
                    logger.info("Default settings created!")
                }
            } else {
                val update = settingsFile.lastModified()
                if (update != lastSettingUpdate) {
                    withContext(context) {
                        settings = try {
                            loadSettings()
                        } catch (e: Exception) {
                            getAndCreateSettings()
                        }
                        lastSettingUpdate = settingsFile.lastModified()
                        logger.info("Settings updated!")
                    }
                }
            }
        }

        return settings
    }

    companion object {

        val DEFAULT_SETTINGS = Settings()

    }

}