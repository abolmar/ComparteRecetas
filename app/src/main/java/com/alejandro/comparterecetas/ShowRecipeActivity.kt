package com.alejandro.comparterecetas

import android.annotation.SuppressLint
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_show_recipe.*

class ShowRecipeActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_recipe)

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

        val ref = FirebaseStorage.getInstance().reference

        // Saca la imagen de la receta de firebase
        ref.child("/Images/recipes/$recipeId/0.png").downloadUrl
            .addOnSuccessListener {
                try {
                    Glide.with(this)
                        .load(it)
                        .fitCenter()
                        .centerCrop()
                        .into(img_main)
                } catch (e: IllegalArgumentException){}
            }

        btn_show_recipe_back.setOnClickListener {
            backRecipes()
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
