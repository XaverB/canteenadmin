package com.example.canteenchecker.adminapp.ui

import android.app.ActivityOptions
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.canteenchecker.adminapp.App
import com.example.canteenchecker.adminapp.R
import com.example.canteenchecker.adminapp.api.AdminApiFactory
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var edtUsername: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnLogin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        edtUsername = findViewById(R.id.edtUserName)
        edtPassword = findViewById(R.id.edtPassword)
        btnLogin = findViewById(R.id.btnLogIn)

        btnLogin.setOnClickListener { login() }
//        btnLogin.isEnabled = false

        setButtonDisabledState()
    }

    private fun setButtonDisabledState() {
        val editTexts = listOf(edtUsername, edtPassword)
        for (editText in editTexts) {
            editText.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    var et1 = edtUsername.text.toString().trim()
                    var et2 = edtPassword.text.toString().trim()

                    btnLogin.isEnabled = et1.isNotEmpty() && et2.isNotEmpty()
                }

                override fun beforeTextChanged(
                    s: CharSequence, start: Int, count: Int, after: Int
                ) {
                }

                override fun afterTextChanged(
                    s: Editable
                ) {
                }
            })
        }
    }

    private fun login() = lifecycleScope.launch {
        val username = edtUsername.text.toString()
        val password = edtPassword.text.toString()

        AdminApiFactory.createAdminAPi()
            .authenticate(username, password)
            .onFailure {
                Toast.makeText(
                    this@LoginActivity,
                    R.string.login_failed,
                    Toast.LENGTH_LONG
                ).show()
            }
            .onSuccess {
                storeLogin(it)
                startTransition()
            }
    }

    private fun startTransition() = lifecycleScope.launch {
        val token = (application as App).authenticationToken
        AdminApiFactory.createAdminAPi().getCanteen(token).onSuccess { canteen ->
            Intent(this@LoginActivity, MainActivity::class.java).also { intent ->
                intent.putExtra("com.example.canteenchecker.adminapp.ui.canteen", canteen)
                startActivity(intent)
            }

        }
            .onFailure {
                Toast.makeText(
                    this@LoginActivity,
                    "´Keine Verbindung zum Server möglich",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun storeLogin(authenticationToken: String) {
        val username = edtUsername.text.toString()
        val password = edtPassword.text.toString()

        val app = (application as App)
        app.authenticationToken = "Bearer $authenticationToken"
        app.username = username
        app.password = password
    }
}