package com.example.myguidance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myguidance.ui.theme.MyGuidanceTheme
import com.cloudinary.android.MediaManager
import com.example.myguidance.ui.theme.AppTheme

class MainActivity : ComponentActivity() {

    private val postViewModel: PostViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initCloudinary()

        setContent {
            val themeViewModel: ThemeViewModel = viewModel()
            val preset by themeViewModel.preset.collectAsState()

            val darkMode by themeViewModel.darkMode.collectAsState()
            val accent by themeViewModel.accent.collectAsState()
            AppTheme(preset, darkMode, accent) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        authViewModel = authViewModel,
                        postViewModel = postViewModel,
                        themeViewModel = themeViewModel
                    )
                }
            }
        }
    }

    private fun initCloudinary() {
        val config = hashMapOf<String, Any>()
        config["cloud_name"] = "ba1crttg"

        try {
            MediaManager.init(this, config)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}