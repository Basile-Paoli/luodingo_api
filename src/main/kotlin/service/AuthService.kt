package fr.ludodingo.service

import fr.ludodingo.repository.UserRepository
import fr.ludodingo.service.Result.Companion.err
import fr.ludodingo.service.Result.Companion.ok
import io.ktor.http.HttpStatusCode
import io.ktor.util.getDigestFunction
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Database


@Serializable
data class Credentials(val username: String, val password: String)

class AuthService(db: Database) {
    private val repository = UserRepository(db)
    private val digest = getDigestFunction("SHA-256", {
        env.get("PASSWORD_SALT")
    })

    suspend fun register(credentials: Credentials): Result<Int> {
        val passwordHash = digest(credentials.password)
        try {
            val id = repository.create(credentials.username, passwordHash)
            return ok(id)
        } catch (e: ExposedSQLException) {
            println(e.message + " " + e.sqlState)
            if (e.sqlState == "23505") {
                return err(HttpStatusCode.BadRequest, "Username already exists")
            }
            return err(HttpStatusCode.InternalServerError, "Internal server error")
        }
    }

    suspend fun login(credentials: Credentials): Int? {
        val user = repository.getByUsername(credentials.username) ?: return null
        val passwordHash = digest(credentials.password)
        if (user.passwordHash.contentEquals(passwordHash)) {
            return user.id
        }
        return null
    }
}
