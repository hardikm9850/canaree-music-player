package dev.olog.msc.music.service.player

import android.arch.lifecycle.DefaultLifecycleObserver
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import android.content.Context
import android.support.annotation.CallSuper
import android.support.v4.math.MathUtils
import android.util.Log
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import dev.olog.msc.BuildConfig
import dev.olog.msc.music.service.interfaces.ExoPlayerListenerWrapper
import dev.olog.msc.music.service.model.MediaEntity
import dev.olog.msc.music.service.volume.IPlayerVolume
import dev.olog.msc.utils.k.extension.crashlyticsLog

abstract class DefaultPlayer(
        context: Context,
        lifecycle: Lifecycle,
        private val mediaSourceFactory: SourceFactory,
        volume: IPlayerVolume

) : CustomExoPlayer,
        ExoPlayerListenerWrapper,
        DefaultLifecycleObserver {

    private val trackSelector = DefaultTrackSelector()
    protected val player: SimpleExoPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector)

    init {
        lifecycle.addObserver(this)

        volume.listener = object : IPlayerVolume.Listener {
            override fun onVolumeChanged(volume: Float) {
                player.volume = volume
            }
        }
    }

    @CallSuper
    override fun onDestroy(owner: LifecycleOwner) {
        player.release()
    }

    @CallSuper
    override fun prepare(mediaEntity: MediaEntity, bookmark: Long) {
        val mediaSource = mediaSourceFactory.get(mediaEntity)
        player.prepare(mediaSource)
        player.playWhenReady = false
        player.seekTo(bookmark)
    }

    @CallSuper
    override fun play(mediaEntity: MediaEntity, hasFocus: Boolean, isTrackEnded: Boolean) {
        val mediaSource = mediaSourceFactory.get(mediaEntity)
        player.prepare(mediaSource, true, true)
        player.playWhenReady = hasFocus
    }

    @CallSuper
    override fun resume() {
        player.playWhenReady = true
    }

    @CallSuper
    override fun pause() {
        player.playWhenReady = false
    }

    @CallSuper
    override fun seekTo(where: Long) {
        val safeSeek = MathUtils.clamp(where.toInt(), 0, getDuration().toInt()).toLong()
        player.seekTo(safeSeek)
    }

    @CallSuper
    override fun isPlaying(): Boolean {
        return player.playWhenReady
    }

    @CallSuper
    override fun getBookmark(): Long {
        return player.currentPosition
    }

    @CallSuper
    override fun getDuration(): Long {
        return player.duration
    }

    @CallSuper
    override fun setVolume(volume: Float) {
        player.volume = volume
    }

    @CallSuper
    override fun onPlayerError(error: ExoPlaybackException) {
        val what = when (error.type) {
            ExoPlaybackException.TYPE_SOURCE -> error.sourceException.message
            ExoPlaybackException.TYPE_RENDERER -> error.rendererException.message
            ExoPlaybackException.TYPE_UNEXPECTED -> error.unexpectedException.message
            else -> "Unknown: $error"
        }

        crashlyticsLog("player error $what")

        if (BuildConfig.DEBUG) {
            Log.e("Player", "onPlayerError $what")
        }
    }

}