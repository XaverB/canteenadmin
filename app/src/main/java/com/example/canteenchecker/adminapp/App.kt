package com.example.canteenchecker.adminapp

import android.app.Application

class App() : Application() {
    var authenticationToken: String = "";
    var username: String = "";
    var password: String = "";
}