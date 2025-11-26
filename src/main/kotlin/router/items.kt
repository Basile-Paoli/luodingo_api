package fr.ludodingo.router

import fr.ludodingo.repository.ItemRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.jetbrains.exposed.sql.Database

fun Application.itemsRouter(db: Database) {
    val itemsRepository = ItemRepository(db   )
    routing {
        route("items") {
            get {
                itemsRepository.listItemsByLanguageAndLevel(
                    languageCode = call.request.queryParameters["language"] ,
                    level = call.request.queryParameters["level"]?.toIntOrNull()
                ).let { items ->
                    call.respond(HttpStatusCode.OK, items)
                }
            }
        }
    }
}
