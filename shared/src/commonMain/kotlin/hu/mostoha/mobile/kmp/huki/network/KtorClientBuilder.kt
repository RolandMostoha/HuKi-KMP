package hu.mostoha.mobile.kmp.huki.network

import hu.mostoha.mobile.kmp.huki.util.isDebugBuild
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

private val REQUEST_TIMEOUT = 30.seconds
private val CONNECT_TIMEOUT = 10.seconds
private val SOCKET_TIMEOUT = 10.seconds

fun createHttpClient(engine: HttpClientEngine): HttpClient =
    HttpClient(engine) {
        install(Logging) {
            logger = Logger.SIMPLE
            level = if (isDebugBuild) LogLevel.ALL else LogLevel.NONE
        }
        install(HttpTimeout) {
            requestTimeoutMillis = REQUEST_TIMEOUT.inWholeMilliseconds
            connectTimeoutMillis = CONNECT_TIMEOUT.inWholeMilliseconds
            socketTimeoutMillis = SOCKET_TIMEOUT.inWholeMilliseconds
        }
        install(ContentNegotiation) {
            json(
                json = Json {
                    ignoreUnknownKeys = true
                },
            )
        }
    }
