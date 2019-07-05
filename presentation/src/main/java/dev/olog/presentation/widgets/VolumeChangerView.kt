package dev.olog.presentation.widgets

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageButton
import dev.olog.core.prefs.MusicPreferencesGateway
import dev.olog.presentation.R
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flowOn

class VolumeChangerView(
    context: Context,
    attrs: AttributeSet
) : AppCompatImageButton(context, attrs), CoroutineScope by MainScope() {

    init {
        // TODO use lottie?? https://assets10.lottiefiles.com/datafiles/dPBsEmiWemqAzfE/data.json
        setImageResource(R.drawable.vd_volume_up)
    }

    var musicPrefs: MusicPreferencesGateway? = null
        set(value) {
            field = value
            if (value != null) {
                startObserving()
            }
        }

    private var job: Job? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!isInEditMode){
            musicPrefs?.let { startObserving() }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        job?.cancel()
    }

    private fun startObserving() {
        job?.cancel()
        job = launch {
            musicPrefs!!.observeVolume()
                .flowOn(Dispatchers.Default)
                .collect { updateImage(it) }
        }
    }

    private fun updateImage(volume: Int) {
        val drawable = when (volume) {
            0 -> R.drawable.vd_volume_mute
            in 1..60 -> R.drawable.vd_volume_down
            else -> R.drawable.vd_volume_up
        }
        setImageResource(drawable)
    }

}