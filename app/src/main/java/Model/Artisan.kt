import com.beust.klaxon.Json
import java.io.Serializable
import java.util.*

data class Artisan (
        @Json(name = "name") var name : String,
        @Json(name = "artisanID") var artisanID : String,
        @Json(name = "city") var city : String,
        @Json(name = "country") var country : String,
        @Json(name = "bio")var bio : String,
        @Json(name = "cgoId") var cgoID : String,
        @Json(name = "lon") var lon : Double,
        @Json(name = "lat") var lat : Double) : Serializable {

    fun generateArtisanID() {
        //TODO fill in logic for generating unique ID for artisan
        var num = Random().nextInt()
        artisanID = name + cgoID + num.toString()
    }
}