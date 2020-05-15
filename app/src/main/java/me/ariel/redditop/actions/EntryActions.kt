package me.ariel.redditop.actions

import io.reactivex.schedulers.Schedulers
import me.ariel.redditop.data.EntriesDao
import me.ariel.redditop.data.EntriesRepository
import me.ariel.redditop.network.RedditApi

/**
 * Note: This is what you would normally consider a "Service", where the main business logic resides.
 * It's called Actions in order to avoid confusing it with some android.app.Service
 */
class EntryActions(
    val redditApi: RedditApi,
    val entriesRepository: EntriesRepository
) {

    fun refreshTopEntries() = redditApi.top()
            .subscribeOn(Schedulers.io())
            .flattenAsFlowable { it.asIterable() }
            .flatMapSingle {remoteEntry ->
                entriesRepository.fetchById(remoteEntry.uid).flatMap { result ->
                    val completable = if(result.isFailure) {
                        // if this is a new entry, insert it as new
                        entriesRepository.create(remoteEntry)
                    } else {
                        // if we already have this entry, update it but keep local flags
                        val value = result.getOrThrow()
                        entriesRepository.update(remoteEntry.copy(
                            isDismissed = value.isDismissed,
                            isRead = value.isRead
                        ))
                    }
                    completable.toSingleDefault(remoteEntry.uid)
                }
            }

}