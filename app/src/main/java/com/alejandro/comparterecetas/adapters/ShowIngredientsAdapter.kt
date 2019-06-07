package com.alejandro.comparterecetas.adapters

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alejandro.comparterecetas.R
import com.alejandro.comparterecetas.models.IngredientsModel
import kotlinx.android.synthetic.main.adapter_show_ingredients.view.*

class ShowIngredientsAdapter(private val items: MutableList<IngredientsModel>, val context: Context) :
    RecyclerView.Adapter<ViewHolderShowIngredients>() {

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolderShowIngredients {
        return ViewHolderShowIngredients(LayoutInflater.from(context).inflate(R.layout.adapter_show_ingredients, p0, false))
    }

    override fun onBindViewHolder(holder: ViewHolderShowIngredients, position: Int) {
        holder.tvShowIngredient.text = items[position].ingredientName
    }
}

class ViewHolderShowIngredients(view: View) : RecyclerView.ViewHolder(view) {
    val tvShowIngredient = view.tv_show_ingredient!!
}