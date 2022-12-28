package com.example.canteenchecker.adminapp.ui

import android.location.Geocoder
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.ui.text.intl.Locale
import com.example.canteenchecker.adminapp.R
import com.example.canteenchecker.adminapp.core.Canteen
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

/**
 * A simple [Fragment] subclass.
 * Use the [StandingDataFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StandingDataFragment : Fragment(R.layout.fragment_standing_data) {

    private var canteen: Canteen? = null

    private lateinit var tvName: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvWebsite: TextView
    private lateinit var tvAddress: TextView

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
            tvPhone = findViewById(R.id.tvPhone)
            tvWebsite = findViewById(R.id.tvWebsite)
            tvAddress = findViewById(R.id.tvAddress)
            tvName = findViewById(R.id.tvName)

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
         menu.findItem(R.id.mniEdit).isVisible = true
        menu.findItem(R.id.mniCancle).isVisible = false
        menu.findItem(R.id.mniSave).isVisible = false
    }


    private fun updateCanteen(canteen: Canteen) {
        tvPhone.text = PhoneNumberUtils.formatNumber(canteen.phoneNumber, Locale.current.region);
        tvAddress.text = canteen.address
        tvWebsite.text = canteen.website
        tvName.text = canteen.name

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
                            .newLatLngZoom(address, DEFAULT_ZOOM_FACTOR)
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
            StandingDataFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("canteen", canteen)
                }
            }
    }
}