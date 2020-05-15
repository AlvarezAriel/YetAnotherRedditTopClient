package me.ariel.redditop.di

import android.content.Context
import androidx.lifecycle.ViewModel
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import dagger.*
import dagger.android.AndroidInjector
import dagger.android.ContributesAndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import dagger.multibindings.IntoMap
import me.ariel.redditop.BaseApplication
import me.ariel.redditop.MainActivity
import me.ariel.redditop.MainActivityViewModel
import me.ariel.redditop.actions.EntryActions
import me.ariel.redditop.data.AppDatabase
import me.ariel.redditop.data.EntriesDeserializer
import me.ariel.redditop.data.EntriesRepository
import me.ariel.redditop.data.Entry
import me.ariel.redditop.network.RedditApi
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidSupportInjectionModule::class,
        ApplicationModule::class,
        MainActivityModule::class
    ]
)
interface ApplicationComponent : AndroidInjector<BaseApplication> {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance applicationContext: Context): ApplicationComponent
    }
}

@Module
open class ApplicationModule {

    @Singleton
    @Provides
    fun provideAppDatabase(context: Context): AppDatabase {
        return AppDatabase.generateDatabase(context)
    }

    @Singleton
    @Provides
    fun provideGsonConverterFactory(): GsonConverterFactory {
        val type = TypeToken.getParameterized(
            List::class.java,
            Entry::class.java
        ).type

        val gson = GsonBuilder()
            .registerTypeAdapter(
                type, EntriesDeserializer()
            )
            .create()

        return GsonConverterFactory.create(gson)
    }


    @Singleton
    @Provides
    fun provideRetrofitInstance(customConverter: GsonConverterFactory): Retrofit {
        return Retrofit.Builder()
            .baseUrl(RedditApi.BASE_URL)
            .addConverterFactory(customConverter)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun provideRedditApi(retrofit: Retrofit): RedditApi {
        return retrofit.create(RedditApi::class.java)
    }

    @Singleton
    @Provides
    fun provideEntriesRepository(appDatabase: AppDatabase): EntriesRepository {
        return EntriesRepository(appDatabase.albumDao())
    }

    @Singleton
    @Provides
    fun provideEntryActions(redditApi: RedditApi, repository: EntriesRepository, context:Context): EntryActions {
        return EntryActions(redditApi, repository, context)
    }
}

@Module
abstract class MainActivityModule {

    @ContributesAndroidInjector(
        modules = [
            ViewModelBuilder::class
        ]
    )
    internal abstract fun mainActivity(): MainActivity

    @Binds
    @IntoMap
    @ViewModelKey(MainActivityViewModel::class)
    abstract fun bindViewModel(viewmodel: MainActivityViewModel): ViewModel
}