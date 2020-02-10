package ru.jacobkant.weatherapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.jacobkant.weatherapp.presentation.WeatherFragment

interface DialogShower

class MainActivity : AppCompatActivity(), DialogShower {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.container,
                    WeatherFragment()
                )
                .commitAllowingStateLoss()
        }
    }

}
