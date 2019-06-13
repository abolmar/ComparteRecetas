package com.alejandro.comparterecetas.fragments

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alejandro.comparterecetas.FavoriteRecipesActivity
import com.alejandro.comparterecetas.R
import kotlinx.android.synthetic.main.fragment_favorites.view.*

/**
 * A simple [Fragment] subclass.
 *
 */
class FavoritesFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_favorites, container, false)

        view.btn_all_recipes.setOnClickListener {
            val intent = Intent(context, FavoriteRecipesActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("ALL", "Todas")
            startActivity(intent)
        }

        view.btn_lunch_recipes.setOnClickListener {
            val intent = Intent(context, FavoriteRecipesActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("LUNCH", "Comida")
            startActivity(intent)
        }

        view.btn_dinner_recipes.setOnClickListener {
            val intent = Intent(context, FavoriteRecipesActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("DINNER", "Cena")
            startActivity(intent)
        }

        view.btn_afternoonSnack_recipes.setOnClickListener {
            val intent = Intent(context, FavoriteRecipesActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("AFTERNOONSNACK", "Merienda")
            startActivity(intent)
        }

        view.btn_dessert_recipes.setOnClickListener {
            val intent = Intent(context, FavoriteRecipesActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("DESSERT", "Postre")
            startActivity(intent)
        }

        return view
    }

}
