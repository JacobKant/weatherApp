package ru.jacobkant.weatherapp.model

enum class TemperatureUnit {
    C,
    F;

    companion object {
        fun valueOf(ordinal: Int) = values()[ordinal]
    }
}




