package com.alejandro.comparterecetas

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.alejandro.comparterecetas.adapters.IngredientsAdapter
import com.alejandro.comparterecetas.database.DataBaseHandler
import com.alejandro.comparterecetas.models.ImagesModel
import com.alejandro.comparterecetas.models.Ingredients
import com.alejandro.comparterecetas.models.IngredientsModel
import com.alejandro.comparterecetas.models.RecipesModel
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_crud.*
import java.io.*
import java.lang.ClassCastException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CRUDActivity : AppCompatActivity() {

    private var dbFirebase = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth
    private var recipesFirebase = dbFirebase.collection("recipes") //  Crea una nueva colección en Firebase
    private var ingredientsFirebase = dbFirebase.collection("ingredients") //  Crea una nueva colección en Firebase
    private var imagesFirebase = dbFirebase.collection("images")
    private val date: String = SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault()).format(Date())
    private var dbHandler: DataBaseHandler? = null
    private val selectedIngredients: ArrayList<Ingredients> = ArrayList()
    private var selectedPhotoUri0: Uri? = null
    private var selectedPhotoUri1: Uri? = null
    private var selectedPhotoUri2: Uri? = null
    private var selectedPhotoUri3: Uri? = null
    private var selectedCategory: String = "" //  Guarda la selección del spinner
    private var rButtonchecked: Int = 0 //  Guarda la selección del radioButton (privada/pública)
    private val imagesView: ArrayList<ImageView> = ArrayList() //  Contiene las images seleccionadas en los ImageView
    private val savedImagePath: ArrayList<String> = ArrayList() // Contiene las rutas a las imágenes guardadas en el directorio "Images/recipes" del proyecto
    private val savedIngredients: ArrayList<String> = ArrayList() //  Contiene los ingredientes de la receta


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crud)

        // Inicializa Firebase Auth
        auth = FirebaseAuth.getInstance()

        //  Crea un directorio para las imagenes si no existe
