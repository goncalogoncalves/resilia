package com.goncalogoncalves.resilia.tools.contentvalidator

import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val root = File(args.firstOrNull() ?: "content")
    val errors = ContentValidator(root).validate()
    if (errors.isEmpty()) {
        println("Content OK: ${root.absolutePath}")
        return
    }
    System.err.println("Content validation failed (${errors.size} error(s)):")
    errors.forEach { System.err.println("  - $it") }
    exitProcess(1)
}
