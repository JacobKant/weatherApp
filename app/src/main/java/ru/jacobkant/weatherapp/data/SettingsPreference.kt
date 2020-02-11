package ru.jacobkant.weatherapp.data

import android.content.Context
import android.content.SharedPreferences
import ru.jacobkant.weatherapp.model.AppMode
import ru.jacobkant.weatherapp.model.TemperatureUnit
import javax.inject.Inject
import javax.inject.Singleton

private const val moscowCityId: Long = 524901

@Singleton
class SettingsPreference @Inject constructor(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var currentTemperatureUnit: TemperatureUnit by PreferencesDelegate(
        preferences = sharedPreferences,
        name = "currentTemperatureUnit",
        defValue = TemperatureUnit.C
    )

    var appMode: AppMode by PreferencesDelegate(
        preferences = sharedPreferences,
        name = "appMode",
        defValue = AppMode.CityByLocation
    )

    var lastCityId: Long by PreferencesDelegate(
        preferences = sharedPreferences,
        name = "lastCityId",
        defValue = moscowCityId
    )

}