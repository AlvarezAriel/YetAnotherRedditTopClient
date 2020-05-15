package me.ariel.redditop

import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import me.ariel.redditop.actions.EntryActions
import me.ariel.redditop.data.EntriesRepository
import timber.log.Timber
import javax.inject.Inject

class MainActivityViewModel @Inject constructor(
    private val repository: EntriesRepository,
    private val actions: EntryActions
) : ViewModel() {

    val entries = LiveDataReactiveStreams.fromPublisher(repository.findAll())

    private val refresher = actions.refreshTopEntries()
        .observeOn(AndroidSchedulers.mainThread())

    fun refreshEntries() {
        refresher.subscribe { Timber.d("Refreshed entry: %s", it) }
    }
}