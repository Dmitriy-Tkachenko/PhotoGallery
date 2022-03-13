package com.tkachenko.photogallery.api

import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

private const val API_KEY = "81b54e64cd360ec2fbf60f68592778ef"

class PhotoInterceptor: Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originRequest: Request = chain.request()

        val newUrl: HttpUrl = originRequest.url().newBuilder()
            .addQueryParameter("api_key", API_KEY)
            .addQueryParameter("format", "json")
            .addQueryParameter("nojsoncallback", "1")
            .addQueryParameter("extras" ,"url_s")
            .addQueryParameter("safesearch", "1")
            .build()

        val newRequest: Request = originRequest.newBuilder()
            .url(newUrl)
            .build()

        return chain.proceed(newRequest)
    }
}