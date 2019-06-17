package com.alejandro.comparterecetas.fragments

import android.animation.Animator
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewGroup
import android.widget.Toast
import com.alejandro.comparterecetas.MainActivity
import com.alejandro.comparterecetas.R
import com.alejandro.comparterecetas.adapters.AllRecipesAdapter
import com.alejandro.comparterecetas.database.DataBaseHandler
import com.alejandro.comparterecetas.models.RecipesModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_recipes.*
import kotlinx.android.synthetic.main.fragment_recipes.view.*

/**
 * A simple [Fragment] subclass.
 *
 */
class RecipesFragment : Fragment() {

    private var dbHandler: DataBaseHandler? = null
    private var dbFirebase = FirebaseFirestore.getInstance()
    private var recipesFirebase = dbFirebase.collection("recipes")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        if ((activity as MainActivity).isNetworkConnected()) (activity as MainActivity).updateFirebase()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val args = arguments
        val toPosition = args?.getInt("position", 0)


        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_recipes, container, false)

        if ((activity as MainActivity).isNetworkConnected()){
            view.textView_offInternet.visibility = View.GONE
            view.rv_all_users_recipes.visibility = View.VISIBLE
            //init db
            dbHandler = DataBaseHandler(this.context!!)

            view.swipeRefreshLayout_recipes.setOnRefreshListener {

                initRecipesRecyclerView(view, 0)

                if (view.open_search_button.visibility == View.GONE){
                    closeSearch(view)
                    view.open_search_button.visibility = View.VISIBLE
                    view.tv_recetas.visibility = View.VISIBLE
                }
            }

            initRecipesRecyclerView(view, toPosition)

            view.open_search_button.setOnClickListener {
                openSearch(view)
                view.open_search_button.visibility = View.GONE
                view.tv_recetas.visibility = View.GONE
            }
            view.close_search_button.setOnClickListener {
                if (view.search_input_text.text.toString().isNotEmpty()){
                    initRecipesRecyclerView(view, 0)
                }
                closeSearch(view)
                view.open_search_button.visibility = View.VISIBLE
                view.tv_recetas.visibility = View.VISIBLE
            }

        } else {
            view.textView_offInternet.visibility = View.VISIBLE
            view.rv_all_users_recipes.visibility = View.GONE
            view.swipeRefreshLayout_recipes.visibility = View.INVISIBLE
        }

        return view
    }

    private fun initRecipesRecyclerView(view: View, position: Int?){
        //  Devuelve aquellas recetas que sean públicas (type = 1) y ordenadas por las más recientes
        recipesFirebase.whereEqualTo("type", 1).orderBy("date", Query.Direction.DESCENDING).get().addOnSuccessListener { documentSnapshot ->
            val usersRecipesFirebase: MutableList<RecipesModel> = documentSnapshot.toObjects(RecipesModel::class.java) // Recetas de todos los usuarios

            try {
                view.rv_all_users_recipes.layoutManager = LinearLayoutManager(context)
                view.rv_all_users_recipes.layoutManager = GridLayoutManager(context, 1)
                view.rv_all_users_recipes?.adapter = AllRecipesAdapter(usersRecipesFirebase, "recipes", "", context!!)
                view.rv_all_users_recipes.setHasFixedSize(true)
                view.rv_all_users_recipes.setItemViewCacheSize(20)

                view.rv_all_users_recipes.scrollToPosition(position!!)

                view.swipeRefreshLayout_recipes.isRefreshing = false

            } catch (e: KotlinNullPointerException){}

        }
    }

    //  Inicializa el buscador
    private fun openSearch(view: View) {
        view.search_input_text.setText("")
        view.search_open_view.visibility = View.VISIBLE
        val circularReveal = ViewAnimationUtils.createCircularReveal(
            search_open_view,
            (view.open_search_button.right + view.open_search_button.left) / 2,
            (view.open_search_button.top + view.open_search_button.bottom) / 2,
            0f, 100f
        )
        circularReveal.duration = 200
        circularReveal.start()


        view.execute_search_button.setOnClickListener {
            if (view.search_input_text.text.toString().isNotEmpty()){
                recipesFirebase.whereEqualTo("type", 1).orderBy("date", Query.Direction.DESCENDING).get().addOnSuccessListener { documentSnapshot ->
                    val usersRecipesFirebase: MutableList<RecipesModel> = documentSnapshot.toObjects(RecipesModel::class.java) // Recetas de todos los usuarios
                    val recipeSearch: ArrayList<RecipesModel> = ArrayList()

                    for (i in usersRecipesFirebase){
                        if (i.name.toLowerCase().contains(view.search_input_text.text.toString().toLowerCase())){
                            recipeSearch.add(i)
                        }
                    }

                    if (recipeSearch.size != 0){
                        try {
                            view.rv_all_users_recipes.layoutManager = LinearLayoutManager(context)
                            view.rv_all_users_recipes.layoutManager = GridLayoutManager(context, 1)
                            view.rv_all_users_recipes?.adapter = AllRecipesAdapter(recipeSearch, "recipes", "", context!!)
                            view.rv_all_users_recipes.setHasFixedSize(true)
                            view.rv_all_users_recipes.setItemViewCacheSize(20)

                        } catch (e: KotlinNullPointerException){}
                    } else {
                        Toast.makeText(context, "No existe ninguna coincidencia", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    //  Cierra el buscador
    private fun closeSearch(view: View) {
        val circularConceal = ViewAnimationUtils.createCircularReveal(
            search_open_view,
            (view.open_search_button.right + view.open_search_button.left) / 2,
            (view.open_search_button.top + view.open_search_button.bottom) / 2,
            100f, 0f
        )

        circularConceal.duration = 200
        circularConceal.start()
        circularConceal.addListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(animation: Animator?) = Unit
            override fun onAnimationCancel(animation: Animator?) = Unit
            override fun onAnimationStart(animation: Animator?) = Unit
            override fun onAnimationEnd(animation: Animator?) {
                view.search_open_view.visibility = View.INVISIBLE
                view.search_input_text.setText("")
                circularConceal.removeAllListeners()
            }
        })
    }
}
