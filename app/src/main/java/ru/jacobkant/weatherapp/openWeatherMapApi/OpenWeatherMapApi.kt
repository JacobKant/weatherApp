package ru.jacobkant.weatherapp.openWeatherMapApi

import com.facebook.stetho.okhttp3.StethoInterceptor
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.QueryMap
import ru.jacobkant.weatherapp.BuildConfig
import ru.jacobkant.weatherapp.openWeatherMapApi.model.CurrentWeatherResponse


interface OpenWeatherMapApi {
    companion object {
        private const val Current = "/data/2.5/weather"

        fun create(): OpenWeatherMapApi {
            val okHttpBuilder = OkHttpClient.Builder()
                .addInterceptor {
                    val originalReq = it.request()
                    val originalUrl = originalReq.url()
                    val newUrl = originalUrl.newBuilder()
                        .addQueryParameter("APPID", "11072ff32c9bc1774260bca215c764e5")
                        .addQueryParameter("lang", "ru")
                        .addQueryParameter("units", "metric")
                        .build()
                    val newReq = originalReq.newBuilder()
                        .url(newUrl)
                        .build()
                    it.proceed(newReq)
                }
            if (BuildConfig.DEBUG) okHttpBuilder.addNetworkInterceptor(StethoInterceptor())
            return Retrofit.Builder()
                .client(okHttpBuilder.build())
                .baseUrl("https://api.openweathermap.org/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()))
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(OpenWeatherMapApi::class.java)
        }
    }

    @GET(Current)
    fun getCurrentWeather(@QueryMap options: Map<String, String>?): Single<CurrentWeatherResponse>

}