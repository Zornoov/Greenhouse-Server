package lv.divsk

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import java.util.*
import kotlin.time.Duration.Companion.seconds

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 300.seconds
    }

    routing {
        get("/") {
            val html = buildString {
                appendLine("<html><head><title>Список Теплиц</title></head><body>")
                appendLine("<h1>Список Теплиц</h1>")
                appendLine("<ul>")
                for ((id, gh) in greenhouseRepository.greenhouses) {
                    appendLine("<li>")
                    appendLine("ID: $id | Temp: ${gh.temperature}°C | Water: ${gh.waterLevel}%")
                    appendLine("""
                    <form action="/request-update/$id" method="post" style="display:inline;">
                        <button type="submit">Обновить</button>
                    </form>
                """.trimIndent())

                    appendLine("</li>")
                }
                appendLine("</ul>")
                appendLine("</body></html>")
            }

            call.respondText(html, ContentType.Text.Html)
        }

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

        webSocket("/connect") {
            val uuid = UUID.randomUUID()
            greenhouseRepository.createGreenhouse(uuid, 0, 0, this)
            send(uuid.toString())
            greenhouseRepository.requestUpdate(uuid)
            try {
                incoming.consumeEach { frame ->
                    if (frame is Frame.Text && frame.readText() == "pong") { }
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
}