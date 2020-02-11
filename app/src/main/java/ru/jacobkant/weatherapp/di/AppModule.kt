package ru.jacobkant.weatherapp.di

import android.app.Application
import android.content.Context
import androidx.room.Room
import dagger.Binds
import dagger.Module
import dagger.Provides
import ru.jacobkant.weatherapp.data.AppRouter
import ru.jacobkant.weatherapp.data.CityDao
import ru.jacobkant.weatherapp.data.WeatherDatabase
import ru.jacobkant.weatherapp.data.WeatherRepositoryImpl
import ru.jacobkant.weatherapp.model.WeatherInteractor
import ru.jacobkant.weatherapp.model.WeatherInteractorImpl
import ru.jacobkant.weatherapp.model.WeatherRepository
import ru.jacobkant.weatherapp.openWeatherMapApi.OpenWeatherMapApi
import ru.terrakok.cicerone.Cicerone
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.Router
import javax.inject.Singleton

@Module(includes = [WeatherModule::class])
class AppModule(private val app: Application) {

    @Provides
    fun provideContext(): Context {
        return app
    }

    @Singleton
    @Provides
    fun provideApi(): OpenWeatherMapApi {
        return OpenWeatherMapApi.create()
    }

    @Singleton
    @Provides
    fun provideDb(context: Context): WeatherDatabase {
        return Room.databaseBuilder(context, WeatherDatabase::class.java, "db")
            .createFromAsset("db.sqlite")
            .build()
    }

    @Singleton
    @Provides
    fun provideDaoCities(db: WeatherDatabase): CityDao {
        return db.cityDao()
    }


    private val cicerone: Cicerone<AppRouter> = Cicerone.create(AppRouter())

    @Singleton
    @Provides
    fun provideRootRouter(): AppRouter {
        return cicerone.router
    }

    @Singleton
    @Provides
    fun provideRootNavigationHolder(): NavigatorHolder {
        return cicerone.navigatorHolder
    }
}

@Module
abstract class WeatherModule {
    @Binds
    abstract fun weatherInteractor(a: WeatherInteractorImpl): WeatherInteractor

    @Binds
    abstract fun weatherRepository(a: WeatherRepositoryImpl): WeatherRepository
}