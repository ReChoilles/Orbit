package com.qx.orbit.bili.presentation.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material3.AlertDialog
import androidx.wear.compose.material3.AlertDialogDefaults
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text

@Composable
fun WysAlertDialog(
    show: Boolean,
    onDismissRequest: () -> Unit,
    title: String,
    content: @Composable (() -> Unit)? = null,
    onConfirm: () -> Unit
) {
    AlertDialog(
        visible = show,
        onDismissRequest = onDismissRequest,
        modifier = Modifier
            .fillMaxSize(),
        icon = {
            Spacer(modifier = Modifier.height(24.dp))
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
               },
        title = {
            Text(
                text = title,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )
                },
        text = content,
        confirmButton = {
            AlertDialogDefaults.ConfirmButton(
                onClick = { onConfirm() }
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "确认",
                    modifier = Modifier.size(24.dp)
                    )
                            }
                        },
        dismissButton = {
            AlertDialogDefaults.DismissButton(
                onClick = { onDismissRequest() }
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "取消",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    )
}
