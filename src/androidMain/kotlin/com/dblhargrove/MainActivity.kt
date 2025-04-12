package com.dblhargrove

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.dblhargrove.logic.database.DatabaseProvider
import com.dblhargrove.logic.database.provideAndroidContext
import com.dblhargrove.logic.network.setupHttpClient

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        provideAndroidContext(this) // ← REQUIRED
        setupHttpClient() // Inject Android HttpClient
        DatabaseProvider.initialize()
    }
}

