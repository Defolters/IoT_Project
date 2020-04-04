package io.github.defolters.homeapp.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import io.github.defolters.homeapp.R
import io.github.defolters.homeapp.TempFragment
import io.github.defolters.homeapp.bottom.BottomSheetSuccess
import io.paperdb.Paper
import kotlinx.android.synthetic.main.main_fragment.*
import kotlin.math.round


class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()

        const val TEMP = "TEMP"
        const val HUM = "HUM"
        const val CO2 = "CO2"
    }

    private lateinit var viewModel: MainViewModel
    private var isUpdating: Boolean = false

    private val titles = listOf("Температура", "Влажность", "CO2")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

        //load from cache
        val temp = Paper.book().read<String>(TEMP, "0")
        val hum = Paper.book().read<String>(HUM, "0")
        val co2 = Paper.book().read<String>(CO2, "0")
        tvTemp.text = temp
        tvHum.text = hum
        tvCo.text = co2

        viewPager.adapter = Adapter()

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = titles[position]
        }.attach()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewPager.currentItem = tab?.position ?: 0
            }
        })

        ivSettings.setOnClickListener {
            val bottomSheetSuccess =
                BottomSheetSuccess()
            bottomSheetSuccess.onApply = { it, it2 ->
                viewModel.changeFrequency(it)
                viewModel.changeNotification(it2)
            }
            bottomSheetSuccess.show(
                fragmentManager!!,
                "add_photo_dialog_fragment"
            )
        }

        fabUpdate.setOnClickListener {
            getData()

        }

        multiWaveHeader.waveHeight = 20
        multiWaveHeader.velocity = 1.5f
    }

    inner class Adapter : FragmentStateAdapter(this) {

        override fun createFragment(position: Int): Fragment {
            return TempFragment.newInstance(position + 1)
        }

        override fun getItemCount(): Int {
            return 3
        }
    }

    private fun getData() {
        viewModel.getData(context) {
            group.visibility = View.VISIBLE
            spin_kit.visibility = View.GONE
            it?.let {
                tvTemp.text = it.Temperature.roundTo(2).toString() + "°"
                tvHum.text = it.Humidity.roundTo(2).toString()
                tvCo.text = it.CO2.toInt().toString()
                //save to cache
                Paper.book().write(TEMP, it.Temperature.roundTo(2).toString() + "°")
                Paper.book().write(HUM, it.Humidity.roundTo(2).toString())
                Paper.book().write(CO2, it.CO2.toInt().toString())
                // update graphs
                viewPager.adapter = Adapter()
            }
        }

        group.visibility = View.GONE
        spin_kit.visibility = View.VISIBLE
    }
}

fun Double.roundTo(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return round(this * multiplier) / multiplier
}


