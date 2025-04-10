package lv.divsk.repository

import io.ktor.websocket.*
import lv.divsk.entity.Greenhouse
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class GreenhouseRepository {
    val logger = LoggerFactory.getLogger(GreenhouseRepository::class.java)

    val greenhouses = ConcurrentHashMap<UUID, Greenhouse>()

    fun createGreenhouse(id: UUID,waterLevel: Int, temperature: Int, session: WebSocketSession): Boolean {
        val greenhouse = Greenhouse(id, waterLevel, temperature, session)
        greenhouses[id] = greenhouse
        logger.info("Connected new greenhouse: $id | Temp=$temperature | Water=$waterLevel")
        return true
    }

    fun updateGreenhouse(id: UUID, temperature: Int, waterLevel: Int) {
        val gh = greenhouses[id]
        if (gh != null) {
            greenhouses[id] = gh.copy(temperature = temperature, waterLevel = waterLevel)
            logger.info("Updated data for greenhouse: $id | Temp=$temperature | Water=$waterLevel")
        }
    }

    fun removeBySession(session: WebSocketSession): Boolean {
        val entry = greenhouses.entries.find { it.value.session == session }
        entry?.let {
            greenhouses.remove(it.key)
            logger.info("Disconnected greenhouse: ${it.key}")
            return true
        }
        return false
    }

    suspend fun requestUpdate(id: UUID) {
        greenhouses[id]?.session?.send("update_request")
    }

    suspend fun startWatering(id: UUID) {
        greenhouses[id]?.session?.send("start_watering")
    }

    fun getGreenhouseById(id: UUID): Greenhouse? = greenhouses[id]
}