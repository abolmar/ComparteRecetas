package com.alejandro.comparterecetas.adapters

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alejandro.comparterecetas.R
import com.alejandro.comparterecetas.ShowRecipeActivity
import com.alejandro.comparterecetas.models.ImagesModel
import com.alejandro.comparterecetas.models.RecipesModel
import com.alejandro.comparterecetas.models.UsersModel
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.adapter_all_recipes.view.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageException

class AllRecipesAdapter(private val items: MutableList<RecipesModel>, private val fragment: String, private val category: String, val context: Context) :
    RecyclerView.Adapter<ViewHolderAllRecipes>() {

    private var dbFirebase = FirebaseFirestore.getInstance()
    private var usersFirebase = dbFirebase.collection("usersLogin")
    private var imagesFirebase = dbFirebase.collection("images")

    override fun getItemId(position: Int): Long {
        return items[position].id.hashCode().toLong()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolderAllRecipes {
        return ViewHolderAllRecipes(LayoutInflater.from(context).inflate(R.layout.adapter_all_recipes, p0, false))
    }

    override fun onBindViewHolder(holder: ViewHolderAllRecipes, position: Int) {

        val ref = FirebaseStorage.getInstance().reference

        imagesFirebase.whereEqualTo("recipeId", items[position].id).get().addOnSuccessListener { images->
            val recipeImagesFirebase = images.toObjects(ImagesModel::class.java)
            for (i in recipeImagesFirebase){
                if (i.name.endsWith("0")){
                    // Saca la imagen de la receta de firebase
                    ref.child("/Images/recipes/${items[position].id}/${i.name}.png").downloadUrl
                        .addOnSuccessListener {
                            try {
                                Glide.with(context)
                                    .load(it)
                                    .listener(object : RequestListener<Drawable> {
                                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                                            holder.progress.visibility = View.GONE
                                            return false
                                        }

                                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                                            holder.progress.visibility = View.GONE
                                            return false
                                        }
                                    })
                                    .fitCenter()
                                    .centerCrop()
                                    .into(holder.imgMain)
                            } catch (e: IllegalArgumentException) {
                            }
                        }
                }
            }
        }

        usersFirebase.whereEqualTo("id", items[position].userId).get().addOnSuccessListener {
            val userImageProfile = it.toObjects(UsersModel::class.java)
            for (i in userImageProfile) {
                // Saca la imagen de perfil del usuario de firebase
                ref.child("/Images/profile/${items[position].userId}/${i.imageName}.png")
                    .downloadUrl
                    .addOnSuccessListener { userImage ->
                        try {
                            try {
                                Glide.with(context)
                                    .load(userImage)
                                    .fitCenter()
                                    .centerCrop()
                                    .into(holder.imgUser)
                            } catch (e: StorageException) {
                            }
                        } catch (e: IllegalArgumentException) {
                        }
                    }

                holder.tvUserName.text = i.name
            }
        }


        holder.tvRecipeName.text = items[position].name
        holder.tvHours.text = items[position].timeH.toString()
        holder.tvMinutes.text = items[position].timeM.toString()
        holder.tvPeople.text = items[position].people.toString()

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
            intent.putExtra("fromFragment", fragment)
            intent.putExtra("fromCategory", category)

            intent.putExtra("position", position)

            context.startActivity(intent)
        }
    }
}

class ViewHolderAllRecipes(view: View) : RecyclerView.ViewHolder(view) {

    val tvRecipeName = view.tv_adapter_all_recipe_name!!
    val imgMain = view.img_adapter_all_recipe_main!!
    val imgUser = view.img_adapter_all_recipe_user!!
    val tvHours = view.tv_adapter_all_recipes_add_hours!!
    val tvMinutes = view.tv_adapter_all_recipes_add_minutes!!
    val tvUserName = view.tv_adapter_all_recipes_user_name!!
    val tvPeople = view.tv_adapter_all_recipes_people!!

    var container = view.cv_all_recipes!!
    var progress = view.progressBar_all_recipes!!

}