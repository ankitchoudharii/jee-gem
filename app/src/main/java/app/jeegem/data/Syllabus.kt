package app.jeegem.data

import android.content.Context
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class SyllabusPack(
    val version: Int = 2,
    val exam: String = "",
    val verifiedOn: String = "",
    val source: String = "",
    val subjects: List<SubjectPack> = emptyList(),
)

@Serializable
data class SubjectPack(
    val id: String,
    val name: String,
    val tint: String = "#4C8DFF",
    val units: List<UnitPack> = emptyList(),
)

@Serializable
data class UnitPack(
    val id: String,
    val name: String,
    val priority: String = "medium",
    val topics: List<String> = emptyList(),
)

data class Chapter(
    val id: String,
    val subjectId: String,
    val subjectName: String,
    val subjectTint: String,
    val unitId: String,
    val unitName: String,
    val name: String,
    val priority: String,
)

object SyllabusLoader {
    private val json = Json { ignoreUnknownKeys = true }

    fun load(context: Context): SyllabusPack =
        context.assets.open("syllabus.json").bufferedReader().use { reader ->
            json.decodeFromString(SyllabusPack.serializer(), reader.readText())
        }

    fun flatten(pack: SyllabusPack): List<Chapter> =
        pack.subjects.flatMap { subject ->
            subject.units.flatMap { unit ->
                unit.topics.mapIndexed { index, topic ->
                    Chapter(
                        id = "${unit.id}#$index",
                        subjectId = subject.id,
                        subjectName = subject.name,
                        subjectTint = subject.tint,
                        unitId = unit.id,
                        unitName = unit.name,
                        name = topic,
                        priority = unit.priority,
                    )
                }
            }
        }
}
