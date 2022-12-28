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
import androidx.lifecycle.lifecycleScope
import com.example.canteenchecker.adminapp.App
import com.example.canteenchecker.adminapp.R
import com.example.canteenchecker.adminapp.api.AdminApiFactory
import com.example.canteenchecker.adminapp.core.Canteen
import com.example.canteenchecker.adminapp.core.EditCanteen
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var canteen: Canteen
    val updateCanteenBroadcastReciever: BroadcastReceiver = UpdateCanteenBroadcastReceiver()

    inner class UpdateCanteenBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val updatedCanteen = intent?.getSerializableExtra("canteen", EditCanteen::class.java)

            updatedCanteen
                ?.let {
                    // update canteen with new values and trigger fragment refresh
                    canteen = Canteen(canteen.id,
                    it.name,
                    it.address,
                    it.phoneNumber,
                    it.website,
                    canteen.dish,
                    canteen.dishPrice,
                    canteen.waitingTime)
                    showStandingDataFragment()
                }
                ?: updateCanteen() // fetch from service since something is messed up
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        registerReceiver(updateCanteenBroadcastReciever, IntentFilter("com.example.canteenchecker.adminapp.ui.MainActivity.UpdateCanteenSuccess"))
        updateCanteen()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_activity_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.mniEdit -> showEditFragment().let { true }
        R.id.mniCancle -> cancelEdit().let { true }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable("canteen", canteen)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(updateCanteenBroadcastReciever)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.getSerializable("canteen", Canteen::class.java)?.let { canteen = it }
        super.onRestoreInstanceState(savedInstanceState)
    }

    private fun showEditFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fcwMain, StandingDataEditFragment.newInstance(canteen))
            .addToBackStack(null)
            .commit()
    }

    private fun cancelEdit() {
        supportFragmentManager.popBackStack()
    }

    private fun showStandingDataFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fcwMain, StandingDataFragment.newInstance(canteen))
            .commit()
    }

    private fun updateCanteen() = lifecycleScope.launch {
        val token = (application as App).authenticationToken

        AdminApiFactory.createAdminAPi().getCanteen(token)
            .onSuccess {
                canteen = it
                showStandingDataFragment()
            }
            .onFailure {
                // TODO show error fragment
                Toast.makeText(
                    this@MainActivity,
                    R.string.error_load_canteen,
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}