//        getFile("Images/recipes")


        spinnerCategory()

        radioButtonChecked()

        //init db
        dbHandler = DataBaseHandler(this)

        //  Regresa al menú "perfil"
        btn_crud_back.setOnClickListener {
            backProfile()
        }

        //  Añade ingredientes a la receta
        btn_add.setOnClickListener {
            if (et_ingredient.text.toString() != "") {
                val reg = Ingredients(et_ingredient.text.toString())

                selectedIngredients.add(reg) //  Ingredientes mostrados en el recyclerView
                savedIngredients.add(et_ingredient.text.toString()) //  Ingredientes para guardar en la tabla "ingredients"

                rw_ingredient.layoutManager = LinearLayoutManager(this)
                rw_ingredient.layoutManager = GridLayoutManager(this, 1)
                rw_ingredient.adapter = IngredientsAdapter(selectedIngredients, savedIngredients, this)
                rw_ingredient.smoothScrollToPosition(selectedIngredients.size)

                et_ingredient.setText("")
            }

        }

        btn_crud_save.setOnClickListener {
            if (this.validation()!!) {
                if (!selectedCategory.contentEquals(" -- Elije una categoría -- ")) {
                    val recipe = RecipesModel()
                    val successRecipe: Boolean

                    if (et_hours.text.toString() != "" && et_minutes.text.toString() != "") {
                        //  Guarda la receta

                        recipe.id = "recipe-" + date
                        recipe.name = et_recipe_name.text.toString()
                        recipe.userId = dbHandler!!.getUserId()
                        recipe.timeH = et_hours.text.toString().toInt()
                        recipe.timeM = et_minutes.text.toString().toInt()
                        recipe.preparation = et_preparation.text.toString()
                        recipe.category = selectedCategory
                        recipe.type = rButtonchecked
                        recipe.date = date


                        successRecipe = dbHandler!!.addRecipe(recipe)

                        //  Guarda los ingredientes de la receta
                        saveIngredientsTable()

                        //  Guarda las imagenes de la receta
                        saveImagesTable()

                        if (successRecipe) {
                            if (isNetworkConnected()){
                                saveRecipeInFirebase(recipe)
                            } else {
                                Toast.makeText(this, "Sin conexión - NO FIREBASE DB", Toast.LENGTH_LONG).show()
                            }
                            backProfile()
                        }

                    } else if (et_hours.text.toString() == "" && et_minutes.text.toString() == "") {
                        Toast.makeText(this, "Debes indicar un tiempo aproximado de preparación", Toast.LENGTH_LONG)
                            .show()

                    } else if (et_hours.text.toString() == "") {

                        recipe.id = "recipe-" + date
                        recipe.name = et_recipe_name.text.toString()
                        recipe.userId = dbHandler!!.getUserId()
                        recipe.timeM = et_minutes.text.toString().toInt()
                        recipe.preparation = et_preparation.text.toString()
                        recipe.category = selectedCategory
                        recipe.type = rButtonchecked
                        recipe.date = date

                        successRecipe = dbHandler!!.addRecipe(recipe)

                        if (selectedIngredients.size < 4) {

                        }
                        //  Guarda los ingredientes de la receta
                        saveIngredientsTable()

                        //  Guarda las imagenes de la receta
                        saveImagesTable()

                        if (successRecipe) {
                            if (isNetworkConnected()){
                                saveRecipeInFirebase(recipe)
                            } else {
                                Toast.makeText(this, "Sin conexión - NO FIREBASE DB", Toast.LENGTH_LONG)
                                    .show()
                            }

                            backProfile()
                        }

                    } else if (et_minutes.text.toString() == "") {

                        recipe.id = "recipe-" + date
                        recipe.name = et_recipe_name.text.toString()
                        recipe.userId = dbHandler!!.getUserId()
                        recipe.timeH = et_hours.text.toString().toInt()
                        recipe.preparation = et_preparation.text.toString()
                        recipe.category = selectedCategory
                        recipe.type = rButtonchecked
                        recipe.date = date

                        successRecipe = dbHandler!!.addRecipe(recipe)

                        //  Guarda los ingredientes de la receta
                        saveIngredientsTable()

                        //  Guarda las imagenes de la receta
                        saveImagesTable()

                        if (successRecipe) {
                            if (isNetworkConnected()){
                                saveRecipeInFirebase(recipe)
                            } else {
                                Toast.makeText(this, "Sin conexión - NO FIREBASE DB", Toast.LENGTH_LONG)
                                    .show()
                            }

                            backProfile()
                        }
                    }

                } else {
                    Toast.makeText(this, "Selecciona una categoría", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Acede a al galeŕia de imágenes
        imageView_I.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        // Acede a al galeŕia de imágenes
        imageView_II.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }

        // Acede a al galeŕia de imágenes
        imageView_III.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 2)
        }

        // Acede a al galeŕia de imágenes
        imageView_IV.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 3)
        }

    }

    //  Muestra las imágenes seleccionadas de la galería en sus respectivos ImagenView
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            //  Procedemos a chequear qué imágen fué seleccionada...
            selectedPhotoUri0 = data.data

            val stream = ByteArrayOutputStream()
            MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri0)
                .compress(Bitmap.CompressFormat.JPEG, 50, stream)

            Glide.with(this)
                .load(stream.toByteArray())
                .into(imageView_I)


        } else if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoUri1 = data.data

            val stream = ByteArrayOutputStream()
            MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri1)
                .compress(Bitmap.CompressFormat.JPEG, 50, stream)

            Glide.with(this)
                .load(stream.toByteArray())
                .into(imageView_II)

        } else if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoUri2 = data.data

            val stream = ByteArrayOutputStream()
            MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri2)
                .compress(Bitmap.CompressFormat.JPEG, 50, stream)

            Glide.with(this)
                .load(stream.toByteArray())
                .into(imageView_III)

        } else if (requestCode == 3 && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoUri3 = data.data

            val stream = ByteArrayOutputStream()
            MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri3)
                .compress(Bitmap.CompressFormat.JPEG, 50, stream)

            Glide.with(this)
                .load(stream.toByteArray())
                .into(imageView_IV)

        } else {
            Toast.makeText(this, "Acepta los permisos para acceder a la galeria de imagesView", Toast.LENGTH_LONG)
                .show()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        backProfile()
    }

    private fun backProfile() {
        dbHandler!!.close()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("USER_PROFILE", true)
        startActivity(intent)
    }

    //  Comprueba que los editText no queden sin rellenar
    private fun validation(): Boolean? {
        var validate: Boolean? = null

        if (et_recipe_name.text.toString() != "" && et_preparation.text.toString() != "" && selectedIngredients.size >= 4) {
            validate = true

        } else if (et_recipe_name.text.toString() == "" && et_preparation.text.toString() == "") {
            validate = false
            Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_LONG).show()

        } else if (et_recipe_name.text.toString() == "") {
            validate = false
            Toast.makeText(this, "Rellena el campo nombre", Toast.LENGTH_LONG).show()

        } else if (et_preparation.text.toString() == "") {
            validate = false
            Toast.makeText(this, "Rellena el campo preparacion", Toast.LENGTH_LONG).show()

        } else if (selectedIngredients.size < 4) {
            validate = false
            Toast.makeText(this, "Número de ingredientes insuficiente", Toast.LENGTH_LONG).show()
        }

        return validate
    }

    private fun spinnerCategory() {
        val categories = arrayOf(" -- Elije una categoría -- ", "Comida", "Cena", "Merienda", "Postre")

        val adapter = object : ArrayAdapter<String>(
            this,
            R.layout.spinner_item,
            categories
        ) {
            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val tv = view as TextView

                if (position == 0) {
                    //  Colorea la primera posición de gris para crear un hint
                    tv.setTextColor(Color.GRAY)
                } else {
                    tv.setTextColor(Color.BLACK)
                }

                return view
            }
        }

        // Set the drop down view resource
        adapter.setDropDownViewResource(R.layout.spinner_item)

        // Finally, data bind the spinner object with dapter
        spn_crud_categories.adapter = adapter

        // Set an on item selected listener for spinner object
        spn_crud_categories.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                // Display the selected item text on text view
                selectedCategory = parent.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Another interface callback
            }
        }

    }

    private fun radioButtonChecked() {
        rb_public.setOnClickListener {
            rb_private.isChecked = false
            rButtonchecked = 1
        }

        rb_private.setOnClickListener {
            rb_public.isChecked = false
            rButtonchecked = 0
        }
    }

    //  Crea un directorio para las imagesView si no existe
    private fun getFile(name: String): File {
        val file = File(this.filesDir, name)
        if (!file.mkdirs()) {
            Log.e("", "Directory not created")
        }
        return File(this.filesDir, name)
    }

    //  Guarda las imagesView en el directorio ya creado
    @Throws(IOException::class)
    private fun saveFile(pictureBitmap: Bitmap, fileName: String, iDrecipe: String) {
        val fOut: OutputStream?
        val file = File(this.filesDir.toString() + "/Images/recipes/$iDrecipe", "$fileName.png")
        fOut = FileOutputStream(file)

        pictureBitmap.compress(Bitmap.CompressFormat.PNG, 80, fOut)

        fOut.flush()
        fOut.close()
    }

    //  Guarda las rutas hacia las imágenes en la base de datos
    private fun saveImagesTable() {
        val idRecipe = dbHandler!!.getRecipeId(dbHandler!!.getUserId(),et_recipe_name.text.toString())
        getFile("Images/recipes/$idRecipe")
        val image = ImagesModel()

        imagesView.add(imageView_I)
        imagesView.add(imageView_II)
        imagesView.add(imageView_III)
        imagesView.add(imageView_IV)

        var countImage = 0
        for (i in imagesView) {
            if (i.drawable != null) {
                try {

                    val bitmap = (i.drawable as BitmapDrawable).bitmap

                    //  Guarda las imágenes en el directorio "/Images/recipes/..."
                    saveFile(bitmap, countImage.toString(), idRecipe)

                    //  Guarda la ruta hacia la imagen
                    savedImagePath.add("${this.filesDir}/Images/recipes/$idRecipe/$countImage.png")

                    //  Sube las imágenes selecionadas a firebase
                    if (isNetworkConnected()){
                        val ref = FirebaseStorage.getInstance().getReference("/Images/recipes/$idRecipe/$countImage.png")

                        val baos = ByteArrayOutputStream()

                        bitmap.compress(Bitmap.CompressFormat.PNG, 80, baos)

                        val data = baos.toByteArray()

                        ref.putBytes(data)
                    }
                    countImage++
                } catch (e: ClassCastException) {}
            }

        }

        for ((count, i) in savedImagePath.withIndex()) {
            image.id = "imagePath$count-$date"
            image.recipeId = dbHandler!!.getRecipeId(dbHandler!!.getUserId(),et_recipe_name.text.toString())
            image.imagePath = i
            image.name = "$count"
            image.date = date

            dbHandler!!.addImageTableImages(image)

            if (isNetworkConnected()){
                imagesFirebase.document("imagePath$count-$date").set(image)
            }

        }
    }

    //  Guarda los ingredientes de la receta
    private fun saveIngredientsTable() {
        val ingredient = IngredientsModel()

        for ((count, i) in savedIngredients.withIndex()) {
            ingredient.id = "ingredient-$date"+count.toString()
            ingredient.idRecipe = dbHandler!!.getRecipeId(dbHandler!!.getUserId(),et_recipe_name.text.toString())
            ingredient.ingredientName = i
            ingredient.date = date

            dbHandler!!.addIngredientTableIngredients(ingredient)

            if (isNetworkConnected()){
                // Guarda los ingredientes en Firebase si hay conexión a internet
                ingredientsFirebase.document("ingredient-$date"+count.toString()).set(ingredient)
            }
        }

    }

    //  Guarda las recetas en Firebase
    private fun saveRecipeInFirebase(recipe: RecipesModel) {
        recipesFirebase.document("recipe-$date").set(recipe)
    }

    // Comprueba la conexión a internet
    private fun isNetworkConnected(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null
    }

}
