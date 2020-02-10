package ru.jacobkant.weatherapp.presentation

import androidx.lifecycle.ViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.PublishProcessor
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject

abstract class Event

abstract class MviViewModel<State> : ViewModel() {
    protected val disposables = CompositeDisposable()

    abstract val initialState: State

    @Suppress("LeakingThis")
    protected val state: BehaviorSubject<State> =
        BehaviorSubject.createDefault(initialState)

    val events: PublishProcessor<Event> = PublishProcessor.create()

    val viewState: Observable<State> = state.observeOn(AndroidSchedulers.mainThread())

    val currentState: State
        get() = state.value!!

    init {
        events.subscribe { onEvent(it) }.addTo(disposables)
    }

    protected abstract fun onEvent(event: Event)

    override fun onCleared() {
        super.onCleared()
        disposables.clear()
    }
}