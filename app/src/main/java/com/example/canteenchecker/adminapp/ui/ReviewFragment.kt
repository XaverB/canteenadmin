package com.example.canteenchecker.adminapp.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.canteenchecker.adminapp.R
import com.example.canteenchecker.adminapp.core.Canteen
import com.google.android.gms.maps.SupportMapFragment


class ReviewFragment : Fragment(R.layout.fragment_review) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.run {
            addStatisticsFragment()
            addReviewListFragment()
        }
    }

    private fun addReviewListFragment() =
        (activity as AppCompatActivity).supportFragmentManager.beginTransaction()
            .add(R.id.fcwReviewList, ReviewListFragment.newInstance())
            .addToBackStack(null)
            .commit()

    private fun addStatisticsFragment() =
        (activity as AppCompatActivity).supportFragmentManager.beginTransaction()
            .add(R.id.fcwStatistics, StatisticsFragment.newInstance())
            .addToBackStack(null)
            .commit()



    companion object {

        fun reviewUpdatedBroadcast() =
            Intent().also { intent ->
                intent.action = "com.example.canteenchecker.adminapp.ui.ReviewUpdated"
            }

        @JvmStatic
        fun newInstance() =
            ReviewFragment()
    }
}