package sv.edu.ufg.fis.amb.appturismo.pages

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import android.widget.Toast
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import sv.edu.ufg.fis.amb.appturismo.R
import sv.edu.ufg.fis.amb.appturismo.routes.Routes
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun VideoPage(
    navController: NavHostController
){
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    //val coroutineScope = rememberCoroutineScope()
    val previewView = remember { PreviewView(context) }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    var recording: Recording? by remember { mutableStateOf(null) }
    var videoCapture: VideoCapture<Recorder>? by remember { mutableStateOf(null) }
    var isRecording by remember { mutableStateOf(false) }
    var isFrontCamera by remember { mutableStateOf(false) }
    var currentZoom by remember { mutableFloatStateOf(1f) }
    var cameraControl: CameraControl? by remember { mutableStateOf(null) }

    fun bindCamera(
        cameraProvider: ProcessCameraProvider
    ){
        val cameraSelector = if(isFrontCamera)
                CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA

        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
            .build()

        videoCapture = VideoCapture.withOutput(recorder)

        try{
            cameraProvider.unbindAll()
            val camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                },
                videoCapture
            )
            cameraControl = camera.cameraControl
        }catch (ex : Exception) {
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
        bindCamera(cameraProvider)
    }

    LaunchedEffect(isFrontCamera) {
        val cameraProvider = cameraProviderFuture.get()
        bindCamera(cameraProvider)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
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
            TopOptions(navController){
                isFrontCamera = !isFrontCamera
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ){

            Button(
                modifier = Modifier
                    .size(64.dp),
                onClick = {
                    if(isRecording){
                        recording?.stop()
                        recording = null
                        isRecording = false
                    }else{
                        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                            .format(System.currentTimeMillis())

                        val videoFile = File(getOutputDir(context), "VID_$timestamp.mp4")
                        val outputOptions = FileOutputOptions.Builder(videoFile).build()

                        recording = videoCapture?.output
                            ?.prepareRecording(context, outputOptions)
                            ?.apply {
                                if (ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED
                                ) {
                                    withAudioEnabled()
                                }
                            }?.start(ContextCompat.getMainExecutor(context)) { recordEvent ->
                                when(recordEvent){
                                    is VideoRecordEvent.Start -> {
                                        isRecording = true
                                        Toast.makeText(
                                            context,
                                            "GRABACION INICIADA",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    is VideoRecordEvent.Finalize -> {
                                        isRecording = false
                                        if(recordEvent.hasError()){
                                            Toast.makeText(
                                                context,
                                                "ERROR A LA HORA DE GRABAR",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }else{
                                            Toast.makeText(
                                                context,
                                                "VIDEO GUARDADO CON EXITO",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            }
                    }
                },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRecording) Color.Red else Color.White
                )
            ){}
        }
    }

}

//-----------------------------------------------------------------[TOP OPTIONS]
@Composable
private fun TopOptions(
    navController: NavHostController,
    onSwitchCamera: () -> Unit
){

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 45.dp, start = 15.dp, end = 15.dp),
        horizontalArrangement = Arrangement.SpaceBetween
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

        IconButton(
            onClick = onSwitchCamera,
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = Color.White,
                containerColor = colorResource(R.color.optBtnExitCamera)
            )
        ) {
            Icon(
                painter = painterResource(R.drawable.switch_camera),
                contentDescription = "Switch camera",
                tint = Color.White
            )
        }
    }

}



//-------------------------[OBTENER DIRECTORIO]

private fun getOutputDir(context: Context): File {

    val mediaDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.let {
        File(it, context.getString(R.string.app_name)).apply { mkdirs() }
    }

    return if(mediaDir != null && mediaDir.exists()) mediaDir else context.filesDir
}