package dev.olog.msc.presentation.library.tab

import android.arch.lifecycle.Lifecycle
import android.databinding.ViewDataBinding
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import dagger.Lazy
import dev.olog.msc.BR
import dev.olog.msc.R
import dev.olog.msc.dagger.qualifier.FragmentLifecycle
import dev.olog.msc.dagger.scope.PerFragment
import dev.olog.msc.presentation.base.adapter.AbsAdapter
import dev.olog.msc.presentation.base.adapter.DataBoundViewHolder
import dev.olog.msc.presentation.base.music.service.MediaProvider
import dev.olog.msc.presentation.model.DisplayableItem
import dev.olog.msc.presentation.navigator.Navigator
import dev.olog.msc.utils.MediaId
import dev.olog.msc.utils.MediaIdCategory
import dev.olog.msc.utils.k.extension.elevateAlbumOnTouch
import dev.olog.msc.utils.k.extension.elevateSongOnTouch
import dev.olog.msc.utils.k.extension.setOnClickListener
import dev.olog.msc.utils.k.extension.setOnLongClickListener
import javax.inject.Inject

@PerFragment
class TabFragmentAdapter @Inject constructor(
        @FragmentLifecycle lifecycle: Lifecycle,
        private val navigator: Navigator,
        private val mediaProvider: MediaProvider,
        private val viewModel: TabFragmentViewModel,
        private val lastPlayedArtistsAdapter: Lazy<TabFragmentLastPlayedArtistsAdapter>,
        private val lastPlayedAlbumsAdapter: Lazy<TabFragmentLastPlayedAlbumsAdapter>

) : AbsAdapter<DisplayableItem>(lifecycle) {

    override fun initViewHolderListeners(viewHolder: DataBoundViewHolder, viewType: Int) {
        when (viewType) {
            R.layout.item_tab_shuffle -> {
                viewHolder.setOnClickListener(controller) { _, _, _ ->
                    mediaProvider.shuffle(MediaId.shuffleAllId())
                }
            }
            R.layout.item_tab_album,
            R.layout.item_tab_artist,
            R.layout.item_tab_song -> {
                viewHolder.setOnClickListener(controller) { item, _, _ ->
                    if (item.isPlayable){
                        mediaProvider.playFromMediaId(item.mediaId)
                    } else {
                        navigator.toDetailFragment(item.mediaId)
                        @Suppress("NON_EXHAUSTIVE_WHEN")
                        when (item.mediaId.category){
                            MediaIdCategory.ARTISTS -> {
                                viewModel.insertArtistLastPlayed(item.mediaId)
                                        .subscribe({}, Throwable::printStackTrace)
                            }
                            MediaIdCategory.ALBUMS -> {
                                viewModel.insertAlbumLastPlayed(item.mediaId)
                                        .subscribe({}, Throwable::printStackTrace)
                            }
                        }
                    }
                }
                viewHolder.setOnLongClickListener(controller) { item, _, _ ->
                    navigator.toDialog(item, viewHolder.itemView)
                }
                viewHolder.setOnClickListener(R.id.more, controller) { item, _, view ->
                    navigator.toDialog(item, view)
                }
            }
            R.layout.item_tab_last_played_album_horizontal_list -> {
                val view = viewHolder.itemView as RecyclerView
                setupHorizontalList(view, lastPlayedAlbumsAdapter.get())
            }
            R.layout.item_tab_last_played_artist_horizontal_list -> {
                val view = viewHolder.itemView as RecyclerView
                setupHorizontalList(view, lastPlayedArtistsAdapter.get())
            }
            R.layout.item_tab_download_no_wifi -> {
                viewHolder.setOnClickListener(R.id.useMobileData, controller) { _, _, _ ->
                    viewModel.hideTurnOnMessage(true)
                }
                viewHolder.setOnClickListener(R.id.hideButton, controller) { _, _, _ ->
                    viewModel.hideTurnOnMessage(false)
                }
            }
        }

        when (viewType){
            R.layout.item_tab_album,
            R.layout.item_tab_artist -> viewHolder.elevateAlbumOnTouch()
            R.layout.item_tab_song -> viewHolder.elevateSongOnTouch()
        }
    }

    private fun setupHorizontalList(list: RecyclerView, adapter: AbsAdapter<*>){
        val layoutManager = LinearLayoutManager(list.context, LinearLayoutManager.HORIZONTAL, false)
        list.layoutManager = layoutManager
        list.adapter = adapter
    }

    override fun bind(binding: ViewDataBinding, item: DisplayableItem, position: Int) {
        binding.setVariable(BR.item, item)
    }

    override val onSwipeAction = { _: Int -> viewModel.hideTurnOnMessage(false) }

    override fun canInteractWithViewHolder(viewType: Int): Boolean? {
        return viewType == R.layout.item_tab_download_no_wifi
    }

}
