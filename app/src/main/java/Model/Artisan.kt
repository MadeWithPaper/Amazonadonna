import java.io.Serializable

data class Artisan (var name : String,
                    val artisanID : String,
                    var city : String,
                    var country : String,
                    var bio : String,
                    var cgoID : String,
                    var lon : Double,
                    var lat : Double) : Serializable {

//    @Json(artisanId = "artisanId")
//    val artisanID : String,
//    @Json(name = "name")
//    val artisanName : String,

    private fun generateArtisanID() {
        //TODO fill in logic for generating unique ID for artisan

    }


}