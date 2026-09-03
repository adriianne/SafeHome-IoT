package com.example.safehome.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class LoginErrors(
    val email: String? = null,
    val password: String? = null
)

data class SignupErrors(
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val password: String? = null,
    val confirmPassword: String? = null
)

class AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    // ✅ CORRECT URL from the error message
    private val database: DatabaseReference = FirebaseDatabase
        .getInstance("https://safehome-6c4db-default-rtdb.asia-southeast1.firebasedatabase.app/")
        .reference

    private val _loginErrors = MutableLiveData(LoginErrors())
    val loginErrors: LiveData<LoginErrors> = _loginErrors

    private val _signupErrors = MutableLiveData(SignupErrors())
    val signupErrors: LiveData<SignupErrors> = _signupErrors

    private val _isSubmitting = MutableLiveData(false)
    val isSubmitting: LiveData<Boolean> = _isSubmitting

    private val _formError = MutableLiveData<String?>(null)
    val formError: LiveData<String?> = _formError

    private val _signedIn = MutableLiveData(false)
    val signedIn: LiveData<Boolean> = _signedIn

    private val _accountCreated = MutableLiveData(false)
    val accountCreated: LiveData<Boolean> = _accountCreated

    val currentUserName: String?
        get() = auth.currentUser?.displayName

    val currentUserEmail: String?
        get() = auth.currentUser?.email

    fun isAlreadySignedIn(): Boolean = auth.currentUser != null

    fun clearFormError() {
        _formError.value = null
    }

    fun clearLoginFieldError(field: LoginField) {
        val current = _loginErrors.value ?: LoginErrors()
        _loginErrors.value = when (field) {
            LoginField.EMAIL -> current.copy(email = null)
            LoginField.PASSWORD -> current.copy(password = null)
        }
        _formError.value = null
    }

    fun clearSignupFieldError(field: SignupField) {
        val current = _signupErrors.value ?: SignupErrors()
        _signupErrors.value = when (field) {
            SignupField.FIRST_NAME -> current.copy(firstName = null)
            SignupField.LAST_NAME -> current.copy(lastName = null)
            SignupField.EMAIL -> current.copy(email = null)
            SignupField.PASSWORD -> current.copy(password = null)
            SignupField.CONFIRM_PASSWORD -> current.copy(confirmPassword = null)
        }
        _formError.value = null
    }

    fun signIn(email: String, password: String) {
        val errors = LoginErrors(
            email = Validators.validateEmail(email),
            password = Validators.validateExistingPassword(password)
        )
        _loginErrors.value = errors
        if (errors.email != null || errors.password != null) return

        viewModelScope.launch {
            _isSubmitting.value = true
            _formError.value = null
            try {
                auth.signInWithEmailAndPassword(email.trim(), password).await()
                _signedIn.value = true
            } catch (e: Exception) {
                _formError.value = messageForSignIn(e)
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun signUp(
        firstName: String,
        lastName: String,
        email: String,
        password: String,
        confirmPassword: String
    ) {
        val errors = SignupErrors(
            firstName = Validators.validateName(firstName, "First name"),
            lastName = Validators.validateName(lastName, "Last name"),
            email = Validators.validateEmail(email),
            password = Validators.validateNewPassword(password),
            confirmPassword = Validators.validateConfirmPassword(password, confirmPassword)
        )
        _signupErrors.value = errors

        val hasError = listOfNotNull(
            errors.firstName, errors.lastName, errors.email,
            errors.password, errors.confirmPassword
        ).isNotEmpty()
        if (hasError) return

        viewModelScope.launch {
            _isSubmitting.value = true
            _formError.value = null
            _accountCreated.value = false
            try {
                val first = firstName.trim()
                val last = lastName.trim()
                val mail = email.trim()

                val result = auth.createUserWithEmailAndPassword(mail, password).await()
                val uid = result.user?.uid
                    ?: throw IllegalStateException("Account created but no user ID returned")

                result.user?.updateProfile(
                    UserProfileChangeRequest.Builder()
                        .setDisplayName("$first $last")
                        .build()
                )?.await()

                val profile = mapOf(
                    "firstName" to first,
                    "lastName" to last,
                    "email" to mail,
                    "createdAt" to System.currentTimeMillis()
                )
                database.child("users").child(uid).setValue(profile).await()

                _accountCreated.value = true
                _formError.value = "Account created successfully!"

            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is FirebaseAuthUserCollisionException -> "That email already has an account. Sign in instead."
                    is FirebaseAuthWeakPasswordException -> "That password is too weak. Try a longer one."
                    is FirebaseNetworkException -> "No internet connection. Check your network and try again."
                    else -> e.message ?: "Couldn't create your account. Please try again."
                }
                _formError.value = errorMessage
                e.printStackTrace()
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun signOut() {
        auth.signOut()
        _signedIn.value = false
        _accountCreated.value = false
        _loginErrors.value = LoginErrors()
        _signupErrors.value = SignupErrors()
        _formError.value = null
    }

    fun resetAccountCreated() {
        _accountCreated.value = false
    }

    private fun messageForSignIn(e: Exception): String = when (e) {
        is FirebaseNetworkException -> "No internet connection. Check your network and try again."
        else -> "Email or password is incorrect."
    }
}

enum class LoginField { EMAIL, PASSWORD }
enum class SignupField { FIRST_NAME, LAST_NAME, EMAIL, PASSWORD, CONFIRM_PASSWORD }