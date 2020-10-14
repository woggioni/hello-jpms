package net.woggioni.vminfo

fun main() {
    System.getProperties().asSequence()
            .sortedBy { it.key as String }
            .filter {
                val value = it.value
                when(value) {
                    null -> false
                    is String -> !value.isBlank()
                    else -> true
                }
            }
            .forEach { (key, value) ->
        println("$key -> $value")
    }
}