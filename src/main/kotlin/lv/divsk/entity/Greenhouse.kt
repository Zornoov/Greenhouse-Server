package lv.divsk.entity

import io.ktor.websocket.*
import java.util.*

data class Greenhouse(
    val id: UUID,
    val waterLevel: Int, // 0-100
    val temperature: Int, // 0-100
    val session: WebSocketSession
)