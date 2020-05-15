package me.ariel.redditop

import android.os.Bundle
import dagger.android.support.DaggerAppCompatActivity
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.ariel.redditop.actions.EntryActions
import me.ariel.redditop.network.RedditApi
import timber.log.Timber
import javax.inject.Inject

class MainActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var entryActions: EntryActions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //TODO: remove this. It's here just to quickly try out the entry actions
        entryActions.refreshTopEntries()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { entryId ->
                Timber.d("Saved entry with ID: %s", entryId)
            }
    }
}
