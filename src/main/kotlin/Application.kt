package fr.ludodingo

import fr.ludodingo.repository.connectToDatabase
import fr.ludodingo.router.router
import io.ktor.server.application.Application


fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val database = connectToDatabase()
    router(database)
}
