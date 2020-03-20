package io.github.defolters.homeapp.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import io.github.defolters.homeapp.R
import io.github.defolters.homeapp.TempFragment
import io.github.defolters.homeapp.bottom.BottomSheetSuccess
import kotlinx.android.synthetic.main.bottom_sheet.*
import kotlinx.android.synthetic.main.main_fragment.*


class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel
    private var isUpdating: Boolean = false

    private val titles = listOf("Temp", "Hum", "CO2")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewPager.adapter = Adapter()


        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = titles[position]
        }.attach()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
                Toast.makeText(activity, "${tab?.position}", Toast.LENGTH_SHORT).show()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                Toast.makeText(activity, "${tab?.position}", Toast.LENGTH_SHORT).show()
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewPager.currentItem = tab?.position ?: 0
            }
        })

        ivSettings.setOnClickListener {
            val bottomSheetSuccess =
                BottomSheetSuccess()
            bottomSheetSuccess.show(
                fragmentManager!!,
                "add_photo_dialog_fragment"
            )
        }

        fabUpdate.setOnClickListener {
            if (isUpdating) {
                group.visibility = View.GONE
                spin_kit.visibility = View.VISIBLE

            } else {
                group.visibility = View.VISIBLE
                spin_kit.visibility = View.GONE
            }
            isUpdating = !isUpdating
        }

//        waveHeader.setStartColor(R.color.colorPrimary);
//        waveHeader.setCloseColor(R.color.colorPrimaryDark);
//        waveHeader.setColorAlpha(.5f);
//
//        waveHeader.setWaveHeight(50);
//        waveHeader.setGradientAngle(360);
//        waveHeader.setProgress(.8f);
//        waveHeader.setVelocity(1f);
//        waveHeader.setScaleY(-1f);
//
//        waveHeader.setWaves("PairWave");
//
//        waveHeader.start();

        multiWaveHeader.waveHeight  = 20
        multiWaveHeader.velocity = 1.5f
    }

    inner class Adapter : FragmentStateAdapter(this) {

        override fun createFragment(position: Int): Fragment {
            return TempFragment.newInstance()
        }

        override fun getItemCount(): Int {
            return 3
        }
    }

}
