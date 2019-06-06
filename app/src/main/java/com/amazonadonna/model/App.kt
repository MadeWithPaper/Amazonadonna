package com.amazonadonna.model

object App {
    //Singleton object holding shared utilities

    //back end base url
    const val BACKEND_BASE_URL = "https://99956e2a.ngrok.io"

    //indicate if user using the app is artisan or CGA, should only be edited in HomeScreenArtisan.kt to true (this is the only instance where we are sure an artisan is logged in)
    var artisanMode = false

    //CGA mode - current artisan in focus, set when user selects an artisan form List all Artisan screen
    var currentArtisan : Artisan = Artisan("placeholder", "placeholder", "0", "placeholder", false,"placeholder", "placeholder", "placeholder", "placeholder", 0.0, 0.0, "placeholder", 0, 0.0)
}