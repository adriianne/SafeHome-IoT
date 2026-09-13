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
import com.example.safehome.contract.ChangePasswordContract
import com.example.safehome.presenter.ChangePasswordPresenter
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputLayout

class ChangePasswordActivity : AppCompatActivity(), ChangePasswordContract.View {

    private lateinit var presenter: ChangePasswordContract.Presenter

    private lateinit var tilCurrentPassword: TextInputLayout
    private lateinit var tilNewPassword: TextInputLayout
    private lateinit var tilConfirmPassword: TextInputLayout

    private lateinit var edtCurrentPassword: EditText
    private lateinit var edtNewPassword: EditText
    private lateinit var edtConfirmPassword: EditText

    private lateinit var btnChangePassword: Button
    private lateinit var btnCancel: Button
    private lateinit var txtError: TextView
    private lateinit var cardError: MaterialCardView
    private lateinit var progressChange: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        presenter = ChangePasswordPresenter(this)
        initViews()
        setupListeners()
    }

    private fun initViews() {
        tilCurrentPassword = findViewById(R.id.tilCurrentPassword)
        tilNewPassword = findViewById(R.id.tilNewPassword)
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword)

        edtCurrentPassword = findViewById(R.id.edtCurrentPassword)
        edtNewPassword = findViewById(R.id.edtNewPassword)
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword)

        btnChangePassword = findViewById(R.id.btnChangePassword)
        btnCancel = findViewById(R.id.btnCancel)
        txtError = findViewById(R.id.txtError)
        cardError = findViewById(R.id.cardError)
        progressChange = findViewById(R.id.progressChange)
    }

    private fun setupListeners() {
        btnChangePassword.setOnClickListener {
            presenter.validateAndChangePassword(
                edtCurrentPassword.text.toString(),
                edtNewPassword.text.toString(),
                edtConfirmPassword.text.toString()
            )
        }

        btnCancel.setOnClickListener {
            finish()
        }
    }

    // ===== CHANGE PASSWORD CONTRACT.VIEW IMPLEMENTATIONS =====

    override fun showLoading() {
        progressChange.visibility = View.VISIBLE
        btnChangePassword.isEnabled = false
        btnCancel.isEnabled = false
    }

    override fun hideLoading() {
        progressChange.visibility = View.GONE
        btnChangePassword.isEnabled = true
        btnCancel.isEnabled = true
    }

    override fun showError(message: String) {
        txtError.text = message
        cardError.visibility = View.VISIBLE
    }

    override fun showSuccess(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun showCurrentPasswordError(message: String) {
        tilCurrentPassword.error = if (message.isEmpty()) null else message
    }

    override fun showNewPasswordError(message: String) {
        tilNewPassword.error = if (message.isEmpty()) null else message
    }

    override fun showConfirmPasswordError(message: String) {
        tilConfirmPassword.error = if (message.isEmpty()) null else message
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