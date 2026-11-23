package app.jeegem.data

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import java.time.LocalDate

private val storeJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

object AppStateSerializer : Serializer<AppState> {
    override val defaultValue: AppState = AppState()

    override suspend fun readFrom(input: InputStream): AppState {
        val text = input.readBytes().decodeToString()
        if (text.isBlank()) return defaultValue
        return try {
            storeJson.decodeFromString(AppState.serializer(), text)
        } catch (e: SerializationException) {
            throw CorruptionException("Saved state could not be read", e)
        }
    }

    override suspend fun writeTo(t: AppState, output: OutputStream) {
        output.write(storeJson.encodeToString(AppState.serializer(), t).encodeToByteArray())
    }
}

private val Context.gemDataStore: DataStore<AppState> by dataStore(
    fileName = "jee_gem_state.json",
    serializer = AppStateSerializer,
)

class GemRepository(context: Context) {
    private val store = context.applicationContext.gemDataStore

    val state: Flow<AppState> = store.data

    suspend fun saveSettings(settings: Settings) {
        store.updateData { it.copy(settings = settings) }
    }

    suspend fun recordStatus(chapterId: String, status: ChapterStatus, today: LocalDate) {
        store.updateData { state ->
            val previous = state.chapters[chapterId] ?: ChapterState()
            val next = when (status) {
                ChapterStatus.NOT_STARTED -> previous.copy(
                    status = status,
                    lastStudiedEpochDay = null,
                    revisionStage = 0,
                )

                ChapterStatus.LEARNING -> previous.copy(
                    status = status,
                    lastStudiedEpochDay = today.toEpochDay(),
                    revisionStage = 0,
                )

                ChapterStatus.REVISED -> previous.copy(
                    status = status,
                    lastStudiedEpochDay = today.toEpochDay(),
                    revisionStage = minOf(previous.revisionStage + 1, 3),
                )

                ChapterStatus.MASTERED -> previous.copy(
                    status = status,
                    lastStudiedEpochDay = today.toEpochDay(),
                    revisionStage = 3,
                )
            }
            state.copy(
                chapters = state.chapters + (chapterId to next.copy(updatedAt = System.currentTimeMillis())),
            )
        }
    }

    suspend fun addSession(session: StudySession) {
        store.updateData { state ->
            state.copy(sessions = (state.sessions + session).takeLast(2000))
        }
    }

    suspend fun clearAll() {
        store.updateData { AppState() }
    }
}
