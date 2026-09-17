package com.example.safehome.view.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.safehome.R
import com.example.safehome.contract.SettingsContract
import com.example.safehome.model.UserSettings
import com.example.safehome.presenter.SettingsPresenter
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.util.Locale

class SettingsActivity : AppCompatActivity(), SettingsContract.View {

    private lateinit var presenter: SettingsContract.Presenter

    private lateinit var btnBack: ImageView
    private lateinit var tvSettingsName: TextView
    private lateinit var tvSettingsEmail: TextView
    private lateinit var tvDeviceId: TextView
    private lateinit var btnChangePassword: MaterialCardView
    private lateinit var btnSettingsSignOut: MaterialButton

    private lateinit var tilRate: TextInputLayout
    private lateinit var tilIdle: TextInputLayout
    private lateinit var tilWasteThreshold: TextInputLayout
    private lateinit var tilHighDraw: TextInputLayout
    private lateinit var edtRate: TextInputEditText
    private lateinit var edtIdle: TextInputEditText
    private lateinit var edtWasteThreshold: TextInputEditText
    private lateinit var edtHighDraw: TextInputEditText
    private lateinit var btnSaveSettings: MaterialButton
    private lateinit var progressSettings: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        presenter = SettingsPresenter(this)
        initViews()
        setupListeners()

        presenter.loadUserInfo()
        presenter.loadSettings()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvSettingsName = findViewById(R.id.tvSettingsName)
        tvSettingsEmail = findViewById(R.id.tvSettingsEmail)
        tvDeviceId = findViewById(R.id.tvDeviceId)
        btnChangePassword = findViewById(R.id.btnChangePassword)
        btnSettingsSignOut = findViewById(R.id.btnSettingsSignOut)

        tilRate = findViewById(R.id.tilRate)
        tilIdle = findViewById(R.id.tilIdle)
        tilWasteThreshold = findViewById(R.id.tilWasteThreshold)
        tilHighDraw = findViewById(R.id.tilHighDraw)
        edtRate = findViewById(R.id.edtRate)
        edtIdle = findViewById(R.id.edtIdle)
        edtWasteThreshold = findViewById(R.id.edtWasteThreshold)
        edtHighDraw = findViewById(R.id.edtHighDraw)
        btnSaveSettings = findViewById(R.id.btnSaveSettings)
        progressSettings = findViewById(R.id.progressSettings)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }
        btnChangePassword.setOnClickListener { presenter.onChangePasswordClicked() }
        btnSettingsSignOut.setOnClickListener { presenter.onSignOutClicked() }

        btnSaveSettings.setOnClickListener {
            presenter.saveSettings(
                rate = edtRate.text.toString(),
                idle = edtIdle.text.toString(),
                wasteThreshold = edtWasteThreshold.text.toString(),
                highDraw = edtHighDraw.text.toString()
            )
        }
    }

    // ===== SettingsContract.View =====

    override fun showUserInfo(name: String, email: String) {
        tvSettingsName.text = name
        tvSettingsEmail.text = email
    }

    override fun showPairedDevice(deviceId: String) {
        tvDeviceId.text = deviceId
    }

    override fun showSettings(settings: UserSettings) {
        edtRate.setText(String.format(Locale.US, "%.2f", settings.electricityRatePhp))
        edtIdle.setText(settings.idleThresholdMinutes.toString())
        edtWasteThreshold.setText(settings.wastePowerThresholdW.toString())
        edtHighDraw.setText(settings.highDrawLimitW.toString())
    }

    override fun showRateError(message: String?) {
        tilRate.error = message
        // TextInputLayout hides helper text while an error is showing, so restore it.
        tilRate.helperText =
            if (message == null) getString(R.string.electricity_rate_help) else null
    }

    override fun showIdleError(message: String?) {
        tilIdle.error = message
        tilIdle.helperText =
            if (message == null) getString(R.string.idle_period_help) else null
    }

    override fun showWasteThresholdError(message: String?) {
        tilWasteThreshold.error = message
        tilWasteThreshold.helperText =
            if (message == null) getString(R.string.waste_threshold_help) else null
    }

    override fun showHighDrawError(message: String?) {
        tilHighDraw.error = message
        tilHighDraw.helperText =
            if (message == null) getString(R.string.high_draw_limit_help) else null
    }

    override fun showSaving(saving: Boolean) {
        btnSaveSettings.isEnabled = !saving
        progressSettings.visibility = if (saving) View.VISIBLE else View.GONE
    }

    override fun showSaveSuccess() {
        Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show()
        finish()
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
        // Before super, so the presenter can still touch the view if it needs to.
        presenter.onDestroy()
        super.onDestroy()
    }
}