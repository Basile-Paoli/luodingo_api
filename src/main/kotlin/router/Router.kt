package fr.ludodingo.router

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import org.jetbrains.exposed.sql.Database

fun Application.router(db: Database) {
    cors()
    auth()
    configureSerialization()
    authRoutes(db)

    routing {
        get("/") { call.respond(HttpStatusCode.OK, mapOf("message" to "Hello World!")) }

        authenticate {
            itemsRouter(db)
            usersRoutes(db)
        }
        // Public routes (or handle auth inside)
        coursesRoutes(db)
    }
}
