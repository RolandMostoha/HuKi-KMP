package hu.mostoha.mobile.kmp.huki.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import dev.icerock.moko.permissions.PermissionsController
import hu.mostoha.mobile.kmp.huki.database.HukiDatabase
import hu.mostoha.mobile.kmp.huki.service.AndroidLocationMonitoringService
import hu.mostoha.mobile.kmp.huki.service.LocationMonitoringService
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.dsl.module

val androidPlatformModule = module {
    single<HttpClientEngine> { OkHttp.create() }
    single { PermissionsController(applicationContext = get()) }
    single<RoomDatabase.Builder<HukiDatabase>> {
        val context = get<Context>()
        Room.databaseBuilder<HukiDatabase>(
            context = context,
            name = context.getDatabasePath(HukiDatabase.DATABASE_NAME).absolutePath,
        )
    }
    single<LocationMonitoringService> {
        AndroidLocationMonitoringService(
            context = get(),
            scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
        )
    }
}
