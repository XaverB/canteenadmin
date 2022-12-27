package com.example.canteenchecker.adminapp.ui

import android.content.Context
import android.content.Intent
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.telephony.PhoneNumberUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import com.example.canteenchecker.adminapp.App
import com.example.canteenchecker.adminapp.R
import com.example.canteenchecker.adminapp.api.AdminApiFactory
import com.example.canteenchecker.adminapp.core.Canteen
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch
import org.w3c.dom.Text

class MainActivity : AppCompatActivity() {

    private lateinit var canteen: Canteen

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        updateCanteen()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_activity_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.mniEdit -> showEditFragment().let { true }
        R.id.mniCancle -> showStandingDataFragment().let { true }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable("canteen", canteen)
        super.onSaveInstanceState(outState)
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

                supportFragmentManager.beginTransaction()
                    .add(R.id.fcwMain, StandingDataFragment.newInstance(it))
                    .commit()
            }
            .onFailure {
                Toast.makeText(
                    this@MainActivity,
                    R.string.error_load_canteen,
                    Toast.LENGTH_LONG
                ).show()
            }
    }


}