package com.alejandro.comparterecetas.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.alejandro.comparterecetas.CreateOrEditRecipeActivity
import com.alejandro.comparterecetas.R
import com.alejandro.comparterecetas.ShowRecipeActivity
import com.alejandro.comparterecetas.database.DataBaseHandler
import com.alejandro.comparterecetas.models.RecipesModel
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.adapter_my_recipes.view.*
import java.lang.IndexOutOfBoundsException
import java.text.SimpleDateFormat
import java.util.*


class MyRecipesAdapter(private val items: MutableList<RecipesModel>, val context: Context) :
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

        if (dbHandler!!.getImageUserProfilePath(auth.currentUser!!.uid) != ""){
            Glide.with(context)
                .load(dbHandler!!.getImageUserProfilePath(auth.currentUser!!.uid))
                .fitCenter()
                .centerCrop()
                .into(holder.imgUser)
        }


        holder.tvName.text = items[position].name

        if (items[position].type == 0) {
            holder.imgType.setImageResource(R.drawable.ic_lock_outline_black_24dp)
            holder.btnMenu.setOnClickListener { view ->
                val popupMenu = PopupMenu(context, view)
                popupMenu.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.my_recipe_public -> {
                            uploadItem(position, 1)
                            notifyItemChanged(position)
                            dbHandler!!.updateUserRecipeType(items[position].id, 1, date)
                            recipesFirebase.document(items[position].id).update("type", 1)
                            recipesFirebase.document(items[position].id).update("date", date)
                            true
                        }
                        R.id.my_recipe_edit -> {
                            val intent = Intent(context, CreateOrEditRecipeActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                            intent.putExtra("load", "edit")
                            intent.putExtra("recipeId", items[position].id)
                            intent.putExtra("recipeName", items[position].name)
                            intent.putExtra("category", items[position].category)
                            intent.putExtra("type", items[position].type)
                            intent.putExtra("hours", items[position].timeH)
                            intent.putExtra("minutes", items[position].timeM)
                            intent.putExtra("people", items[position].people)
                            intent.putExtra("preparation", items[position].preparation)
                            intent.putExtra("createOrEdit", "Editar receta")
                            context.startActivity(intent)
                            true
                        }
                        R.id.my_recipe_delete -> {
                            removeRecipe(position)
                            true
                        }
                        else -> false
                    }
                }

                popupMenu.inflate(R.menu.menu_user_recipe_public)
                popupMenu.show()
            }
        } else {
            holder.imgType.setImageResource(R.drawable.ic_public_black_24dp)
            holder.btnMenu.setOnClickListener { view ->
                val popupMenu = PopupMenu(context, view)

                popupMenu.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.itemId) {
                        R.id.my_recipe_private -> {
                            uploadItem(position, 0)
                            notifyItemChanged(position)
                            dbHandler!!.updateUserRecipeType(items[position].id, 0, date)
                            recipesFirebase.document(items[position].id).update("type", 0)
                            recipesFirebase.document(items[position].id).update("date", date)
                            true
                        }
                        R.id.my_recipe_edit -> {
                            val intent = Intent(context, CreateOrEditRecipeActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                            intent.putExtra("load", "edit")
                            intent.putExtra("recipeId", items[position].id)
                            intent.putExtra("recipeName", items[position].name)
                            intent.putExtra("category", items[position].category)
                            intent.putExtra("type", items[position].type)
                            intent.putExtra("hours", items[position].timeH)
                            intent.putExtra("minutes", items[position].timeM)
                            intent.putExtra("people", items[position].people)
                            intent.putExtra("preparation", items[position].preparation)
                            intent.putExtra("createOrEdit", "Editar receta")
                            context.startActivity(intent)
                            true
                        }
                        R.id.my_recipe_delete -> {
                            removeRecipe(position)
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

        holder.container.setOnClickListener {
            val intent = Intent(context, ShowRecipeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)

            intent.putExtra("recipeName", items[position].name)
            intent.putExtra("recipeCategory", items[position].category)
            intent.putExtra("recipeHour", items[position].timeH)
            intent.putExtra("recipeMinute", items[position].timeM)
            intent.putExtra("recipePeople", items[position].people)
            intent.putExtra("recipePreparation", items[position].preparation)
            intent.putExtra("recipeId", items[position].id)
            intent.putExtra("fromFragment", "profile")
            intent.putExtra("fromCategory", "")

            context.startActivity(intent)
        }
    }

    private fun uploadItem(position: Int, type: Int) {
        try {
            items[position] = RecipesModel(
                items[position].id,
                items[position].name,
                items[position].userId,
                items[position].preparation,
                items[position].timeH,
                items[position].timeM,
                items[position].people,
                items[position].category,
                type,
                items[position].date,
                items[position].remove
            )
        } catch (e: IndexOutOfBoundsException) {
        }
    }

    // Para eliminar una receta de la base de datos
    private fun removeRecipe(position: Int) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Eliminar la receta?")

        builder.setPositiveButton("Eliminar") { _, _ ->
            dbHandler!!.updateUserRecipeRemove(items[position].id, 1, date)
            removeItem(position)
            Toast.makeText(context, "Receta eliminada", Toast.LENGTH_SHORT).show()
        }

        builder.setNegativeButton("Cancelar") { _, _ -> }

        val alert = builder.create()
        alert.show()
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

    val imgUser = view.img_adapter_recipe_user!!
    val tvName = view.tv_adapter_recipe_name!!
    val imgType = view.img_adapter_recipe_type!!
    val btnMenu = view.btn_adapter_recipe_menu!!

    val container = view.cv_my_recipes!!

}