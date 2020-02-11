package ru.jacobkant.weatherapp

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import ru.jacobkant.weatherapp.data.PermissionHelper
import ru.jacobkant.weatherapp.data.PermissionResult
import ru.jacobkant.weatherapp.data.ShowMessageCommand
import ru.jacobkant.weatherapp.presentation.WeatherFragment
import ru.terrakok.cicerone.Navigator
import ru.terrakok.cicerone.NavigatorHolder
import ru.terrakok.cicerone.android.support.SupportAppNavigator
import ru.terrakok.cicerone.commands.Command
import javax.inject.Inject


class MainActivity : AppCompatActivity() {

    private val compositeDisposable = CompositeDisposable()

    @Inject
    lateinit var permissionHelper: PermissionHelper

    @Inject
    lateinit var navigatorHolder: NavigatorHolder

    private val navigator: Navigator = object : SupportAppNavigator(this, R.id.container) {
        override fun applyCommands(commands: Array<Command>) {
            super.applyCommands(commands)
            supportFragmentManager.executePendingTransactions()
        }

        override fun applyCommand(command: Command) {
            if (command is ShowMessageCommand) {
                Toast.makeText(this@MainActivity, command.message, Toast.LENGTH_LONG).show()
            } else super.applyCommand(command)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.ComponentHolder.appComponent.inject(this)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.container,
                    WeatherFragment()
                ).commitAllowingStateLoss()
        }
    }

    override fun onResumeFragments() {
        super.onResumeFragments()
        navigatorHolder.setNavigator(navigator)
    }

    override fun onStart() {
        super.onStart()
        permissionHelper.getRequests()
            .flatMapSingle {
                if (it.size == 1 && it.first() == PermissionHelper.enableGps) {
                    val locationManager =
                        getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    val gpsEnabled =
                        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    if (!gpsEnabled) showEnableLocationDialog()
                    else permissionHelper.sendResult(
                        PermissionResult(
                            listOf(PermissionHelper.enableGps),
                            true
                        )
                    )
                    Single.just(0)
                } else
                    RxPermissions(this)
                        .requestEach(*it.toTypedArray())
                        .toList()
                        .doOnSuccess { permissions ->
                            val listNames = permissions.map { perm -> perm.name }
                            val isGranted = permissions.all { perm -> perm.granted }
                            permissionHelper.sendResult(PermissionResult(listNames, isGranted))
                        }
            }
            .subscribe()
            .addTo(compositeDisposable)
    }

    private val dialog: AlertDialog by lazy {
        AlertDialog.Builder(this, R.style.AppTheme_Dialog_Alert)
            .setTitle(getString(R.string.frag_weather_enable_location__dialog_title))
            .setCancelable(false)
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss()
                permissionHelper.sendResult(
                    PermissionResult(
                        listOf(PermissionHelper.enableGps),
                        false
                    )
                )
            }
            .setPositiveButton(getString(R.string.frag_weather_enable_location__dialog_pbutton)) { dialog, _ ->
                permissionHelper.sendResult(
                    PermissionResult(
                        listOf(PermissionHelper.enableGps),
                        true
                    )
                )
                val settingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(settingsIntent)
                dialog.dismiss()
            }
            .create()
    }

    private fun showEnableLocationDialog() {
        if (!dialog.isShowing) dialog.show()
    }

    override fun onStop() {
        compositeDisposable.clear()
        super.onStop()
    }

    override fun onPause() {
        navigatorHolder.removeNavigator()
        super.onPause()
    }

}
