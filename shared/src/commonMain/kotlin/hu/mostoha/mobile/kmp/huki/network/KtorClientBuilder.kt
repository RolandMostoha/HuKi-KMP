package hu.mostoha.mobile.kmp.huki.network

import hu.mostoha.mobile.kmp.huki.util.isDebugBuild
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

fun createHttpClient(engine: HttpClientEngine): HttpClient =
    HttpClient(engine) {
        install(Logging) {
            logger = Logger.SIMPLE
            level = if (isDebugBuild) LogLevel.ALL else LogLevel.NONE
        }
        install(ContentNegotiation) {
            json(
                json = Json {
                    ignoreUnknownKeys = true
                },
            )
        }
    }
