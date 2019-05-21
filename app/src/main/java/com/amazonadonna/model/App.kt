package com.amazonadonna.model

object App {
    //Singleton object holding shared utilities

    //back end base url
    const val BACKEND_BASE_URL = "https://99956e2a.ngrok.io"

    //TODO remove after implementation
    val testArtisan = Artisan("Jacky Test", "acac9350-7297-11e9-82e2-2d4e8dd4269e", "(831) 905-0008", "slo", "usa", "DO NOT REMOVE ME, USING TO TEST STANDALONE ARTISAN FEATURES", "AFIFS27DVTVK5RZ5GIR2DHAFMNAQ", 0.0, 0.0, "https://artisan-prof-pics-new.s3.amazonaws.com/acac9350-7297-11e9-82e2-2d4e8dd4269e.png", 0, 5000.0)

    //indicate if user using the app is artisan or CGA, should only be edited in HomeScreenArtisan.kt to true (this is the only instance where we are sure an artisan is logged in)
    var artisanMode = false

    //CGA mode - current artisan in focus, set when user selects an artisan form List all Artisan screen
    var currentArtisan : Artisan = Artisan("placeholder", "placeholder", "0", "placeholder", "placeholder", "placeholder", "placeholder", 0.0, 0.0, "placeholder", 0, 0.0)


    fun setTestingApp(newArtisanMode: Boolean) {
        currentArtisan = Artisan("placeholder", "placeholder", "0", "placeholder", "placeholder", "placeholder", "placeholder", 0.0, 0.0, "placeholder", 0, 0.0)
        artisanMode = newArtisanMode
    }
}