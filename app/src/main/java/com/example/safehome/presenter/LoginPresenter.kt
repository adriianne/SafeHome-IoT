package com.example.safehome.presenter

import com.example.safehome.contract.LoginContract
import com.example.safehome.model.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginPresenter(
    private val view: LoginContract.View,
    private val repository: AuthRepository = AuthRepository()
) : LoginContract.Presenter {

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    override fun validateAndLogin(email: String, password: String) {
        // Validate Email
        if (email.isBlank()) {
            view.showEmailError("Email is required")
            return
        }
        if (!email.matches(Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))) {
            view.showEmailError("Invalid email format")
            return
        }
        view.showEmailError("")

        // Validate Password
        if (password.isBlank()) {
            view.showPasswordError("Password is required")
            return
        }
        if (password.length < 6) {
            view.showPasswordError("Password must be at least 6 characters")
            return
        }
        view.showPasswordError("")

        // Perform Login
        view.showLoading()
        scope.launch {
            val result = repository.login(email, password)
            withContext(Dispatchers.Main) {
                view.hideLoading()
                result.fold(
                    onSuccess = { user ->
                        view.onLoginSuccess(user)
                        view.navigateToHome()
                    },
                    onFailure = { error ->
                        view.showError(error.message ?: "Login failed. Please try again.")
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        job.cancel()
    }
}