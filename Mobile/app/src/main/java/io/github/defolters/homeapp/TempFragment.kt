package io.github.defolters.homeapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import kotlinx.android.synthetic.main.temp_fragment.*


class TempFragment : Fragment() {

    companion object {
        fun newInstance(type: Int) = TempFragment().apply {
            arguments = Bundle().apply {
                putInt(TYPE, type)
            }
        }

        const val TYPE = "TYPE"
    }

    private lateinit var viewModel: TempViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.temp_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(TempViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val type = arguments?.getInt(TYPE) ?: 1
        chart_followers.loadChart(type)
    }
}
