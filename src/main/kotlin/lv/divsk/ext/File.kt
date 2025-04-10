@file:Suppress("unused")

package lv.divsk.ext

import java.io.File
import java.nio.file.Paths

fun absolute(fileName: String): File {
    val path = Paths.get(fileName).toAbsolutePath().toString()
    return File(path)
}

fun local(fileName: String): File {
    val fileUrl = object {}.javaClass.getResource("/$fileName")
        ?: throw IllegalArgumentException("File not found in resources: $fileName")
    return File(fileUrl.toURI())
}