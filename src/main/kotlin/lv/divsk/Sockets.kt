package lv.divsk

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import lv.divsk.route.rootPageRoute
import lv.divsk.route.socketRoutes
import lv.divsk.route.updateRequestRoutes
import kotlin.time.Duration.Companion.seconds

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 300.seconds
    }

    routing {
        rootPageRoute()
        updateRequestRoutes()
        socketRoutes()
    }
}
