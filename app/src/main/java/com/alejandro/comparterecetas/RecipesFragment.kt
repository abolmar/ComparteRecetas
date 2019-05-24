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
import com.alejandro.comparterecetas.models.UsersModel

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_recipes.view.*
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import io.grpc.android.AndroidChannelBuilder


// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

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
            //     ***** (Esta función se encuentra en fragment_recipes.xml encerrando al recycler view) *****
            view.swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.design_default_color_primary)
            view.swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.colorAccent)

            view.swipeRefreshLayout.setOnRefreshListener {
                recipesFirebase.whereEqualTo("type", 1).get().addOnSuccessListener { documentSnapshot ->
                    val usersRecipesFirebase: MutableList<RecipesModel> = documentSnapshot.toObjects(RecipesModel::class.java) // Recetas de todos los usuarios

                    try {
                        view.rv_all_users_recipes.layoutManager = LinearLayoutManager(context)
                        view.rv_all_users_recipes.layoutManager = GridLayoutManager(context, 1)
                        view.rv_all_users_recipes?.adapter = AllRecipesAdapter(usersRecipesFirebase, context!!)

                        view.swipeRefreshLayout.isRefreshing = false

                    } catch (e: KotlinNullPointerException){
                        Log.d("capullo", "Algo falla en RecipesFragment -->: $e")
                    }
                    // Lineas de prueba
                    view.rv_all_users_recipes.setHasFixedSize(true)
                    view.rv_all_users_recipes.setItemViewCacheSize(20)
                }
            }
            // *******************************Funciona pero...**********************************************************
            //**********************************************************************************************************


            recipesFirebase.whereEqualTo("type", 1).get().addOnSuccessListener { documentSnapshot ->
                val usersRecipesFirebase: MutableList<RecipesModel> = documentSnapshot.toObjects(RecipesModel::class.java) // Recetas de todos los usuarios

                try {
                    view.rv_all_users_recipes.layoutManager = LinearLayoutManager(context)
                    view.rv_all_users_recipes.layoutManager = GridLayoutManager(context, 1)
                    view.rv_all_users_recipes?.adapter = AllRecipesAdapter(usersRecipesFirebase, context!!)

                } catch (e: KotlinNullPointerException){
                    Log.d("capullo", "Algo falla: $e")
                }
                // Lineas de prueba
                view.rv_all_users_recipes.setHasFixedSize(true)
                view.rv_all_users_recipes.setItemViewCacheSize(20)
            }

            return view

        } else {
            view.textView_offInternet.visibility = View.VISIBLE
            view.rv_all_users_recipes.visibility = View.GONE

            return inflater.inflate(R.layout.fragment_recipes, container, false)
        }

    }

}
