package com.alejandro.comparterecetas.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alejandro.comparterecetas.R
import com.alejandro.comparterecetas.models.Ingredients
import kotlinx.android.synthetic.main.recipe_ingredient.view.*
import java.lang.IndexOutOfBoundsException
import java.util.ArrayList

class IngredientsAdapter(private val items: ArrayList<Ingredients>, private val ingredients: ArrayList<String>, val context: Context) :
    RecyclerView.Adapter<ViewHolderIngredient>() {

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolderIngredient {
        return ViewHolderIngredient(LayoutInflater.from(context).inflate(R.layout.recipe_ingredient, p0, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolderIngredient, position: Int) {
        holder.tvIngredient.text = items[position].ingredient

        holder.btnDelete.setOnClickListener {
            removeItem(position)
            ingredients.removeAt(position) //  Elimina ingrediente del ArrayList de la tabla "ingredients"
        }
    }

    // Para eliminar un ingrediente del recyclerView
    private fun removeItem(position: Int) {
        try {
            items.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, itemCount)
        } catch (e: IndexOutOfBoundsException) {
        }
    }
}

class ViewHolderIngredient(view: View) : RecyclerView.ViewHolder(view) {
    val tvIngredient = view.tv_ingrdient
    val btnDelete = view.btn_delete_ingredient
}