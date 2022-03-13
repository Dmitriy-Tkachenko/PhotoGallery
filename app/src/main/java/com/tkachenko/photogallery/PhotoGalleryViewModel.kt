package com.tkachenko.photogallery

import android.app.Application
import androidx.lifecycle.*

class PhotoGalleryViewModel(private val app: Application) : AndroidViewModel(app) {
    val galleryItemLiveData: LiveData<List<GalleryItem>>

    private val flickrFetch =  FlickrFetch()
    private val mutableSearchTerm = MutableLiveData<String>()

    val searchTerm: String
        get() = mutableSearchTerm.value ?: ""

    init {
        mutableSearchTerm.value = QueryPreferences.getStoredQuery(context = app)

        galleryItemLiveData = Transformations.switchMap(mutableSearchTerm) { searchTerm ->
            if (searchTerm.isBlank()) {
                flickrFetch.fetchPhotos()
            } else {
                flickrFetch.searchPhotos(query = searchTerm)
            }
        }
    }

    fun fetchPhotos(query: String = "") {
        QueryPreferences.setStoredQuery(context = app, query = query)
        mutableSearchTerm.value = query
    }
}