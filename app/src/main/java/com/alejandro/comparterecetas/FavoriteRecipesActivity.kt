package com.alejandro.comparterecetas

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.alejandro.comparterecetas.adapters.AllRecipesAdapter
import com.alejandro.comparterecetas.database.DataBaseHandler
import com.alejandro.comparterecetas.models.FavoritesModel
import com.alejandro.comparterecetas.models.RecipesModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_favorite_recipes.*

class FavoriteRecipesActivity : AppCompatActivity() {

    private var dbHandler: DataBaseHandler? = null
    private var dbFirebase = FirebaseFirestore.getInstance()
    private var recipesFirebase = dbFirebase.collection("recipes")
    private var myFavorites: ArrayList<FavoritesModel> = ArrayList() // Este array contiene todas las recetas favoritas cuya categoría es la que se le pase por parámetro
    private val myRecipesFavorites: ArrayList<RecipesModel> = ArrayList() // Este array contendrá aquellas recetas que coincidan con las guardadas en favoritas

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite_recipes)

        dbHandler = DataBaseHandler(this)

        val extras = intent.extras

        var allCategories: String? = ""
        var lunch: String? = ""
        var dinner: String? = ""
        var afternoonSnack: String? = ""
        var dessert: String? = ""

        var passCategory = ""

        if (extras != null) {
            allCategories = extras.getString("ALL")
            if (allCategories != null){
                tv_title_category.text = allCategories
                myFavorites = dbHandler!!.getAllMyFavoriteRecipes(dbHandler!!.getUserId())
                passCategory = allCategories
            }

            lunch = extras.getString("LUNCH")
            if (lunch != null){
                tv_title_category.text = lunch
                myFavorites = dbHandler!!.getMyFavoriteRecipes(dbHandler!!.getUserId(), lunch)
                passCategory = lunch
            }

            dinner = extras.getString("DINNER")
            if (dinner != null){
                tv_title_category.text = dinner
                myFavorites = dbHandler!!.getMyFavoriteRecipes(dbHandler!!.getUserId(), dinner)
                passCategory = dinner
            }

            afternoonSnack = extras.getString("AFTERNOONSNACK")
            if (afternoonSnack != null){
                tv_title_category.text = afternoonSnack
                myFavorites = dbHandler!!.getMyFavoriteRecipes(dbHandler!!.getUserId(), afternoonSnack)
                passCategory = afternoonSnack
            }

            dessert = extras.getString("DESSERT")
            if (dessert != null){
                tv_title_category.text = dessert
                myFavorites = dbHandler!!.getMyFavoriteRecipes(dbHandler!!.getUserId(), dessert)
                passCategory = dessert
            }
        }


        // Pasamos el contenido de las recetas favoritas al recycler view
        for (i in myFavorites){
            Log.d("capullo", "Receta id: ${i.recipeId}")
            recipesFirebase.whereEqualTo("id", i.recipeId).whereEqualTo("type", 1).orderBy("date", Query.Direction.DESCENDING).get().addOnSuccessListener { documentSnapshot ->

                val usersRecipesFirebase: MutableList<RecipesModel> = documentSnapshot.toObjects(RecipesModel::class.java) // Recetas de todos los usuarios

                // O guardo el contenido en el array o guardo el contenido en el array, no queda otra :(
                myRecipesFavorites.addAll(usersRecipesFirebase)

                try {
                    rv_favorite_recipes.layoutManager = LinearLayoutManager(this)
                    rv_favorite_recipes.layoutManager = GridLayoutManager(this, 1)
                    rv_favorite_recipes?.adapter = AllRecipesAdapter(myRecipesFavorites, "favorites", passCategory, this)
                } catch (e: KotlinNullPointerException){}

            }.addOnFailureListener {exception ->
                Log.d("capullo", "Algo falla en Firebase: $exception")
            }
        }


        btn_favorite_recipes_back.setOnClickListener {
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
        intent.putExtra("USER_FAVORITES", true)
        startActivity(intent)
    }
}
