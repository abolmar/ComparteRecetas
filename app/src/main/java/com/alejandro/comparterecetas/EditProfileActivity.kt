package com.alejandro.comparterecetas

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.alejandro.comparterecetas.database.DataBaseHandler
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import kotlinx.android.synthetic.main.activity_edit_profile.*
import java.io.*
import java.lang.NullPointerException
import java.text.SimpleDateFormat
import java.util.*

class EditProfileActivity : AppCompatActivity() {

    private var selectedPhotoUri: Uri? = null
    private var dbFirebase = FirebaseFirestore.getInstance()
    private var dbHandler: DataBaseHandler? = null
    private lateinit var auth: FirebaseAuth
    private var usersLogin = dbFirebase.collection("usersLogin")
    private val date = SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.getDefault()).format(Date())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // Inicializa Firebase Auth
        auth = FirebaseAuth.getInstance()

        //init db
        dbHandler = DataBaseHandler(this)

        //  Crea un directorio para las imagenes si no existe
        getFile("Images/profile/${dbHandler!!.getUserId()}")


        change_name_edit_profile.setText(dbHandler!!.getUserName())


        try {
            if (dbHandler!!.getImageUserProfilePath(dbHandler!!.getUserId()) != "") {
                //  Saca la imagen de perfil de la carpeta local
                try {
                    Glide.with(this)
                        .load(dbHandler!!.getImageUserProfilePath(dbHandler!!.getUserId()))
                        .fitCenter()
                        .centerCrop()
                        .into(img_edit_profile)
                } catch (e: StorageException) {
                }
            }
        } catch (e: IllegalStateException) {}

        //  Selecciona una imagen para la foto de perfil
        change_image_edit_profile.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }



        save_edit_profile.setOnClickListener {
            val myProfile = File("${this.filesDir}/Images/profile/${dbHandler!!.getUserId()}")

            try {
                for (i in myProfile.listFiles()) {
                    i.delete()
                }
            } catch (e: NullPointerException){}

            saveProfileImages()

            when {
                change_name_edit_profile.text.toString() != dbHandler!!.getUserName() -> {
                    dbHandler!!.updateUserLoginName(dbHandler!!.getUserId(), change_name_edit_profile.text.toString())

                    if (isNetworkConnected()) {
                        usersLogin.document(dbHandler!!.getUserId()).update("name", change_name_edit_profile.text.toString())
                    }

                    backProfile()

                }
                change_name_edit_profile.text.toString().isEmpty() -> Toast.makeText(this, "El campo 'Nombre' no puede quedar vacío", Toast.LENGTH_SHORT).show()

                else -> backProfile()
            }


        }

        btn_edit_profile_back.setOnClickListener {
            backProfile()
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            //  Procedemos a chequear qué imágen fué seleccionada...
            Log.d("RegisterActivity", "Foto seleccionada")

            selectedPhotoUri = data.data

            val stream = ByteArrayOutputStream()
            MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)
                .compress(Bitmap.CompressFormat.JPEG, 50, stream)

            Glide.with(this)
                .load(stream.toByteArray())
                .into(img_edit_profile)

        } else {
            Log.e("", "Ninguna imágen a sido seleccionada")
        }
    }

    //  Crea un directorio para la imagen de perfil si no existe
    private fun getFile(name: String): File {
        val file = File(this.filesDir, name)
        if (!file.mkdirs()) {
            Log.e("", "Directory not created")
        }
        return File(this.filesDir, name)
    }


    //  Guarda las imagesView en el directorio ya creado
    @Throws(IOException::class)
    private fun saveFile(pictureBitmap: Bitmap, name: String) {
        val fOut: OutputStream?

        val file = File(this.filesDir.toString() + "/Images/profile/${dbHandler!!.getUserId()}", "$name.png")
        fOut = FileOutputStream(file)

        pictureBitmap.compress(Bitmap.CompressFormat.PNG, 80, fOut)

        fOut.flush()
        fOut.close()
    }

    //  Guarda las imágen de perfil en el directorio local y en Firebase
    private fun saveProfileImages() {
        if (img_edit_profile != null) {
            val oldImageName: String = dbHandler!!.getImageUserName(dbHandler!!.getUserId())

            val bitmap = (img_edit_profile.drawable as BitmapDrawable).bitmap
            saveFile(bitmap, date)
            dbHandler!!.updateImageUserPath(dbHandler!!.getUserId(), "${this.filesDir}/Images/profile/${dbHandler!!.getUserId()}/$date.png")
            dbHandler!!.updateImageUserName(dbHandler!!.getUserId(), date)

            //  Sube las imágenes selecionadas a firebase
            if (isNetworkConnected()) {
                // Guarda la imagen de perfil en FIrestore
                val ref = FirebaseStorage.getInstance().getReference("/Images/profile/${dbHandler!!.getUserId()}/$date.png")
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 80, baos)
                val data = baos.toByteArray()
                ref.putBytes(data)

                // Actualiza la base de datos de Firebase
                usersLogin.document(dbHandler!!.getUserId()).update("imageName", date)
                usersLogin.document(dbHandler!!.getUserId()).update("imagePath", "${this.filesDir}/Images/profile/${dbHandler!!.getUserId()}/$date.png")

                // Si ya hay una imagen guardada en Firestore, se elimina la antigua
                if (oldImageName != "0") {
                    val storage = FirebaseStorage.getInstance()
                    val storageRef = storage.reference
                    val desertRef = storageRef.child("Images/profile/${dbHandler!!.getUserId()}/$oldImageName.png")
                    desertRef.delete()
                }

            }
        }
    }

    // Comprueba la conexión a internet
    private fun isNetworkConnected(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null
    }

    private fun backProfile() {
        dbHandler!!.close()
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("USER_PROFILE", true)
        startActivity(intent)
    }
}
