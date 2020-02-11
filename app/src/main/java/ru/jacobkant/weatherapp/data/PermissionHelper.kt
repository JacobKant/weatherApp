package ru.jacobkant.weatherapp.data

import android.content.Context
import android.os.Handler
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject
import javax.inject.Singleton

data class PermissionResult(
    val permissions: List<String>,
    val isGranted: Boolean
)


@Singleton
class PermissionHelper @Inject constructor(private val context: Context) {
    private val result: PublishSubject<PermissionResult> = PublishSubject.create()
    private val requests: PublishSubject<List<String>> = PublishSubject.create()

    companion object {
        const val enableGps = "EnableGps"
    }

    fun requestPermission(permissions: List<String>): Single<Boolean> {
        return result
            .doOnSubscribe { Handler().post { requests.onNext(permissions) } }
            .filter { it.permissions == permissions }
            .map { it.isGranted }
            .first(false)
    }

    fun requestEnableGps(): Single<Boolean> {
        val permissions = listOf(enableGps)
        return result
            .doOnSubscribe { Handler().post { requests.onNext(permissions) } }
            .filter { it.permissions == permissions }
            .map { it.isGranted }
            .first(false)
    }

    fun sendResult(res: PermissionResult) = result.onNext(res)

    fun getRequests(): Observable<List<String>> = requests.hide()

//    fun requestEnableLocation(): Observable<Boolean> {
//
//    }
}