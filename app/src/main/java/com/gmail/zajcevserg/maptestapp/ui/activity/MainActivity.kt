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
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.window.layout.WindowMetricsCalculator
import com.gmail.zajcevserg.maptestapp.R
import com.gmail.zajcevserg.maptestapp.databinding.ActivityMainBinding
import com.gmail.zajcevserg.maptestapp.ui.custom.SettingsPageTransformer
import com.gmail.zajcevserg.maptestapp.ui.fragment.LayersSettingsFragment
import com.gmail.zajcevserg.maptestapp.ui.fragment.MissionsFragment
import com.gmail.zajcevserg.maptestapp.ui.fragment.SubstratesFragment
import com.gmail.zajcevserg.maptestapp.viewmodel.LayersVM

import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

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

        binding.pager.adapter = SettingsPageAdapter()
        binding.pager.setPageTransformer(SettingsPageTransformer())
        //binding.pager.isUserInputEnabled = false


        val names = resources.getStringArray(R.array.settings_names)
        TabLayoutMediator(binding.tabLayoutSettings, binding.pager) { tab, position ->
            log("pos $position")
            tab.text = names[position]
            //mViewModel.liveDataCurrentTab.value = tab
        }.attach()


        mViewModel.liveDataMapInteraction.observe(this) {
            it ?: return@observe
            if (it == 0) {
                binding.drawerLayout.close()
            }
        }

    }

    private inner class SettingsPageAdapter
        : FragmentStateAdapter(supportFragmentManager, lifecycle) {
        override fun getItemCount(): Int {
            return 3
        }

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> LayersSettingsFragment()
                1 -> SubstratesFragment()
                2 -> MissionsFragment()
                else -> Fragment()
            }
        }

    }


}


fun log(text: String){
    Log.v("myLog", text)
}
