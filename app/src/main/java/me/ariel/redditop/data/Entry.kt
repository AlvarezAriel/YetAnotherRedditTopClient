package me.ariel.redditop.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

typealias EntityID = String

@Entity(
    tableName = "Entries",
    indices = [Index("uid")]
)
data class Entry(

    @PrimaryKey(autoGenerate = true)
    var internal_id: Long = 0,

    @ColumnInfo(name = "uid")
    val uid: EntityID,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "url")
    val url: String,

    @ColumnInfo(name = "thumbnail")
    val thumbnail: String,

    @ColumnInfo(name = "preview")
    val preview: String? = null,

    @ColumnInfo(name = "author")
    val author: String,

    @ColumnInfo(name = "date")
    val date_seconds: Long,

    @ColumnInfo(name = "ups")
    val ups: Long,

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
