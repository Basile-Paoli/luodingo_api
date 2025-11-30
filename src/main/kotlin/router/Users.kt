package fr.ludodingo.router

import fr.ludodingo.repository.UserRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Database

@Serializable
data class UserProfileDto(
        val id: Int,
        val username: String,
        val currentLanguage: String?,
        val currentLevel: String,
        val xp: Int,
        val gems: Int,
        val hearts: Int,
        val completedLessons: List<CompletedLessonDto>
)

@Serializable data class CompletedLessonDto(val lessonId: String, val score: Int)

@Serializable data class CompleteLessonRequest(val lessonId: String, val score: Int)

@Serializable data class UpdateLevelRequest(val level: String)

fun Route.usersRoutes(db: Database) {
    val userRepository = UserRepository(db)

    route("/users") {
        authenticate {
            get("/me") {
                val principal = call.principal<JWTPrincipal>()
                val username = principal!!.payload.getClaim("username").asString()

                val user = userRepository.getByUsername(username)
                if (user != null) {
                    val progress = userRepository.getUserProgress(user.id)
                    call.respond(
                            UserProfileDto(
                                    id = user.id,
                                    username = user.username,
                                    currentLanguage = user.currentLanguage,
                                    currentLevel = user.currentLevel,
                                    xp = user.xp,
                                    gems = user.gems,
                                    hearts = user.hearts,
                                    completedLessons =
                                            progress.map {
                                                CompletedLessonDto(it.lessonId, it.score)
                                            }
                            )
                    )
                } else {
                    call.respond(HttpStatusCode.NotFound, "User not found")
                }
            }

            post("/me/progress") {
                val principal = call.principal<JWTPrincipal>()
                val username = principal!!.payload.getClaim("username").asString()
                val user = userRepository.getByUsername(username)

                if (user != null) {
                    val request = call.receive<CompleteLessonRequest>()
                    userRepository.addProgress(user.id, request.lessonId, request.score)
                    call.respond(HttpStatusCode.OK, mapOf("status" to "success"))
                } else {
                    call.respond(HttpStatusCode.NotFound, "User not found")
                }
            }

            post("/me/level") {
                val principal = call.principal<JWTPrincipal>()
                val username = principal!!.payload.getClaim("username").asString()
                val user = userRepository.getByUsername(username)

                if (user != null) {
                    val request = call.receive<UpdateLevelRequest>()
                    userRepository.updateLevel(user.id, request.level)
                    call.respond(HttpStatusCode.OK, mapOf("status" to "success"))
                } else {
                    call.respond(HttpStatusCode.NotFound, "User not found")
                }
            }
        }
    }
}
