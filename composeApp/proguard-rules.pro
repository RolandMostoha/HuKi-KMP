# Crashlytics: readable stack traces, de-obfuscated with the uploaded mapping file.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Exception class names are logged and reported, e.g. `NetworkUtils`, `DefaultLocationLinkResolver`.
-keepnames class * extends java.lang.Throwable

# kotlinx-serialization, Room, Ktor, OkHttp, Koin, Mapbox and Firebase ship their own consumer rules.
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Ktor: JVM-only references that don't exist on Android.
-dontwarn java.lang.management.**
-dontwarn org.slf4j.impl.StaticLoggerBinder

# Firebase component discovery: registrars are instantiated reflectively by name.
-keep class * implements com.google.firebase.components.ComponentRegistrar {
    public <init>();
}

-dontwarn org.jetbrains.annotations.**
