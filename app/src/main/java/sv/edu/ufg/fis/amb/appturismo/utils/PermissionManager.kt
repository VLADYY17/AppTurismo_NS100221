package sv.edu.ufg.fis.amb.appturismo.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat

@Composable
fun GetPermissionsLauncher(
    onPermissionsGranted : () -> Unit,
    onPermissionsFailed: () -> Unit
): ManagedActivityResultLauncher<Array<String>, Map<String, @JvmSuppressWildcards Boolean>> {

    return rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
            result ->
        val isGranted = result.values.all { it }
        if(isGranted){
            onPermissionsGranted()
        }else{
            onPermissionsFailed()
        }
    }
}

fun CheckPermissions(
    context: Context,
    permissions: Array<String>,
    onPermissionsGranted: () -> Unit,
    onPermissionsFailed: () -> Unit
){
    val allPermissionsGranted = permissions.all {
        permission ->
        ContextCompat
            .checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    if(allPermissionsGranted){
        onPermissionsGranted()
    }else{
        onPermissionsFailed()
    }
}
