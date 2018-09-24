package com.wagerr.wallet.data.api

import com.wagerr.wallet.BuildConfig
import com.wagerr.wallet.data.model.GithubUpdateRsp
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class GithubUpdateApi {
    companion object Factory {
        private val client = OkHttpClient().newBuilder()
                .addInterceptor(HttpLoggingInterceptor().apply {
                    if (BuildConfig.DEBUG) {
                        level = HttpLoggingInterceptor.Level.BODY
                    }
                })
                .build()
        private val retrofit = Retrofit.Builder()
                .baseUrl("http://github.com")
                .client(client)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        private val githubUpdateService = retrofit.create(GithubUpdateService::class.java)

        fun getGithubUpdateData(): Observable<GithubUpdateRsp> {
            return githubUpdateService.getGithubUpdateData().subscribeOn(Schedulers.io())
        }

    }
}

interface GithubUpdateService {
    @GET("https://api.github.com/repos/LooorTor/wagerr-android/releases/latest")
    fun getGithubUpdateData(): Observable<GithubUpdateRsp>
}