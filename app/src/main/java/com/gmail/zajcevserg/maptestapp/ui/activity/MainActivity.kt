package com.gmail.zajcevserg.maptestapp.ui.activity

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.window.layout.WindowMetricsCalculator
import com.gmail.zajcevserg.maptestapp.R
import com.gmail.zajcevserg.maptestapp.databinding.ActivityMainBinding
import com.gmail.zajcevserg.maptestapp.viewmodel.LayersVM

import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val mViewModel: LayersVM by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val metrics = WindowMetricsCalculator.getOrCreate()
            .computeCurrentWindowMetrics(this)

        binding.navView.layoutParams.width =
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
            metrics.bounds.width() else metrics.bounds.height()

        binding.tabLayoutSettings.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> findNavController(R.id.settings_nav_host).navigate(R.id.layers_settings)
                    1 -> findNavController(R.id.settings_nav_host).navigate(R.id.substrates_settings)
                    2 -> findNavController(R.id.settings_nav_host).navigate(R.id.missions_settings)
                }

                mViewModel.liveDataCurrentTab.value = tab
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
        })

        binding.tabLayoutSettings.selectTab(mViewModel.liveDataCurrentTab.value)


        mViewModel.liveDataMapInteraction.observe(this) {
            it ?: return@observe
            if (it == 0) {
                binding.drawerLayout.close()
            }
        }



    }


}


fun log(text: String){
    Log.v("myLog", text)
}
