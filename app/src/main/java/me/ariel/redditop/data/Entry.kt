package me.ariel.redditop.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.threeten.bp.OffsetDateTime

typealias EntityID = String

@Entity(
    tableName = "Entries"
)
class Entry(
    @PrimaryKey(autoGenerate = false)
    val uid: EntityID,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "author")
    val author:String,

    @ColumnInfo(name = "date")
    val date: OffsetDateTime? = null,

    @ColumnInfo(name = "comments_count")
    val commentsCount: Int,

    @ColumnInfo(name = "is_read")
    val isRead: Int
)
