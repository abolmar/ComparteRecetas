package com.alejandro.comparterecetas

import android.content.Context
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import android.net.ConnectivityManager
import com.alejandro.comparterecetas.database.DataBaseHandler
import com.alejandro.comparterecetas.fragments.FavoritesFragment
import com.alejandro.comparterecetas.fragments.ProfileFragment
import com.alejandro.comparterecetas.fragments.RecipesFragment
import com.alejandro.comparterecetas.models.ImagesModel
import com.alejandro.comparterecetas.models.IngredientsModel
import com.alejandro.comparterecetas.models.RecipesModel
import com.alejandro.comparterecetas.models.UsersModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.io.File
import java.io.FileInputStream
import java.lang.ClassCastException


class MainActivity : AppCompatActivity() {

    private val manager = supportFragmentManager
    private var dbHandler: DataBaseHandler? = null
    private var dbFirebase = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth
    private var recipesFirebase = dbFirebase.collection("recipes")
    private var ingredientsFirebase = dbFirebase.collection("ingredients")
    private var imagesFirebase = dbFirebase.collection("images")
    private var usersLoginFirebase = dbFirebase.collection("usersLogin")
    private var position: Int? = 0

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_recetas -> {
                recipes(0)
                return@OnNavigationItemSelectedListener true
            }

            R.id.navigation_favoritas -> {
                favorites()
                return@OnNavigationItemSelectedListener true
            }

