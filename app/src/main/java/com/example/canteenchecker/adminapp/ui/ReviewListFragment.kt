package com.example.canteenchecker.adminapp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.canteenchecker.adminapp.App
import com.example.canteenchecker.adminapp.R
import com.example.canteenchecker.adminapp.api.AdminApiFactory
import com.example.canteenchecker.adminapp.core.Review
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.text.NumberFormat


class ReviewListFragment : Fragment(R.layout.fragment_review_list) {

    private val reviewAdapter = ReviewAdapter()
    private lateinit var reviews: MutableList<Review>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_review_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.run {
            var rcView = findViewById<RecyclerView>(R.id.rvReviews)
            rcView.adapter = reviewAdapter

            // credits to https://www.geeksforgeeks.org/android-swipe-to-delete-and-undo-in-recyclerview-with-kotlin/
            // 02.01.2023
            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    // this method is called when we swipe our item to right direction.
                    // on below line we are getting the item at a particular position.
                    val deletedReview: Review =
                        reviews[viewHolder.adapterPosition]

                    // below line is to get the position
                    // of the item at that position.
                    val position = viewHolder.adapterPosition

                    // this method is called when item is swiped.
                    // below line is to remove item from our array list.
//                    reviews = reviews.minus(reviews[viewHolder.adapterPosition])
                    reviews.removeAt(viewHolder.adapterPosition)

                    // below line is to notify our item is removed from adapter.
                    reviewAdapter.notifyItemRemoved(viewHolder.adapterPosition)

                    // below line is to display our snackbar with action.
                    // below line is to display our snackbar with action.
                    // below line is to display our snackbar with action.
                    Snackbar.make(rcView, "Deleted " + deletedReview.remark, Snackbar.LENGTH_SHORT)
                        .addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
                            override fun onDismissed(
                                transientBottomBar: Snackbar?,
                                event: Int
                            ) {
                                super.onDismissed(transientBottomBar, event)
                                if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT ||
                                    event == Snackbar.Callback.DISMISS_EVENT_SWIPE) {
                                    // do the real delete
                                    deleteReview(deletedReview.id)
                                }
                            }
                        })
                        .setAction(
                            "Undo",
                            View.OnClickListener {
                                // adding on click listener to our action of snack bar.
                                // below line is to add our item to array list with a position.
                                //reviews = reviews.plus(deletedReview)
                                reviews.add(position, deletedReview)

                                // below line is to notify item is
                                // added to our adapter class.
                                reviewAdapter.notifyItemInserted(position)
                            }).show()
                }
                // at last we are adding this
                // to our recycler view.
            }).attachToRecyclerView(rcView)
        }

        updateReviews()
    }

    private fun deleteReview(id: String) = lifecycleScope.launch {
        val token = (requireActivity().application as App).authenticationToken
        AdminApiFactory.createAdminAPi().deleteReview(token, id)
            .onFailure {
                Toast.makeText(requireContext(), "Could not delete review ${id}", Toast.LENGTH_LONG).show()
            }
    }

    private fun updateReviews() = lifecycleScope.launch {
        val token = (requireActivity().application as App).authenticationToken
        reviews =
            AdminApiFactory.createAdminAPi().getReviews(token).getOrElse {
            Toast.makeText(
                requireContext(),
                R.string.message_loading_reviews_failed,
                Toast.LENGTH_LONG
            ).show()
            emptyList()
        }
        .toMutableList()

        reviewAdapter.displayReviews(reviews)
    }

    private class ReviewAdapter : RecyclerView.Adapter<ReviewAdapter.ViewHolder>() {

        private class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvReview: TextView = itemView.findViewById(R.id.tvRemark)
            val tvAuthorAndDate: TextView = itemView.findViewById(R.id.tvAuthorDate)
            val rtbAverageRating: RatingBar = itemView.findViewById(R.id.rtbAverageRating)
            val txvAverageRating: TextView = itemView.findViewById(R.id.txvAverageRating)
        }

        private var reviews = emptyList<Review>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_review, parent, false)
        )

        override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.run {
            val review = reviews[position]

            tvReview.text = review.remark
            tvAuthorAndDate.text = "${review.creator}, ${DateFormat.getDateInstance().format(review.creationDate)}"
            rtbAverageRating.rating = review.rating
            txvAverageRating.text = NumberFormat.getNumberInstance().format(review.rating)

            // navigate to CanteensDetailsActivity
//            itemView.setOnClickListener {
//                itemView.context.run {
//                    startActivity(CanteenDetailActivity.intent(this, review.id))
//                }
//            }
        }

        override fun getItemCount(): Int = reviews.size

        fun displayReviews(reviews: List<Review>) {
            this.reviews = reviews
            notifyDataSetChanged()
        }
    }

    companion object {

        @JvmStatic
        fun newInstance() =
            ReviewListFragment()

    }
}