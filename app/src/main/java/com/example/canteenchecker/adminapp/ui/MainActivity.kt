package com.example.canteenchecker.adminapp.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.canteenchecker.adminapp.App
import com.example.canteenchecker.adminapp.R
import com.example.canteenchecker.adminapp.ui.WaitingTimeFragment
import com.example.canteenchecker.adminapp.api.AdminApiFactory
import com.example.canteenchecker.adminapp.core.Canteen
import com.example.canteenchecker.adminapp.core.EditCanteen
import com.example.canteenchecker.adminapp.core.Review
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var demoCollectionAdapter: SwipeViewFragmentAdapter
    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        demoCollectionAdapter = SwipeViewFragmentAdapter(this)
        viewPager = findViewById(R.id.viewPager2)
        viewPager.adapter = demoCollectionAdapter

        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when(position) {
                0 -> tab.text = "Data"
                1 -> tab.text = "Waiting time"
                2 -> tab.text = "Reviews"
            }
        }.attach()

    }

    inner class SwipeViewFragmentAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            val fragment = when(position) {
                0 -> StandingDataFragment.newInstance()
                1 -> WaitingTimeFragment.newInstance(0)
                2 -> ReviewFragment.newInstance()
                else -> StandingDataFragment.newInstance()
            }
            return fragment
        }
    }

}
