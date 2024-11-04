package sv.edu.ufg.fis.amb.appturismo

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import sv.edu.ufg.fis.amb.appturismo.pages.FotoPage
import sv.edu.ufg.fis.amb.appturismo.pages.GalPage
import sv.edu.ufg.fis.amb.appturismo.pages.MainPage
import sv.edu.ufg.fis.amb.appturismo.pages.MediaViewPage
import sv.edu.ufg.fis.amb.appturismo.pages.VideoPage
import sv.edu.ufg.fis.amb.appturismo.routes.ARG_MVP
import sv.edu.ufg.fis.amb.appturismo.routes.ROOT_MVP
import sv.edu.ufg.fis.amb.appturismo.routes.Routes

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun SetupNavGraph(
    navController: NavHostController
){
    NavHost(
        navController = navController,
        startDestination = Routes.MainRoute.route
    ){
        composable(
            route = Routes.MainRoute.route
        ) {
            MainPage(navController)
        }

        composable(
            route = Routes.FotoRoute.route
        ){
            FotoPage(navController)
        }

        composable(
            route = Routes.VideoRoute.route
        ){
            VideoPage(navController)
        }

        composable(
            route = Routes.GalRoute.route
        ) {
            GalPage(navController)
        }

        composable(
            route = Routes.MediaViewRoute.route
        ) { backStackEntry ->
            val uriString = backStackEntry.arguments?.getString(ARG_MVP) ?: ""
            val uri = Uri.parse(Uri.decode(uriString))
            MediaViewPage(navController, uri)
        }
    }
}