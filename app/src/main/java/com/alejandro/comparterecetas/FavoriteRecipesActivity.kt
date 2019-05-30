package com.alejandro.comparterecetas

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.alejandro.comparterecetas.adapters.AllRecipesAdapter
import com.alejandro.comparterecetas.models.RecipesModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_favorite_recipes.*

class FavoriteRecipesActivity : AppCompatActivity() {

    private var dbFirebase = FirebaseFirestore.getInstance()
    private var recipesFirebase = dbFirebase.collection("recipes")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorite_recipes)

        val extras = intent.extras

        var desserts: String? = ""

        if (extras != null) {
            desserts = extras.getString("ALL")
        }

        // ************************************************************HAY QUE HACER QUE NO MUESTRE LAS RECETAS PRIVADAS
        recipesFirebase.whereEqualTo("category", desserts).orderBy("date", Query.Direction.DESCENDING).get().addOnSuccessListener { documentSnapshot ->
            val usersRecipesFirebase: MutableList<RecipesModel> = documentSnapshot.toObjects(RecipesModel::class.java) // Recetas de todos los usuarios

            try {
                rv_favorite_recipes.layoutManager = LinearLayoutManager(this)
                rv_favorite_recipes.layoutManager = GridLayoutManager(this, 1)
                rv_favorite_recipes?.adapter = AllRecipesAdapter(usersRecipesFirebase, this)

            } catch (e: KotlinNullPointerException){
                Log.d("capullo", "Algo fallaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa: $e")
            }
        }.addOnFailureListener {exception ->
            Log.d("capullo", "Algo falla en Firebase: $exception")
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
        intent.putExtra("USER_PROFILE", false)
        startActivity(intent)
    }
}
