package com.alejandro.comparterecetas.adapters

import android.content.Context
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.alejandro.comparterecetas.R
import com.alejandro.comparterecetas.database.DataBaseHandler
import com.alejandro.comparterecetas.models.RecipesModel
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.adapter_my_recipes.view.*
import java.lang.IndexOutOfBoundsException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MyRecipesAdapter(private val items: ArrayList<RecipesModel>, val context: Context) :
    RecyclerView.Adapter<ViewHolderMyRecipes>() {

    private var dbHandler: DataBaseHandler? = null

    private var dbFirebase = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth
    private var recipesFirebase = dbFirebase.collection("recipes")
    private val date = SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault()).format(Date())

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolderMyRecipes {
        return ViewHolderMyRecipes(LayoutInflater.from(context).inflate(R.layout.adapter_my_recipes, p0, false))
    }


    override fun onBindViewHolder(holder: ViewHolderMyRecipes, position: Int) {
        //init db
        dbHandler = DataBaseHandler(context)

        auth = FirebaseAuth.getInstance()

        Glide.with(context)
            .load(dbHandler!!.getImageuserProfile(auth.currentUser!!.uid))
            .fitCenter()
            .centerCrop()
            .into(holder.imgUser)

        holder.tvName.text = items[position].name
        holder.tvPositiveV.text = items[position].positive.toString()
        holder.tvNegativeV.text = items[position].negative.toString()


        if (items.get(position).type == 0){
            holder.imgType.setImageResource(R.drawable.ic_lock_outline_black_24dp)
            holder.btnMenu.setOnClickListener {
                notifyItemChanged(position)
                val popupMenu = PopupMenu(context, it)
                notifyItemChanged(position)
                popupMenu.setOnMenuItemClickListener {
                    when(it.itemId){
                        R.id.my_recipe_public -> {
                            dbHandler!!.updateUserRecipeType(items[position].id, 1, date)
                            notifyItemChanged(position)
                            true
                        }
                        R.id.my_recipe_edit -> {
                            Toast.makeText(context, "Editar receta", Toast.LENGTH_SHORT).show()
                            true
                        }
                        R.id.my_recipe_delete -> {
                            Toast.makeText(context, "Receta eliminada", Toast.LENGTH_SHORT).show()
                            dbHandler!!.updateUserRecipeRemove(items[position].id, 1, date)
                            removeItem(position)
                            true
                        }
                        else -> false
                    }
                }

                notifyItemChanged(position)
                popupMenu.inflate(R.menu.menu_user_recipe_public)
                popupMenu.show()
            }

       } else {
            holder.imgType.setImageResource(R.drawable.ic_public_black_24dp)
            holder.btnMenu.setOnClickListener {
                val popupMenu = PopupMenu(context, it)

                popupMenu.setOnMenuItemClickListener {
                    when(it.itemId){
                        R.id.my_recipe_private -> {
                            dbHandler!!.updateUserRecipeType(items[position].id, 0, date)
                            notifyItemChanged(position)
                            true
                        }
                        R.id.my_recipe_edit -> {
                            Toast.makeText(context, "Editar receta", Toast.LENGTH_SHORT).show()
                            true
                        }
                        R.id.my_recipe_delete -> {
                            Toast.makeText(context, "Receta eliminada", Toast.LENGTH_SHORT).show()
                            dbHandler!!.updateUserRecipeRemove(items[position].id, 1, date)
                            removeItem(position)
                            true
                        }
                        else -> false
                    }
                }

                popupMenu.inflate(R.menu.menu_user_recipe_private)
                popupMenu.show()

            }
        }

        dbHandler!!.close()
    }

    // Para eliminar una receta del recyclerView
    private fun removeItem(position: Int) {
        try {
            items.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, itemCount)
        } catch (e: IndexOutOfBoundsException) {
        }
    }


}

class ViewHolderMyRecipes(view: View) : RecyclerView.ViewHolder(view) {

    val imgUser = view.img_adapter_recipe_user
    val tvName = view.tv_adapter_recipe_name
    val tvPositiveV = view.tv_adapter_recipe_positive
    val tvNegativeV = view.tv_adapter_recipe_negative
    val imgType = view.img_adapter_recipe_type
    val btnMenu = view.btn_adapter_recipe_menu

}