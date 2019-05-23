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


// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 *
 */
class PerfilFragment : Fragment() {

    private var selectedPhotoUri: Uri? = null
    private var dbHandler: DataBaseHandler? = null
    private var dbFirebase = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth
    private var recipesFirebase = dbFirebase.collection("recipes")
    private var ingredientsFirebase = dbFirebase.collection("ingredients")
    private var imagesFirebase = dbFirebase.collection("images")
    private val date = SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault()).format(Date())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)


//        if ((activity as MainActivity).isNetworkConnected()){
//            auth = FirebaseAuth.getInstance()
//
//            // PROBAR A CREAR UNA CLASE(METODO) CON ESTO Y PASAR LA A TODOS LOS FRAGMENTS
//            recipesFirebase.whereEqualTo("userId", auth.currentUser!!.uid).get().addOnSuccessListener { documentSnapshot ->
//                val userRecipesRemove: ArrayList<RecipesModel> = dbHandler!!.getAllMyRecipesRemoved(dbHandler!!.getUserId()) // Recetas del usuario actual
//                val userRecipes: ArrayList<RecipesModel> = dbHandler!!.getAllMyRecipes(dbHandler!!.getUserId()) // Recetas del usuario actual
//                val userRecipesFirebase = documentSnapshot.toObjects(RecipesModel::class.java) // Recetas del usuario actual
//
//                var insertRecipe:Boolean = true
//                var countRecipe:Int = 0
//                for ((addRecipe, recipe) in userRecipes.withIndex()){
//                    for (recipeF in userRecipesFirebase){
//                        if (recipe.id == recipeF.id && recipe.date != recipeF.date){
//                            recipesFirebase.document(recipe.id).set(userRecipes[countRecipe])
//                            countRecipe++
//                        } else if (recipe.id == recipeF.id && recipe.date == recipeF.date){
//                            insertRecipe = false
//                            countRecipe++
//                        }
//                    }
//
//                    if (insertRecipe){
//                        recipesFirebase.document(recipe.id).set(userRecipes[addRecipe])
//                    }
//
//                    insertRecipe = true
//
//                    ingredientsFirebase.whereEqualTo("idRecipe", recipe.id).get().addOnSuccessListener {
//                        val userIngredientsFirebase = it.toObjects(IngredientsModel::class.java)
//                        val userIngredients: ArrayList<IngredientsModel> = dbHandler!!.getAllMyIngredients(recipe.id)
//
//                        var insertIngredient: Boolean = true
//                        var countIngredient:Int = 0
//                        for ((addIngredient, ingredient) in userIngredients.withIndex()){
//                            for (ingredientF in userIngredientsFirebase){
//                                if (ingredient.id == ingredientF.id && ingredient.date != ingredientF.date){
//                                    ingredientsFirebase.document(ingredient.id).set(userIngredients[countIngredient])
//                                    countIngredient++
//
//                                } else if (ingredient.id == ingredientF.id && ingredient.date == ingredientF.date) {
//                                    insertIngredient = false
//                                    countIngredient++
//                                }
//                            }
//
//                            if (insertIngredient){
//                                ingredientsFirebase.document(ingredient.id).set(userIngredients[addIngredient])
//                            }
//
//                            insertIngredient = true
//                        }
//                    }
//
//                    imagesFirebase.whereEqualTo("recipeId", recipe.id).get().addOnSuccessListener {
//                        val userImagesFirebase = it.toObjects(ImagesModel::class.java)
//                        val userImages: ArrayList<ImagesModel> = dbHandler!!.getAllMyImages(recipe.id)
//
//                        Log.d("imagen", "Total imagenes en receta, ${recipe.name}: ${userImagesFirebase.size} - En sqlite: ${userImages.size}")
//
//                        var insertImage: Boolean = true
//                        var countImages:Int = 0
//                        for ((addImage, imagePath) in userImages.withIndex()){
//                            for (imageF in userImagesFirebase){
//                                if (imagePath.id == imageF.id && imagePath.date != imageF.date){ // Si la imagen ha sido actualizada
//
//                                    imagesFirebase.document("imagePath$countImages-${imagePath.id}").set(userImages[countImages])
//
//                                    updateSaveImage(imagePath.imagePath, imagePath.recipeId, imagePath.name)
//
//                                    countImages++
//
//                                } else if (imagePath.id == imageF.id && imagePath.date == imageF.date) { // Si no, no se hace nada
//                                    insertImage = false
//                                    countImages++
//                                }
//                            }
//
//                            if (insertImage){ // Si la imagen no existe, se inserta
//                                Log.d("imagen", "Añadir imagen numero: $addImage con id: ${imagePath.id}")
//
//                                imagesFirebase.document(imagePath.id).set(userImages[addImage])
//
//                                updateSaveImage(imagePath.imagePath, imagePath.recipeId, imagePath.name)
//                            }
//
//                            insertImage = true
//                        }
//
//                    }
//
//                }
//
//                for (recipe in userRecipesRemove){
//                    if (recipe.remove == 1){
//                        val userIngredients: ArrayList<IngredientsModel> = dbHandler!!.getAllMyIngredients(recipe.id)
//                        val userImages: ArrayList<ImagesModel> = dbHandler!!.getAllMyImages(recipe.id)
//
//                        // Elimina la receta
//                        recipesFirebase.document(recipe.id).delete()
//                        dbHandler!!.removeRecipe(recipe.id)
//
//                        for (ingredient in userIngredients){
//                            // Elimina los ingredientes de la receta
//                            ingredientsFirebase.document(ingredient.id).delete()
//                            dbHandler!!.removeIngredient(ingredient.id)
//                        }
//
//                        for (imagePath in userImages) {
//                            // Elimina las imágenes de la receta
//                            imagesFirebase.document(imagePath.id).delete()
//                            //******************************************************************************************
//                            // NO ELIMINA LOS ARCHIVOS DE NINGUN LADO***************************************************
//                            val storage = FirebaseStorage.getInstance()
//                            // [START delete_file]
//                            // Create a storage reference from our app
//                            val storageRef = storage.reference
//                            // Create a reference to the file to delete
//                            val desertRef = storageRef.child("Images/recipes/${imagePath.recipeId}/${imagePath.name}.png")
//                            // Delete the file
//                            desertRef.delete().addOnSuccessListener {
//                                imagesFirebase.document(imagePath.id).delete()
//                                val myFile = File(imagePath.imagePath)
//                                myFile.delete()
//
//                                dbHandler!!.removeImage(imagePath.id)
//                            }.addOnFailureListener {
//                                // Uh-oh, an error occurred!
//                            }
//
//                        }
//
//
//                    }
//                }
//
//            }
//
//        }

    }

//    private fun updateSaveImage(imagePath: String, recipeId: String, imageName: String){
//        try{
//            val ref = FirebaseStorage.getInstance().getReference("/Images/recipes/$recipeId/$imageName.png")
//
//            val stream = FileInputStream(File(imagePath))
//
//            val uploadTask: UploadTask
//
//            uploadTask = ref.putStream(stream)
//
//            uploadTask
//                .addOnFailureListener {}
//                .addOnSuccessListener {}
//
//        } catch (e: ClassCastException){}
//    }

    override fun onDestroy() {
        super.onDestroy()
        (activity as MainActivity).updateFirebase()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inicializa Firebase Auth
        auth = FirebaseAuth.getInstance()

        //init db
        dbHandler = DataBaseHandler(this.context!!)

        //  Crea un directorio para las imagenes si no existe
//        getFile("Images/profile/${dbHandler!!.getUserId()}")


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

//                    R.id.nombre -> {
//
//                        true
//                    }
                    R.id.profile -> {
                        val intent = Intent(context, EditProfileActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)

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
