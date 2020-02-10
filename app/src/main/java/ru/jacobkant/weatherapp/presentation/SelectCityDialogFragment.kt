package ru.jacobkant.weatherapp.presentation

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.frag_select_city.*
import ru.jacobkant.weatherapp.App
import ru.jacobkant.weatherapp.R
import ru.jacobkant.weatherapp.data.CityRow
import ru.jacobkant.weatherapp.di.getViewModel

class SelectCityDialogFragment : DialogFragment() {

    private val vm: SelectCityViewModel by getViewModel(App.ComponentHolder.appComponent)

    private val disposable = CompositeDisposable()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.frag_select_city, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        frag_select_city_search.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s == vm.currentState.inputText) return
                vm.events.onNext(QueryChanged(s.toString()))
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
        frag_select_city_search.setOnItemClickListener { parent, _, position, _ ->
            val cityRow = parent.adapter.getItem(position) as CityRow
            vm.events.onNext(SelectCity(cityRow))
            frag_select_city_search.dismissDropDown()
            dismissAllowingStateLoss()
        }

        vm.viewState.subscribe {
            frag_select_city_search.setAdapter(
                ArrayAdapter(
                    requireContext(),
                    R.layout.vh_list_item,
                    it.cityList
                )
            )
        }.addTo(disposable)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        frag_select_city_search?.dismissDropDown()
        disposable.clear()
    }
}