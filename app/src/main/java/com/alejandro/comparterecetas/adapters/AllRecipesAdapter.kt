package com.alejandro.comparterecetas.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.alejandro.comparterecetas.EditProfileActivity
import com.alejandro.comparterecetas.R
import com.alejandro.comparterecetas.ShowRecipeActivity
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
import kotlinx.android.synthetic.main.fragment_recipes.view.*
import java.text.SimpleDateFormat
import java.util.*


class AllRecipesAdapter(private val items: MutableList<RecipesModel>, val context: Context) :
    RecyclerView.Adapter<ViewHolderAllRecipes>() {

    private lateinit var auth: FirebaseAuth
    private var dbFirebase = FirebaseFirestore.getInstance()
    private var dbHandler: DataBaseHandler? = null
    private var recipesFirebase = dbFirebase.collection("recipes")
    private var usersFirebase = dbFirebase.collection("usersLogin")
    private val date: String = SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault()).format(Date())

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

        // Inicializa Firebase Auth
        auth = FirebaseAuth.getInstance()

        val userId = auth.currentUser!!.uid

        val ref = FirebaseStorage.getInstance().reference

        // Saca la imagen de la receta de firebase
        ref.child("/Images/recipes/${items[position].id}/0.png").downloadUrl
            .addOnSuccessListener {
                try {
                    Glide.with(context)
                        .load(it)
                        .fitCenter()
                        .centerCrop()
                        .into(holder.imgMain)
                } catch (e: IllegalArgumentException) {
                }
            }


        usersFirebase.whereEqualTo("id", items[position].userId).get().addOnSuccessListener {
            val userImageProfile = it.toObjects(UsersModel::class.java)
            for (i in userImageProfile) {
                // Saca la imagen de perfil del usuario de firebase
                ref.child("/Images/profile/${items[position].userId}/${i.imageName}.png")
                    .downloadUrl //dbHandler!!.getImageUserName(items[position].userId)
                    .addOnSuccessListener {
                        try {
                            try {
                                Glide.with(context)
                                    .load(it)
                                    .fitCenter()
                                    .centerCrop()
                                    .into(holder.imgUser)
                            } catch (e: StorageException) {
                            }
                        } catch (e: IllegalArgumentException) {
                        }
                    }

            }
        }


        holder.tvName.text = items[position].name
        holder.tvPositiveV.text = items[position].positive.toString()
        holder.tvNegativeV.text = items[position].negative.toString()

        holder.container.setOnClickListener {
            val intent = Intent(context, ShowRecipeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)

            intent.putExtra("recipeName", items[position].name)
            intent.putExtra("recipeCategory", items[position].category)
            intent.putExtra("recipeHour", items[position].timeH)
            intent.putExtra("recipeminute", items[position].timeM)
            intent.putExtra("recipePreparation", items[position].preparation)
            intent.putExtra("recipeId", items[position].id)

            context.startActivity(intent)
        }

        //**************************************************************************************************************
        // O SE MEJORA O SE ELIMINA*************************************************************************************
        if (dbHandler!!.getPositiveVote(items[position].id, userId) == 0 && dbHandler!!.getNegativeVote(items[position].id, userId) == 0) {
            holder.btnPositiveV.setColorFilter(Color.BLACK)
            holder.btnNegativeV.setColorFilter(Color.BLACK)

            holder.btnPositiveV.setOnClickListener {
                //  Si el id retornado es cero, se inserta un nuevo registro
                if (dbHandler!!.getIdVote(items[position].id, userId) == "") {
                    val vote = VotesModel()
                    val successVote: Boolean

                    vote.id = "vote-$date"
                    vote.recipeId = items[position].id
                    vote.userId = userId
                    vote.votePositive = 1
                    vote.voteNegative = 0
                    vote.date = date

                    successVote = dbHandler!!.addUserVoteTableUserVotes(vote)

                    if (successVote) {
                        //  Suma uno a los votos positivos de la receta
                        items[position].positive += 1
                        //  Suma uno a los votos positivos de la receta
//                        dbHandler!!.updateRecipePositiveVoteAdd(
//                            items[position].id,
//                            items[position].positive + 1
//                        )

//                        dbHandler!!.close()
                    }

                } else {
                    dbHandler!!.updateUserVotePositive(items[position].id, userId, 1)

                    //  Suma uno a los votos positivos de la receta
                    items[position].positive += 1
//                    dbHandler!!.updateRecipePositiveVoteAdd(
//                        items[position].id,
//                        items[position].positive + 1
//                    )
                }

                notifyItemChanged(position)
                notifyDataSetChanged()
            }

            holder.btnNegativeV.setOnClickListener {
                if (dbHandler!!.getIdVote(items[position].id, userId) == "") {
                    val vote = VotesModel()
                    val successVote: Boolean

                    vote.id = "vote-$date"
                    vote.recipeId = items[position].id
                    vote.userId = userId
                    vote.votePositive = 0
                    vote.voteNegative = 1
                    vote.date = date

                    successVote = dbHandler!!.addUserVoteTableUserVotes(vote)

                    if (successVote) {
                        //  Suma uno a los votos negativos de la receta
                        items[position].negative += 1
//                        dbHandler!!.updateRecipeNegativeVoteAdd(
//                            items[position].id,
//                            items[position].negative + 1
//                        )
//
//                        dbHandler!!.close()
                    }

                } else {
                    dbHandler!!.updateUserVoteNegative(items[position].id, userId, 1)

                    //  Suma uno a los votos negativos de la receta
                    items[position].negative += 1
//                    dbHandler!!.updateRecipeNegativeVoteAdd(
//                        items[position].id,
//                        items[position].negative + 1
//                    )
                }
                notifyItemChanged(position)
                notifyDataSetChanged()
            }
        }

        if (dbHandler!!.getPositiveVote(items[position].id, userId) == 0 && dbHandler!!.getNegativeVote(items[position].id, userId) == 1) {
            holder.btnNegativeV.setColorFilter(Color.RED)
            holder.tvNegativeV.setTextColor(Color.RED)
            holder.btnPositiveV.setColorFilter(Color.BLACK)
            holder.tvPositiveV.setTextColor(Color.BLACK)

            holder.btnPositiveV.setOnClickListener {
                dbHandler!!.updateUserVotePositive(items[position].id, userId, 1)
                dbHandler!!.updateUserVoteNegative(items[position].id, userId, 0)

                //  Suma uno a los votos positivos de la receta
                items[position].positive += 1
//                dbHandler!!.updateRecipePositiveVoteAdd(
//                    items[position].id,
//                    items[position].positive + 1
//                )

                //  Resta uno a los votos negativos de la receta
                items[position].negative -= 1
//                dbHandler!!.updateRecipeNegativeVoteRemove(
//                    items[position].id,
//                    items[position].negative - 1
//                )

                notifyItemChanged(position)
                notifyDataSetChanged()
            }

            holder.btnNegativeV.setOnClickListener {
                dbHandler!!.updateUserVoteNegative(items[position].id, userId, 0)

                //  Resta uno a los votos negativos de la receta
                items[position].negative -= 1
//                dbHandler!!.updateRecipeNegativeVoteRemove(
//                    items[position].id,
//                    items[position].negative - 1
//                )

                notifyItemChanged(position)
                notifyDataSetChanged()
            }

            dbHandler!!.close()
        }

        if (dbHandler!!.getPositiveVote(items[position].id, userId) == 1 && dbHandler!!.getNegativeVote(items[position].id, userId) == 0) {
            holder.btnPositiveV.setColorFilter(Color.RED)
            holder.btnNegativeV.setColorFilter(Color.BLACK)

            holder.btnNegativeV.setOnClickListener {
                dbHandler!!.updateUserVotePositive(items[position].id, userId, 0)
                dbHandler!!.updateUserVoteNegative(items[position].id, userId, 1)

                //  Suma uno a los votos negativos de la receta
                items[position].negative += 1
//                dbHandler!!.updateRecipeNegativeVoteAdd(
//                    items[position].id,
//                    items[position].negative + 1
//                )

                //  Resta uno a los votos positivos de la receta
                items[position].positive -= 1
//                dbHandler!!.updateRecipePositiveVoteRemove(
//                    items[position].id,
//                    items[position].positive - 1
//                )

                notifyItemChanged(position)
                notifyDataSetChanged()
            }

            holder.btnPositiveV.setOnClickListener {
                dbHandler!!.updateUserVotePositive(items[position].id, userId, 0)

                //  Resta uno a los votos positivos de la receta
                items[position].positive -= 1
//                dbHandler!!.updateRecipePositiveVoteRemove(
//                    items[position].id,
//                    items[position].positive - 1
//                )

                notifyItemChanged(position)
                notifyDataSetChanged()
            }

            dbHandler!!.close()
        }
        // O SE MEJORA O SE ELIMINA*************************************************************************************
        //**************************************************************************************************************
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

    var container = view.cv_all_recipes!!

}