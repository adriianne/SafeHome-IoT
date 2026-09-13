package com.example.safehome.presenter

import com.example.safehome.contract.SettingsContract
import com.example.safehome.model.repository.AuthRepository

class SettingsPresenter(
    private val view: SettingsContract.View,
    private val authRepository: AuthRepository = AuthRepository()
) : SettingsContract.Presenter {

    override fun loadUserInfo() {
        val user = authRepository.getCurrentUser()
        if (user != null) {
            val name = user.displayName.ifEmpty {
                "${user.firstName} ${user.lastName}".trim()
            }
            view.showUserInfo(name.ifEmpty { "User" }, user.email)
        } else {
            view.navigateToLogin()
        }
    }

    override fun onChangePasswordClicked() {
        view.navigateToChangePassword()
    }

    override fun onSignOutClicked() {
        authRepository.signOut()
        view.navigateToLogin()
    }

    override fun onDestroy() {
        // No coroutine scope used — nothing to clean up
    }
}