            R.id.navigation_perfil -> {
                profile()
                return@OnNavigationItemSelectedListener true
            }
        }

        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHandler = DataBaseHandler(this)

        val extras = intent.extras

        if (extras != null) {
            position = extras.getInt("position") // Posición de la receta pulsada en el recycler view
        }

        val intent = intent

        val favorites = intent.getBooleanExtra("USER_FAVORITES", false)
        val profile = intent.getBooleanExtra("USER_PROFILE", false)

        recipes(position)

        if (favorites){
            favorites()
            navigation.selectedItemId = R.id.navigation_favoritas
        }

        if (profile){
            profile()
            navigation.selectedItemId = R.id.navigation_perfil
        }

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    //  Elimina la cache al cerrar la aplicación
    override fun onDestroy() {
        super.onDestroy()
        val file = File("${this.cacheDir}")
        file.deleteRecursively()
    }


    private fun recipes(position:Int?){
        val args = Bundle()

        args.putInt("position", position!!)

        val transaction = manager.beginTransaction()
        val fragment = RecipesFragment()
        fragment.arguments = args

        transaction.replace(R.id.fragment_container, fragment).commit()
    }

    private fun favorites(){
        val transaction = manager.beginTransaction()
        val fragment = FavoritesFragment()

        manager.popBackStack()
        transaction.replace(R.id.fragment_container, fragment).addToBackStack(null).commit()
    }

    private fun profile(){
        val transaction = manager.beginTransaction()
        val fragment = ProfileFragment()

        manager.popBackStack()
        transaction.replace(R.id.fragment_container, fragment).addToBackStack(null).commit()
    }

    // Comprueba la conexión a internet
    fun isNetworkConnected(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null
    }

    // Al pulsar el botón de retroceso, siempre se marcara el botón recipes
    override fun onBackPressed() {
        super.onBackPressed()

        if (navigation.selectedItemId == R.id.navigation_recetas){
            finish()
        }

        navigation.selectedItemId = R.id.navigation_recetas
    }

    // Actualiza el contenido de Firebase en caso de que se haya creado, modificado o eliminado alguna receta estando sin conexión
    fun updateFirebase(){
        auth = FirebaseAuth.getInstance()

        usersLoginFirebase.whereEqualTo("id", auth.currentUser!!.uid).get().addOnSuccessListener {
            val userLoginF = it.toObjects(UsersModel::class.java)
            val userLoginImageName = dbHandler!!.getImageUserName(auth.currentUser!!.uid)
            val userImagePath = dbHandler!!.getImageUserProfilePath(auth.currentUser!!.uid)
            val userLoginName = dbHandler!!.getUserName()

            for (i in userLoginF){
                val oldImageName = i.imageName

                if (i.imageName != "0"){
                    if (userLoginImageName != i.imageName){

                        usersLoginFirebase.document(auth.currentUser!!.uid).update("imageName", userLoginImageName)

                        updateSaveImageUserProfile(userImagePath, auth.currentUser!!.uid, userLoginImageName)

                        val storage = FirebaseStorage.getInstance()
                        val storageRef = storage.reference
                        val desertRef = storageRef.child("Images/profiles/${auth.currentUser!!.uid}/$oldImageName.png")
                        desertRef.delete()

                    }
                }

                if (i.name != userLoginName){
                    usersLoginFirebase.document(auth.currentUser!!.uid).update("name", userLoginName)
                }
            }
        }

        recipesFirebase.whereEqualTo("userId", auth.currentUser!!.uid).get().addOnSuccessListener { userRecipe ->
            val userRecipesRemove: ArrayList<RecipesModel> = dbHandler!!.getAllMyRecipesRemoved(dbHandler!!.getUserId()) // Recetas "eliminadas" del usuario actual
            val userRecipes: ArrayList<RecipesModel> = dbHandler!!.getAllMyRecipes(dbHandler!!.getUserId()) // Recetas del usuario actual
            val userRecipesFirebase = userRecipe.toObjects(RecipesModel::class.java) // Recetas del usuario actual

            var insertRecipe = true
            var countRecipe = 0
            for ((addRecipe, recipe) in userRecipes.withIndex()){
                for (recipeF in userRecipesFirebase){
                    if (recipe.id == recipeF.id && recipe.date != recipeF.date){
                        recipesFirebase.document(recipe.id).set(userRecipes[countRecipe])
                        countRecipe++
                    } else if (recipe.id == recipeF.id && recipe.date == recipeF.date){
                        insertRecipe = false
                        countRecipe++
                    }
                }

                if (insertRecipe){
                    recipesFirebase.document(recipe.id).set(userRecipes[addRecipe])
                }

                insertRecipe = true

                ingredientsFirebase.whereEqualTo("idRecipe", recipe.id).get().addOnSuccessListener { recipeIngredients ->
                    val userIngredients: ArrayList<IngredientsModel> = dbHandler!!.getAllMyIngredients(recipe.id)
                    val userIngredientsFirebase = recipeIngredients.toObjects(IngredientsModel::class.java)

                    var insertIngredient = true
                    var countIngredient = 0

                    for ((addIngredient, ingredient) in userIngredients.withIndex()){
                        for (ingredientF in userIngredientsFirebase){
                            if (ingredient.id == ingredientF.id && ingredient.date != ingredientF.date){
                                ingredientsFirebase.document(ingredient.id).set(userIngredients[countIngredient])
                                countIngredient++

                            } else if (ingredient.id == ingredientF.id && ingredient.date == ingredientF.date) {
                                insertIngredient = false
                                countIngredient++
                            }
                        }

                        if (insertIngredient){
                            ingredientsFirebase.document(ingredient.id).set(userIngredients[addIngredient])
                        }

                        insertIngredient = true
                    }
                }

                imagesFirebase.whereEqualTo("recipeId", recipe.id).get().addOnSuccessListener { images ->
                    val recipeImages: ArrayList<ImagesModel> = dbHandler!!.getAllMyImages(recipe.id)
                    val recipeImagesFirebase = images.toObjects(ImagesModel::class.java)

                    var insertImage = true
                    var countImages = 0
                    for ((addImage, image) in recipeImages.withIndex()){
                        for (imageF in recipeImagesFirebase){
                            if (image.id == imageF.id && image.date != imageF.date){ // Si la imagen ha sido actualizada

                                imagesFirebase.document("imagePath$countImages-${image.id}").set(recipeImages[countImages])

                                updateSaveImageRecipe(image.imagePath, image.recipeId, image.name)

                                countImages++

                            } else if (image.id == imageF.id && image.date == imageF.date) { // Si no, no se hace nada
                                insertImage = false
                                countImages++
                            }
                        }

                        if (insertImage){ // Si la imagen no existe, se inserta

                            imagesFirebase.document(image.id).set(recipeImages[addImage])

                            updateSaveImageRecipe(image.imagePath, image.recipeId, image.name)
                        }

                        insertImage = true
                    }

                }
            }

            for (recipe in userRecipesRemove){
                if (recipe.remove == 1){
                    val userIngredients: ArrayList<IngredientsModel> = dbHandler!!.getAllMyIngredients(recipe.id)
                    val recipeImages: ArrayList<ImagesModel> = dbHandler!!.getAllMyImages(recipe.id)

                    // Elimina la receta
                    recipesFirebase.document(recipe.id).delete()
                    dbHandler!!.removeRecipe(recipe.id)

                    for (ingredient in userIngredients){
                        // Elimina los ingredientes de la receta
                        ingredientsFirebase.document(ingredient.id).delete()
                        dbHandler!!.removeIngredient(ingredient.id)
                    }

                    val myF = File("${this.filesDir}/Images/recipes/${recipe.id}")
                    myF.deleteRecursively()

                    for (image in recipeImages) {
                        val storage = FirebaseStorage.getInstance()
                        val storageRef = storage.reference
                        val desertRef = storageRef.child("Images/recipes/${image.recipeId}/${image.name}.png")
                        //  Si se han eliminado correctamente los archivo en Firestore, se procederá a eliminar los datos de las bases de datos
                        desertRef.delete().addOnSuccessListener {
                            imagesFirebase.document(image.id).delete()
                            dbHandler!!.removeImage(image.id)
                        }.addOnFailureListener {

                        }
                    }
                }
            }
        }
    }

    // Actualiza o inserta imágenes en la carpeta de las recetas
    private fun updateSaveImageRecipe(imagePath: String, recipeId: String, imageName: String){
        try{
            val ref = FirebaseStorage.getInstance().getReference("/Images/recipes/$recipeId/$imageName.png")

            val stream = FileInputStream(File(imagePath))

            val uploadTask: UploadTask

            uploadTask = ref.putStream(stream)

            uploadTask
                .addOnFailureListener {}
                .addOnSuccessListener {}

        } catch (e: ClassCastException){}
    }

    // Actualiza o inserta imágenes en la carpeta del perfil
    private fun updateSaveImageUserProfile(imagePath: String, userId: String, imageName: String){
        try{
            val ref = FirebaseStorage.getInstance().getReference("/Images/profile/$userId/$imageName.png")

            val stream = FileInputStream(File(imagePath))

            val uploadTask: UploadTask

            uploadTask = ref.putStream(stream)

            uploadTask
                .addOnFailureListener {}
                .addOnSuccessListener {}

        } catch (e: ClassCastException){}
    }
}
