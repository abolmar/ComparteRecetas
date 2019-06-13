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
//    private lateinit var auth: FirebaseAuth
    private var recipesFirebase = dbFirebase.collection("recipes") //  Crea una nueva colección en Firebase
    private var ingredientsFirebase = dbFirebase.collection("ingredients") //  Crea una nueva colección en Firebase
    private var imagesFirebase = dbFirebase.collection("images") //  Crea una nueva colección en Firebase
    private val date: String = SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault()).format(Date())
    private var dbHandler: DataBaseHandler? = null
    private var selectedPhotoUri0: Uri? = null
    private var selectedPhotoUri1: Uri? = null
    private var selectedPhotoUri2: Uri? = null
    private var selectedPhotoUri3: Uri? = null
    private var selectedCategory: String = "" //  Guarda la selección del spinner
    private var rButtonchecked: Int = 0 //  Guarda la selección del radioButton (privada/pública)
    private val imagesView: ArrayList<ImageView> = ArrayList() //  Contiene las images seleccionadas en los ImageView
    private val savedImagePath: ArrayList<String> = ArrayList() // Contiene las rutas a las imágenes guardadas en el directorio "Images/recipes" del proyecto
    private val selectedIngredients: ArrayList<Ingredients> = ArrayList() //  Contiene los ingredientes de la receta
    private var createOrEdit: String? = ""
    private var addOrEdit: String? = ""
    private var recipeId:String? = ""
    private var recipeName: String? = ""
    private var recipeCategory: String? = ""
    private var recipeType: Int? = 0
    private var recipeHour: Int? = 0
    private var recipeMinute: Int? = 0
    private var recipePreparation: String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crud)

        //init db
        dbHandler = DataBaseHandler(this)

        val extras = intent.extras

        if (extras != null){
            createOrEdit = extras.getString("createOrEdit")
            addOrEdit = extras.getString("load")
            recipeId = extras.getString("recipeId")
            recipeName = extras.getString("recipeName")
            recipeCategory = extras.getString("category")
            recipeType = extras.getInt("type")
            recipeHour = extras.getInt("hours")
            recipeMinute = extras.getInt("minutes")
            recipePreparation = extras.getString("preparation")
        }

        tv_create_or_edit_recipe.text = createOrEdit

        if (addOrEdit == "edit"){
            et_recipe_name.setText(recipeName)
            et_hours.setText("$recipeHour")
            et_minutes.setText("$recipeMinute")
            et_preparation.setText(recipePreparation)


            val userIngredients: ArrayList<IngredientsModel> = dbHandler!!.getAllMyIngredients(recipeId)

            for (i in userIngredients){
                val reg = Ingredients(
                    i.ingredientName
                )
                selectedIngredients.add(reg)
                rw_ingredient.layoutManager = LinearLayoutManager(this)
                rw_ingredient.layoutManager = GridLayoutManager(this, 1)
                rw_ingredient.adapter = IngredientsAdapter(selectedIngredients, this)
            }

            val userImages: ArrayList<ImagesModel> = dbHandler!!.getAllMyImages(recipeId)

            val imageViewArray: ArrayList<ImageView> = ArrayList()
            imageViewArray.add(imageView_I)
            imageViewArray.add(imageView_II)
            imageViewArray.add(imageView_III)
            imageViewArray.add(imageView_IV)

            val imageButtonArray: ArrayList<ImageButton> = ArrayList()
            imageButtonArray.add(btn_del_imageView_I)
            imageButtonArray.add(btn_del_imageView_II)
            imageButtonArray.add(btn_del_imageView_III)
            imageButtonArray.add(btn_del_imageView_IV)

            for ((count, i) in userImages.withIndex()){
                Glide.with(this).load(i.imagePath).into(imageViewArray[count])
                imageButtonArray[count].visibility = View.VISIBLE
            }

            // Se les da un valor a las variables de tipo Uri
            when(userImages.size){
                1->{
                    selectedPhotoUri0 = Uri.parse("0")
                }
                2->{
                    selectedPhotoUri0 = Uri.parse("0")
                    selectedPhotoUri1 = Uri.parse("0")
                }
                3->{
                    selectedPhotoUri0 = Uri.parse("0")
                    selectedPhotoUri1 = Uri.parse("0")
                    selectedPhotoUri2 = Uri.parse("0")
                }
                4->{
                    selectedPhotoUri0 = Uri.parse("0")
                    selectedPhotoUri1 = Uri.parse("0")
                    selectedPhotoUri2 = Uri.parse("0")
                    selectedPhotoUri3 = Uri.parse("0")
                }
            }
        }

        spinnerCategory()

        radioButtonChecked()


        //  Regresa al menú "perfil"
        btn_crud_back.setOnClickListener {
            backtoProfile()
        }

        btn_del_imageView_I.setOnClickListener {
            selectedPhotoUri0 = null
            Glide.with(this).load(R.drawable.ic_add_a_new_photo_100dp).into(imageView_I)
            btn_del_imageView_I.visibility = View.INVISIBLE
        }

        btn_del_imageView_II.setOnClickListener {
            selectedPhotoUri1 = null
            Glide.with(this).load(R.drawable.ic_add_a_new_photo_100dp).into(imageView_II)
            btn_del_imageView_II.visibility = View.INVISIBLE
        }

        btn_del_imageView_III.setOnClickListener {
            selectedPhotoUri2 = null
            Glide.with(this).load(R.drawable.ic_add_a_new_photo_100dp).into(imageView_III)
            btn_del_imageView_III.visibility = View.INVISIBLE
        }

        btn_del_imageView_IV.setOnClickListener {
            selectedPhotoUri3 = null
            Glide.with(this).load(R.drawable.ic_add_a_new_photo_100dp).into(imageView_IV)
            btn_del_imageView_IV.visibility = View.INVISIBLE
        }

        //  Añade ingredientes a la receta
        btn_add_ingredient.setOnClickListener {
            if (et_ingredient.text.toString() != "") {

                val reg = Ingredients(et_ingredient.text.toString())
                selectedIngredients.add(reg) //  Ingredientes mostrados en el recyclerView

                rw_ingredient.layoutManager = LinearLayoutManager(this)
                rw_ingredient.layoutManager = GridLayoutManager(this, 1)
                rw_ingredient.adapter = IngredientsAdapter(selectedIngredients, this)
                rw_ingredient.smoothScrollToPosition(selectedIngredients.size)

                et_ingredient.setText("")
            }

        }

        //  Guarda la receta
        btn_crud_save.setOnClickListener {
            if (this.validation()!! && imageSelected()) {
                if (!selectedCategory.contentEquals(" -- Elije una categoría -- ")) {
                    val recipe = RecipesModel()
                    val successRecipe: Boolean

                    if (et_hours.text.toString() != "" && et_minutes.text.toString() != "") {

                        if (addOrEdit != "edit"){

                            recipe.id = "recipe-$date"
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
                                }
                                backtoProfile()
                            }

                        } else {
                            // ACTUALIZAR LA RECETA, LOS INGREDIENTES Y LAS FOTOS!!!
                            successRecipe = dbHandler!!.updateUserRecipe(recipeId,
                                et_recipe_name.text.toString(),
                                selectedCategory,
                                rButtonchecked,
                                et_hours.text.toString().toInt(),
                                et_minutes.text.toString().toInt(),
                                et_preparation.text.toString(),
                                date
                            )

                            //  Edita y guarda los ingredientes de la receta
                            editIngredientsTable()
                            saveIngredientsTable()

                            //  Edita y guarda las imagenes de la receta
                            editImagesTable()
                            saveImagesTable()

                            if (successRecipe){
                                if (isNetworkConnected()){
                                    updateRecipeFirebase(recipeId,
                                        et_recipe_name.text.toString(),
                                        selectedCategory,
                                        rButtonchecked,
                                        et_hours.text.toString().toInt(),
                                        et_minutes.text.toString().toInt(),
                                        et_preparation.text.toString(),
                                        date
                                    )
                                }

                                backtoProfile()
                            }
                        }

                    } else if (et_hours.text.toString() == "" && et_minutes.text.toString() == "") {
                        Toast.makeText(this, "Debes indicar un tiempo aproximado de preparación", Toast.LENGTH_LONG)
                            .show()

                    } else if (et_hours.text.toString() == "") {

                        if (addOrEdit != "edit"){
                            recipe.id = "recipe-$date"
                            recipe.name = et_recipe_name.text.toString()
                            recipe.userId = dbHandler!!.getUserId()
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
                                    Toast.makeText(this, "Sin conexión - NO FIREBASE DB", Toast.LENGTH_LONG)
                                        .show()
                                }

                                backtoProfile()
                            }

                        } else {
                            // ACTUALIZAR LA RECETA, LOS INGREDIENTES Y LAS FOTOS!!!
                            successRecipe = dbHandler!!.updateUserRecipe(recipeId,
                                et_recipe_name.text.toString(),
                                selectedCategory,
                                rButtonchecked,
                                0,
                                et_minutes.text.toString().toInt(),
                                et_preparation.text.toString(),
                                date
                            )

                            //  Edita y guarda los ingredientes de la receta
                            editIngredientsTable()
                            saveIngredientsTable()

                            //  Edita y guarda las imagenes de la receta
                            editImagesTable()
                            saveImagesTable()

                            if (successRecipe){
                                if (isNetworkConnected()){
                                    updateRecipeFirebase(recipeId,
                                        et_recipe_name.text.toString(),
                                        selectedCategory,
                                        rButtonchecked,
                                        0,
                                        et_minutes.text.toString().toInt(),
                                        et_preparation.text.toString(),
                                        date
                                    )
                                }

                                backtoProfile()
                            }
                        }


                    } else if (et_minutes.text.toString() == "") {

                        if (addOrEdit != "edit"){
                            recipe.id = "recipe-$date"
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

                                backtoProfile()
                            }
                        } else {
                            // ACTUALIZAR LA RECETA, LOS INGREDIENTES Y LAS FOTOS!!!
                            successRecipe = dbHandler!!.updateUserRecipe(recipeId,
                                et_recipe_name.text.toString(),
                                selectedCategory,
                                rButtonchecked,
                                et_hours.text.toString().toInt(),
                                0,
                                et_preparation.text.toString(),
                                date
                            )

                            //  Edita y guarda los ingredientes de la receta
                            editIngredientsTable()
                            saveIngredientsTable()

                            //  Edita y guarda las imagenes de la receta
                            editImagesTable()
                            saveImagesTable()

                            if (successRecipe){
                                if (isNetworkConnected()){
                                    updateRecipeFirebase(recipeId,
                                        et_recipe_name.text.toString(),
                                        selectedCategory,
                                        rButtonchecked,
                                        et_hours.text.toString().toInt(),
                                        0,
                                        et_preparation.text.toString(),
                                        date
                                    )
                                }

                                backtoProfile()
                            }
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

            btn_del_imageView_I.visibility = View.VISIBLE

        } else if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoUri1 = data.data

            val stream = ByteArrayOutputStream()
            MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri1)
                .compress(Bitmap.CompressFormat.JPEG, 50, stream)

            Glide.with(this)
                .load(stream.toByteArray())
                .into(imageView_II)

            btn_del_imageView_II.visibility = View.VISIBLE

        } else if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoUri2 = data.data

            val stream = ByteArrayOutputStream()
            MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri2)
                .compress(Bitmap.CompressFormat.JPEG, 50, stream)

            Glide.with(this)
                .load(stream.toByteArray())
                .into(imageView_III)

            btn_del_imageView_III.visibility = View.VISIBLE

        } else if (requestCode == 3 && resultCode == Activity.RESULT_OK && data != null) {
            selectedPhotoUri3 = data.data

            val stream = ByteArrayOutputStream()
            MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri3)
                .compress(Bitmap.CompressFormat.JPEG, 50, stream)

            Glide.with(this)
                .load(stream.toByteArray())
                .into(imageView_IV)

            btn_del_imageView_IV.visibility = View.VISIBLE

        } else {
            Toast.makeText(this, "Acepta los permisos para acceder a la galeria de imagesView", Toast.LENGTH_LONG)
                .show()
        }
    }

    //  Comprueba que los editText no queden sin rellenar
    private fun validation(): Boolean? {
        var validate: Boolean? = null

        if (et_recipe_name.text.toString() != "" && et_preparation.text.toString() != "" && selectedIngredients.size >= 2) {
            validate = true

        } else if (et_recipe_name.text.toString() == "" && et_preparation.text.toString() == "") {
            validate = false
            Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_SHORT).show()

        } else if (et_recipe_name.text.toString() == "") {
            validate = false
            Toast.makeText(this, "Rellena el campo nombre", Toast.LENGTH_SHORT).show()

        } else if (et_preparation.text.toString() == "") {
            validate = false
            Toast.makeText(this, "Rellena el campo preparacion", Toast.LENGTH_SHORT).show()

        } else if (selectedIngredients.size < 2) {
            validate = false
            Toast.makeText(this, "Número de ingredientes insuficiente", Toast.LENGTH_SHORT).show()
        }

        return validate
    }

    //  Comprueba que se elija al menos una imagen de la galería
    private fun imageSelected(): Boolean {
        val validate: Boolean
        if (selectedPhotoUri0 == null && selectedPhotoUri1 == null && selectedPhotoUri2 == null && selectedPhotoUri3 == null ){
            validate = false
            Toast.makeText(this, "Selecciona al menos una imágen para la receta", Toast.LENGTH_LONG).show()
        } else {
            validate = true
        }

        return  validate
    }

    //  Guarda la categoría a la que pertenecerá la receta
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

        if (addOrEdit != "edit") {
            // Set an on item selected listener for spinner object
            spn_crud_categories.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    // Display the selected item text on text view
                    selectedCategory = parent.getItemAtPosition(position).toString()
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

        } else {
            when (recipeCategory) {
                "Comida" -> {
                    spn_crud_categories.setSelection(1)
                }
                "Cena" -> {
                    spn_crud_categories.setSelection(2)
                }
                "Merienda" -> {
                    spn_crud_categories.setSelection(3)
                }
                "Postre" -> {
                    spn_crud_categories.setSelection(4)
                }
            }

            spn_crud_categories.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                    // Display the selected item text on text view
                    selectedCategory = parent.getItemAtPosition(position).toString()
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }
    }

    //  Comprueba si la receta es privada o pública
    private fun radioButtonChecked() {
        if (addOrEdit == "edit") {
            when(recipeType){
                0->{
                    rb_private.isChecked = true
                    rButtonchecked = 0
                }
                1->{
                    rb_public.isChecked = true
                    rButtonchecked = 1
                }
            }
        } else {
            rb_private.isChecked = true
        }

        rb_public.setOnClickListener {
            rb_private.isChecked = false
            rButtonchecked = 1
        }

        rb_private.setOnClickListener {
            rb_public.isChecked = false
            rButtonchecked = 0
        }

    }

    //  Crea un directorio para las imágenes si no existe
    private fun getFile(name: String): File {
        val file = File(this.filesDir, name)
        if (!file.mkdirs()) {
            Log.e("", "Directory not created")
        }
        return File(this.filesDir, name)
    }

    //  Guarda las imágenes en el directorio ya creado
    @Throws(IOException::class)
    private fun saveFile(pictureBitmap: Bitmap, fileName: String, idRecipe: String) {
        val fOut: OutputStream?
        val file = File(this.filesDir.toString() + "/Images/recipes/$idRecipe", "$fileName.png")
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

        if (selectedPhotoUri0 != null){
            imagesView.add(imageView_I)
        }
        if (selectedPhotoUri1 != null){
            imagesView.add(imageView_II)
        }
        if (selectedPhotoUri2 != null){
            imagesView.add(imageView_III)
        }
        if (selectedPhotoUri3 != null){
            imagesView.add(imageView_IV)
        }

        var countImage = 0
        for (i in imagesView) {
            if (i.drawable != null) {
                try {

                    val bitmap = (i.drawable as BitmapDrawable).bitmap

                    //  Guarda las imágenes en el directorio "/Images/recipes/..."
                    saveFile(bitmap, "$date$countImage", idRecipe)

                    //  Guarda la ruta hacia la imagen
                    savedImagePath.add("${this.filesDir}/Images/recipes/$idRecipe/$date$countImage.png")

                    //  Sube las imágenes selecionadas a firebase
                    if (isNetworkConnected()){
                        val ref = FirebaseStorage.getInstance().getReference("/Images/recipes/$idRecipe/$date$countImage.png")

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
            image.name = "$date$count"
            image.date = date

            dbHandler!!.addImageTableImages(image)

            if (isNetworkConnected()){
                imagesFirebase.document("imagePath$count-$date").set(image)
            }

        }
    }

    //  Edita las rutas hacia las imágenes en la base de datos
    private fun editImagesTable() {
        val recipeImages: ArrayList<ImagesModel> = dbHandler!!.getAllMyImages(recipeId)

        val myF = File("${this.filesDir}/Images/recipes/$recipeId")
        myF.deleteRecursively()

        for (image in recipeImages) {
            val storage = FirebaseStorage.getInstance()
            val imagesRef = storage.reference.child("Images/recipes/${image.recipeId}/${image.name}.png")

            imagesFirebase.document(image.id).delete()
            dbHandler!!.removeImage(image.id)

            imagesRef.delete()

        }
    }

    //  Guarda los ingredientes de la receta
    private fun saveIngredientsTable() {
        val ingredient = IngredientsModel()
        val recipeId = dbHandler!!.getRecipeId(dbHandler!!.getUserId(),et_recipe_name.text.toString())

        for ((count, i) in selectedIngredients.withIndex()) {
            ingredient.id = "ingredient-$date$count"
            ingredient.idRecipe = recipeId
            ingredient.ingredientName = i.ingredient
            ingredient.date = date

            dbHandler!!.addIngredientTableIngredients(ingredient)

            if (isNetworkConnected()){
                // Guarda los ingredientes en Firebase si hay conexión a internet
                ingredientsFirebase.document("ingredient-$date$count").set(ingredient)
            }
        }
    }

    //  Edita los ingredientes de la receta
    private fun editIngredientsTable() {
        val recipeId = dbHandler!!.getRecipeId(dbHandler!!.getUserId(),et_recipe_name.text.toString())
        val userIngredients: ArrayList<IngredientsModel> = dbHandler!!.getAllMyIngredients(recipeId)

        for (i in userIngredients) {
            ingredientsFirebase.document(i.id).delete()
            dbHandler!!.removeIngredient(i.id)
        }
    }

    //  Guarda las recetas en Firebase
    private fun saveRecipeInFirebase(recipe: RecipesModel) {
        recipesFirebase.document("recipe-$date").set(recipe)
    }

    //  Actualiza la receta en Firebase
    private fun updateRecipeFirebase(id: String?, name: String, category: String, type: Int, hour: Int, minute: Int, preparation: String, date: String){
        recipesFirebase.document(id.toString()).update(
            "name", name,
            "category", category,
            "type", type,
            "timeH", hour,
            "timeM", minute,
            "preparation", preparation,
            "date", date
        )
    }

    // Comprueba la conexión a internet
    private fun isNetworkConnected(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null
    }

    override fun onBackPressed() {
        super.onBackPressed()
        backtoProfile()
    }

    private fun backtoProfile() {
        dbHandler!!.close()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("USER_PROFILE", true)
        startActivity(intent)
    }

}
