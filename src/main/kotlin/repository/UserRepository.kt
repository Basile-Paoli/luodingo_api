package fr.ludodingo.repository

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

class DbUser(val id: Int, val username: String, val passwordHash: ByteArray)

enum class UserRole {
    USER,
    ADMIN
}

class UserRepository(val db: Database) {
    init {
        transaction(db) {
            initTable(UsersTable)
        }
    }

    object UsersTable : IntIdTable() {
        val username = varchar("username", 50).index().uniqueIndex()
        val passwordHash = binary("passwordHash", 1024)
        val role = enumeration<UserRole>("role").default(UserRole.USER)
    }

    suspend fun create(username: String, passwordHash: ByteArray): Int = query {
        insertAndGetId {
            it[UsersTable.username] = username
            it[UsersTable.passwordHash] = passwordHash
        }.value
    }

    suspend fun getByUsername(username: String): DbUser? = query {
        selectAll().where {
            UsersTable.username eq username
        }.firstOrNull()?.let {
            DbUser(
                it[UsersTable.id].value,
                it[UsersTable.username],
                it[UsersTable.passwordHash]
            )
        }
    }

    suspend inline fun <T> query(crossinline block: suspend UsersTable.() -> T): T = newSuspendedTransaction(db = db) {
        UsersTable.block()
    }
}
