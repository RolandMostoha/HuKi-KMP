package hu.mostoha.mobile.kmp.huki.util

/**
 * True when running a debug build, false in release. Use to gate debug-only behavior
 * (verbose logging, developer toggles) without injecting a flag everywhere.
 */
expect val isDebugBuild: Boolean
