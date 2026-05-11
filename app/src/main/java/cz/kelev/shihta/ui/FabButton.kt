package cz.kelev.shihta.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun FabButtons(
    onSettingsClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
        FloatingActionButton(
            onClick = onShareClick,
            shape = CircleShape,
            containerColor = Color(0xFFF5F0E8),
            contentColor = Color(0xFF8B5E3C),
            modifier = Modifier.size(48.dp)
        ) {
            Icon(Icons.Default.Share, contentDescription = "Share")
        }
        FloatingActionButton(
            onClick = onSettingsClick,
            shape = CircleShape,
            containerColor = Color(0xFF6B4226),
            contentColor = Color.White,
            modifier = Modifier.size(56.dp).padding(top = 16.dp)
        ) {
            Icon(Icons.Default.Settings, contentDescription = "Settings")
        }
    }
}