package com.gmail.zajcevserg.maptestapp.ui.activity

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.res.Configuration
import android.os.Bundle
import android.view.animation.AccelerateInterpolator

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.window.layout.WindowMetricsCalculator

import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

import com.gmail.zajcevserg.maptestapp.R
import com.gmail.zajcevserg.maptestapp.databinding.ActivityMainBinding
import com.gmail.zajcevserg.maptestapp.ui.fragment.LayersSettingsFragment
import com.gmail.zajcevserg.maptestapp.ui.fragment.MissionsFragment
import com.gmail.zajcevserg.maptestapp.ui.fragment.SubstratesFragment
import com.gmail.zajcevserg.maptestapp.viewmodel.LayersVM
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val mViewModel: LayersVM by viewModel<LayersVM>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val metrics = WindowMetricsCalculator.getOrCreate()
            .computeCurrentWindowMetrics(this)

        binding.navView.layoutParams.width =
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
            metrics.bounds.width() else metrics.bounds.height()

        val mAdapter = SettingsPageAdapter()
        binding.pager.adapter = mAdapter
        binding.pager.isUserInputEnabled = false
        binding.pager.offscreenPageLimit = 3

        val names = resources.getStringArray(R.array.settings_names)
        TabLayoutMediator(binding.tabLayoutSettings, binding.pager) { tab, position ->
            tab.text = names[position]
        }.attach()

        binding.tabLayoutSettings.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab) {
                binding.pager.setCurrentItem(tab.position, false)
                val view = binding.pager[0]

                val animAlpha = ObjectAnimator.ofFloat(view, "alpha",0f, 1f)
                val animTranslationY = ObjectAnimator.ofFloat(view, "translationY",24f, 0f)
                AnimatorSet().apply {
                    interpolator = AccelerateInterpolator()
                    duration = 120
                    playTogether(animAlpha, animTranslationY)
                    start()
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {}
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
        })

        mViewModel.liveDataMapInteraction.observe(this) {
            it ?: return@observe
            if (it == 1) {
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

