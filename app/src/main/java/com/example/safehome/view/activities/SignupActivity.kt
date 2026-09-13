package com.example.safehome.view.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.safehome.R
import com.example.safehome.contract.SignupContract
import com.example.safehome.model.User
import com.example.safehome.presenter.SignupPresenter
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputLayout

class SignupActivity : AppCompatActivity(), SignupContract.View {

    private lateinit var presenter: SignupContract.Presenter

    private lateinit var tilFirstName: TextInputLayout
    private lateinit var tilLastName: TextInputLayout
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var tilConfirmPassword: TextInputLayout

    private lateinit var edtFirstName: EditText
    private lateinit var edtLastName: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtPassword: EditText
    private lateinit var edtConfirmPassword: EditText

    private lateinit var btnCreateAccount: Button
    private lateinit var btnGoToLogin: Button
    private lateinit var txtSignupError: TextView
    private lateinit var cardSignupError: MaterialCardView
    private lateinit var progressSignup: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        presenter = SignupPresenter(this)
        initViews()
        setupListeners()
    }

    private fun initViews() {
        tilFirstName = findViewById(R.id.tilFirstName)
        tilLastName = findViewById(R.id.tilLastName)
        tilEmail = findViewById(R.id.tilSignupEmail)
        tilPassword = findViewById(R.id.tilSignupPassword)
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword)

        edtFirstName = findViewById(R.id.edtFirstName)
        edtLastName = findViewById(R.id.edtLastName)
        edtEmail = findViewById(R.id.edtSignupEmail)
        edtPassword = findViewById(R.id.edtSignupPassword)
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword)

        btnCreateAccount = findViewById(R.id.btnCreateAccount)
        btnGoToLogin = findViewById(R.id.btnGoToLogin)
        txtSignupError = findViewById(R.id.txtSignupError)
        cardSignupError = findViewById(R.id.cardSignupError)
        progressSignup = findViewById(R.id.progressSignup)
    }

    private fun setupListeners() {
        btnCreateAccount.setOnClickListener {
            presenter.validateAndSignup(
                edtFirstName.text.toString(),
                edtLastName.text.toString(),
                edtEmail.text.toString(),
                edtPassword.text.toString(),
                edtConfirmPassword.text.toString()
            )
        }

        btnGoToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    // ===== SIGNUP CONTRACT.VIEW IMPLEMENTATIONS =====

    override fun showLoading() {
        progressSignup.visibility = View.VISIBLE
        btnCreateAccount.isEnabled = false
        btnGoToLogin.isEnabled = false
    }

    override fun hideLoading() {
        progressSignup.visibility = View.GONE
        btnCreateAccount.isEnabled = true
        btnGoToLogin.isEnabled = true
    }

    override fun showError(message: String) {
        txtSignupError.text = message
        cardSignupError.visibility = View.VISIBLE
    }

    override fun hideError() {
        cardSignupError.visibility = View.GONE
    }

    override fun showFirstNameError(message: String) {
        tilFirstName.error = if (message.isEmpty()) null else message
    }

    override fun showLastNameError(message: String) {
        tilLastName.error = if (message.isEmpty()) null else message
    }

    override fun showEmailError(message: String) {
        tilEmail.error = if (message.isEmpty()) null else message
    }

    override fun showPasswordError(message: String) {
        tilPassword.error = if (message.isEmpty()) null else message
    }

    override fun showConfirmPasswordError(message: String) {
        tilConfirmPassword.error = if (message.isEmpty()) null else message
    }

    override fun onSignupSuccess(user: User) {
        Toast.makeText(this, "Account created successfully!", Toast.LENGTH_LONG).show()
    }

    override fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }
}