package com.example.safehome

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.example.safehome.auth.AuthViewModel
import com.example.safehome.auth.LoginField

class LoginActivity : AppCompatActivity() {

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if (viewModel.isAlreadySignedIn()) {
            goToHome()
            return
        }

        val edtEmail = findViewById<EditText>(R.id.edtEmail)
        val edtPassword = findViewById<EditText>(R.id.edtPassword)
        val btnSignIn = findViewById<Button>(R.id.btnSignIn)
        val btnGoToSignup = findViewById<Button>(R.id.btnGoToSignup)
        val txtLoginError = findViewById<TextView>(R.id.txtLoginError)
        val cardLoginError = findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardLoginError)
        val progressLogin = findViewById<ProgressBar>(R.id.progressLogin)

        btnSignIn.setOnClickListener {
            viewModel.signIn(
                email = edtEmail.text.toString(),
                password = edtPassword.text.toString()
            )
        }

        btnGoToSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        edtEmail.doAfterTextChanged {
            viewModel.clearLoginFieldError(LoginField.EMAIL)
        }
        edtPassword.doAfterTextChanged {
            viewModel.clearLoginFieldError(LoginField.PASSWORD)
        }

        viewModel.loginErrors.observe(this) { errors ->
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilEmail).error = errors.email
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilPassword).error = errors.password
        }

        viewModel.formError.observe(this) { message ->
            if (message == null) {
                cardLoginError.visibility = android.view.View.GONE
            } else {
                txtLoginError.text = message
                cardLoginError.visibility = android.view.View.VISIBLE
            }
        }

        viewModel.isSubmitting.observe(this) { busy ->
            btnSignIn.isEnabled = !busy
            btnGoToSignup.isEnabled = !busy
            progressLogin.visibility = if (busy) android.view.View.VISIBLE else android.view.View.GONE
        }

        viewModel.signedIn.observe(this) { signedIn ->
            if (signedIn) goToHome()
        }
    }

    private fun goToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}