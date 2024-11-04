package sv.edu.ufg.fis.amb.appturismo.routes

const val ROOT_MAIN = "main"
const val ROOT_FOTO = "foto"
const val ROOT_VIDEO = "video"
const val ROOT_GAL = "galeria"
const val ROOT_MVP = "mediaView"
const val ARG_MVP = "uri"

sealed class Routes(
    val route : String
){
    object MainRoute: Routes(route = ROOT_MAIN)
    object FotoRoute: Routes(route = ROOT_FOTO)
    object VideoRoute: Routes(route = ROOT_VIDEO)
    object GalRoute: Routes(route = ROOT_GAL)
    object MediaViewRoute: Routes(route = "${ROOT_MVP}/{${ARG_MVP}}")
}