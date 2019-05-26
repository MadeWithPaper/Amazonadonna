package com.amazonadonna.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_add_item_category.*
import android.widget.Spinner
import com.amazonadonna.model.App
import com.amazonadonna.model.Product
import com.amazonadonna.sync.Synchronizer.Companion.SYNC_NEW
import kotlinx.android.synthetic.main.activity_payout_history.*


class AddItemCategory : AppCompatActivity() {

    val SELECT_CATEGORY = "--Select Category--"
    val SELECT_SUBCATEGORY = "--Select Sub-Category--"
    val SELECT_SPECIFICCATEGORY = "--Select Specific Category--"

    var mainCategory : String = SELECT_CATEGORY
    var subCategory : String = SELECT_SUBCATEGORY
    var specificCategory : String = SELECT_SPECIFICCATEGORY

    //array of values for each adapter
    val mainCategoryList = arrayOf(SELECT_CATEGORY, "Jewelry", "Home & Kitchen", "Clothing, Shoes & Accessories", "Wedding", "Handbags & Totes", "Beauty & Grooming", "Stationery & Party Supplies", "Toys & Games", "Pet Supplies", "Sports & Outdoors", "Baby")
    val jewelrysub = arrayOf(SELECT_SUBCATEGORY, "Accessories", "Anklets", "Body Jewelry", "Bracelets", "Brooches & Pins", "Charms", "Cufflings & Shirt Accessories", "Earrings", "Hair Jewelry", "Jewelry Sets", "Necklaces", "Pendants & Coins", "Rings", "Wedding & Engagement")
    val accessoriessub = arrayOf(SELECT_SPECIFICCATEGORY, "Jewelry Boxes & Organizers", "Keychains & Keyrings")
    val nosub = arrayOf("-- Not Applicable --")
    val necklacessub = arrayOf(SELECT_SPECIFICCATEGORY, "Chains", "Chokers", "Lariats", "Pendants", "Strands")
    val earringssub = arrayOf(SELECT_SPECIFICCATEGORY, "Chandelier Earrings","Drop Earrings", "Ear Cuffs", "Hoop Earrings", "Stud Earrings")
    val braceletssub = arrayOf(SELECT_SPECIFICCATEGORY, "Bangles", "Charm Bracelets", "Cuffs", "Link Bracelets", "Stretch Bracelets", "Wrap Bracelets")
    val ringssub = arrayOf(SELECT_SPECIFICCATEGORY, "Bands", "Stacking", "Statement")
    val hairjewelrysub = arrayOf(SELECT_SPECIFICCATEGORY, "Clips & Barrettes", "Headbands", "Pins", "Side Combs", "Tiaras")
    val cufflingssub = arrayOf(SELECT_SPECIFICCATEGORY, "Cufflinks", "Tie Clips", "Tie Pins")
    val homeKitchensub = arrayOf(SELECT_SUBCATEGORY, "Artwork", "Bath", "Bedding", "Furniture", "Home Decor", "Kitchen & Dinning", "Lighting", "Patio, Lawn & Garden", "Storage & Organization", "Cleaning Supplies")
    val artwroksub = arrayOf(SELECT_SPECIFICCATEGORY, "Drawings", "Mixed Media", "Paintings", "Photographs", "Posters", "Prints", "Wall Stickers")
    val bathsub = arrayOf(SELECT_SPECIFICCATEGORY, "Bath Linen", "Bathroom Accessories", "Bathroom Fixtures")
    val beddingsub = arrayOf(SELECT_SPECIFICCATEGORY, "Bedspreads & Coverlets", "Blankets & Quilts", "Duvets & Duvet Covers", "Nursery Bedding", "Bed Pillows", "Sheets & Pillowcase")
    val furnituresub = arrayOf(SELECT_SPECIFICCATEGORY, "Bedroom Furniture", "Children's Furniture", "Dining Room Furniture", "Hallway Furniture", "Home Bar Furniture", "Home Entertainment Furniture", "Home Office Furniture", "Kitchen Furniture", "Living Room Furniture", "Nursery Furniture")
    val homedecorsub = arrayOf(SELECT_SPECIFICCATEGORY, "Artificial Flora", "Candles & Holders", "Carpets & Rugs", "Children's Room Decor", "clocks", "Curtains, Blinds & Shutters", "Decorative Accessories", "Doormats", "Doorstops", "Gift Baskets", "Guestbooks", "Home Fragrance", "Lamps", "Magnets", "Memo Boards", "Mirrors", "Photo Albums", "Photo Frames", "Signs & Plaques", "Slipcovers", "Tapestries", "Terrariums", "Throw Pillows", "Vases")
    val kitchendiningsub = arrayOf(SELECT_SPECIFICCATEGORY, "Bakeware", "Containers & Storage", "Cookware", "Kitchen Linen", "Kitchen Utensils & Tools", "Tableware")
    val homelightingsub = arrayOf(SELECT_SPECIFICCATEGORY, "Ceiling Lighting", "Lamps", "Lightswitch Plates", "Outdoor Lighting", "Wall Lighting Fixtures")
    val lawngardensub = arrayOf(SELECT_SPECIFICCATEGORY, "Backyard Birding", "Outdoor Decor")
    val storageorganizationsub = arrayOf(SELECT_SPECIFICCATEGORY, "Baskets & Bins", "Bathroom Storage & Organization", "Beauty & Personal Care Storage & Organization", "Clothing & Wardrobe Storage", "Home Stroage Hooks", "Jewelry Boxes & Organizers", "Kitchen Storage & Organization", "Office Storage & Organization", "Racks & Shelves", "Ring Bearer Pillows", "Storage Bags", "Wedding Dress Hangers")
    val cleanningsupsub = arrayOf(SELECT_SPECIFICCATEGORY, "Cleaning Tools", "Dishwashing", "Laundry")
    val weddingsub = arrayOf(SELECT_SUBCATEGORY, "Fashion Accessories", "Gifts & Keepsakes", "Handbags & Pocket Accessories", "Jewelry & Jewelry Accessories", "Stationery & Party Supplies", "Wedding Decor")
    val fashionaccessoriessub = arrayOf(SELECT_SPECIFICCATEGORY, "Garters", "Gloves", "Hair Accessories", "Hats & Veils", "Scarves, Shawls & Sashes", "Ties", "Wedding Dress Belts")
    val giftskeepsakessub = arrayOf(SELECT_SPECIFICCATEGORY, "Bar Accessories", "Cups & Mugs", "Cutting Boards", "Gift Baskets", "Gift Tags", "Glassware", "Guestbooks", "Photo Albums", "Photo Frames", "Posters & Prints", "Wedding Dress Hangers")
    val weddingaccessoriessub = arrayOf(SELECT_SPECIFICCATEGORY, "Clutches", "Totes", "Wristlets", "Coin Purses & Pouches", "Keychains & Keyrings", "Money Clips", "Pocket Squares")
    val weddingjewelrysub = arrayOf(SELECT_SPECIFICCATEGORY, "Anklets", "Bracelets", "Brooches & Pins", "Charms", "Cufflinks & Shirt Accessories", "Earrings", "Jewelry Sets", "Necklaces", "Ring Bearer Pillows", "Ring Boxes", "Wedding & Engagement Rings")
    val weddingstationerysub = arrayOf(SELECT_SPECIFICCATEGORY, "Party Supplies", "Stationery")
    val weddingdecorsub = arrayOf(SELECT_SPECIFICCATEGORY, "Aisle Runners", "Artificial Flora", "Candles & Holders", "Centerpieces", "Confetti", "Decorative Boxes", "Dining & Tableware", "Garlands & Banners", "Jars", "Place Cards", "Ring Bearer Pillows", "Ring Boxes", "Signs", "Table Numbers", "Vases")
    val csasub = arrayOf(SELECT_SUBCATEGORY, "Women", "Men", "Girls", "Boys", "baby", "Hair Accessories", "Handbags & Shoulder Bags", "Insoles & Shoe Accessories", "Luggage & Travel Gear")
    val womenandmensub = arrayOf(SELECT_SPECIFICCATEGORY, "Clothing", "Shoes", "Accessories", "Watches")
    val girlboysub = arrayOf(SELECT_SPECIFICCATEGORY, "Clothing", "Shoes", "Accessories")
    val babygendersub = arrayOf(SELECT_SPECIFICCATEGORY, "Baby Girls", "Baby Boys")
    val hairaccessoriessub = arrayOf(SELECT_SPECIFICCATEGORY, "Bun Shapers", "Clips & Barrettes", "Hair Extensions & Wigs", "Hair Pins", "Hair Ties & Elastics", "Headbands", "Side Combs", "Tiaras")
    val csabags = arrayOf(SELECT_SPECIFICCATEGORY, "Backpack Handbags", "Clutches", "Cross-Body Bags", "Satchels", "Totes", "Wristlets")
    val luggagesub = arrayOf(SELECT_SPECIFICCATEGORY, "Bags", "Luggage", "Travel Accessories", "Wallets, Identification & Bag", "Accessories")
    val handbagssub = arrayOf(SELECT_SUBCATEGORY, "Backpack Handbags", "Clutches", "Cross-Body Bags", "Satchels", "Totes", "Wristlets")
    val groomingsub = arrayOf(SELECT_SUBCATEGORY, "Baby & Child Care", "Fragrance", "Hair Care", "Makeup", "Personal Care", "Shaving & Hair Removal", "Skin Care", "Tools & Accessories", "Wellness & Relaxation")
    val childcaresub = arrayOf(SELECT_SPECIFICCATEGORY, "Bath", "Diaper Care")
    val fragrancesub = arrayOf(SELECT_SPECIFICCATEGORY, "Men", "Women")
    val haircaresub = arrayOf(SELECT_SPECIFICCATEGORY, "Hair & Scalp Care", "Hair Accessories", "Shampoo", "Styling Products")
    val makeupsub = arrayOf(SELECT_SPECIFICCATEGORY, "Eyes", "Face", "Lips", "Nails")
    val personalcaresub = arrayOf(SELECT_SPECIFICCATEGORY, "Bath & Bathing Accessories", "Deodorants & Antiperspirants")
    val hairremovalsub = arrayOf(SELECT_SPECIFICCATEGORY, "Aftershaves", "Beard & Mustache Care", "Razors & Blades", "Shaving & Grooming Sets", "Shaving Accessories", "Shaving Creams, Lotions & Gels")
    val skincaresub = arrayOf(SELECT_SPECIFICCATEGORY, "Body", "Eyes", "Face", "Feet, Hands & Nails", "Lip Care", "Sets & Kits")
    val beautytoolssb = arrayOf(SELECT_SPECIFICCATEGORY, "Bathing Accessories", "Feet, Hand & Nail Tools", "Mirrors & Magnifiers", "Shaving & Hair Removal Tools", "Storage & Organization")
    val stationerypartysub = arrayOf(SELECT_SUBCATEGORY, "Party Supplies", "Pens & Pencils", "Stationery")
    val partysuppliessub = arrayOf(SELECT_SPECIFICCATEGORY, "Aisle Runners", "Centerpieces", "Ceremony Programs", "Confetti", "Decorations", "Garlands & Decorative Banners", "Menu Cards", "Party Favors", "Party Hats & Masks", "Place Cards", "Table Numbers")
    val stationerysub = arrayOf(SELECT_SPECIFICCATEGORY, "Appointment Books & Planners", "Gift Tags", "Gift Wrapping Paper", "Invitations", "Labels", "Notecards & Greeting Cards", "Paper", "Paper Stationery", "Photo Albums", "Stamps", "Stickers", "Tank You Cards", "Wall Calendars")
    val toysgamessub = arrayOf(SELECT_SUBCATEGORY, "Baby & Toddler Toys", "Dolls, Toy Figures & Accessories", "Lawn & Playground", "Learning & Education", "Musical Toy Instruments", "Novelty & Gag Toys", "Plushies & Stuffed Animals", "Pretend Play", "Puppets", "Puzzles")
    val noveltygagtoysub = arrayOf(SELECT_SPECIFICCATEGORY, "Magnets & Magnetic Toys", "Money Banks", "Temporary Tattoos")
    val petsuppliessub = arrayOf(SELECT_SUBCATEGORY, "Dogs", "Cats")
    val dogsub = arrayOf(SELECT_SPECIFICCATEGORY, "Apparel & Accessories", "Beds & Furniture", "Collars, Harnesses & Leashes", "Feeding & Watering Supplies", "Litter & Housebreaking", "Storage & Organization", "Toys", "Treats", "Memorials & Ums")
    val catsub = arrayOf(SELECT_SPECIFICCATEGORY, "Apparel & Accessories", "Beds & Furniture", "Collars, Harnesses & Leashes", "Feeding & Watering Supplies", "Toys")
    val sportsgoodssub = arrayOf(SELECT_SUBCATEGORY, "Camping & Hiking", "Car & Vehicle Accessories", "Cycling", "Fishing", "Hunting & Shooting", "Sports & Fitness")
    val campingsub = arrayOf(SELECT_SPECIFICCATEGORY, "Axes", "Fire Starters", "Flasks", "Knives", "Lighters", "Survival Bracelets", "Walking Sticks")
    val vehicleaccessoriessub = arrayOf(SELECT_SPECIFICCATEGORY, "Decals", "Door Straps & Grab Handles", "Gold Cart Seats & Covers", "Hitch Covers", "Plates", "Tire Covers")
    val huntingsub = arrayOf(SELECT_SPECIFICCATEGORY, "Archery", "Gun Accessories", "Knives & Accessories")
    val fitnesssub = arrayOf(SELECT_SPECIFICCATEGORY, "Accessories", "Golf", "Skateboarding", "Yoga")
    val babysub = arrayOf(SELECT_SUBCATEGORY, "Baby & Toddler Toys", "Baby Clothing, Shoes & Accessories", "Child Carriers", "Diaper Changing", "Nursery Bedding", "Nursery Decor", "Nursery Furniture", "Nursing & Feeding", "Pacifiers & Teethers")
    val diapersuppliessub = arrayOf(SELECT_SPECIFICCATEGORY, "Changing Pads & Covers", "Diaper Bags", "Diapers", "Pails, Liners & Bags")
    val nurserybeddingsub = arrayOf(SELECT_SPECIFICCATEGORY, "Back & Body Supports", "Blankets & Swaddling", "Crib Bedding", "Pillow Protectors", "Pillowcases", "Pillows", "Playard Bedding", "Quilts & Bed Covers")
    val nurserydecorsub = arrayOf(SELECT_SPECIFICCATEGORY, "Clocks", "Framed Pictures", "Height Charts", "Light Switch Decorations", "Mobiles", "Night Lights & Night Light", "Covers", "Wall Letters & Numbers", "Wall Stickers")
    val nursingfeedingsub = arrayOf(SELECT_SPECIFICCATEGORY, "Bibs & Burp Cloths", "Drinkware")
    val pacifierssub = arrayOf(SELECT_SPECIFICCATEGORY, "Pacifier Accessories", "Pacifiers", "Teethers")

