package com.example.canteenchecker.adminapp

import com.example.canteenchecker.adminapp.ui.ReviewFragment
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class ReviewFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        sendBroadcast(ReviewFragment.reviewUpdatedBroadcast())

    }
}