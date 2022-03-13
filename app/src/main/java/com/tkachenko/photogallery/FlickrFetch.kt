package com.tkachenko.photogallery

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.tkachenko.photogallery.api.FlickrApi
import com.tkachenko.photogallery.api.PhotoResponse
import okhttp3.Request
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import com.tkachenko.photogallery.api.PhotoInterceptor
import okhttp3.OkHttpClient


private const val TAG = "FlickrFetchr"

class FlickrFetch {
    private val flickrApi: FlickrApi

    init {
        val gson: Gson =
            GsonBuilder().registerTypeAdapter(PhotoResponse::class.java, PhotoDeserializer())
                .create()
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://www.flickr.com/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(OkHttpClient().newBuilder().addInterceptor(PhotoInterceptor()).build())
            .build()

        flickrApi = retrofit.create(FlickrApi::class.java)
    }

    fun fetchPhotosRequest(): Call<PhotoResponse> {
        return flickrApi.fetchPhotos()
    }

    fun fetchPhotos(): LiveData<List<GalleryItem>> {
        return fetchPhotoMetadata(fetchPhotosRequest())
    }

    fun searchPhotosRequest(query: String): Call<PhotoResponse> {
        return flickrApi.searchPhotos(query)
    }

    fun searchPhotos(query: String): LiveData<List<GalleryItem>> {
        return fetchPhotoMetadata(searchPhotosRequest(query))
    }

    private fun fetchPhotoMetadata(photoRequest: Call<PhotoResponse>) : LiveData<List<GalleryItem>> {
        val responseLiveData: MutableLiveData<List<GalleryItem>> = MutableLiveData()

        photoRequest.enqueue(object : Callback<PhotoResponse> {
            override fun onResponse(call: Call<PhotoResponse>, response: Response<PhotoResponse>) {
                Log.i(TAG, "Response received")
                val photoResponse: PhotoResponse? = response.body()
                var galleryItems: List<GalleryItem> = photoResponse?.galleryItems ?: mutableListOf()
                galleryItems = galleryItems.filterNot {
                    it.url.isBlank()
                }
                responseLiveData.value = galleryItems
            }

            override fun onFailure(call: Call<PhotoResponse>, t: Throwable) {
                Log.e(TAG, "Failed to fetch photos", t)
            }

        })
        return responseLiveData
    }

    @WorkerThread
    fun fetchPhoto(url: String): Bitmap? {
        val response: Response<ResponseBody> = flickrApi.fetchUrlBytes(url).execute()
        val bitmap = response.body()?.byteStream()?.use(BitmapFactory::decodeStream)
        Log.i(TAG, "Decoded bitmap=$bitmap from Response=$response")
        return bitmap
    }
}