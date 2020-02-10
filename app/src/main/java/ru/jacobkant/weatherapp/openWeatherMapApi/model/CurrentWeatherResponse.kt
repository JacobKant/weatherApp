package ru.jacobkant.weatherapp.openWeatherMapApi.model

import com.google.gson.annotations.SerializedName


data class CurrentWeatherResponse(
    @SerializedName("coord")
    var coord: Coord? = null,
    @SerializedName("weather")
    var weather: List<Weather>? = null,
    @SerializedName("base")
    var base: String? = null,
    @SerializedName("main")
    var main: Main? = null,
    @SerializedName("wind")
    var wind: Wind? = null,
    @SerializedName("clouds")
    var clouds: Clouds? = null,
    @SerializedName("dt")
    var dt: Long? = null,
    @SerializedName("sys")
    var sys: Sys? = null,
    @SerializedName("id")
    var id: Long? = null,
    @SerializedName("name")
    var name: String? = null
)

data class Coord(
    @SerializedName("lon")
    val lon: Double? = 0.0,
    @SerializedName("lat")
    val lat: Double? = 0.0
)

data class Clouds(
    @SerializedName("all")
    var all: Double? = 0.0
)

data class Main(
    @SerializedName("temp")
    var temp: Double? = 0.0,
    @SerializedName("pressure")
    var pressure: Double? = 0.0,
    @SerializedName("humidity")
    var humidity: Double? = 0.0,
    @SerializedName("temp_min")
    var tempMin: Double? = 0.0,
    @SerializedName("temp_max")
    var tempMax: Double? = 0.0,
    @SerializedName("sea_level")
    var seaLevel: Double? = 0.0,
    @SerializedName("grnd_level")
    var grndLevel: Double? = 0.0,
    @SerializedName("temp_kf")
    var tempKf: Double? = 0.0
)

data class Sys(
    @SerializedName("message")
    var message: Double? = 0.0,
    @SerializedName("country")
    var country: String? = null,
    @SerializedName("sunrise")
    var sunrise: Long? = null,
    @SerializedName("sunset")
    var sunset: Long? = null,
    @SerializedName("pod")
    var pod: String? = null
)


data class Weather(
    @SerializedName("id")
    var id: Long? = null,
    @SerializedName("main")
    var main: String? = null,
    @SerializedName("description")
    var description: String? = null,
    @SerializedName("icon")
    var icon: String? = null
)

data class Wind(
    @SerializedName("speed")
    val speed: Double? = 0.0,
    @SerializedName("deg")
    val deg: Double? = 0.0
)