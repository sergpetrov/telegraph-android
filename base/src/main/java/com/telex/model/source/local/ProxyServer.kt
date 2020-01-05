package com.telex.model.source.local

/**
 * @author Sergey Petrov
 */
data class ProxyServer(
    val type: Type,
    val host: String,
    val port: Int,
    val user: String?,
    val password: String?,
    var enabled: Boolean
) {
    enum class Type { HTTP, SOCKS }
}
