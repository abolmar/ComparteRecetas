package com.alejandro.comparterecetas

import android.annotation.SuppressLint
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import com.alejandro.comparterecetas.adapters.ShowImagesAdapter
import com.alejandro.comparterecetas.adapters.ShowIngredientsAdapter
import com.alejandro.comparterecetas.database.DataBaseHandler
import com.alejandro.comparterecetas.models.FavoritesModel
import com.alejandro.comparterecetas.models.ImagesModel
import com.alejandro.comparterecetas.models.IngredientsModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_show_recipe.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ShowRecipeActivity : AppCompatActivity() {

    private var dbHandler: DataBaseHandler? = null
    private var dbFirebase = FirebaseFirestore.getInstance()
    private var ingredientsFirebase = dbFirebase.collection("ingredients")
    private var imagesFirebase = dbFirebase.collection("images")
    private val date: String = SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault()).format(Date())
    private val favorite = FavoritesModel()

    private var recipeName: String? = ""
    private var recipeCategory: String? = ""
    private var recipeHour: Int? = 0
    private var recipeMinute: Int? = 0
    private var recipePeople: Int? = 0
    private var recipePreparation: String? = ""
    private var recipeId: String? = ""

    private var toFragment: String? = ""
    private var toCategory: String? = ""

    private var position: Int? = 0

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_recipe)

        //init db
        dbHandler = DataBaseHandler(this)

        val extras = intent.extras

        if (extras != null) {
            recipeName = extras.getString("recipeName")
            recipeCategory = extras.getString("recipeCategory")
            recipeHour = extras.getInt("recipeHour")
            recipeMinute = extras.getInt("recipeMinute")
            recipePeople = extras.getInt("recipePeople")
            recipePreparation = extras.getString("recipePreparation")
            recipeId = extras.getString("recipeId")

            toFragment = extras.getString("fromFragment") // A qué fragment debe volver
            toCategory = extras.getString("fromCategory") // Si el fragment es "Favoritas", a qué categoría debe volver

            position = extras.getInt("position")
        }

        tv_title.text = recipeName
        tv_category_name.text = recipeCategory
        tv_hour.text = "$recipeHour h"
        tv_minute.text = "$recipeMinute m"
        tv_people.text = "$recipePeople"
        tv_preparation_content.text = recipePreparation

        ingredientsFirebase.whereEqualTo("idRecipe", recipeId).get().addOnSuccessListener {
            val ingredientsRecipeFirebase = it.toObjects(IngredientsModel::class.java)

            rv_ingredients.layoutManager = LinearLayoutManager(this)
            rv_ingredients.layoutManager = GridLayoutManager(this, 1)
            rv_ingredients.adapter = ShowIngredientsAdapter(ingredientsRecipeFirebase, this)
        }


        if (toFragment != "profile"){
            imagesFirebase.whereEqualTo("recipeId", recipeId).get().addOnSuccessListener {
                val imagesRecipeFirebase = it.toObjects(ImagesModel::class.java)
                showImagesRecipe(imagesRecipeFirebase, "x")
            }
        } else {
            val myRecipeImages: ArrayList<ImagesModel> = dbHandler!!.getAllMyImages(recipeId)
            showImagesRecipe(myRecipeImages, "profile")
        }

        btn_show_recipe_back.setOnClickListener {
            backToView()
        }

        //  Contiene el valor representativo de una receta marcada como favorita(1)
        when (dbHandler!!.getFavoriteRecipe(dbHandler!!.getUserId(), recipeId)) {
            0 -> {
                img_favorite_No.visibility = View.VISIBLE
                img_favorite_Yes.visibility = View.GONE
            }
            1 -> {
                img_favorite_No.visibility = View.GONE
                img_favorite_Yes.visibility = View.VISIBLE
            }
        }

        img_favorite_No.setOnClickListener {
            favorite.id = "favorite-$date"
            favorite.recipeId = recipeId.toString()
            favorite.userId = dbHandler!!.getUserId()
            favorite.type = recipeCategory.toString()
            favorite.favorite = 1
            favorite.date = date

            dbHandler!!.addFavoriteRecipe(favorite)

            img_favorite_Yes.visibility = View.VISIBLE
            img_favorite_No.visibility = View.GONE
        }

        img_favorite_Yes.setOnClickListener {
            dbHandler!!.removeFavoriteRecipe(recipeId)
            img_favorite_No.visibility = View.VISIBLE
            img_favorite_Yes.visibility = View.GONE
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showImagesRecipe(images: MutableList<ImagesModel>, fragment: String){
        rv_images.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rv_images.adapter = ShowImagesAdapter(images, fragment, this)

        val countImages = images.size

        count_images.text = " / $countImages"

        if (countImages > 1){
            var count = 0

            if (count == 0){
                back_image.isEnabled = false
            }

            next_image.setOnClickListener {
                count++
                rv_images.smoothScrollToPosition(count)
                image_position.text = "${count+1}"
                if (count == countImages-1){
                    back_image.isEnabled = true
                    next_image.isEnabled = false
                }else {
                    back_image.isEnabled = true
                    next_image.isEnabled = true
                }
            }

            back_image.setOnClickListener {
                count--
                rv_images.smoothScrollToPosition(count)
                image_position.text = "${count+1}"
                if (count == 0){
                    back_image.isEnabled = false
                    next_image.isEnabled = true
                } else {
                    back_image.isEnabled = true
                    next_image.isEnabled = true
                }
            }
        } else {
            next_image.visibility = View.INVISIBLE
            back_image.visibility = View.INVISIBLE
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        backToView()
    }

    private fun backToView() {
        when (toFragment) {
            "recipes" -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra("position", position)
                startActivity(intent)
            }
            "profile" -> {
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.putExtra("USER_PROFILE", true)
                startActivity(intent)
            }
            else -> when (toCategory) {
                "Todas" -> {
                    val intent = Intent(this, FavoriteRecipesActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.putExtra("ALL", "Todas")
                    startActivity(intent)
                }
                "Comida" -> {
                    val intent = Intent(this, FavoriteRecipesActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.putExtra("LUNCH", "Comida")
                    startActivity(intent)
                }
                "Cena" -> {
                    val intent = Intent(this, FavoriteRecipesActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.putExtra("DINNER", "Cena")
                    startActivity(intent)
                }
                "Merienda" -> {
                    val intent = Intent(this, FavoriteRecipesActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.putExtra("AFTERNOONSNACK", "Merienda")
                    startActivity(intent)
                }
                "Postre" -> {
                    val intent = Intent(this, FavoriteRecipesActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.putExtra("DESSERT", "Postre")
                    startActivity(intent)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
    }
}
