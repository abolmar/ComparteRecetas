package com.alejandro.comparterecetas

import android.annotation.SuppressLint
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import com.alejandro.comparterecetas.adapters.ShowImagesAdapter
import com.alejandro.comparterecetas.adapters.ShowIngredientsAdapter
import com.alejandro.comparterecetas.database.DataBaseHandler
import com.alejandro.comparterecetas.models.FavoritesModel
import com.alejandro.comparterecetas.models.ImagesModel
import com.alejandro.comparterecetas.models.IngredientsModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.RequestOptions.circleCropTransform
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_show_recipe.*
import java.io.LineNumberReader
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ShowRecipeActivity : AppCompatActivity() {

    private var dbHandler: DataBaseHandler? = null
    private val date: String = SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault()).format(Date())
    private val favorite = FavoritesModel()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_recipe)

        //init db
        dbHandler = DataBaseHandler(this)

        var recipeName: String? = ""
        var recipeCategory: String? = ""
        var recipeHour: Int? = 0
        var recipeMinute: Int? = 0
        var recipePreparation: String? = ""
        var recipeId: String? = ""

        val extras = intent.extras

        if (extras != null) {
            recipeName = extras.getString("recipeName")
            recipeCategory = extras.getString("recipeCategory")
            recipeHour = extras.getInt("recipeHour")
            recipeMinute = extras.getInt("recipeminute")
            recipePreparation = extras.getString("recipePreparation")
            recipeId = extras.getString("recipeId")
        }

        tv_title.text = recipeName
        tv_category_name.text = recipeCategory
        tv_hour.text = "$recipeHour h"
        tv_minute.text = "$recipeMinute m"
        tv_preparation_content.text = recipePreparation

        val ingredients: ArrayList<IngredientsModel> = dbHandler!!.getAllMyIngredients(recipeId)
        val images: ArrayList<ImagesModel> = dbHandler!!.getAllMyImages(recipeId)

        rv_images.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rv_images.adapter = ShowImagesAdapter(images, this)

        count_images.text = " / ${images.size}"

        if (images.size > 1){
            var count = 0

            if (count == 0){
                back_image.isEnabled = false
            }

            next_image.setOnClickListener {
                count++
                rv_images.smoothScrollToPosition(count)
                image_position.text = "${count+1}"
                if (count == images.size-1){
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


        rv_ingredients.layoutManager = LinearLayoutManager(this)
        rv_ingredients.layoutManager = GridLayoutManager(this, 1)
        rv_ingredients.adapter = ShowIngredientsAdapter(ingredients, this)

        btn_show_recipe_back.setOnClickListener {
            backRecipes()
        }

        //  Contiene el valor representativo de una receta marcada como favorita(1), desmarcada como favorita(-1) o si no ha sido marcada(0)
        val isFavorite = dbHandler!!.getFavoriteRecipe(dbHandler!!.getUserId(), recipeId)

        when (isFavorite) {
            -1, 0 -> {
                img_favorite_No.visibility = View.VISIBLE
                img_favorite_Yes.visibility = View.GONE
            }
            1 -> {
                img_favorite_No.visibility = View.GONE
                img_favorite_Yes.visibility = View.VISIBLE
            }
        }

        img_favorite_No.setOnClickListener {
            when (isFavorite) {
                0 -> {
                    favorite.id = "favorite-$date"
                    favorite.recipeId = recipeId.toString()
                    favorite.userId = dbHandler!!.getUserId()
                    favorite.type = recipeCategory.toString()
                    favorite.favorite = 1
                    favorite.date = date

                    dbHandler!!.addFavoriteRecipe(favorite)
                }
                -1 -> dbHandler!!.updateFavoriteRecipe(dbHandler!!.getUserId(), recipeId, 1, date)
            }

            img_favorite_Yes.visibility = View.VISIBLE
            img_favorite_No.visibility = View.GONE
        }

        img_favorite_Yes.setOnClickListener {
            dbHandler!!.updateFavoriteRecipe(dbHandler!!.getUserId(), recipeId, -1, date)
            img_favorite_No.visibility = View.VISIBLE
            img_favorite_Yes.visibility = View.GONE
        }


    }

    override fun onBackPressed() {
        super.onBackPressed()
        backRecipes()
    }

    private fun backRecipes() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("USER_PROFILE", false)
        startActivity(intent)
    }
}
