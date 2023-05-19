package pt.ulisboa.tecnico.cmov.librarist.screens.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState

@Composable
fun PermissionDialog(permission: String, showDialog: MutableState<Boolean>){
    AlertDialog(
        onDismissRequest = { showDialog.value = false },
        title = { Text(text = "$permission permission Denied") },
        text = { Text(text = "Some features won't work without the $permission permissions.") },
        confirmButton = {
            TextButton(onClick = { showDialog.value = false }) {
                Text(text = "OK")
            }
        }
    )
}