package com.alejandro.comparterecetas

import android.content.Context
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import android.net.ConnectivityManager
import android.util.Log
import com.alejandro.comparterecetas.database.DataBaseHandler
import com.alejandro.comparterecetas.models.ImagesModel
import com.alejandro.comparterecetas.models.IngredientsModel
import com.alejandro.comparterecetas.models.RecipesModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import java.io.File
import java.io.FileInputStream
import java.lang.ClassCastException


class MainActivity : AppCompatActivity() {

    private val manager = supportFragmentManager
    //*******************************************************//
    private var dbHandler: DataBaseHandler? = null
    private var dbFirebase = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth
    private var recipesFirebase = dbFirebase.collection("recipes")
    private var ingredientsFirebase = dbFirebase.collection("ingredients")
    private var imagesFirebase = dbFirebase.collection("images")

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_recetas -> {
                recetas()
                return@OnNavigationItemSelectedListener true
            }

            R.id.navigation_favoritas -> {
                favoritas()
                return@OnNavigationItemSelectedListener true
            }

            R.id.navigation_perfil -> {
                perfil()
                return@OnNavigationItemSelectedListener true
            }
        }

        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHandler = DataBaseHandler(this)

        val intent = intent
        val perfil = intent.getBooleanExtra("USER_PROFILE", false)

        if (perfil){
            perfil()
            navigation.selectedItemId = R.id.navigation_perfil
        } else {
            recetas()
        }
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

    }

    private fun recetas(){
//        if (isNetworkConnected()){
//            updateFirebase()
//        }
        val transaction = manager.beginTransaction()
        val fragment = RecetasFragment()

        transaction.replace(R.id.fragment_container, fragment).commit()
    }

    private fun favoritas(){
        val transaction = manager.beginTransaction()
        val fragment = FavoritasFragment()

        manager.popBackStack()
        transaction.replace(R.id.fragment_container, fragment).addToBackStack(null).commit()
    }

    private fun perfil(){
        if (isNetworkConnected()){
            updateFirebase()
        }
        val transaction = manager.beginTransaction()
        val fragment = PerfilFragment()

        manager.popBackStack()  //--> Sustitulle a "manager.popBackStackImmediate()", el que está puesto funciona de maravilla, pero por si acaso aquí está el otro -> BORRAME CUANDO ACABES!!!
        transaction.replace(R.id.fragment_container, fragment).addToBackStack(null).commit()
    }

    // Comprueba la conexión a internet
    fun isNetworkConnected(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null
    }

    // Al pulsar el botón de retroceso, siempre se marcara el botón recetas
    override fun onBackPressed() {
        super.onBackPressed()
        navigation.selectedItemId = R.id.navigation_recetas
    }

    fun updateFirebase(){
        auth = FirebaseAuth.getInstance()

        recipesFirebase.whereEqualTo("userId", auth.currentUser!!.uid).get().addOnSuccessListener { documentSnapshot ->
            val userRecipesRemove: ArrayList<RecipesModel> = dbHandler!!.getAllMyRecipesRemoved(dbHandler!!.getUserId()) // Recetas del usuario actual
            val userRecipes: ArrayList<RecipesModel> = dbHandler!!.getAllMyRecipes(dbHandler!!.getUserId()) // Recetas del usuario actual
            val userRecipesFirebase = documentSnapshot.toObjects(RecipesModel::class.java) // Recetas del usuario actual

            var insertRecipe:Boolean = true
            var countRecipe:Int = 0
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

                ingredientsFirebase.whereEqualTo("idRecipe", recipe.id).get().addOnSuccessListener {
                    val userIngredientsFirebase = it.toObjects(IngredientsModel::class.java)
                    val userIngredients: ArrayList<IngredientsModel> = dbHandler!!.getAllMyIngredients(recipe.id)

                    var insertIngredient: Boolean = true
                    var countIngredient:Int = 0
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

                imagesFirebase.whereEqualTo("recipeId", recipe.id).get().addOnSuccessListener {
                    val recipeImagesFirebase = it.toObjects(ImagesModel::class.java)
                    val recipeImages: ArrayList<ImagesModel> = dbHandler!!.getAllMyImages(recipe.id)

                    Log.d("imagen", "Total imagenes en receta, ${recipe.name}: ${recipeImagesFirebase.size} - En sqlite: ${recipeImages.size}")

                    var insertImage: Boolean = true
                    var countImages:Int = 0
                    for ((addImage, image) in recipeImages.withIndex()){
                        for (imageF in recipeImagesFirebase){
                            if (image.id == imageF.id && image.date != imageF.date){ // Si la imagen ha sido actualizada

                                imagesFirebase.document("image$countImages-${image.id}").set(recipeImages[countImages])

                                updateSaveImage(image.imagePath, image.recipeId, image.name)

                                countImages++

                            } else if (image.id == imageF.id && image.date == imageF.date) { // Si no, no se hace nada
                                insertImage = false
                                countImages++
                            }
                        }

                        if (insertImage){ // Si la imagen no existe, se inserta
                            Log.d("imagen", "Añadir imagen numero: $addImage con id: ${image.id}")

                            imagesFirebase.document(image.id).set(recipeImages[addImage])

                            updateSaveImage(image.imagePath, image.recipeId, image.name)
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
                            Log.d("capullo", "Lo borro todo!!!!!!")
                        }.addOnFailureListener {
                            Log.d("capullo", "No borro una mierda")
                        }

                    }


                }
            }

        }
    }

    private fun updateSaveImage(imagePath: String, recipeId: String, imageName: String){
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

}
