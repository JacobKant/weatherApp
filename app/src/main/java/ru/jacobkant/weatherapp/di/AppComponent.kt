package ru.jacobkant.weatherapp.di

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Component
import ru.jacobkant.weatherapp.MainActivity
import ru.jacobkant.weatherapp.presentation.WeatherFragment
import javax.inject.Singleton

@Singleton
@Component(modules = [AppModule::class, ViewModelModule::class])
interface AppComponent : ComponentWithViewModels {
    fun inject(fragment: MainActivity)
    fun inject(fragment: WeatherFragment)
}


interface ComponentWithViewModels {
    fun viewModelFactory(): ViewModelProvider.Factory
}

inline fun <reified VM : ViewModel> Fragment.getViewModel(component: ComponentWithViewModels) = lazy {
    ViewModelProvider(this, component.viewModelFactory()).get(VM::class.java)
}


