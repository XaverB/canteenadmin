package com.example.canteenchecker.adminapp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.canteenchecker.adminapp.R
import com.google.android.gms.maps.SupportMapFragment


class ReviewFragment : Fragment(R.layout.fragment_review) {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.run {
            addStatisticsFragment()
        }
    }

    private fun addStatisticsFragment() =
        (activity as AppCompatActivity).supportFragmentManager.beginTransaction()
            .add(R.id.fcwStatistics, StatisticsFragment.newInstance())
            .addToBackStack(null)
            .commit()

    companion object {
        @JvmStatic
        fun newInstance() =
            ReviewFragment()
    }
}