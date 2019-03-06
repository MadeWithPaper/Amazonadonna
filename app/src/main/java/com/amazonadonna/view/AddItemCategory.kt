package com.amazonadonna.view

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.amazonadonna.model.Artisan
import kotlinx.android.synthetic.main.activity_add_item_category.*
import android.widget.Spinner
import com.amazonadonna.model.Product
import com.amazonadonna.sync.Synchronizer.Companion.SYNC_NEW


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

    var product : Product = Product(0.0, "0", "placeholder", "placeholder", arrayOf("testingurl", "Not set"), "undefined", "undefined", "undefined", "undefined", "undefined", "undefined", "placeholder", "placeholder", "placeholder", "placeholder", "Placeholder", 0,  SYNC_NEW, 0)
    private var editMode : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item_category)

        val artisan = intent.extras?.getSerializable("selectedArtisan") as Artisan

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
                when (spinnerValue) {
                    "Jewelry" -> setSpinner(itemCategory_subSpinner, jewelrysub)
                    "Home & Kitchen" -> setSpinner(itemCategory_subSpinner, homeKitchensub)
                    "Clothing, Shoes & Accessories" -> setSpinner(itemCategory_subSpinner, csasub)
                    "Wedding" -> setSpinner(itemCategory_subSpinner, weddingsub)
                    "Handbags & Totes" -> setSpinner(itemCategory_subSpinner, handbagssub)
                    "Beauty & Grooming" -> setSpinner(itemCategory_subSpinner, groomingsub)
                    "Stationery & Party Supplies" -> setSpinner(itemCategory_subSpinner, stationerypartysub)
                    "Toys & Games" -> setSpinner(itemCategory_subSpinner, toysgamessub)
                    "Pet Supplies" -> setSpinner(itemCategory_subSpinner, petsuppliessub)
                    "Sports & Outdoors" -> setSpinner(itemCategory_subSpinner, sportsgoodssub)
                    "Baby" -> setSpinner(itemCategory_subSpinner, babysub)
                    SELECT_CATEGORY -> setSpinner(itemCategory_subSpinner, arrayOf(SELECT_SUBCATEGORY))
                }
            }
        }

        itemCategory_subSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val spinnerValue = itemCategory_subSpinner.getSelectedItem().toString()
                subCategory = spinnerValue
                when (spinnerValue) {
                    "Accessories" -> setSpinner(itemCategory_specificSpinner, accessoriessub)
                    "Anklets", "Body Jewelry", "Brooches & Pins", "Charms", "Jewelry Sets",
                        "Pendants & Coins", "Wedding & Engagement", "Insoles & Shoe Accessories",
                        "Backpack Handbags", "Clutches", "Cross-Body Bags", "Satchels", "Totes",
                        "Wristlets", "Wellness & Relaxation", "Pens & Pencils", "Baby & Toddler Toys",
                        "Dolls, Toy Figures & Accessories", "Lawn & Playground", "Learning & Education",
                        "Musical Toy Instruments", "Plushies & Stuffed Animals", "Pretend Play",
                        "Puppets", "Puzzles", "Cycling", "Fishing", "Child Carriers", "Nursery Furniture" -> setSpinner(itemCategory_specificSpinner, nosub)
                    "Bracelets" -> setSpinner(itemCategory_specificSpinner, braceletssub)
                    "Cufflings & Shirt Accessories" -> setSpinner(itemCategory_specificSpinner, cufflingssub)
                    "Earrings" -> setSpinner(itemCategory_specificSpinner, earringssub)
                    "Hair Jewelry" -> setSpinner(itemCategory_specificSpinner, hairjewelrysub)
                    "Necklaces" -> setSpinner(itemCategory_specificSpinner, necklacessub)
                    "Rings" -> setSpinner(itemCategory_specificSpinner, ringssub)
                    "Artwork" -> setSpinner(itemCategory_specificSpinner, artwroksub)
                    "Bath" -> setSpinner(itemCategory_specificSpinner, bathsub)
                    "Bedding" -> setSpinner(itemCategory_specificSpinner, beddingsub)
                    "Furniture" -> setSpinner(itemCategory_specificSpinner, furnituresub)
                    "Home Decor" -> setSpinner(itemCategory_specificSpinner, homedecorsub)
                    "Kitchen & Dinning" -> setSpinner(itemCategory_specificSpinner, kitchendiningsub)
                    "Lighting" -> setSpinner(itemCategory_specificSpinner, homelightingsub)
                    "Patio, Lawn & Garden" -> setSpinner(itemCategory_specificSpinner, lawngardensub)
                    "Storage & Organization" -> setSpinner(itemCategory_specificSpinner, storageorganizationsub)
                    "Cleaning Supplies" -> setSpinner(itemCategory_specificSpinner, cleanningsupsub)
                    "Fashion Accessories" -> setSpinner(itemCategory_specificSpinner, fashionaccessoriessub)
                    "Gifts & Keepsakes" -> setSpinner(itemCategory_specificSpinner, giftskeepsakessub)
                    "Handbags & Pocket Accessories" -> setSpinner(itemCategory_specificSpinner, weddingaccessoriessub)
                    "Jewelry & Jewelry Accessories" -> setSpinner(itemCategory_specificSpinner, weddingjewelrysub)
                    "Stationery & Party Supplies" -> setSpinner(itemCategory_specificSpinner, weddingstationerysub)
                    "Wedding Decor" -> setSpinner(itemCategory_specificSpinner, weddingdecorsub)
                    "Women", "Men" -> setSpinner(itemCategory_specificSpinner, womenandmensub)
                    "Girls", "Boy" -> setSpinner(itemCategory_specificSpinner, girlboysub)
                    "baby" -> setSpinner(itemCategory_specificSpinner, babygendersub)
                    "Hair Accessories" -> setSpinner(itemCategory_specificSpinner, hairaccessoriessub)
                    "Handbags & Shoulder Bags" -> setSpinner(itemCategory_specificSpinner, csabags)
                    "Luggage & Travel Gear" -> setSpinner(itemCategory_specificSpinner, luggagesub)
                    "Baby & Child Care" -> setSpinner(itemCategory_specificSpinner, childcaresub)
                    "Fragrance" -> setSpinner(itemCategory_specificSpinner, fragrancesub)
                    "Hair Care" -> setSpinner(itemCategory_specificSpinner, haircaresub)
                    "Makeup" -> setSpinner(itemCategory_specificSpinner, makeupsub)
                    "Personal Care" -> setSpinner(itemCategory_specificSpinner, personalcaresub)
                    "Shaving & Hair Removal" -> setSpinner(itemCategory_specificSpinner, hairremovalsub)
                    "Skin Care" -> setSpinner(itemCategory_specificSpinner, skincaresub)
                    "Tools & Accessories" -> setSpinner(itemCategory_specificSpinner, beautytoolssb)
                    "Party Supplies" -> setSpinner(itemCategory_specificSpinner, partysuppliessub)
                    "Stationery" -> setSpinner(itemCategory_specificSpinner, stationerysub)
                    "Novelty & Gag Toys" -> setSpinner(itemCategory_specificSpinner, noveltygagtoysub)
                    "Dogs" -> setSpinner(itemCategory_specificSpinner, dogsub)
                    "Cats" -> setSpinner(itemCategory_specificSpinner, catsub)
                    "Camping & Hiking" -> setSpinner(itemCategory_specificSpinner, campingsub)
                    "Car & Vehicle Accessories" -> setSpinner(itemCategory_specificSpinner, vehicleaccessoriessub)
                    "Hunting & Shooting" -> setSpinner(itemCategory_specificSpinner, huntingsub)
                    "Sports & Fitness" -> setSpinner(itemCategory_specificSpinner, fitnesssub)
                    "Diaper Changing" -> setSpinner(itemCategory_specificSpinner, diapersuppliessub)
                    "Nursery Bedding" -> setSpinner(itemCategory_specificSpinner, nurserybeddingsub)
                    "Nursery Decor" -> setSpinner(itemCategory_specificSpinner, nurserydecorsub)
                    "Nursing & Feeding" -> setSpinner(itemCategory_specificSpinner, nursingfeedingsub)
                    "Pacifiers & Teethers" -> setSpinner(itemCategory_specificSpinner, pacifierssub)
                    SELECT_SUBCATEGORY -> setSpinner(itemCategory_specificSpinner, arrayOf(SELECT_SPECIFICCATEGORY))
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
            addItemCategoryContinue(mainCategory, subCategory, specificCategory, artisan)
        }

        if (intent.hasExtra("product")) {
            //edit product
            product = intent.extras?.getSerializable("product") as Product
            Log.i("Edit Item", product.toString())
            editMode = true
            //Log.i("AddItemCategoryEdit", mainArrayAdapter.getPosition(product.category).toString())
            itemCategory_mainSpinner.setSelection(mainArrayAdapter.getPosition(product.category))
//
//            val subInt = editPosition(itemCategory_subSpinner, product.subCategory)
//
//            itemCategory_subSpinner.setSelection(subInt)
//
//
//            itemCategory_specificSpinner.setSelection(editPosition(itemCategory_specificSpinner, product.specificCategory))
        } else {
            //creating new product set artisanID
            product.artisanId = artisan.artisanId
        }

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
    private fun setSpinner(spinnersub : Spinner, values: Array<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, values)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) // Specify the layout to use when the list of choices appears
        spinnersub.adapter = adapter
    }

    private fun addItemCategoryContinue(main : String, sub : String, specific : String, artisan: Artisan) {
        if (main != SELECT_CATEGORY && sub != SELECT_SUBCATEGORY && specific != SELECT_SPECIFICCATEGORY){
            Log.i("AddItemCategory", "Main: " + main + " Sub: " + sub + " Specific: " + specific)
            val intent = Intent(this, AddItemInfo::class.java)
            //Create and set product detail
            product.category = main
            product.subCategory = sub
            product.specificCategory = specific
            product.artisanId = artisan.artisanId
            intent.putExtra("product", product)
            intent.putExtra("editMode", editMode)
            intent.putExtra("selectedArtisan", artisan)
            startActivity(intent)
            finish()
        } else {
            // do nothing not all categories are set correctly
            //TODO warning message?
        }
    }

}
