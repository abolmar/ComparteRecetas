package com.alejandro.comparterecetas.adapters

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.alejandro.comparterecetas.R
import com.alejandro.comparterecetas.database.DataBaseHandler
import com.alejandro.comparterecetas.models.RecipesModel
import com.alejandro.comparterecetas.models.UsersModel
import com.alejandro.comparterecetas.models.VotesModel
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.adapter_all_recipes.view.*
import com.google.firebase.storage.StorageReference
import com.bumptech.glide.Glide
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageException


class AllRecipesAdapter(private val items: MutableList<RecipesModel>, val context: Context) :
    RecyclerView.Adapter<ViewHolderAllRecipes>() {

    private lateinit var auth: FirebaseAuth
    private var dbFirebase = FirebaseFirestore.getInstance()
    private var dbHandler: DataBaseHandler? = null
    private var recipesFirebase = dbFirebase.collection("recipes")
    private var usersFirebase = dbFirebase.collection("usersLogin")

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
        //init db
        dbHandler = DataBaseHandler(context)

        val ref = FirebaseStorage.getInstance().reference

        try {
            // Saca la imagen de la receta de firebase
            ref.child("/Images/recipes/${items[position].id}/0.png").downloadUrl
                .addOnSuccessListener {
                        Glide.with(context)
                            .load(it)
                            .fitCenter()
                            .centerCrop()
                            .into(holder.imgMain)
                }

            usersFirebase.whereEqualTo("id", items[position].userId).get().addOnSuccessListener {
                val userImageProfile = it.toObjects(UsersModel::class.java)
                for (i in userImageProfile){
                    // Saca la imagen de perfil del usuario de firebase
                    ref.child("/Images/profiles/${items[position].userId}/${i.imageName}.png").downloadUrl //dbHandler!!.getImageUserName(items[position].userId)
                        .addOnSuccessListener {
                            try {
                                Glide.with(context)
                                    .load(it)
                                    .fitCenter()
                                    .centerCrop()
                                    .into(holder.imgUser)
                            } catch (e: StorageException ){ }
                        }
                }
            }
        } catch (e: IllegalArgumentException){}

        holder.tvName.text = items[position].name
        holder.tvPositiveV.text = items[position].positive.toString()
        holder.tvNegativeV.text = items[position].negative.toString()

        if (dbHandler!!.getPositiveVote(items[position].id, dbHandler!!.getUserId()) == 0 && dbHandler!!.getNegativeVote(items[position].id, dbHandler!!.getUserId()) == 0
        ) {
            holder.btnPositiveV.setColorFilter(Color.BLACK)
            holder.btnNegativeV.setColorFilter(Color.BLACK)

            holder.btnPositiveV.setOnClickListener {
                //  Si el id retornado es cero, se inserta un nuevo registro
                if (dbHandler!!.getIdVote(items[position].id, dbHandler!!.getUserId()) == "") {
                    val vote = VotesModel()
                    val successVote: Boolean
                    vote.recipeId = items[position].id
                    vote.userId = dbHandler!!.getUserId()
                    vote.votePositive = 1
                    vote.voteNegative = 0
                    successVote = dbHandler!!.addUserVoteTableUserVotes(vote)

                    if (successVote) {
                        //  Suma uno a los votos positivos de la receta
                        dbHandler!!.updateRecipePositiveVoteAdd(
                            items[position].id,
                            items[position].positive + 1
                        )

                        dbHandler!!.close()
                    }

                } else {
                    dbHandler!!.updateUserVotePositive(items[position].id, dbHandler!!.getUserId(), 1)

                    //  Suma uno a los votos positivos de la receta
                    dbHandler!!.updateRecipePositiveVoteAdd(
                        items[position].id,
                        items[position].positive + 1
                    )
                }

                notifyItemChanged(position)
                notifyDataSetChanged()
            }

            holder.btnNegativeV.setOnClickListener {
                if (dbHandler!!.getIdVote(items[position].id, dbHandler!!.getUserId()) == "") {
                    val vote = VotesModel()
                    val successVote: Boolean
                    vote.recipeId = items[position].id
                    vote.userId = dbHandler!!.getUserId()
                    vote.votePositive = 0
                    vote.voteNegative = 1
                    successVote = dbHandler!!.addUserVoteTableUserVotes(vote)

                    if (successVote) {
                        //  Suma uno a los votos negativos de la receta
                        dbHandler!!.updateRecipeNegativeVoteAdd(
                            items[position].id,
                            items[position].negative + 1
                        )

                        dbHandler!!.close()
                    }

                } else {
                    dbHandler!!.updateUserVoteNegative(items[position].id, dbHandler!!.getUserId(), 1)

                    //  Suma uno a los votos negativos de la receta
                    dbHandler!!.updateRecipeNegativeVoteAdd(
                        items[position].id,
                        items[position].negative + 1
                    )
                }
                notifyItemChanged(position)
                notifyDataSetChanged()
            }
        }

        if (dbHandler!!.getPositiveVote(
                items[position].id,
                dbHandler!!.getUserId()
            ) == 0 && dbHandler!!.getNegativeVote(items[position].id, dbHandler!!.getUserId()) == 1
        ) {
            holder.btnNegativeV.setColorFilter(Color.RED)
            holder.tvNegativeV.setTextColor(Color.RED)
            holder.btnPositiveV.setColorFilter(Color.BLACK)
            holder.tvPositiveV.setTextColor(Color.BLACK)

            holder.btnPositiveV.setOnClickListener {
                dbHandler!!.updateUserVotePositive(items[position].id, dbHandler!!.getUserId(), 1)
                dbHandler!!.updateUserVoteNegative(items[position].id, dbHandler!!.getUserId(), 0)

                //  Suma uno a los votos positivos de la receta
                dbHandler!!.updateRecipePositiveVoteAdd(
                    items[position].id,
                    items[position].positive + 1
                )

                //  Resta uno a los votos negativos de la receta
                dbHandler!!.updateRecipeNegativeVoteRemove(
                    items[position].id,
                    items[position].negative - 1
                )

                notifyItemChanged(position)
                notifyDataSetChanged()
            }

            holder.btnNegativeV.setOnClickListener {
                dbHandler!!.updateUserVoteNegative(items[position].id, dbHandler!!.getUserId(), 0)

                //  Resta uno a los votos negativos de la receta
                dbHandler!!.updateRecipeNegativeVoteRemove(
                    items[position].id,
                    items[position].negative - 1
                )

                notifyItemChanged(position)
                notifyDataSetChanged()
            }

            dbHandler!!.close()
        }

        if (dbHandler!!.getPositiveVote(
                items[position].id,
                dbHandler!!.getUserId()
            ) == 1 && dbHandler!!.getNegativeVote(items[position].id, dbHandler!!.getUserId()) == 0
        ) {
            holder.btnPositiveV.setColorFilter(Color.RED)
            holder.btnNegativeV.setColorFilter(Color.BLACK)

            holder.btnNegativeV.setOnClickListener {
                dbHandler!!.updateUserVotePositive(items[position].id, dbHandler!!.getUserId(), 0)
                dbHandler!!.updateUserVoteNegative(items[position].id, dbHandler!!.getUserId(), 1)

                //  Suma uno a los votos negativos de la receta
                dbHandler!!.updateRecipeNegativeVoteAdd(
                    items[position].id,
                    items[position].negative + 1
                )

                //  Resta uno a los votos positivos de la receta
                dbHandler!!.updateRecipePositiveVoteRemove(
                    items[position].id,
                    items[position].positive - 1
                )

                notifyItemChanged(position)
                notifyDataSetChanged()
            }

            holder.btnPositiveV.setOnClickListener {
                dbHandler!!.updateUserVotePositive(items[position].id, dbHandler!!.getUserId(), 0)

                //  Resta uno a los votos positivos de la receta
                dbHandler!!.updateRecipePositiveVoteRemove(
                    items[position].id,
                    items[position].positive - 1
                )

                notifyItemChanged(position)
                notifyDataSetChanged()
            }

            dbHandler!!.close()
        }
    }
}

class ViewHolderAllRecipes(view: View) : RecyclerView.ViewHolder(view) {

    val tvName = view.tv_adapter_all_recipe_name!!
    val tvPositiveV = view.tv_adapter_all_recipe_positive!!
    val tvNegativeV = view.tv_adapter_all_recipe_negative!!
    val btnPositiveV = view.img_adapter_all_recipe_positive!!
    val btnNegativeV = view.img_adapter_all_recipe_negative!!
    var imgMain = view.img_adapter_all_recipe_main!!
    var imgUser = view.img_adapter_all_recipe_user!!

}