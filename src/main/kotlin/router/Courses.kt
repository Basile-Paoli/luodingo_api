package fr.ludodingo.router

import fr.ludodingo.repository.CourseRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import org.jetbrains.exposed.sql.Database

fun Route.coursesRoutes(db: Database) {
    val courseRepository = CourseRepository(db)

    route("/courses") {
        get {
            val language = call.request.queryParameters["language"]
            val courses = courseRepository.getCourses(language)
            call.respond(courses)
        }

        get("/{id}") {
            // Not implemented yet, usually returns course details
            call.respond(HttpStatusCode.NotImplemented)
        }

        get("/{courseId}/lessons/{lessonId}") {
            val lessonId = call.parameters["lessonId"]
            if (lessonId == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing lessonId")
                return@get
            }

            val lesson = courseRepository.getLesson(lessonId)
            if (lesson != null) {
                call.respond(lesson)
            } else {
                call.respond(HttpStatusCode.NotFound, "Lesson not found")
            }
        }
    }
}
