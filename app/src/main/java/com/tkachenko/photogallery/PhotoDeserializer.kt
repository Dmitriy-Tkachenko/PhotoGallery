package com.tkachenko.photogallery

import com.google.gson.*
import com.tkachenko.photogallery.api.PhotoResponse
import java.lang.reflect.Type

class PhotoDeserializer : JsonDeserializer<PhotoResponse> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): PhotoResponse {
        val jsonPhotos: JsonElement = json!!.asJsonObject.get("photos")
        return Gson().fromJson(jsonPhotos, PhotoResponse::class.java)
    }
}