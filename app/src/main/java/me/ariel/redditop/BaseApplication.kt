package me.ariel.redditop

import android.app.Application
import com.jakewharton.threetenabp.AndroidThreeTen
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import me.ariel.redditop.di.DaggerApplicationComponent
import timber.log.Timber
import javax.inject.Inject

class BaseApplication : Application(), HasAndroidInjector {

    @Inject
    @JvmField
    var dispatchingAndroidInjector: DispatchingAndroidInjector<Any?>? = null

    override fun onCreate() {
        super.onCreate()
        DaggerApplicationComponent.factory().create(applicationContext)
            .inject(this)

        // Compatibility library for java.time.* for SDK < 26
        AndroidThreeTen.init(this)

        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree())
    }

    override fun androidInjector(): AndroidInjector<Any?>? {
        return dispatchingAndroidInjector
    }

}