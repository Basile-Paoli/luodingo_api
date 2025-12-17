package fr.ludodingo.repository

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class DbUser(
        val id: Int,
        val username: String,
        val passwordHash: ByteArray,
        val currentLanguage: String?,
        val currentLevel: String,
        val xp: Int,
        val gems: Int,
        val hearts: Int
)

enum class UserRole {
    USER,
    ADMIN
}

class UserRepository(val db: Database) {
    object UsersTable : IntIdTable() {
        val username = varchar("username", 50).index().uniqueIndex()
        val passwordHash = binary("passwordHash", 1024)
        val role = enumeration<UserRole>("role").default(UserRole.USER)
        val currentLanguage = varchar("current_language", 10).nullable()
        val currentLevel = varchar("current_level", 20).default("UNKNOWN")
        val xp = integer("xp").default(0)
        val gems = integer("gems").default(0)
        val hearts = integer("hearts").default(5)
    }

    object UserProgressTable : IntIdTable() {
        val user = reference("user", UsersTable)
        val lessonId =
                varchar("lesson_id", 50) // Assuming lesson IDs are strings from the frontend/seed
        val score = integer("score")
        val timestamp = long("timestamp")

        init {
            uniqueIndex(user, lessonId)
        }
    }

    init {
        transaction(db) {
            initTable(UsersTable)
            initTable(UserProgressTable)
        }
    }

    suspend fun create(username: String, passwordHash: ByteArray, language: String? = "en"): Int =
            query {
                insertAndGetId {
                            it[UsersTable.username] = username
                            it[UsersTable.passwordHash] = passwordHash
                            it[UsersTable.currentLanguage] = language
                            it[UsersTable.currentLevel] = "UNKNOWN"
                        }
                        .value
            }

    suspend fun getByUsername(username: String): DbUser? = query {
        selectAll().where { UsersTable.username eq username }.firstOrNull()?.let {
            DbUser(
                    it[UsersTable.id].value,
                    it[UsersTable.username],
                    it[UsersTable.passwordHash],
                    it[UsersTable.currentLanguage],
                    it[UsersTable.currentLevel],
                    it[UsersTable.xp],
                    it[UsersTable.gems],
                    it[UsersTable.hearts]
            )
        }
    }

    suspend fun getUserProgress(userId: Int): List<CompletedLesson> =
            newSuspendedTransaction(db = db) {
                UserProgressTable.selectAll().where { UserProgressTable.user eq userId }.map {
                    CompletedLesson(it[UserProgressTable.lessonId], it[UserProgressTable.score])
                }
            }

    suspend fun addProgress(userId: Int, lessonId: String, score: Int) =
            newSuspendedTransaction(db = db) {
                // Check if exists, update if score is higher? Or just ignore?
                // For now, simple insert or update
                val existing =
                        UserProgressTable.selectAll()
                                .where {
                                    (UserProgressTable.user eq userId) and
                                            (UserProgressTable.lessonId eq lessonId)
                                }
                                .firstOrNull()

                if (existing == null) {
                    UserProgressTable.insertAndGetId {
                        it[user] = userId
                        it[UserProgressTable.lessonId] = lessonId
                        it[UserProgressTable.score] = score
                        it[timestamp] = System.currentTimeMillis()
                    }

                    // Add XP
                    UsersTable.update({ UsersTable.id eq userId }) {
                        with(SqlExpressionBuilder) {
                            it.update(xp, xp + 10) // Fixed XP for now
                        }
                    }
                } else {
                    // Update score if higher
                    val currentScore = existing[UserProgressTable.score]
                    if (score > currentScore) {
                        UserProgressTable.update({
                            UserProgressTable.id eq existing[UserProgressTable.id]
                        }) { it[UserProgressTable.score] = score }
                    }
                }
            }

    suspend fun updateLevel(userId: Int, level: String) =
            newSuspendedTransaction(db = db) {
                UsersTable.update({ UsersTable.id eq userId }) {
                    it[UsersTable.currentLevel] = level
                }
            }

    suspend inline fun <T> query(crossinline block: suspend UsersTable.() -> T): T =
            newSuspendedTransaction(db = db) { UsersTable.block() }
}

data class CompletedLesson(val lessonId: String, val score: Int)
