package com.amazonadonna.view

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.amazonadonna.model.Artisan
import kotlinx.android.synthetic.main.activity_add_item_category.*
import java.util.*

class AddItemCategory : AppCompatActivity() {

    //array of values for each adapter
    val mainCategoryList = arrayOf("Jewelry", "Home & Kitchen", "Clothing, Shoes & Accessories", "Wedding", "Handbags & Totes", "Beauty & Grooming", "Stationery & Party Supplies", "Toys & Games", "Pet Supplies", "Sports & Outdoors", "Baby")
    val jewelrysub = arrayOf("Accessories", "Anklets", "Body Jewelry", "Bracelets", "Brooches & Pins", "Charms", "Cufflings & Shirt Accessories", "Earrings", "Hair Jewelry", "Jewelry Sets", "Necklaces", "Pendants & Coins", "Rings", "Wedding & Engagement")
    val accessoriessub = arrayOf("Jewelry Boxes & Organizers", "Keychains & Keyrings")
    val nosub = arrayOf("")
    val necklacessub = arrayOf("Chains", "Chokers", "Lariats", "Pendants", "Strands")
    val earringssub = arrayOf("Chandelier Earrings","Drop Earrings", "Ear Cuffs", "Hoop Earrings", "Stud Earrings")
    val braceletssub = arrayOf("Bangles", "Charm Bracelets", "Cuffs", "Link Bracelets", "Stretch Bracelets", "Wrap Bracelets")
    val ringssub = arrayOf("Bands", "Stacking", "Statement")
    val hairjewelrysub = arrayOf("Clips & Barrettes", "Headbands", "Pins", "Side Combs", "Tiaras")
    val cufflingssub = arrayOf("Cufflinks", "Tie Clips", "Tie Pins")
    val homeKitchensub = arrayOf("Artwork", "Bath", "Bedding", "Furniture", "Home Decor", "Kitchen & Dinning", "Lighting", "Patio, Lawn & Garden", "Storage & Organization", "Cleaning Supplies")
    val artwroksub = arrayOf("Drawings", "Mixed Media", "Paintings", "Photographs", "Posters", "Prints", "Wall Stickers")
    val bathsub = arrayOf("Bath Linen", "Bathroom Accessories", "Bathroom Fixtures")
    val beddingsub = arrayOf("Bedspreads & Coverlets", "Blankets & Quilts", "Duvets & Duvet Covers", "Nursery Bedding", "Bed Pillows", "Sheets & Pillowcase")
    val furnituresub = arrayOf("Bedroom Furniture", "Children's Furniture", "Dining Room Furniture", "Hallway Furniture", "Home Bar Furniture", "Home Entertainment Furniture", "Home Office Furniture", "Kitchen Furniture", "Living Room Furniture", "Nursery Furniture")
    val homedecorsub = arrayOf("Artificial Flora", "Candles & Holders", "Carpets & Rugs", "Children's Room Decor", "clocks", "Curtains, Blinds & Shutters", "Decorative Accessories", "Doormats", "Doorstops", "Gift Baskets", "Guestbooks", "Home Fragrance", "Lamps", "Magnets", "Memo Boards", "Mirrors", "Photo Albums", "Photo Frames", "Signs & Plaques", "Slipcovers", "Tapestries", "Terrariums", "Throw Pillows", "Vases")
    val kitchendiningsub = arrayOf("Bakeware", "Containers & Storage", "Cookware", "Kitchen Linen", "Kitchen Utensils & Tools", "Tableware")
    val homelightingsub = arrayOf("Ceiling Lighting", "Lamps", "Lightswitch Plates", "Outdoor Lighting", "Wall Lighting Fixtures")
    val lawngardensub = arrayOf("Backyard Birding", "Outdoor Decor")
    val storageorganizationsub = arrayOf("Baskets & Bins", "Bathroom Storage & Organization", "Beauty & Personal Care Storage & Organization", "Clothing & Wardrobe Storage", "Home Stroage Hooks", "Jewelry Boxes & Organizers", "Kitchen Storage & Organization", "Office Storage & Organization", "Racks & Shelves", "Ring Bearer Pillows", "Storage Bags", "Wedding Dress Hangers")
    val cleanningsupsub = arrayOf("Cleaning Tools", "Dishwashing", "Laundry")
    val weddingsub = arrayOf("Fashion Accessories", "Gifts & Keepsakes", "Handbags & Pocket Accessories", "Jewelry & Jewelry Accessories", "Stationery & Party Supplies", "Wedding Decor")
    val fashionaccessoriessub = arrayOf("Garters", "Gloves", "Hair Accessories", "Hats & Veils", "Scarves, Shawls & Sashes", "Ties", "Wedding Dress Belts")
    val giftskeepsakessub = arrayOf("Bar Accessories", "Cups & Mugs", "Cutting Boards", "Gift Baskets", "Gift Tags", "Glassware", "Guestbooks", "Photo Albums", "Photo Frames", "Posters & Prints", "Wedding Dress Hangers")
    val weddingaccessoriessub = arrayOf("Clutches", "Totes", "Wristlets", "Coin Purses & Pouches", "Keychains & Keyrings", "Money Clips", "Pocket Squares")
    val weddingjewelrysub = arrayOf("Anklets", "Bracelets", "Brooches & Pins", "Charms", "Cufflinks & Shirt Accessories", "Earrings", "Jewelry Sets", "Necklaces", "Ring Bearer Pillows", "Ring Boxes", "Wedding & Engagement Rings")
    val weddingstationerysub = arrayOf("Party Supplies", "Stationery")
    val weddingdecorsub = arrayOf("Aisle Runners", "Artificial Flora", "Candles & Holders", "Centerpieces", "Confetti", "Decorative Boxes", "Dining & Tableware", "Garlands & Banners", "Jars", "Place Cards", "Ring Bearer Pillows", "Ring Boxes", "Signs", "Table Numbers", "Vases")
    val csasub = arrayOf("Women", "Men", "Girls", "Boys", "baby", "Hair Accessories", "Handbags & Shoulder Bags", "Insoles & Shoe Accessories", "Luggage & Travel Gear")
    val womenandmensub = arrayOf("Clothing", "Shoes", "Accessories", "Watches")
    val girlboysub = arrayOf("Clothing", "Shoes", "Accessories")
    val babygendersub = arrayOf("Baby Girls", "Baby Boys")
    val hairaccessoriessub = arrayOf("Bun Shapers", "Clips & Barrettes", "Hair Extensions & Wigs", "Hair Pins", "Hair Ties & Elastics", "Headbands", "Side Combs", "Tiaras")
    val csabags = arrayOf("Backpack Handbags", "Clutches", "Cross-Body Bags", "Satchels", "Totes", "Wristlets")
    val luggagesub = arrayOf("Bags", "Luggage", "Travel Accessories", "Wallets, Identification & Bag", "Accessories")
    val handbagssub = arrayOf("Backpack Handbags", "Clutches", "Cross-Body Bags", "Satchels", "Totes", "Wristlets")
    val groomingsub = arrayOf("Baby & Child Care", "Fragrance", "Hair Care", "Makeup", "Personal Care", "Shaving & Hair Removal", "Skin Care", "Tools & Accessories", "Wellness & Relaxation")
    val childcaresub = arrayOf("Bath", "Diaper Care")
    val fragrancesub = arrayOf("Men", "Women")
    val haircaresub = arrayOf("Hair & Scalp Care", "Hair Accessories", "Shampoo", "Styling Products")
    val makeupsub = arrayOf("Eyes", "Face", "Lips", "Nails")
    val personalcaresub = arrayOf("Bath & Bathing Accessories", "Deodorants & Antiperspirants")
    val hairremovalsub = arrayOf("Aftershaves", "Beard & Mustache Care", "Razors & Blades", "Shaving & Grooming Sets", "Shaving Accessories", "Shaving Creams, Lotions & Gels")
    val skincaresub = arrayOf("Body", "Eyes", "Face", "Feet, Hands & Nails", "Lip Care", "Sets & Kits")
    val beautytoolssb = arrayOf("Bathing Accessories", "Feet, Hand & Nail Tools", "Mirrors & Magnifiers", "Shaving & Hair Removal Tools", "Storage & Organization")
    val stationerypartysub = arrayOf("Party Supplies", "Pens & Pencils", "Stationery")
    val partysuppliessub = arrayOf("Aisle Runners", "Centerpieces", "Ceremony Programs", "Confetti", "Decorations", "Garlands & Decorative Banners", "Menu Cards", "Party Favors", "Party Hats & Masks", "Place Cards", "Table Numbers")
    val stationerysub = arrayOf("Appointment Books & Planners", "Gift Tags", "Gift Wrapping Paper", "Invitations", "Labels", "Notecards & Greeting Cards", "Paper", "Paper Stationery", "Photo Albums", "Stamps", "Stickers", "Tank You Cards", "Wall Calendars")
    val toysgamessub = arrayOf("Baby & Toddler Toys", "Dolls, Toy Figures & Accessories", "Lawn & Playground", "Learning & Education", "Musical Toy Instruments", "Novelty & Gag Toys", "Plushies & Stuffed Animals", "Pretend Play", "Puppets", "Puzzles")
    val noveltygagtoysub = arrayOf("Magnets & Magnetic Toys", "Money Banks", "Temporary Tattoos")
    val petsuppliessub = arrayOf("Dogs", "Cats")
    val dogsub = arrayOf("Apparel & Accessories", "Beds & Furniture", "Collars, Harnesses & Leashes", "Feeding & Watering Supplies", "Litter & Housebreaking", "Storage & Organization", "Toys", "Treats", "Memorials & Ums")
    val catsub = arrayOf("Apparel & Accessories", "Beds & Furniture", "Collars, Harnesses & Leashes", "Feeding & Watering Supplies", "Toys")
    val sportsgoodssub = arrayOf("Camping & Hiking", "Car & Vehicle Accessories", "Cycling", "Fishing", "Hunting & Shooting", "Sports & Fitness")
    val campingsub = arrayOf("Axes", "Fire Starters", "Flasks", "Knives", "Lighters", "Survival Bracelets", "Walking Sticks")
    val vehicleaccessoriessub = arrayOf("Decals", "Door Straps & Grab Handles", "Gold Cart Seats & Covers", "Hitch Covers", "Plates", "Tire Covers")
    val huntingsub = arrayOf("Archery", "Gun Accessories", "Knives & Accessories")
    val fitnesssub = arrayOf("Accessories", "Golf", "Skateboarding", "Yoga")
    val babysub = arrayOf("Baby & Toddler Toys", "Baby Clothing, Shoes & Accessories", "Child Carriers", "Diaper Changing", "Nursery Bedding", "Nursery Decor", "Nursery Furniture", "Nursing & Feeding", "Pacifiers & Teethers")
    val diapersuppliessub = arrayOf("Changing Pads & Covers", "Diaper Bags", "Diapers", "Pails, Liners & Bags")
    val nurserybeddingsub = arrayOf("Back & Body Supports", "Blankets & Swaddling", "Crib Bedding", "Pillow Protectors", "Pillowcases", "Pillows", "Playard Bedding", "Quilts & Bed Covers")
    val nurserydecorsub = arrayOf("Clocks", "Framed Pictures", "Height Charts", "Light Switch Decorations", "Mobiles", "Night Lights & Night Light", "Covers", "Wall Letters & Numbers", "Wall Stickers")
    val nursingfeedingsub = arrayOf("Bibs & Burp Cloths", "Drinkware")
    val pacifierssub = arrayOf("Pacifier Accessories", "Pacifiers", "Teethers")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_item_category)
        val artisan = intent.extras?.getSerializable("selectedArtisan") as Artisan

        // Create an ArrayAdapter
        val mainArrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, mainCategoryList)
        val jewelrySubArrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, jewelrysub)
        val homeSubArrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, homeKitchensub)
        val csaSubArrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, csasub)
        val weddingSubArrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, weddingsub)
        val handbagstotesSubArrayAdapter = ArrayAdapter (this, android.R.layout.simple_spinner_item, handbagssub)
        val beautySubArrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, groomingsub)
        val stationerySubArrayAdapter = ArrayAdapter (this, android.R.layout.simple_spinner_item, stationerypartysub)
        val toysgamesSubArrayAdapter = ArrayAdapter (this, android.R.layout.simple_spinner_item, toysgamessub)
        val petsuppliesSubArrayAdapter = ArrayAdapter (this, android.R.layout.simple_spinner_item, petsuppliessub)
        val sportsgoodsSubArrayAdapter = ArrayAdapter (this, android.R.layout.simple_spinner_item, sportsgoodssub)
        val babySubArrayAdapter = ArrayAdapter (this, android.R.layout.simple_spinner_item, babysub)
        val placeholderArrayAdapter = ArrayAdapter (this, android.R.layout.simple_spinner_item, weddingsub)
        // Set layout to use when the list of choices appear
        mainArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Set Adapter to Spinner
        itemCategory_mainSpinner.adapter = mainArrayAdapter

        itemCategory_mainSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val spinnerValue = itemCategory_mainSpinner.getSelectedItem().toString()

                if (spinnerValue.equals("Jewelry")) {
                    jewelrySubArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    itemCategory_subSpinner.adapter = jewelrySubArrayAdapter
                } else if (spinnerValue.equals("Home & Kitchen")){
                    homeSubArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    itemCategory_subSpinner.adapter = homeSubArrayAdapter
                } else if (spinnerValue.equals("Clothing, Shoes & Accessories")) {
                    csaSubArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    itemCategory_subSpinner.adapter = csaSubArrayAdapter
                } else if (spinnerValue.equals("Wedding")) {
                    weddingSubArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    itemCategory_subSpinner.adapter = weddingSubArrayAdapter
                } else if (spinnerValue.equals("Handbags & Totes")) {
                    handbagstotesSubArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    itemCategory_subSpinner.adapter = handbagstotesSubArrayAdapter
                } else if (spinnerValue.equals("Beauty & Grooming")) {
                    beautySubArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    itemCategory_subSpinner.adapter = beautySubArrayAdapter
                } else if (spinnerValue.equals("Stationery & Party Supplies")) {
                    stationerySubArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    itemCategory_subSpinner.adapter = stationerySubArrayAdapter
                } else if (spinnerValue.equals("Toys & Games")) {
                    toysgamesSubArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    itemCategory_subSpinner.adapter = toysgamesSubArrayAdapter
                } else if (spinnerValue.equals("Pet Supplies")) {
                    petsuppliesSubArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    itemCategory_subSpinner.adapter = petsuppliesSubArrayAdapter
                } else if (spinnerValue.equals("Sports & Outdoors")) {
                    sportsgoodsSubArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    itemCategory_subSpinner.adapter = sportsgoodsSubArrayAdapter
                } else if (spinnerValue.equals("Baby")) {
                    babySubArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    itemCategory_subSpinner.adapter = babySubArrayAdapter
                } else {
                    placeholderArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    itemCategory_subSpinner.adapter = placeholderArrayAdapter
                }
            }
        }
    }
}
