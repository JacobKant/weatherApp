package ru.jacobkant.weatherapp.model

data class Weather(
    val placeName: String,
    val temperature: Double,
    val iconCode: String,
    val coordinates: Pair<Double, Double>,
    val weatherDescription: String,
    val humidity: Int,
    val pressure: Int,
    val windSpeed: Int,
    val cloudnes: Int,
    val currentTemperatureUnit: TemperatureUnit
)