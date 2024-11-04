package sv.edu.ufg.fis.amb.appturismo.pages


import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import sv.edu.ufg.fis.amb.appturismo.R
import sv.edu.ufg.fis.amb.appturismo.routes.ROOT_MVP
import sv.edu.ufg.fis.amb.appturismo.routes.Routes

import java.io.File

@Composable
fun GalPage(
    navController: NavHostController
){
    val context = LocalContext.current
    val files = remember { getMediaFiles(context) }

    Scaffold(
        topBar = { TopBar(navController) }
    ) {
        paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding()
                )
                .background(color = Color.Black)
        ) {
            LazyVerticalGrid(
                contentPadding = PaddingValues(8.dp),
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items(files) { file ->
                    MediaItem(context, file) { uri ->
                        val encodeUri = Uri.encode(uri.toString())
                        navController.navigate("${ROOT_MVP}/$encodeUri")
                    }
                }
            }
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
                    text = stringResource(R.string.topBarName),
                    fontSize = 25.sp
                )
            },
            navigationIcon = {
                IconButton(
                    modifier = Modifier
                        .padding(start = 10.dp, end = 10.dp)
                        .size(20.dp),
                    onClick = {
                        navController.navigate(Routes.MainRoute.route)
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "close app"
                    )
                }
            }
        )
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth()
        )
    }

}

//-----------------------------------------------------------------[ITEMS]
@Composable
private fun MediaItem(
    context: Context,
    file: File,
    onClick: (Uri) -> Unit
){
    val uri = Uri.fromFile(file)
    val isVideo = uri.toString().endsWith(".mp4")

    Box(
        modifier = Modifier
            .padding(4.dp)
            .size(300.dp)
            .clickable { onClick(uri) }
    ){
        if(isVideo){
            Icon(
                modifier = Modifier
                    .align(Alignment.Center),
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "play video",
                tint = Color.White
            )
        }

        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(uri)
                .crossfade(true)
                .build(),
            contentDescription = "Thumbnail",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize(),
        )
    }
}

//-------------------------[RECUPERAR FOTOS Y VIDEOS]
private fun getMediaFiles(context: Context) : List<File> {
    val mediaDir = getOutputDir(context)
    return mediaDir
        .listFiles { file -> file.extension in listOf("jpg", "png", "mp4") }?.toList()
        ?: emptyList()
}

//-------------------------[OBTENER DIRECTORIO]

private fun getOutputDir(context: Context): File {

    val mediaDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.let {
        File(it, context.getString(R.string.app_name)).apply { mkdirs() }
    }

    return if(mediaDir != null && mediaDir.exists()) mediaDir else context.filesDir
}