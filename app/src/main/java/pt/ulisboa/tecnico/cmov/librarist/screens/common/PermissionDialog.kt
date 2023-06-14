package pt.ulisboa.tecnico.cmov.librarist.screens.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.platform.LocalContext
import pt.ulisboa.tecnico.cmov.librarist.R

@Composable
fun PermissionDialog(permission: String, showDialog: MutableState<Boolean>){
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = { showDialog.value = false },
        title = { Text(text = permission + context.getString(R.string.some_permission_denied)) },
        text = { Text(text = context.getString(R.string.without_permission) + permission + context.getString(R.string.permissions)) },
        confirmButton = {
            TextButton(onClick = { showDialog.value = false }) {
                Text(text = context.getString(R.string.ok))
            }
        }
    )
}