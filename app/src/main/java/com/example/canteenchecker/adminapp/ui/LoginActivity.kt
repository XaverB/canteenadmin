package com.example.canteenchecker.adminapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.lifecycle.lifecycleScope
import com.example.canteenchecker.adminapp.R
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var edtUsername: EditText;
    private lateinit var edtPassword: EditText;
    private lateinit var btnLogin: Button;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        edtUsername = findViewById(R.id.edtUserName)
        edtPassword = findViewById(R.id.edtPassword)
        btnLogin = findViewById(R.id.btnLogIn)

        btnLogin.setOnClickListener { login() }

    }

    private fun login() = lifecycleScope.launch() {
        val username = edtUsername.text.toString()
        val password = edtPassword.text.toString()




    }
}