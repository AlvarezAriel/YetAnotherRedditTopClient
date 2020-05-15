package me.ariel.redditop

import android.content.res.Configuration
import android.os.Bundle
import android.text.format.DateUtils
import android.text.format.DateUtils.MINUTE_IN_MILLIS
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.include_entries_list.*
import kotlinx.android.synthetic.main.include_entry_detail.*
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
        EntryListAdapter(viewModel) {
            Timber.d("On item clicked: %s", it.title)
            viewModel.selectEntry(it)
            detail_container.isVisible = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        entries_recyclerview.layoutManager = LinearLayoutManager(this)
        entries_recyclerview.adapter = adapter

        viewModel.entries.observe(this, Observer {
            adapter.updateDataSet(it)
            btn_dismiss_all.text = getString(R.string.dismiss_all, it.size)
        })

        viewModel.isRefreshing.observe(this, Observer {
            entries_swipe_refresh.isRefreshing = it
        })

        entries_swipe_refresh.setOnRefreshListener { viewModel.refreshEntries() }

        btn_dismiss_all.setOnClickListener { viewModel.dismissAll() }

        btn_close.setOnClickListener {
            detail_container.isVisible = false
        }

        btn_download.setOnClickListener {
            viewModel.downloadImage()
        }

        viewModel.isDownloadingImage.observe(this, Observer {
            notification_banner.isVisible = it
        })

        viewModel.selectedEntry.observe(this, Observer {
            if (it != null) {
                entry_detail_title.text = it.title

                Glide.with(this)
                    .load(it.preview ?: it.getFinalThumbnail())
                    .fitCenter()
                    .into(entry_detail_thumbnail)

                btn_download.isVisible = it.getDownloadableImageUrl() != null
            } else {
                detail_container.isVisible = false
            }
        })

    }

    fun updateRotationDependentUi() {
        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            btn_close.isVisible = false
        } else if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            btn_close.isVisible = true
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateRotationDependentUi()
    }
}

class EntryItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val title: TextView = itemView.findViewById(R.id.entry_title)
    val thumbnail: ImageView = itemView.findViewById(R.id.entry_thumbnail)
    val date: TextView = itemView.findViewById(R.id.entry_date)
    val author: TextView = itemView.findViewById(R.id.entry_author)
    val dismissBtn: Button = itemView.findViewById(R.id.btn_dismiss)
    val commentsCount: TextView = itemView.findViewById(R.id.entry_comments_count)
    val readIndicator: View = itemView.findViewById(R.id.entry_read_indicator)
}

class EntryListAdapter(
    private val viewModel: MainActivityViewModel,
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
    class MediaFileDiffCallback(
        private val oldList: List<Entry>,
        private val newList: List<Entry>
    ) : DiffUtil.Callback() {

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
        holder.dismissBtn.setOnClickListener { viewModel.dismissEntry(entry) }

        val now = Instant.now().atZone(ZoneId.systemDefault()).toEpochSecond() * 1000L
        val creationDate =
            Instant.ofEpochMilli(entry.date_seconds * 1000L).atZone(ZoneId.systemDefault())
                .toEpochSecond() * 1000L

        holder.date.text = DateUtils.getRelativeTimeSpanString(creationDate, now, MINUTE_IN_MILLIS)
        holder.commentsCount.text =
            holder.itemView.context.getString(R.string.n_comments, entry.commentsCount)
        holder.readIndicator.setBackgroundResource(if (entry.isRead) R.drawable.ic_read_indicator_on else R.drawable.ic_read_indicator_off)
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