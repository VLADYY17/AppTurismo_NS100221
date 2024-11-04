package sv.edu.ufg.fis.amb.appturismo.pages

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import sv.edu.ufg.fis.amb.appturismo.R
import sv.edu.ufg.fis.amb.appturismo.routes.Routes
import androidx.camera.core.Preview as CameraCorePreview
import sv.edu.ufg.fis.amb.appturismo.theme.AppTurismoTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.coroutines.resumeWithException

@Composable
fun FotoPage(
    navController: NavHostController
){
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val previewView = remember { PreviewView(context) }
    var imageCapture by remember { mutableStateOf(ImageCapture.Builder().build()) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isFlashEnabled by remember { mutableStateOf(false) }
    var isFrontCamera by remember { mutableStateOf(false) }
    var currentZoom by remember { mutableStateOf(1f) }

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var cameraControl: CameraControl? by remember { mutableStateOf(null) }

    fun BindCamera(cameraProvider: ProcessCameraProvider){
        val cameraSelector = if (isFrontCamera)
            CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA

        imageCapture = ImageCapture.Builder()
            .setFlashMode(if (isFlashEnabled)
                ImageCapture.FLASH_MODE_ON else ImageCapture.FLASH_MODE_OFF
            ).build()

        try {
            cameraProvider.unbindAll()
            val camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                CameraCorePreview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                },
                imageCapture
            )
            cameraControl = camera.cameraControl

        }catch (exc : Exception){
            Toast.makeText(
                context,
                "NO SE PUEDE INICIALIZAR LA CAMARA",
                Toast.LENGTH_SHORT
            ).show()
            navController.navigate(Routes.MainRoute.route)
        }

    }

    LaunchedEffect(cameraProviderFuture) {
        val cameraProvider = cameraProviderFuture.get()
        BindCamera(cameraProvider)
    }

    LaunchedEffect(isFlashEnabled, isFrontCamera) {
        val cameraProvider = cameraProviderFuture.get()
        BindCamera(cameraProvider)
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ){
            AndroidView(
                factory = { previewView },
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.80f)
                    .pointerInput(Unit){
                        detectTransformGestures{
                                _, _, zoomChange, _ ->
                            val newZoom = (currentZoom * zoomChange).coerceIn(1f, 10f)
                            currentZoom = newZoom
                            cameraControl?.setZoomRatio(newZoom)
                        }
                    }
            )
            TopOptions(navController)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(
                onClick = { isFlashEnabled = !isFlashEnabled}
            ) {
                Icon(
                    painter = painterResource(R.drawable.flash_on),
                    contentDescription = "flash",
                    tint = if(isFlashEnabled) Color.Yellow else Color.White
                )
            }

            Button(
                modifier = Modifier.size(64.dp),
                onClick = {
                    coroutineScope.launch {
                        imageUri = savePhoto(context, imageCaptured = imageCapture )
                        if(imageUri != null){
                            Toast.makeText(
                                context,
                                "FOTO GUARDADA CON EXITO",
                                Toast.LENGTH_SHORT
                            ).show()
                        }else{
                            Toast.makeText(
                                context,
                                "NO SE PUDO GUARDAR LA FOTO",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ){ Text("") }

            IconButton(
                onClick = { isFrontCamera = !isFrontCamera }
            ) {
                Icon(
                    painter = painterResource(R.drawable.switch_camera),
                    contentDescription = "Switch camera",
                    tint = Color.White
                )
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
private fun Preview(){

    AppTurismoTheme(
        dynamicColor = false
    ) {
        FotoPage(rememberNavController())
    }
}

//-----------------------------------------------------------------[TOP OPTIONS]
@Composable
private fun TopOptions(
    navController: NavHostController
){

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 45.dp, start = 15.dp, end = 15.dp)
    ) {
        IconButton(
            onClick = { navController.popBackStack() },
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = Color.White,
                containerColor = colorResource(R.color.optBtnExitCamera)
            )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Home",
            )
        }
    }

}

//-----------------------------------------------------------------[FUNCTIONS]

//-------------------------[GUARDAR FOTOS]
@OptIn(ExperimentalCoroutinesApi::class)
suspend fun savePhoto(
    context: Context,
    imageCaptured: ImageCapture
) : Uri? = suspendCancellableCoroutine {
    continuacion ->
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        .format(System.currentTimeMillis())
    val photoFile = File(
        getOutputDir(context),
        "IMG_$timestamp.jpg"
    )

    val photoUri: Uri = FileProvider.getUriForFile(
        context,
        "sv.edu.ufg.fis.amb.appturismo.fileprovider",
        photoFile
    )

    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

    imageCaptured.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                continuacion.resume(photoUri) {}
            }

            override fun onError(exception: ImageCaptureException) {
                continuacion.resumeWithException(exception)
            }
        }
    )
}

//-----------------------------------------------------------------[FUNCTIONS]

//-------------------------[OBTENER DIRECTORIO]
private fun getOutputDir(context: Context): File {

    val mediaDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.let {
        File(it, context.getString(R.string.app_name)).apply { mkdirs() }
    }

    return if(mediaDir != null && mediaDir.exists()) mediaDir else context.filesDir
}
