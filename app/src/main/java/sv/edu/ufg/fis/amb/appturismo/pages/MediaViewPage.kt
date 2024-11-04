package sv.edu.ufg.fis.amb.appturismo.pages

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import sv.edu.ufg.fis.amb.appturismo.R

@Composable
fun MediaViewPage(
    navController: NavHostController,
    uri: Uri
){
    val context = LocalContext.current
    val isVideo = uri.toString().endsWith(".mp4")

    Box(
        modifier = Modifier.fillMaxSize()
    ){
        TopBar(navController)

        if(isVideo){
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = {
                    VideoView(context).apply {
                        setVideoURI(uri)
                        setMediaController(MediaController(context))
                        start()
                    }
                }
            )
        }else{
            AsyncImage(
                model = uri,
                contentDescription = "Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

    }
}

//-----------------------------------------------------------------[TOP BAR]
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    navController: NavHostController
){

    Column {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.topBarGal),
                    fontSize = 25.sp
                )
            },
            navigationIcon = {
                IconButton(
                    modifier = Modifier
                        .padding(start = 10.dp, end = 10.dp)
                        .size(20.dp),
                    onClick = {
                        navController.popBackStack()
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "back",
                        tint = Color.White
                    )
                }
            }
        )
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth()
        )
    }

}