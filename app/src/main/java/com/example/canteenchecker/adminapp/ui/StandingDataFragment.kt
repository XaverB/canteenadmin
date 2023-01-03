package com.example.canteenchecker.adminapp.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Geocoder
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
 * Use the [StandingDataFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StandingDataFragment : Fragment(R.layout.fragment_standing_data) {
    private val canteenBroadcastReceiver: BroadcastReceiver = CanteenBroadcastReceiver()
    private val updateCanteenBroadcastReceiver: BroadcastReceiver = UpdateCanteenBroadcastReceiver()
    private val updateWaitingTimeBroadcastReceiver: BroadcastReceiver =
        UpdateWaitingTimeBroadcastReciever()

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
            it.getSerializable("canteen", Canteen::class.java)?.let { c -> canteen = c }
        }
//        updateCanteen()
        requireContext().registerReceiver(
            updateCanteenBroadcastReceiver,
            IntentFilter("com.example.canteenchecker.adminapp.ui.MainActivity.UpdateCanteenSuccess")
        )
        requireContext().registerReceiver(
            updateWaitingTimeBroadcastReceiver,
            IntentFilter("com.example.canteenchecker.adminapp.ui.MainActivity.UpdateWaitingTime")
        )
        requireContext().registerReceiver(
            canteenBroadcastReceiver,
            IntentFilter("com.example.canteenchecker.adminapp.ui.MainActivity.CanteenFetched")
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        requireContext().unregisterReceiver(updateCanteenBroadcastReceiver)
        requireContext().unregisterReceiver(updateWaitingTimeBroadcastReceiver)
        requireContext().unregisterReceiver(canteenBroadcastReceiver)
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
            it.getSerializable("canteen", Canteen::class.java)?.let { canteen ->
                updateCanteen(canteen)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable("canteen", canteen)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.mniEdit -> showEditFragment().let { true }
        else -> super.onOptionsItemSelected(item)
    }


    private fun showEditFragment() {
        canteen?.let {
            (activity as AppCompatActivity).supportFragmentManager.beginTransaction()
                .add(R.id.fcwEdit, StandingDataEditFragment.newInstance(it))
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        menu.findItem(R.id.mniEdit).isVisible = true
    }

    private fun updateCanteen() = lifecycleScope.launch {
        val token = (requireActivity().application as App).authenticationToken

        AdminApiFactory.createAdminAPi().getCanteen(token)
            .onSuccess {
                canteen = it
                updateCanteen(it)

                WaitingTimeFragment.waitingTimeUpdatedIntent(it.waitingTime).let { intent ->
                    activity?.sendBroadcast(intent)
                }

                DishFragment.dishUpdatedIntent(it.dish, it.dishPrice).let { intent ->
                    activity?.sendBroadcast(intent)
                }
            }
            .onFailure {
                Toast.makeText(
                    requireContext(),
                    R.string.error_load_canteen,
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun updateCanteen(canteen: Canteen) {
        this.canteen = canteen;
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


    inner class UpdateWaitingTimeBroadcastReciever : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.getIntExtra("waitingTime", 0)?.let { waitingTime ->
                canteen?.let {
                    canteen = it.copy(waitingTime = waitingTime)
                    updateCanteen(canteen!!)
                }
            }
        }
    }

    inner class CanteenBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.getSerializableExtra("canteen", Canteen::class.java)?.let { canteen ->
                canteen?.let {
                    updateCanteen(canteen)
                }
            }
        }
    }


    // triggered from edit fragment so we do not have to fetch again
    inner class UpdateCanteenBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val updatedCanteen =
                intent?.getSerializableExtra("canteen", EditCanteen::class.java)

            updatedCanteen
                ?.let {
                    canteen?.let { c ->
                        // update canteen with new values and trigger fragment refresh
                        canteen = Canteen(
                            c.id,
                            it.name,
                            it.address,
                            it.phoneNumber,
                            it.website,
                            c.dish,
                            c.dishPrice,
                            c.waitingTime
                        )
                        updateCanteen(canteen!!)
                    }

                }
                ?: updateCanteen() // fetch from service since something is messed up
        }
    }

    companion object {
        private const val DEFAULT_ZOOM_FACTOR = 13f

        @JvmStatic
        fun waitingTimeIntent(waitingTime: Int) =
            Intent().also { intent ->
                intent.action =
                    "com.example.canteenchecker.adminapp.ui.MainActivity.UpdateWaitingTime"
                intent.putExtra("waitingTime", waitingTime)
            }

        @JvmStatic
        fun editCanteenIntent(updatedCanteen: EditCanteen) =
            Intent().also { intent ->
                intent.action =
                    "com.example.canteenchecker.adminapp.ui.MainActivity.UpdateCanteenSuccess"
                intent.putExtra("canteen", updatedCanteen)
            }

        fun canteenFetchedIntent(canteen: Canteen) =
        Intent().also { intent ->
            intent.action =
                "com.example.canteenchecker.adminapp.ui.MainActivity.CanteenFetched"
            intent.putExtra("canteen", canteen)
        }


        @JvmStatic
        fun newInstance(canteen: Canteen) =
            StandingDataFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("canteen", canteen)
                }
            }

        fun newInstance() = StandingDataFragment()
    }
}