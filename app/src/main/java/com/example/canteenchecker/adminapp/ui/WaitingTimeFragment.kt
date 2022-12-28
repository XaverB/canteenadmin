package com.example.canteenchecker.adminapp.ui

import android.animation.ValueAnimator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.airbnb.lottie.LottieAnimationView
import com.example.canteenchecker.adminapp.App
import com.example.canteenchecker.adminapp.R
import com.example.canteenchecker.adminapp.api.AdminApiFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch


private const val ARG_WAITING_TIME = "waitingTime"

/**
 * A simple [Fragment] subclass.
 * Use the [WaitingTimeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class WaitingTimeFragment : Fragment(R.layout.fragment_waiting_time) {
    private val waitingTimeFetchedBroadcastReceiver: BroadcastReceiver = WaitingTimeFetchedBroadcastReceiver()

    private var waitingTime: Int? = null

    private lateinit var tvWaitingTimeValue: TextView
    private lateinit var tvChoseWaitingTime: TextView
    private lateinit var sbWaitingTime: SeekBar
    private lateinit var btnSave: FloatingActionButton
    private lateinit var ltWaitingTime: LottieAnimationView

    private var selectedWaitingTime: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            waitingTime = it.getInt(ARG_WAITING_TIME)
        }

        requireContext().registerReceiver(
            waitingTimeFetchedBroadcastReceiver,
            IntentFilter("com.example.canteenchecker.adminapp.ui.MainActivity.WaitingTimeFetched")
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        requireContext().unregisterReceiver(waitingTimeFetchedBroadcastReceiver)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.run {
            tvWaitingTimeValue = findViewById(R.id.tvWaitingTimeValue)
            tvChoseWaitingTime = findViewById(R.id.tvChoseWaitingTime)
            sbWaitingTime = findViewById(R.id.sbWaitingTime)
            btnSave = findViewById(R.id.fbSaveWaitingTime)
            ltWaitingTime = findViewById(R.id.ltWaitingTime)

            sbWaitingTime?.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seek: SeekBar,
                    progress: Int, fromUser: Boolean
                ) {
                    updateSelectedWaitingTime(progress)

                    if (progress == sbWaitingTime.max)
                        Toast.makeText(requireContext(), "Schneller arbeiten", Toast.LENGTH_SHORT).show()
                }

                override fun onStartTrackingTouch(seek: SeekBar) {
                }

                override fun onStopTrackingTouch(seek: SeekBar) {
                }
            })

            btnSave.setOnClickListener { save() }
        }
    }

    private fun updateSelectedWaitingTime(progress: Int) {
        selectedWaitingTime = progress
        tvChoseWaitingTime.text = "Select waiting time: ${selectedWaitingTime}m";
    }

    private fun save() = lifecycleScope.launch {
        val token = (requireActivity().application as App).authenticationToken
        AdminApiFactory.createAdminAPi().updateWaitingTime(token, selectedWaitingTime)?.onSuccess {
            StandingDataFragment.waitingTimeIntent(selectedWaitingTime).let {
                activity?.sendBroadcast(it)
            }
            animateTextView(selectedWaitingTime)
//            tvWaitingTimeValue.text = selectedWaitingTime.toString() + "m"
            ltWaitingTime.playAnimation()

        }?.onFailure {
            Toast.makeText(requireContext(), "Kaputt", Toast.LENGTH_LONG).show()
        }
    }

    fun animateTextView(value: Int) {

        val initialValue = tvWaitingTimeValue.text.toString().toInt()

        val valueAnimator = ValueAnimator.ofInt(initialValue, value)
        valueAnimator.duration = 1500
        valueAnimator.addUpdateListener { valueAnimator ->
            tvWaitingTimeValue.text = valueAnimator.animatedValue.toString()
        }
        valueAnimator.start()
    }

    // standing data fragment informs us about fresh fetched waiting time data
    // especially useful on init
    inner class WaitingTimeFetchedBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.getIntExtra("waitingTime", 0)?.let { waitingTime ->
                animateTextView(waitingTime)
//                tvWaitingTimeValue.text = "${waitingTime}m"
            }
        }
    }

    companion object {

        @JvmStatic
        fun waitingTimeUpdatedIntent(waitingTime: Int) =
            Intent().also { intent ->
                intent.action =
                    "com.example.canteenchecker.adminapp.ui.MainActivity.WaitingTimeFetched"
                intent.putExtra("waitingTime", waitingTime)
            }

        @JvmStatic
        fun newInstance(waitingTime: Int) =
            WaitingTimeFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_WAITING_TIME, waitingTime)

                }
            }
    }
}