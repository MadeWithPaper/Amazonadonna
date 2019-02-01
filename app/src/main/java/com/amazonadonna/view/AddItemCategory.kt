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
    val mainCategoryList = arrayOf("Jewelry", "Home & Kitchen", "Clothing, Shoes & Accessories", "Wedding")
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
    val csasub = arrayOf("Women", "Men", "Girls", "Boys", "baby", "Hair Accessories", "Handbags & Shoulder Bags", "Insoles & Shoe Accessories", "Luggage & Travel Gear")
    //TODO sub of csa
    val weddingsub = arrayOf("Fashion Accessories", "Gifts & Keepsakes", "Handbags & Pocket Accessories", "Jewelry & Jewelry Accessories", "Stationery & Party Supplies", "Wedding Decor")

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
                } else {
                    placeholderArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    itemCategory_subSpinner.adapter = placeholderArrayAdapter
                }
            }

        }


    }
}
