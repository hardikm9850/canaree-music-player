package dev.olog.msc.presentation.neural.network

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.ContentResolver
import android.net.Uri
import dev.olog.msc.domain.entity.Album
import dev.olog.msc.domain.interactor.tab.GetAllAlbumsUseCase
import dev.olog.msc.presentation.utils.images.NeuralImages
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.rxkotlin.Observables
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.BehaviorSubject

class NeuralNetworkActivityViewModel(
        private val contentResolver: ContentResolver,
        getAllAlbumsForUseCase: GetAllAlbumsUseCase

) : ViewModel() {

    private val stylePublisher = BehaviorSubject.createDefault(-1)
    private val imagePublisher = BehaviorSubject.createDefault("")

    val currentNeuralStyle = MutableLiveData<Int>()
    val currentNeuralImage = MutableLiveData<String>()

    private val cachedImages = mutableListOf<Album>()

    fun getImagesAlbum(): Single<List<Album>> {
        if (cachedImages.isNotEmpty()){
            return Single.just(cachedImages)
        } else {
            return getImagesAlbum
        }
    }

    private val getImagesAlbum: Single<List<Album>> = getAllAlbumsForUseCase.execute()
            .observeOn(Schedulers.computation())
            .firstOrError()
            .map {
                val result = mutableListOf<Album>()
                for (album in it) {
                    try {
                        contentResolver.openInputStream(Uri.parse(album.image))
                        result.add(album)
                    } catch (ex: Exception){/*no image */}
                }
                cachedImages.addAll(result)
                result
            }

    fun updateCurrentNeuralStyle(stylePosition: Int){
        NeuralImages.setStyle(stylePosition)
        currentNeuralStyle.value = stylePosition
        stylePublisher.onNext(stylePosition)
    }

    fun updateCurrentNeuralImage(image: String){
        currentNeuralImage.value = image
        imagePublisher.onNext(image)
    }

    val observeImageLoadedSuccesfully = Observables.combineLatest(
            stylePublisher.filter { it != -1 }, imagePublisher.filter { it != "" },
            { style, image -> image to style })
            .observeOn(AndroidSchedulers.mainThread())

}