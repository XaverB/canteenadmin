package com.example.canteenchecker.adminapp.ui

import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.ui.text.intl.Locale
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.canteenchecker.adminapp.App
import com.example.canteenchecker.adminapp.R
import com.example.canteenchecker.adminapp.api.AdminApiFactory
import com.example.canteenchecker.adminapp.core.Canteen
import com.example.canteenchecker.adminapp.core.EditCanteen
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class StandingDataEditFragment : Fragment(R.layout.fragment_standing_data_edit),
    GoogleMap.OnMarkerDragListener, Geocoder.GeocodeListener {
    private var canteen: Canteen? = null

    private lateinit var edtName: EditText
    private lateinit var edtPhone: EditText
    private lateinit var edtWebsite: EditText
    private lateinit var edtAddress: EditText

    private lateinit var mapFragment: SupportMapFragment

    private lateinit var map: GoogleMap

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

            edtAddress.setOnFocusChangeListener { _, b -> updateAddressOnFocusChanged(b) }

            // maps
            mapFragment = childFragmentManager
                .findFragmentById(R.id.fcwMap) as SupportMapFragment
            mapFragment.getMapAsync {
                map = it
                it.setOnMarkerDragListener(this@StandingDataEditFragment)
                it.uiSettings.apply {
                    setAllGesturesEnabled(true)
                    isZoomControlsEnabled = true
                }
            }


            canteen?.let { updateCanteen(it) }
        }
    }

    private fun updateAddressOnFocusChanged(hasFocus: Boolean) {
        if (hasFocus) return

        val text = edtAddress.text.toString()
        if (text.isEmpty()) return

        Geocoder(requireContext()).getFromLocationName(text, 1, this@StandingDataEditFragment)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        arguments?.let {
            it.getSerializable("canteen", Canteen::class.java)?.let { canteen ->
                updateCanteen(canteen)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_activity_main, menu)
        return super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.mniCancle)?.isVisible = true
        menu.findItem(R.id.mniSave)?.isVisible = true
        menu.findItem(R.id.mniEdit)?.isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.mniSave -> save().let { true }
        R.id.mniCancle -> cancelEdit().let { true }
        else -> super.onOptionsItemSelected(item)
    }

    private fun cancelEdit() {
        requireActivity().supportFragmentManager.popBackStack()
//        (activity as AppCompatActivity).supportFragmentManager.popBackStack()
    }

    private fun updateCanteen(canteen: Canteen) {
        edtPhone.setText(PhoneNumberUtils.formatNumber(canteen.phoneNumber, Locale.current.region))
        edtAddress.setText(canteen.address)
        edtWebsite.setText(canteen.website)
        edtName.setText(canteen.name)

        Geocoder(requireContext())
            .getFromLocationName(canteen.address, 1, this@StandingDataEditFragment)
    }

    private fun save() = lifecycleScope.launch {
        val updatedCanteen = EditCanteen(
            edtName.text.toString(),
            edtAddress.text.toString(),
            edtWebsite.text.toString(),
            edtPhone.text.toString()
        )

        val authenticationToken = (activity?.application as App).authenticationToken

        AdminApiFactory.createAdminAPi().updateCanteen(
            authenticationToken,
            updatedCanteen
        ).onFailure {
            Toast.makeText(requireContext(), "Nix gut update", Toast.LENGTH_LONG).show()
        }.onSuccess {
            StandingDataFragment.editCanteenIntent(updatedCanteen).let {
                activity?.sendBroadcast(it)
            }
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    companion object {
        private const val DEFAULT_ZOOM_FACTOR = 13f

        @JvmStatic
        fun newInstance(canteen: Canteen) =
            StandingDataEditFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("canteen", canteen)
                }
            }
    }

    override fun onMarkerDrag(p0: Marker) {
    }

    override fun onMarkerDragStart(p0: Marker) {
    }

    override fun onGeocode(addresses: MutableList<Address>) {
        if (addresses.isEmpty()) return
        addresses[0].let {
            updateAddressTextbox(it)
            updateMarker(it)
        }
    }

    override fun onMarkerDragEnd(p0: Marker) {
        val latLng: LatLng = p0.position
        val geocoder = Geocoder(requireContext())
        // result will be handled in onGeocode function
        geocoder.getFromLocation(
            latLng.latitude,
            latLng.longitude,
            1,
            this@StandingDataEditFragment
        )
    }


    private fun updateAddressTextbox(it: Address) {
        edtAddress.setText(it.getAddressLine(0))
    }

    private fun updateMarker(address: Address) = lifecycleScope.launch {
        map.apply {
            clear()

            addMarker(
                MarkerOptions().position(LatLng(address.latitude, address.longitude))
                    .draggable(true)
            )

            animateCamera(
                CameraUpdateFactory
                    .newLatLngZoom(
                        LatLng(address.latitude, address.longitude),
                        DEFAULT_ZOOM_FACTOR
                    )
            )
        }

    }
}