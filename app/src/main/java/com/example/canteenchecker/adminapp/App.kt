package com.example.canteenchecker.adminapp

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging

class App : Application() {

    var authenticationToken: String = ""
    var username: String = ""
    var password: String = ""

    override fun onCreate() {
        super.onCreate()
        FirebaseMessaging.getInstance().subscribeToTopic("UpdateReview")
        FirebaseMessaging.getInstance().subscribeToTopic("UpdateReviews")
    }
}