package com.example.canteenchecker.adminapp.ui

import android.content.Intent
import android.location.Geocoder
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.lifecycleScope
import com.example.canteenchecker.adminapp.App
import com.example.canteenchecker.adminapp.R
import com.example.canteenchecker.adminapp.api.AdminApiFactory
import com.example.canteenchecker.adminapp.core.Canteen
import com.example.canteenchecker.adminapp.core.EditCanteen
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass.
 * Use the [StandingDataEditFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StandingDataEditFragment : Fragment(R.layout.fragment_standing_data_edit) {
    private var canteen: Canteen? = null

    private lateinit var edtName: EditText
    private lateinit var edtPhone: EditText
    private lateinit var edtWebsite: EditText
    private lateinit var edtAddress: EditText

    private lateinit var mapFragment: SupportMapFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        arguments?.let {
            canteen = it.getSerializable("canteen", Canteen::class.java)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.run {
            edtPhone = findViewById(R.id.edtPhone)
            edtWebsite = findViewById(R.id.edtWebsite)
            edtAddress = findViewById(R.id.edtAddress)
            edtName = findViewById(R.id.edtName)

            // maps
            mapFragment = childFragmentManager
                .findFragmentById(R.id.fcwMap) as SupportMapFragment
            mapFragment.getMapAsync {
                it.uiSettings.apply {
                    setAllGesturesEnabled(false)
                    isZoomControlsEnabled = true
                }
            }

            canteen?.let { updateCanteen(it) }
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        arguments?.let {
            it.getSerializable("canteen", Canteen::class.java)?.let {
                canteen ->  updateCanteen(canteen)
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu){
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.mniEdit).isVisible = false
        menu.findItem(R.id.mniCancle).isVisible = true
        menu.findItem(R.id.mniSave).isVisible = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.mniSave -> save().let { true }
        else -> super.onOptionsItemSelected(item)
    }

    private fun updateCanteen(canteen: Canteen) {
        edtPhone.setText(PhoneNumberUtils.formatNumber(canteen.phoneNumber, Locale.current.region))
        edtAddress.setText(canteen.address)
        edtWebsite.setText(canteen.website)
        edtName.setText(canteen.name)

        val address = Geocoder(requireContext())
            .getFromLocationName(canteen.address, 1)
            ?.firstOrNull()?.run {
                LatLng(latitude, longitude)
            }
        mapFragment.getMapAsync { map ->
            map.apply {
                clear()
                if (address != null) {
                    addMarker(
                        MarkerOptions()
                            .position(address)
                    )

                    animateCamera(
                        CameraUpdateFactory
                            .newLatLngZoom(address, StandingDataEditFragment.DEFAULT_ZOOM_FACTOR)
                    )
                } else {
                    animateCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(0.0, 0.0), 0f
                        )
                    )
                }
            }
        }
    }

    private fun save() = lifecycleScope.launch {

        val updatedCanteen = EditCanteen(
        edtName.text.toString(),
        edtAddress.text.toString(),
        edtWebsite.text.toString(),
        edtPhone.text.toString())

        // TODO
        val authenticationToken = (activity?.application as App).authenticationToken

        AdminApiFactory.createAdminAPi().updateCanteen(
            authenticationToken,
            updatedCanteen).
        onFailure {
            Toast.makeText(requireContext(), "Nix gut update", Toast.LENGTH_LONG).show()
        }.
        onSuccess {
            // TODO
            Intent().also { intent ->
                intent.action = "com.example.canteenchecker.adminapp.ui.MainActivity.UpdateCanteenSuccess"
                intent.putExtra("canteen", updatedCanteen)
                activity?.sendBroadcast(intent)
            }
            activity?.supportFragmentManager?.popBackStack()
        }
    }

    companion object {
        private const val DEFAULT_ZOOM_FACTOR = 13f

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment StandingDataFragment.
         */
        @JvmStatic
        fun newInstance(canteen: Canteen) =
            StandingDataEditFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("canteen", canteen)
                }
            }
    }
}