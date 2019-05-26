package com.alejandro.comparterecetas.adapters

import android.content.Context
import android.net.Uri
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.alejandro.comparterecetas.R
import com.alejandro.comparterecetas.models.ImagesModel
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.adapter_show_images.view.*

class ShowImagesAdapter(
    private val items: MutableList<ImagesModel>, val context: Context) :
    RecyclerView.Adapter<ViewHolderShowImages>() {
    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolderShowImages {
        return ViewHolderShowImages(LayoutInflater.from(context).inflate(R.layout.adapter_show_images, p0, false))
    }

    override fun onBindViewHolder(holder: ViewHolderShowImages, position: Int) {

        val ref = FirebaseStorage.getInstance().reference
        // Saca la imagen de la receta de firebase
        ref.child("/Images/recipes/${items[position].recipeId}/${items[position].name}.png").downloadUrl
            .addOnSuccessListener {
                try {
                        Glide.with(context)
                            .load(it)
                            .fitCenter()
                            .centerCrop()
                            .into(holder.imgShowImage)

                } catch (e: IllegalArgumentException) {}
            }
    }
}


class ViewHolderShowImages(view: View) : RecyclerView.ViewHolder(view) {
    val imgShowImage = view.img_show_image
}

