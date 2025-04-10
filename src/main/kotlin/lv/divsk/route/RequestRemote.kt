package lv.divsk.route

import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import lv.divsk.greenhouseRepository
import java.util.UUID

fun Route.updateRequestRoutes() {
    post("/request-update/{id}") {
        val id = call.parameters["id"]?.let(UUID::fromString)
        if (id != null) {
            greenhouseRepository.requestUpdate(id)
        }
        call.respondRedirect("/")
    }

    post("/update-water/{id}") {
        val id = call.parameters["id"]?.let(UUID::fromString)
        if (id != null) {
            greenhouseRepository.startWatering(id)
        }
        call.respondRedirect("/")
    }
}