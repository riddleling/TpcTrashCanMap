package site.riddleling.tpctrashcanmap

import android.app.Application
import org.koin.core.context.GlobalContext.startKoin

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            modules(appModules)
        }
    }
}