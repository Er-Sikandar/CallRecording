package com.sikandar.callrecording

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface FileUploadService {
    @Multipart
    @POST("api/Audio")
    fun uploadAudioFile(
//        @Part("description") description: RequestBody,
        @Part file: MultipartBody.Part
    ): Call<UploadResponse>
}
