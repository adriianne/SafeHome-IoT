package com.example.safehome

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.example.safehome.auth.AuthViewModel
import com.example.safehome.auth.SignupField

class SignupActivity : AppCompatActivity() {

    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val edtFirstName = findViewById<EditText>(R.id.edtFirstName)
        val edtLastName = findViewById<EditText>(R.id.edtLastName)
        val edtEmail = findViewById<EditText>(R.id.edtSignupEmail)
        val edtPassword = findViewById<EditText>(R.id.edtSignupPassword)
        val edtConfirmPassword = findViewById<EditText>(R.id.edtConfirmPassword)
        val btnCreateAccount = findViewById<Button>(R.id.btnCreateAccount)
        val btnGoToLogin = findViewById<Button>(R.id.btnGoToLogin)
        val txtSignupError = findViewById<TextView>(R.id.txtSignupError)
        val cardSignupError = findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardSignupError)
        val progressSignup = findViewById<ProgressBar>(R.id.progressSignup)

        btnCreateAccount.setOnClickListener {
            viewModel.signUp(
                firstName = edtFirstName.text.toString(),
                lastName = edtLastName.text.toString(),
                email = edtEmail.text.toString(),
                password = edtPassword.text.toString(),
                confirmPassword = edtConfirmPassword.text.toString()
            )
        }

        btnGoToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        edtFirstName.doAfterTextChanged { viewModel.clearSignupFieldError(SignupField.FIRST_NAME) }
        edtLastName.doAfterTextChanged { viewModel.clearSignupFieldError(SignupField.LAST_NAME) }
        edtEmail.doAfterTextChanged { viewModel.clearSignupFieldError(SignupField.EMAIL) }
        edtPassword.doAfterTextChanged { viewModel.clearSignupFieldError(SignupField.PASSWORD) }
        edtConfirmPassword.doAfterTextChanged { viewModel.clearSignupFieldError(SignupField.CONFIRM_PASSWORD) }

        viewModel.signupErrors.observe(this) { errors ->
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilFirstName).error = errors.firstName
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilLastName).error = errors.lastName
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilSignupEmail).error = errors.email
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilSignupPassword).error = errors.password
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.tilConfirmPassword).error = errors.confirmPassword
        }

        viewModel.formError.observe(this) { message ->
            if (message == null) {
                cardSignupError.visibility = View.GONE
            } else {
                txtSignupError.text = message
                cardSignupError.visibility = View.VISIBLE
            }
        }

        viewModel.isSubmitting.observe(this) { busy ->
            btnCreateAccount.isEnabled = !busy
            btnGoToLogin.isEnabled = !busy
            progressSignup.visibility = if (busy) View.VISIBLE else View.GONE
        }

        viewModel.accountCreated.observe(this) { created ->
            if (created) {
                Toast.makeText(
                    this,
                    "Account created successfully! Please sign in.",
                    Toast.LENGTH_LONG
                ).show()

                // Navigate to Login
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }

        // OBSERVE: Signed in - if auto-sign in happens
        viewModel.signedIn.observe(this) { signedIn ->
            if (signedIn) {
                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }
    }
}