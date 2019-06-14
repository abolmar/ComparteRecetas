package com.alejandro.comparterecetas

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.View
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
    private var allCategories: String? = ""
    private var lunch: String? = ""
    private var dinner: String? = ""
    private var afternoonSnack: String? = ""
    private var dessert: String? = ""
    private var passCategory = ""

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite_recipes)

        dbHandler = DataBaseHandler(this)

        val extras = intent.extras

        if (extras != null) {
            allCategories = extras.getString("ALL")
            if (allCategories != null){
                categoryOfRecipesFavorites("Todas mis favoritas", dbHandler!!.getAllMyFavoriteRecipes(dbHandler!!.getUserId()), "¡Añade recetas a tus favoritos!")
                passCategory = allCategories!!
            }

            lunch = extras.getString("LUNCH")
            if (lunch != null){
                categoryOfRecipesFavorites("Comidas", dbHandler!!.getMyFavoriteRecipes(dbHandler!!.getUserId(), lunch), "¡Añade comidas a tus favoritos!")
                passCategory = lunch!!
            }

            dinner = extras.getString("DINNER")
            if (dinner != null){
                categoryOfRecipesFavorites("Cenas", dbHandler!!.getMyFavoriteRecipes(dbHandler!!.getUserId(), dinner), "¡Añade cenas a tus favoritos!")
                passCategory = dinner!!
            }

            afternoonSnack = extras.getString("AFTERNOONSNACK")
            if (afternoonSnack != null){
                categoryOfRecipesFavorites("Meriendas", dbHandler!!.getMyFavoriteRecipes(dbHandler!!.getUserId(), afternoonSnack), "¡Añade meriendas a tus favoritos!")
                passCategory = afternoonSnack!!
            }

            dessert = extras.getString("DESSERT")
            if (dessert != null){
                categoryOfRecipesFavorites("Postres", dbHandler!!.getMyFavoriteRecipes(dbHandler!!.getUserId(), dessert), "¡Añade postres a tus favoritos!")
                passCategory = dessert!!
            }
        }

        if (isNetworkConnected()){
            rv_favorite_recipes.visibility = View.VISIBLE
            lostConection.visibility = View.GONE

            // Pasamos el contenido de las recetas favoritas al recycler view
            for (i in myFavorites){
                recipesFirebase.whereEqualTo("id", i.recipeId).orderBy("date", Query.Direction.DESCENDING).get().addOnSuccessListener { documentSnapshot ->

                    val usersRecipesFirebase: MutableList<RecipesModel> = documentSnapshot.toObjects(RecipesModel::class.java) // Recetas de todos los usuarios

                    myRecipesFavorites.addAll(usersRecipesFirebase)

                    // Para recargar el contenido de la vista
                    swipeRefreshLayout_favorites.setOnRefreshListener {
                        initAllRecipesAdapter(myRecipesFavorites, "favorites", passCategory)
                    }

                    initAllRecipesAdapter(myRecipesFavorites, "favorites", passCategory)
                }
            }

        } else {
            swipeRefreshLayout_favorites.visibility = View.GONE
            rv_favorite_recipes.visibility = View.GONE
            addToFavorites.visibility = View.GONE
            lostConection.visibility = View.VISIBLE
        }


        btn_favorite_recipes_back.setOnClickListener {
            backRecipes()
        }
    }

    private fun initAllRecipesAdapter(myFavorites: ArrayList<RecipesModel>, favorites: String, category: String){
        try {
            rv_favorite_recipes.layoutManager = LinearLayoutManager(this)
            rv_favorite_recipes.layoutManager = GridLayoutManager(this, 1)
            rv_favorite_recipes?.adapter = AllRecipesAdapter(myFavorites, favorites, category, this)
        } catch (e: KotlinNullPointerException){}

        swipeRefreshLayout_favorites.isRefreshing = false
    }

    private fun categoryOfRecipesFavorites(categoryTitle:String, arrayListMyFavorites: ArrayList<FavoritesModel>, addRecipe: String){
        tv_title_category.text = categoryTitle

        myFavorites = arrayListMyFavorites

        if (myFavorites.size == 0){
            swipeRefreshLayout_favorites.visibility = View.GONE
            addToFavorites.visibility = View.VISIBLE
            addToFavorites.text = addRecipe
        } else {
            swipeRefreshLayout_favorites.visibility = View.VISIBLE
            addToFavorites.visibility = View.GONE
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

    // Comprueba la conexión a internet
    private fun isNetworkConnected(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null
    }
}
