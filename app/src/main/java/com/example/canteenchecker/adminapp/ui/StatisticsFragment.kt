package com.example.canteenchecker.adminapp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.canteenchecker.adminapp.App
import com.example.canteenchecker.adminapp.R
import com.example.canteenchecker.adminapp.api.AdminApiFactory
import kotlinx.coroutines.launch
import java.text.NumberFormat


class StatisticsFragment : Fragment(R.layout.fragment_statistics) {

    private lateinit var txvAverageRating: TextView
    private lateinit var rbAverageRating: RatingBar
    private lateinit var txvTotalRatings: TextView

    private lateinit var prbRatingsOne: ProgressBar
    private lateinit var prbRatingsTwo: ProgressBar
    private lateinit var prbRatingsThree: ProgressBar
    private lateinit var prbRatingsFour: ProgressBar
    private lateinit var prbRatingsFive: ProgressBar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.run {
            txvAverageRating = findViewById(R.id.txvAverageRating)
            rbAverageRating = findViewById(R.id.rtbAverageRating)
            txvTotalRatings = findViewById(R.id.txvTotalRatings)

            prbRatingsOne = findViewById(R.id.prbRatingsOne)
            prbRatingsTwo = findViewById(R.id.prbRatingsTwo)
            prbRatingsThree = findViewById(R.id.prbRatingsThree)
            prbRatingsFour = findViewById(R.id.prbRatingsFour)
            prbRatingsFive = findViewById(R.id.prbRatingsFive)

        }
        // load review from server
        updateReview()
    }

    private fun updateReview() = lifecycleScope.launch {
        val token = (requireActivity().application as App).authenticationToken

        AdminApiFactory.createAdminAPi()
            .getReviewStatistics(token)
            .onFailure {
                txvAverageRating.text = null
                rbAverageRating.rating = 0f
                txvTotalRatings.text = null
                prbRatingsOne.progress = 0
                prbRatingsOne.max = 1
                prbRatingsTwo.progress = 0
                prbRatingsTwo.max = 1
                prbRatingsThree.progress = 0
                prbRatingsThree.max = 1
                prbRatingsFour.progress = 0
                prbRatingsFour.max = 1
                prbRatingsFive.progress = 0
                prbRatingsFive.max = 1
            }
            .onSuccess { reviews ->
                txvAverageRating.text = NumberFormat.getNumberInstance()
                    .format(reviews.averageRating)
                rbAverageRating.rating = reviews.averageRating
                txvTotalRatings.text = NumberFormat.getNumberInstance()
                    .format(reviews.totalRatings)

                prbRatingsOne.progress = reviews.countOneStar
                prbRatingsOne.max = reviews.totalRatings
                prbRatingsTwo.progress = reviews.countTwoStars
                prbRatingsTwo.max = reviews.totalRatings
                prbRatingsThree.progress = reviews.countThreeStars
                prbRatingsThree.max = reviews.totalRatings
                prbRatingsFour.progress = reviews.countFourStars
                prbRatingsFour.max = reviews.totalRatings
                prbRatingsFive.progress = reviews.countFiveStars
                prbRatingsFive.max = reviews.totalRatings

            }
    }

    companion object {
        fun newInstance() = StatisticsFragment()
    }
}