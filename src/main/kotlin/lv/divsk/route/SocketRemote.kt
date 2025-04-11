package lv.divsk.route

import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import lv.divsk.greenhouseRepository
import java.util.*

fun Route.socketRoutes() {
    webSocket("/connect") {
        val uuid = UUID.randomUUID()
        greenhouseRepository.createGreenhouse(uuid, 0, 0, this)
        send(uuid.toString())
        greenhouseRepository.requestUpdate(uuid)
        try {
            incoming.consumeEach { frame ->
                if (frame is Frame.Text && frame.readText() == "pong") { /* handle ping */ }
            }
        } catch (e: Exception) {
            println("Error in /connect: ${e.message}")
        } finally {
            greenhouseRepository.removeBySession(this)
        }
    }

    webSocket("/data") {
        incoming.consumeEach { frame ->
            if (frame is Frame.Text) {
                frame.readText().split(" ").takeIf { it.size == 3 }?.let { parts ->
                    val id = UUID.fromString(parts[0])
                    val temp = parts[1].toIntOrNull()
                    val water = parts[2].toIntOrNull()
                    if (temp != null && water != null) {
                        greenhouseRepository.updateGreenhouse(id, temp, water)
                    }
                }
            }
        }
    }
}