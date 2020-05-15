package me.ariel.redditop.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

typealias EntityID = String

@Entity(
    tableName = "Entries"
)
data class Entry(
    @PrimaryKey(autoGenerate = false)
    val uid: EntityID,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "url")
    val url: String,

    @ColumnInfo(name = "thumbnail")
    val thumbnail: String,

    @ColumnInfo(name = "author")
    val author: String,

    @ColumnInfo(name = "date")
    val date: Long,

    @ColumnInfo(name = "comments_count")
    val commentsCount: Int,

    @ColumnInfo(name = "is_read")
    val isRead: Boolean = false,

    @ColumnInfo(name = "is_dismissed")
    val isDismissed: Boolean = false
) {

    /**
     * Not all entries have thumbnails.
     * Instead, they can have a label matching some default image.
     */
    fun getFinalThumbnail() = when (thumbnail) {
        "self" -> "https://www.reddit.com/static/self_default2.png"
        "default" -> "https://www.reddit.com/static/noimage.png"
        "nsfw" -> "https://www.reddit.com/static/nsfw2.png"
        else -> thumbnail
    }

}
