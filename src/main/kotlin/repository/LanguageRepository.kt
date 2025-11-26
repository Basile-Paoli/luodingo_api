package fr.ludodingo.repository

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object LanguageRepository {
    object LanguagesTable : Table() {
        val name = varchar("name", 100).uniqueIndex()
        val code = varchar("code", 10)
        override val primaryKey = PrimaryKey(code)
    }

    init {
        init()
    }

    fun init() {
        transaction {
            initTable(LanguagesTable)
            seedData()
        }
    }

    fun seedData() {
        if (LanguagesTable.selectAll().empty().not()) {
            return
        }

        val languages = listOf(
            "English" to "en",
            "Japanese" to "ja",
        )
        LanguagesTable.batchInsert(languages) { (name, code) ->
            this[LanguagesTable.name] = name
            this[LanguagesTable.code] = code
        }

    }

    suspend fun <T> query(block: suspend LanguagesTable.() -> T): T = newSuspendedTransaction {
        LanguagesTable.block()
    }

}
