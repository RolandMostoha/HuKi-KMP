package hu.mostoha.mobile.kmp.huki

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
