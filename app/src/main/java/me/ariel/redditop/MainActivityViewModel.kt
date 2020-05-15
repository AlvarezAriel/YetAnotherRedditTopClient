package me.ariel.redditop

import androidx.lifecycle.LiveDataReactiveStreams
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import me.ariel.redditop.actions.EntryActions
import me.ariel.redditop.data.EntriesRepository
import me.ariel.redditop.data.Entry
import timber.log.Timber
import javax.inject.Inject

class MainActivityViewModel @Inject constructor(
    private val repository: EntriesRepository,
    private val actions: EntryActions
) : ViewModel() {

    val entries = LiveDataReactiveStreams.fromPublisher(repository.findAll())
    val isRefreshing = MutableLiveData<Boolean>(false)
    val selectedEntry = MutableLiveData<Entry?>(null)

    private val refresher = actions.refreshTopEntries()
        .observeOn(AndroidSchedulers.mainThread())

    fun refreshEntries() {
        isRefreshing.postValue(true)
        refresher
            .doOnComplete { isRefreshing.postValue(false) }
            .subscribe { Timber.d("Refreshed entry: %s", it) }
    }

    fun dismissAll() {
        entries.value?.let {
            it.forEach {
                dismissEntry(it)
            }
        }
    }

    fun dismissEntry(someEntry:Entry) {
        repository.dismiss(someEntry).subscribe {
            Timber.d("Dismissed: %s", someEntry.title)
        }
    }
}