package hu.mostoha.mobile.kmp.huki.util

import androidx.lifecycle.SavedStateHandle
import androidx.savedstate.SavedState
import androidx.savedstate.read
import androidx.savedstate.savedState
import co.touchlab.kermit.Logger
import hu.mostoha.mobile.kmp.huki.model.data.MainSessionSnapshot
import hu.mostoha.mobile.kmp.huki.model.domain.MainSession
import hu.mostoha.mobile.kmp.huki.model.mapper.toMainSession
import hu.mostoha.mobile.kmp.huki.model.mapper.toMainSessionSnapshot
import kotlinx.serialization.json.Json

private const val MAIN_SESSION_KEY = "main_session"
private const val SESSION_JSON_KEY = "json"

private val sessionJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

fun MainSession.encodeSession(): String =
    sessionJson.encodeToString(MainSessionSnapshot.serializer(), toMainSessionSnapshot())

fun decodeSession(json: String): MainSession? =
    try {
        sessionJson.decodeFromString(MainSessionSnapshot.serializer(), json).toMainSession()
    } catch (exception: IllegalArgumentException) {
        Logger.w(exception) { "Session: failed to decode the saved session." }
        null
    }

/**
 * Accepts both the [SavedState] written by [registerMainSession] and the plain JSON of [mainSessionSavedStateHandle].
 */
fun SavedStateHandle.restoreMainSession(): MainSession? {
    val json = when (val value = get<Any>(MAIN_SESSION_KEY)) {
        is String -> value
        is SavedState -> value.read { getStringOrNull(SESSION_JSON_KEY) }
        else -> null
    }
    return json?.let(::decodeSession)
}

/**
 * The [snapshot] runs only when the platform saves state, so frequent changes such as camera moves cost nothing.
 */
fun SavedStateHandle.registerMainSession(snapshot: () -> String) {
    setSavedStateProvider(MAIN_SESSION_KEY) {
        savedState { putString(SESSION_JSON_KEY, snapshot()) }
    }
}

fun mainSessionSavedStateHandle(json: String?): SavedStateHandle =
    if (json.isNullOrEmpty()) SavedStateHandle() else SavedStateHandle(mapOf(MAIN_SESSION_KEY to json))
