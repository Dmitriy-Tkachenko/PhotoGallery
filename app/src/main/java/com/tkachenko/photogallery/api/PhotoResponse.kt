package com.tkachenko.photogallery.api

import com.google.gson.annotations.SerializedName
import com.tkachenko.photogallery.GalleryItem

class PhotoResponse {
    @SerializedName("photo")
    lateinit var galleryItems: List<GalleryItem>
}