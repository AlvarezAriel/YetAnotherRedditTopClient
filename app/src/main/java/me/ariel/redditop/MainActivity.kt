package me.ariel.redditop

import android.os.Bundle
import android.text.format.DateUtils
import android.text.format.DateUtils.MINUTE_IN_MILLIS
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import me.ariel.redditop.data.EntityID
import me.ariel.redditop.data.Entry
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import timber.log.Timber
import javax.inject.Inject

class MainActivity : DaggerAppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    val viewModel by viewModels<MainActivityViewModel> { viewModelFactory }

    private val adapter by lazy {
        EntryListAdapter { Timber.d("On item clicked: %s", it.title) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        entries_recyclerview.layoutManager = LinearLayoutManager(this)
        entries_recyclerview.adapter = adapter

        viewModel.entries.observe(this, Observer {
            adapter.updateDataSet(it)

        })

        viewModel.isRefreshing.observe(this, Observer {
            entries_swipe_refresh.isRefreshing = it
        })

        entries_swipe_refresh.setOnRefreshListener {
            viewModel.refreshEntries()
        }

    }
}

class EntryItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val title: TextView = itemView.findViewById(R.id.entry_title)
    val thumbnail: ImageView = itemView.findViewById(R.id.entry_thumbnail)
    val date: TextView = itemView.findViewById(R.id.entry_date)
    val author: TextView = itemView.findViewById(R.id.entry_author)
}

class EntryListAdapter(
    private val onItemClicked: (Entry) -> Unit
) : RecyclerView.Adapter<EntryItemViewHolder>() {

    private val entries = mutableListOf<Entry>()

    init {
        setHasStableIds(true)
    }

    /**
     * Takes the diff with the current list and the incoming
     * update and notifies the adapter accordingly.
     */
    class MediaFileDiffCallback(private val oldList: List<Entry>, private val newList: List<Entry>) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].internal_id == newList[newItemPosition].internal_id
        }

        override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
            return oldList[oldPosition] == newList[newPosition]
        }
    }

    fun updateDataSet(mediaFileList: List<Entry>) {
        val diffCallback = MediaFileDiffCallback(entries, mediaFileList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        entries.clear()
        entries.addAll(mediaFileList)
        diffResult.dispatchUpdatesTo(this)
    }

    fun forceUpdateAll(mediaFileList: List<Entry>) {
        entries.clear()
        entries.addAll(mediaFileList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntryItemViewHolder {
        val postViewItem =
            LayoutInflater.from(parent.context).inflate(R.layout.item_entry, parent, false)
        return EntryItemViewHolder(postViewItem)
    }

    override fun getItemCount(): Int = entries.size

    override fun onBindViewHolder(holder: EntryItemViewHolder, position: Int) {
        val entry = entries[position]
        holder.title.text = entry.title
        holder.author.text = entry.author

        holder.itemView.setOnClickListener { onItemClicked.invoke(entry) }

        val now = Instant.now().atZone(ZoneId.systemDefault()).toEpochSecond() * 1000L
        val creationDate = Instant.ofEpochMilli(entry.date_seconds * 1000L).atZone(ZoneId.systemDefault()).toEpochSecond() * 1000L

        holder.date.text = DateUtils.getRelativeTimeSpanString(creationDate, now, MINUTE_IN_MILLIS)

        Glide.with(holder.itemView)
            .load(entry.getFinalThumbnail())
            .fitCenter()
            .centerCrop()
            .into(holder.thumbnail)
    }

    /**
     * Required when using stable ids
     */
    override fun getItemId(position: Int): Long = entries[position].internal_id
}