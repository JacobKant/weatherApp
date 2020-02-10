package ru.jacobkant.weatherapp.data

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject
import javax.inject.Singleton

data class SelectCity(val cityId: Long)

@Singleton
class EventBus @Inject constructor() {
    private val bus = PublishSubject.create<Any>()

    fun <T> getEvent(clazz: Class<T>): Observable<T> {
        return bus.ofType(clazz)
    }

    fun sendEvent(event: Any) {
        return bus.onNext(event)
    }
}