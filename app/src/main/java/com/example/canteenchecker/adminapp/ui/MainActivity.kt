package com.example.canteenchecker.adminapp.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.AttributeSet
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.canteenchecker.adminapp.App
import com.example.canteenchecker.adminapp.R
import com.example.canteenchecker.adminapp.api.AdminApiFactory
import com.example.canteenchecker.adminapp.core.Canteen
import com.example.canteenchecker.adminapp.core.EditCanteen
import com.example.canteenchecker.adminapp.core.EditDish
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var fragmentCollectionAdapter: SwipeViewFragmentAdapter
    private lateinit var viewPager: ViewPager2

    private lateinit var canteen: Canteen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        intent?.getSerializableExtra(
            "com.example.canteenchecker.adminapp.ui.canteen",
            Canteen::class.java
        )?.let {
            this.canteen = it
        }

        savedInstanceState?.let {
            it.getSerializable(
                "com.example.canteenchecker.adminapp.ui.canteen",
                Canteen::class.java
            )?.let { canteen ->
                this.canteen = canteen
            }
        }

        viewPager = findViewById(R.id.viewPager2)
        fragmentCollectionAdapter = SwipeViewFragmentAdapter(this@MainActivity)
        viewPager.adapter = fragmentCollectionAdapter

        val tabLayout = findViewById<TabLayout>(R.id.tab_layout)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "Data"
                1 -> tab.text = "Waiting time"
                2 -> tab.text = "Reviews"
                3 -> tab.text = "Dish"
            }
        }.attach()

        updateCanteen()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        outState.putSerializable("com.example.canteenchecker.adminapp.ui.canteen", canteen)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        savedInstanceState.getSerializable(
            "com.example.canteenchecker.adminapp.ui.canteen",
            Canteen::class.java
        )?.let {
            this.canteen = it
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_activity_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.mniLogout -> logout().let { true }
        else -> super.onOptionsItemSelected(item)
    }

    private fun logout() {
        (application as App).authenticationToken = ""
        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
    }

    private fun updateCanteen() = lifecycleScope.launch {
        val token = (application as App).authenticationToken

        AdminApiFactory.createAdminAPi().getCanteen(token)
            .onSuccess {
                canteen = it

                sendBroadcast(WaitingTimeFragment.waitingTimeUpdatedIntent(it.waitingTime))

                sendBroadcast(DishFragment.dishUpdatedIntent(it.dish, it.dishPrice))

                sendBroadcast(StandingDataFragment.canteenFetchedIntent(it))
            }
            .onFailure {
                Toast.makeText(
                    this@MainActivity,
                    R.string.error_load_canteen,
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    inner class SwipeViewFragmentAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int = 4

        override fun createFragment(position: Int): Fragment {
            val fragment = when (position) {
                0 -> StandingDataFragment.newInstance(canteen)
                1 -> WaitingTimeFragment.newInstance(canteen.waitingTime)
                2 -> ReviewFragment.newInstance()
                3 -> DishFragment.newInstance(EditDish(canteen.dish, canteen.dishPrice))
                else -> StandingDataFragment.newInstance()
            }
            return fragment
        }
    }

}
