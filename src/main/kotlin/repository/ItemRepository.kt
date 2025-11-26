package fr.ludodingo.repository

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class Item(
    val word: String,
    val translation: String,
    val audioUrl: String,
    val level: Int,
    val languageCode: String
)

@Serializable
private data class GenericItem(
    val name: String,
    val translation: String,
    val audioUrl: String,
    val level: Int
)

class ItemRepository(val db: Database) {
    object ItemsTable : IntIdTable() {
        val word = varchar("word", 100)
        val translation = varchar("translation", 100)
        val audioUrl = varchar("audio_url", 255)
        val level = integer("level")
        val language = reference("language", LanguageRepository.LanguagesTable.code)

        init {
            index(false, language, level)
        }
    }

    init {
        init()
    }

    fun init() {
        transaction(db) {
            LanguageRepository.seedData()
            initTable(ItemsTable)
            seedData()
        }
    }


    private fun seedData() {


        transaction(db = db) {
            if (ItemsTable.selectAll().empty().not()) {
                return@transaction
            }

            val filePath = "/items/en.json"
            val file = this::class.java.getResourceAsStream(filePath)
            val englishItems: List<GenericItem> = Json.decodeFromString(
                file!!.readBytes().toString(Charsets.UTF_8)
            )

            ItemsTable.batchInsert(englishItems) {
                this[ItemsTable.word] = it.name
                this[ItemsTable.translation] = it.translation
                this[ItemsTable.audioUrl] = it.audioUrl
                this[ItemsTable.level] = it.level
                this[ItemsTable.language] = "en"
            }
        }
    }

    suspend fun listItemsByLanguageAndLevel(languageCode: String?, level: Int?): List<Item> {
        var filter: Op<Boolean> = Op.TRUE
        if (languageCode != null) {
            filter = filter and (ItemsTable.language eq languageCode)
        }
        if (level != null) {
            filter = filter and (ItemsTable.level lessEq  level)
        }

        return newSuspendedTransaction(db = db) {
            ItemsTable.selectAll().where(filter).map {
                Item(
                    word = it[ItemsTable.word],
                    translation = it[ItemsTable.translation],
                    audioUrl = it[ItemsTable.audioUrl],
                    level = it[ItemsTable.level],
                    languageCode = it[ItemsTable.language]
                )
            }
        }
    }

}
