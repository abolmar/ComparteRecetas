package com.alejandro.comparterecetas

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
import com.alejandro.comparterecetas.adapters.MyRecipesAdapter
import com.alejandro.comparterecetas.database.DataBaseHandler
import com.alejandro.comparterecetas.login.LoginActivity
import com.alejandro.comparterecetas.models.RecipesModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_profile.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import com.bumptech.glide.Glide

/**
 * A simple [Fragment] subclass.
 *
 */
class PerfilFragment : Fragment() {

    private var dbHandler: DataBaseHandler? = null
    private var dbFirebase = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        if ((activity as MainActivity).isNetworkConnected()) (activity as MainActivity).updateFirebase() //?????????????
    }

//    override fun onDestroy() {
//        super.onDestroy()
//        (activity as MainActivity).updateFirebase()
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inicializa Firebase Auth
        auth = FirebaseAuth.getInstance()

        //init db
        dbHandler = DataBaseHandler(this.context!!)

        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_profile, container, false)

        try {
            if (dbHandler!!.getImageUserProfilePath(dbHandler!!.getUserId()) != ""){
                //  Saca la imagen de perfil de la carpeta local
                Glide.with(this)
                    .load(dbHandler!!.getImageUserProfilePath(dbHandler!!.getUserId()))
                    .fitCenter()
                    .centerCrop()
                    .into(view.profile_image)

            }
        } catch ( e: IllegalStateException){}

        val user: ArrayList<RecipesModel> = dbHandler!!.getAllMyRecipes(dbHandler!!.getUserId())

        view.rv_user_recipes.layoutManager = LinearLayoutManager(context)
        view.rv_user_recipes.layoutManager = GridLayoutManager(context, 1)
        view.rv_user_recipes.adapter = MyRecipesAdapter(user, context!!)

        //  Pone el nombre del usuario con sesion iniciada
        view.tv_nombre.text = dbHandler!!.getUserName()

        //  Hace que el botón "imageButton_threeDots" despliegue un menú
        view.imageButton_threeDots.setOnClickListener {
            val popupMenu = PopupMenu(context, it)

            popupMenu.setOnMenuItemClickListener {
                when(it.itemId){

                    R.id.profile -> {
                        val intent = Intent(context, EditProfileActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)

                        true
                    }

                    R.id.session -> {
                        if ((activity as MainActivity).isNetworkConnected()){

                            val userFirebase = auth.currentUser

                            userFirebase?.let {
                                val uid = userFirebase.uid
                                dbFirebase.collection("usersLogin").document(uid)
                                    .update("login", 0)
                                    .addOnSuccessListener {
                                        dbHandler!!.updateLogoutTableUserLogin()

                                        val intent = Intent(context, LoginActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        startActivity(intent)
                                    }
                            }
                        } else {
                            dbHandler!!.updateLogoutTableUserLogin()

                            val intent = Intent(context, LoginActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }

                        true
                    }

                    else -> false
                }
            }

            popupMenu.inflate(R.menu.menu_perfil)

            popupMenu.show()

        }

        //  Crear una nueva receta
        view.fab_nueva_receta.setOnClickListener {
            val intent = Intent(context, CRUDActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        return view
    }

}

// (activity as AppCompatActivity) => interesante - interesante - interesante - interesante - interesante - interesante - interesante
