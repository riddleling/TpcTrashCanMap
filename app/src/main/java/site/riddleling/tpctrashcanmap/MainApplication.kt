package site.riddleling.tpctrashcanmap

import android.app.Application
import com.google.android.libraries.places.api.Places
import org.koin.core.context.GlobalContext.startKoin

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Places.initializeWithNewPlacesApiEnabled(this, BuildConfig.PLACES_API_KEY)
        startKoin {
            modules(appModules)
        }
    }
}