    var product : Product = Product(0.0, "0", "placeholder", "placeholder", arrayOf("undefined", "undefined", "undefined", "undefined", "undefined", "undefined"), "undefined", "undefined", "undefined", "undefined", "undefined", "undefined", "placeholder", "placeholder", "placeholder", "placeholder", "Placeholder", 0,  SYNC_NEW, 0)
    private var editMode : Boolean = false
    private lateinit var subMap : Map<String, Array<String>>
    private lateinit var specificMap : Map<String, Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item_category)

        //val artisan = intent.extras?.getSerializable("selectedArtisan") as Artisan
        initDataMap()

        setSupportActionBar(addItem_toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // Create an ArrayAdapter
        val mainArrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, mainCategoryList)
        // Set layout to use when the list of choices appear
        mainArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Set Adapter to Spinner
        itemCategory_mainSpinner.adapter = mainArrayAdapter

        itemCategory_mainSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val spinnerValue = itemCategory_mainSpinner.getSelectedItem().toString()
                mainCategory = spinnerValue
                Log.i("AddItemCategory.kt", "2")
                when (spinnerValue) {
                    SELECT_CATEGORY -> setSpinner(itemCategory_subSpinner, arrayOf(SELECT_SUBCATEGORY), editMode)
                    else -> setSpinner(itemCategory_subSpinner, subMap.getOrElse(spinnerValue, {arrayOf(SELECT_SUBCATEGORY)}), editMode, 0)
                }
            }
        }

        itemCategory_subSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val spinnerValue = itemCategory_subSpinner.getSelectedItem().toString()
                subCategory = spinnerValue
                Log.i("AddItemCategory.kt", "3")
                when (spinnerValue) {
                    "Anklets", "Body Jewelry", "Brooches & Pins", "Charms", "Jewelry Sets",
                        "Pendants & Coins", "Wedding & Engagement", "Insoles & Shoe Accessories",
                        "Backpack Handbags", "Clutches", "Cross-Body Bags", "Satchels", "Totes",
                        "Wristlets", "Wellness & Relaxation", "Pens & Pencils", "Baby & Toddler Toys",
                        "Dolls, Toy Figures & Accessories", "Lawn & Playground", "Learning & Education",
                        "Musical Toy Instruments", "Plushies & Stuffed Animals", "Pretend Play",
                        "Puppets", "Puzzles", "Cycling", "Fishing", "Child Carriers", "Nursery Furniture" -> setSpinner(itemCategory_specificSpinner, nosub, editMode, 1)
                    SELECT_SUBCATEGORY -> setSpinner(itemCategory_specificSpinner, arrayOf(SELECT_SPECIFICCATEGORY), editMode)
                    else -> setSpinner(itemCategory_specificSpinner, specificMap.getOrElse(spinnerValue, {arrayOf(SELECT_SPECIFICCATEGORY)}), editMode, 1)
                }
            }
        }

        itemCategory_specificSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                specificCategory = itemCategory_specificSpinner.selectedItem.toString()
            }
        }

        addItemCategory_continueButton.setOnClickListener {
            addItemCategoryContinue(mainCategory, subCategory, specificCategory)
        }

        if (intent.hasExtra("product")) {
            //edit product

            Log.i("AddItemCategory.kt", "1")

            product = intent.extras?.getSerializable("product") as Product
            Log.i("AddItemCategory.kt", "edit item product")
            editMode = true
            itemCategory_mainSpinner.setSelection(mainArrayAdapter.getPosition(product.category))

            val sub = subMap.get(product.category)!!
            setSpinner(itemCategory_subSpinner, sub, editMode, 0)

            val spc = specificMap.get(product.subCategory)!!
            setSpinner(itemCategory_specificSpinner, spc, editMode, 1)

        } else {
            //creating new product set artisanID
            product.artisanId = App.currentArtisan.artisanId
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun editPosition(spinner: Spinner, value : String) : Int {
        Log.d("edit position", "call to edit position" )

        for (i in 1..spinner.count) {
            Log.d("edit position", "inside for" + spinner.count)

            if (spinner.getItemAtPosition(i).toString() == value) {
                Log.d("edit position", i.toString())
                return i
            }
        }
        return -1
    }

    //spinner factory
    private fun setSpinner(spinnersub : Spinner, values: Array<String>, editMode : Boolean, spc : Int = -1) {
        val spAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, values)
        spAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) // Specify the layout to use when the list of choices appears
        spinnersub.adapter = spAdapter

        if (editMode && spc == 0) {
            Log.d("AddItemCategory.kt:", "sub editMode: $editMode, index: ${spAdapter.getPosition(product.subCategory)}")
            spinnersub.setSelection(spAdapter.getPosition(product.subCategory))
        } else if (editMode && spc == 1){
            Log.d("AddItemCategory.kt:", "spc editMode: $editMode, index: ${spAdapter.getPosition(product.subCategory)}")
            spinnersub.setSelection(spAdapter.getPosition(product.specificCategory))
        }
    }

    private fun addItemCategoryContinue(main : String, sub : String, specific : String) {
        if (main != SELECT_CATEGORY && sub != SELECT_SUBCATEGORY && specific != SELECT_SPECIFICCATEGORY){
            Log.i("AddItemCategory", "Main: " + main + " Sub: " + sub + " Specific: " + specific)
            val intent = Intent(this, AddItemInfo::class.java)
            //Create and set product detail
            product.category = main
            product.subCategory = sub
            product.specificCategory = specific
            product.artisanId = App.currentArtisan.artisanId
            intent.putExtra("product", product)
            intent.putExtra("editMode", editMode)
            //intent.putExtra("selectedArtisan", artisan)
            startActivity(intent)
            //finish()
        } else {
            // do nothing not all categories are set correctly
            //TODO warning message?
        }
    }

    private fun initDataMap(){
        subMap = mapOf("Jewelry" to jewelrysub,
                        "Home & Kitchen" to homeKitchensub,
                        "Clothing, Shoes & Accessories" to csasub,
                        "Wedding" to weddingsub,
                        "Handbags & Totes" to handbagssub,
                        "Beauty & Grooming" to groomingsub,
                        "Stationery & Party Supplies" to stationerypartysub,
                        "Toys & Games" to toysgamessub,
                        "Pet Supplies" to petsuppliessub,
                        "Sports & Outdoors" to sportsgoodssub,
                        "Baby" to babysub)

        specificMap = mapOf("Accessories" to accessoriessub,
                            "Bracelets" to braceletssub,
                            "Cufflings & Shirt Accessories" to cufflingssub,
                            "Earrings" to earringssub,
                            "Hair Jewelry" to hairjewelrysub,
                            "Necklaces" to necklacessub,
                            "Rings" to ringssub,
                            "Artwork" to artwroksub,
                            "Bath" to bathsub,
                            "Bedding" to beddingsub,
                            "Furniture" to furnituresub,
                            "Home Decor" to homedecorsub,
                            "Kitchen & Dinning" to kitchendiningsub,
                            "Lighting" to homelightingsub,
                            "Patio, Lawn & Garden" to lawngardensub,
                            "Storage & Organization" to storageorganizationsub,
                            "Cleaning Supplies" to cleanningsupsub,
                            "Fashion Accessories" to fashionaccessoriessub,
                            "Gifts & Keepsakes" to giftskeepsakessub,
                            "Handbags & Pocket Accessories" to weddingaccessoriessub,
                            "Jewelry & Jewelry Accessories" to weddingjewelrysub,
                            "Stationery & Party Supplies" to weddingstationerysub,
                            "Wedding Decor" to weddingdecorsub,
                            "Women" to womenandmensub,
                            "Men" to womenandmensub,
                            "Girls" to girlboysub,
                            "Boys" to girlboysub,
                            "Baby" to babygendersub,
                            "Hair Accessories" to hairaccessoriessub,
                            "Handbags & Shoulder Bags" to csabags,
                            "Luggage & Travel Gear" to luggagesub,
                            "Baby & Child Care" to childcaresub,
                            "Fragrance" to fragrancesub,
                            "Hair Care" to haircaresub,
                            "Makeup" to makeupsub,
                            "Personal Care" to personalcaresub,
                            "Shaving & Hair Removal" to hairremovalsub,
                            "Skin Care" to skincaresub,
                            "Tools & Accessories" to beautytoolssb,
                            "Party Supplies" to partysuppliessub,
                            "Stationery" to stationerysub,
                            "Novelty & Gag Toys" to noveltygagtoysub,
                            "Dogs" to dogsub,
                            "Cats" to catsub,
                            "Camping & Hiking" to campingsub,
                            "Car & Vehicle Accessories" to vehicleaccessoriessub,
                            "Hunting & Shooting" to huntingsub,
                            "Sports & Fitness" to fitnesssub,
                            "Diaper Changing" to diapersuppliessub,
                            "Nursery Bedding" to nurserybeddingsub,
                            "Nursery Decor" to nurserydecorsub,
                            "Nursing & Feeding" to nursingfeedingsub,
                            "Pacifiers & Teethers" to pacifierssub)
    }

}
