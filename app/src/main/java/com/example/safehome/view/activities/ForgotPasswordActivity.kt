package com.example.safehome.view.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.safehome.R
import com.example.safehome.contract.ForgotPasswordContract
import com.example.safehome.presenter.ForgotPasswordPresenter
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputLayout


class ForgotPasswordActivity : AppCompatActivity(), ForgotPasswordContract.View {

    private lateinit var presenter: ForgotPasswordContract.Presenter

    private lateinit var tilEmail: TextInputLayout
    private lateinit var edtEmail: EditText
    private lateinit var btnReset: Button
    private lateinit var btnBackToLogin: Button
    private lateinit var txtError: TextView
    private lateinit var cardError: MaterialCardView
    private lateinit var progressReset: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        presenter = ForgotPasswordPresenter(this)
        initViews()
        setupListeners()
    }

    private fun initViews() {
        tilEmail = findViewById(R.id.tilEmail)
        edtEmail = findViewById(R.id.edtEmail)
        btnReset = findViewById(R.id.btnReset)
        btnBackToLogin = findViewById(R.id.btnBackToLogin)
        txtError = findViewById(R.id.txtError)
        cardError = findViewById(R.id.cardError)
        progressReset = findViewById(R.id.progressReset)
    }

    private fun setupListeners() {
        btnReset.setOnClickListener {
            presenter.validateAndSendResetEmail(edtEmail.text.toString())
        }

        btnBackToLogin.setOnClickListener {
            navigateToLogin()
        }
    }

    // ===== FORGOT PASSWORD CONTRACT.VIEW IMPLEMENTATIONS =====

    override fun showLoading() {
        progressReset.visibility = View.VISIBLE
        btnReset.isEnabled = false
        btnBackToLogin.isEnabled = false
    }

    override fun hideLoading() {
        progressReset.visibility = View.GONE
        btnReset.isEnabled = true
        btnBackToLogin.isEnabled = true
    }

    override fun showError(message: String) {
        txtError.text = message
        cardError.visibility = View.VISIBLE
    }

    override fun showSuccess(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG).show()
    }

    override fun showEmailError(message: String) {
        tilEmail.error = if (message.isEmpty()) null else message
    }

    override fun navigateToLogin() {
        // Simply finish this activity to return to Login
        // (Login is below us in the back stack)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }
}