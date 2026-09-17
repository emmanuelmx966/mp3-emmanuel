package com.emmanuelmx.mp3emmanuel.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(onBackClick: () -> Unit) {
    var boostBass by remember { mutableFloatStateOf(0f) }
    var boostTreble by remember { mutableFloatStateOf(0f) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ecualizador") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Bajos", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = boostBass,
                onValueChange = { boostBass = it },
                valueRange = 0f..100f
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("Agudos", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = boostTreble,
                onValueChange = { boostTreble = it },
                valueRange = 0f..100f
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text("Perfiles Preestablecidos", style = MaterialTheme.typography.titleMedium)
            val presets = listOf("Normal", "Pop", "Rock", "Jazz", "Classic")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                presets.forEach { preset ->
                    FilterChip(
                        selected = false,
                        onClick = { /* TODO */ },
                        label = { Text(preset) }
                    )
                }
            }
        }
    }
}
