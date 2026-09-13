package com.example.safehome.view.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.safehome.R
import com.example.safehome.contract.SettingsContract
import com.example.safehome.presenter.SettingsPresenter

class SettingsActivity : AppCompatActivity(), SettingsContract.View {

    private lateinit var presenter: SettingsContract.Presenter

    private lateinit var btnBack: ImageView
    private lateinit var tvSettingsName: TextView
    private lateinit var tvSettingsEmail: TextView
    private lateinit var btnChangePassword: View
    private lateinit var btnSettingsSignOut: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        presenter = SettingsPresenter(this)
        initViews()
        setupListeners()

        presenter.loadUserInfo()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvSettingsName = findViewById(R.id.tvSettingsName)
        tvSettingsEmail = findViewById(R.id.tvSettingsEmail)
        btnChangePassword = findViewById(R.id.btnChangePassword)
        btnSettingsSignOut = findViewById(R.id.btnSettingsSignOut)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnChangePassword.setOnClickListener {
            presenter.onChangePasswordClicked()
        }

        btnSettingsSignOut.setOnClickListener {
            presenter.onSignOutClicked()
        }
    }

    // ===== SETTINGS CONTRACT.VIEW IMPLEMENTATIONS =====

    override fun showUserInfo(name: String, email: String) {
        tvSettingsName.text = name
        tvSettingsEmail.text = email
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun navigateToChangePassword() {
        startActivity(Intent(this, ChangePasswordActivity::class.java))
    }

    override fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.onDestroy()
    }
}