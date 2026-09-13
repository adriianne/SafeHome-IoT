package com.example.safehome.presenter

import com.example.safehome.contract.ForgotPasswordContract
import com.example.safehome.model.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ForgotPasswordPresenter(
    private val view: ForgotPasswordContract.View,
    private val repository: AuthRepository = AuthRepository()
) : ForgotPasswordContract.Presenter {

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    override fun validateAndSendResetEmail(email: String) {
        if (email.isBlank()) {
            view.showEmailError("Email is required")
            return
        }
        if (!email.matches(Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))) {
            view.showEmailError("Invalid email format")
            return
        }
        view.showEmailError("")

        view.showLoading()
        scope.launch {
            val result = repository.sendPasswordReset(email)
            withContext(Dispatchers.Main) {
                view.hideLoading()
                result.fold(
                    onSuccess = {
                        view.showSuccess("Reset email sent! Check your inbox.")
                        view.navigateToLogin()
                    },
                    onFailure = { error ->
                        view.showError(error.message ?: "Failed to send reset email")
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        job.cancel()
    }
}