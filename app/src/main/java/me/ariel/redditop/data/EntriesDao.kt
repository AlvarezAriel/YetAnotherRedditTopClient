package me.ariel.redditop.data

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface EntriesDao {
    @Query("SELECT * FROM Entries WHERE is_dismissed = 0 ORDER BY ups DESC")
    fun getAll(): Flowable<List<Entry>>

    @Query("SELECT * FROM Entries WHERE uid = :entryId LIMIT 1")
    fun loadById(entryId: EntityID): Single<Entry>

    @Insert
    fun insert(entry: Entry): Completable

    @Update
    fun update(entry: Entry): Completable

    @Delete
    fun delete(entry: Entry): Completable
}