package lv.divsk.route

import io.ktor.http.ContentType
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import lv.divsk.ext.local
import lv.divsk.greenhouseRepository

fun Route.rootPageRoute() {
    get("/") {
        val template = local("templates/index.html").readText()

        val greenhouseRows = greenhouseRepository.greenhouses.entries.joinToString("\n") { (id, gh) ->
            """
            <tr>
                <td>$id</td>
                <td>${gh.temperature}°C</td>
                <td>${gh.waterLevel}%</td>
                <td>
                    <form action="/request-update/$id" method="post" style="display:inline;">
                        <button type="submit">Update</button>
                    </form>
                </td>
            </tr>
            """
        }

        val htmlContent = template.replace("{{greenhouses}}", greenhouseRows)

        call.respondText(htmlContent, ContentType.Text.Html)
    }
}