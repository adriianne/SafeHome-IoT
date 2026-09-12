package com.example.safehome

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.safehome.view.activities.LoginActivity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}