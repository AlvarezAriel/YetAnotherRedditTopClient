package me.ariel.redditop.data

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class EntriesRepository(
    val dao: EntriesDao
) {

    fun findAll() = dao.getAll()

    fun fetchById(uid:EntityID): Single<Result<Entry>> = dao.loadById(uid)
        .map { Result.success(it) }
        .onErrorReturn { Result.failure(it) }
        .subscribeOn(Schedulers.io())

    fun update(entry:Entry) = dao.update(entry)
        .subscribeOn(Schedulers.io())

    fun create(entry:Entry) = dao.insert(entry)
        .subscribeOn(Schedulers.io())

    fun dismiss(entry:Entry) = dao.update(entry.copy(isDismissed = true))
        .subscribeOn(Schedulers.io())

}