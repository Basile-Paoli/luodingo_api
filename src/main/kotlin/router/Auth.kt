package fr.ludodingo.router

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import fr.ludodingo.service.AuthService
import fr.ludodingo.service.Credentials
import fr.ludodingo.service.RegisterRequest
import fr.ludodingo.service.env
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import org.jetbrains.exposed.sql.Database

fun Application.auth() {
    val issuer = env.get("JWT_ISSUER")
    val jwtSecret = env.get("JWT_SECRET")
    authentication {
        jwt {
            verifier(
                    JWT.require(Algorithm.HMAC256(jwtSecret))
                            .withIssuer(issuer)
                            .withClaimPresence("id")
                            .build()
            )
            validate { JWTPrincipal(it.payload) }

            challenge { _, _ -> call.respond(HttpStatusCode.Unauthorized, "Unauthorized") }
        }
    }
}

fun Application.authRoutes(db: Database) {
    val authService = AuthService(db)
    routing {
        post("/login") {
            val credentials = call.receive<Credentials>()
            val userId = authService.login(credentials)
            if (userId == null) {
                call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
                return@post
            }

            val token =
                    JWT.create()
                            .withIssuer(env.get("JWT_ISSUER"))
                            .withClaim("id", userId)
                            .withClaim("username", credentials.username)
                            .sign(Algorithm.HMAC256(env.get("JWT_SECRET")))

            call.respond(mapOf("token" to token))
        }

        post("/register") {
            val request = call.receive<RegisterRequest>()
            authService.register(request).onFailure { error ->
                call.respond(error.httpStatus, mapOf("error" to error.message))
                return@post
            }

            call.respond(HttpStatusCode.Created)
        }
    }
}

fun RoutingContext.getUserId(): Int {
    val principal =
            call.authentication.principal<JWTPrincipal>()
                    ?: throw IllegalStateException("No JWT principal found")
    return principal.getClaim("id", Int::class)!!
}
