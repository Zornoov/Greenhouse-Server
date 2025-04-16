@file:Suppress("unused")

package lv.divsk.ext

import java.io.File
import java.nio.file.Paths

fun absolute(fileName: String): File {
    val path = Paths.get(fileName).toAbsolutePath().toString()
    return File(path)
}

fun local(fileName: String): File {
    val inputStream = object {}.javaClass.getResourceAsStream("/$fileName")
        ?: throw IllegalArgumentException("File not found in resources: $fileName")

    val tempFile = File.createTempFile("resource-", "-" + fileName.substringAfterLast("/"))
    tempFile.deleteOnExit()
    inputStream.use { input -> tempFile.outputStream().use { output -> input.copyTo(output) } }

    return tempFile
}