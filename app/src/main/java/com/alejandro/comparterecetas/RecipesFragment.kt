package com.alejandro.comparterecetas

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alejandro.comparterecetas.adapters.AllRecipesAdapter
import com.alejandro.comparterecetas.database.DataBaseHandler
import com.alejandro.comparterecetas.models.RecipesModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_recipes.view.*

/**
 * A simple [Fragment] subclass.
 *
 */
class RecetasFragment : Fragment() {

    private var dbHandler: DataBaseHandler? = null
    private var dbFirebase = FirebaseFirestore.getInstance()
    private var recipesFirebase = dbFirebase.collection("recipes")
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        if ((activity as MainActivity).isNetworkConnected()) (activity as MainActivity).updateFirebase() //?????????????
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_recipes, container, false)

        if ((activity as MainActivity).isNetworkConnected()){
            view.textView_offInternet.visibility = View.GONE
            view.rv_all_users_recipes.visibility = View.VISIBLE
            //init db
            dbHandler = DataBaseHandler(this.context!!)

            //**********************************************************************************************************
            // *******************************Funciona pero...**********************************************************
            // TODO:  ***** (Esta función se encuentra en fragment_recipes.xml encerrando al recycler view) *****
            view.swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.design_default_color_primary)
            view.swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.colorAccent)

            view.swipeRefreshLayout.setOnRefreshListener {
                recipesFirebase.whereEqualTo("type", 1).orderBy("date", Query.Direction.DESCENDING).get().addOnSuccessListener { documentSnapshot ->
                    val usersRecipesFirebase: MutableList<RecipesModel> = documentSnapshot.toObjects(RecipesModel::class.java) // Recetas de todos los usuarios

                    try {
                        view.rv_all_users_recipes.layoutManager = LinearLayoutManager(context)
                        view.rv_all_users_recipes.layoutManager = GridLayoutManager(context, 1)
                        view.rv_all_users_recipes?.adapter = AllRecipesAdapter(usersRecipesFirebase, context!!)

                        view.swipeRefreshLayout.isRefreshing = false

                    } catch (e: KotlinNullPointerException){
                        Log.d("capullo", "Algo falla en RecipesFragment -->: $e")
                    }
//                    // Lineas de prueba
//                    view.rv_all_users_recipes.setHasFixedSize(true)
//                    view.rv_all_users_recipes.setItemViewCacheSize(20)
                }
            }
            // *******************************Funciona pero...**********************************************************
            //**********************************************************************************************************

            //  Devuelve aquellas recetas que sean públicas (type = 1) y ordenadas por las más recientes
            recipesFirebase.whereEqualTo("type", 1).orderBy("date", Query.Direction.DESCENDING).get().addOnSuccessListener { documentSnapshot ->
                val usersRecipesFirebase: MutableList<RecipesModel> = documentSnapshot.toObjects(RecipesModel::class.java) // Recetas de todos los usuarios

                try {
                    view.rv_all_users_recipes.layoutManager = LinearLayoutManager(context)
                    view.rv_all_users_recipes.layoutManager = GridLayoutManager(context, 1)
                    view.rv_all_users_recipes?.adapter = AllRecipesAdapter(usersRecipesFirebase, context!!)

                } catch (e: KotlinNullPointerException){
                    Log.d("capullo", "Algo falla: $e")
                }
//                // Lineas de prueba
//                view.rv_all_users_recipes.setHasFixedSize(true)
//                view.rv_all_users_recipes.setItemViewCacheSize(20)
            }

            return view

        } else {
            view.textView_offInternet.visibility = View.VISIBLE
            view.rv_all_users_recipes.visibility = View.GONE

            return view
        }

    }

}
