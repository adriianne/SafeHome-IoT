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
import com.example.safehome.contract.LoginContract
import com.example.safehome.model.User
import com.example.safehome.presenter.LoginPresenter
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity(), LoginContract.View {

    // ✅ Made nullable so onDestroy() can safely check
    private var presenter: LoginContract.Presenter? = null

    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var edtEmail: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnSignIn: Button
    private lateinit var btnGoToSignup: Button
    private lateinit var btnForgotPassword: TextView
    private lateinit var txtLoginError: TextView
    private lateinit var cardLoginError: MaterialCardView
    private lateinit var progressLogin: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ===== AUTO-LOGIN CHECK =====
        // If Firebase has a signed-in user (e.g., after signup), go straight to Home
        if (FirebaseAuth.getInstance().currentUser != null) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
            return   // ← presenter stays null. onDestroy() must handle null.
        }

        setContentView(R.layout.activity_login)

        presenter = LoginPresenter(this)
        initViews()
        setupListeners()
    }

    private fun initViews() {
        tilEmail = findViewById(R.id.tilEmail)
        tilPassword = findViewById(R.id.tilPassword)
        edtEmail = findViewById(R.id.edtEmail)
        edtPassword = findViewById(R.id.edtPassword)
        btnSignIn = findViewById(R.id.btnSignIn)
        btnGoToSignup = findViewById(R.id.btnGoToSignup)
        btnForgotPassword = findViewById(R.id.btnForgotPassword)
        txtLoginError = findViewById(R.id.txtLoginError)
        cardLoginError = findViewById(R.id.cardLoginError)
        progressLogin = findViewById(R.id.progressLogin)
    }

    private fun setupListeners() {
        btnSignIn.setOnClickListener {
            presenter?.validateAndLogin(
                edtEmail.text.toString(),
                edtPassword.text.toString()
            )
        }

        btnGoToSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        btnForgotPassword.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    // ===== LOGIN CONTRACT.VIEW IMPLEMENTATIONS =====

    override fun showLoading() {
        progressLogin.visibility = View.VISIBLE
        btnSignIn.isEnabled = false
        btnGoToSignup.isEnabled = false
    }

    override fun hideLoading() {
        progressLogin.visibility = View.GONE
        btnSignIn.isEnabled = true
        btnGoToSignup.isEnabled = true
    }

    override fun showError(message: String) {
        txtLoginError.text = message
        cardLoginError.visibility = View.VISIBLE
    }

    override fun hideError() {
        cardLoginError.visibility = View.GONE
    }

    override fun showEmailError(message: String) {
        tilEmail.error = if (message.isEmpty()) null else message
    }

    override fun showPasswordError(message: String) {
        tilPassword.error = if (message.isEmpty()) null else message
    }

    override fun onLoginSuccess(user: User) {
        Toast.makeText(this, "Welcome back, ${user.displayName}!", Toast.LENGTH_SHORT).show()
    }

    override fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun navigateToSignup() {
        startActivity(Intent(this, SignupActivity::class.java))
    }

    override fun navigateToForgotPassword() {
        startActivity(Intent(this, ForgotPasswordActivity::class.java))
    }

    override fun onDestroy() {
        super.onDestroy()
        // ✅ Safe null-check — presenter may be null if auto-login skipped init
        presenter?.onDestroy()
    }
}