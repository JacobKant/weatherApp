package ru.jacobkant.weatherapp.data

import ru.terrakok.cicerone.Router
import ru.terrakok.cicerone.commands.Command

data class ShowMessageCommand(val message: CharSequence): Command

class AppRouter: Router() {
    fun pushCommand(command: Command) {
        executeCommands(command)
    }
}