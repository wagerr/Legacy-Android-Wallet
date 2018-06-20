package com.wagerr.wallet.data.worldcup.api

import com.wagerr.wallet.BuildConfig
import com.wagerr.wallet.data.worldcup.model.WorldCupMatch
import com.wagerr.wallet.data.worldcup.model.WorldCupData
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

class WorldCupApi {
    companion object Factory {
        private val client = OkHttpClient().newBuilder()
                .addInterceptor(HttpLoggingInterceptor().apply {
                    if (BuildConfig.DEBUG) {
                        level = HttpLoggingInterceptor.Level.BODY
                    }
                })
                .build()
        private val retrofit = Retrofit.Builder()
                .baseUrl("http://notused.com")
                .client(client)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        private val worldCupService = retrofit.create(WorldCupService::class.java)

        fun getWorldCupMatchData(): Observable<List<WorldCupMatch>> {
            return worldCupService.getMatchData().map {
                val list = mutableListOf<WorldCupMatch>()
                for (round in it.rounds) {
                    list.addAll(round.matches)
                }
                return@map list.toList()
            }.subscribeOn(Schedulers.io())
        }

    }
}

interface WorldCupService {
    //分期助手退回
    @GET("https://raw.githubusercontent.com/openfootball/world-cup.json/master/2018/worldcup.json")
    fun getMatchData(): Observable<WorldCupData>
}