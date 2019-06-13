package com.alejandro.comparterecetas.login

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.alejandro.comparterecetas.MainActivity
import com.alejandro.comparterecetas.R
import com.alejandro.comparterecetas.database.DataBaseHandler
import com.alejandro.comparterecetas.models.UsersModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    private val usersModel = UsersModel()
    private var dbHandler: DataBaseHandler? = null

    //  Inicializa la base de datos de Firebase
    private var dbFirebase = FirebaseFirestore.getInstance()
    //  Crea un usuario nuevo en Firebase
    private lateinit var auth: FirebaseAuth
    //  Crea una nueva "tabla" en Firebase
    private var usersLogin = dbFirebase.collection("usersLogin")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        //  Inicializa Firebase Auth
        auth = FirebaseAuth.getInstance()

        //init db
        dbHandler = DataBaseHandler(this)


        btn_register.setOnClickListener {
            if (isNetworkConnected()) {
                if (validation()) {
                    auth.createUserWithEmailAndPassword(et_reg_email.text.toString(), et_reg_passwd.text.toString())
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                val successUsers: Boolean
                                val user = auth.currentUser

                                user?.let {
                                    val email = user.email
                                    val uid = user.uid

                                    // Una vez creado el usuario, se guarda en la base de datos
                                    usersModel.id = uid
                                    usersModel.email = email.toString()
                                    usersModel.name = et_reg_name.text.toString()
//                                    usersModel.passwd = et_reg_passwd.text.toString()
                                    usersModel.login = 1
                                    usersModel.imagePath = "123"
                                    usersModel.imageName = "0"

                                    successUsers = dbHandler!!.addUserTableUserLogin(usersModel)

                                    usersLogin.document(uid).set(usersModel)

                                    if (successUsers) {
                                        val intent = Intent(this, MainActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        startActivity(intent)
                                        progressBar.visibility = View.VISIBLE
                                    }
                                }

                            } else {
                                Toast.makeText(baseContext, "El email ya se encuentra registrado.", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }

                }
            } else {
                Toast.makeText(this, "Comprueba tu conexión a Internet", Toast.LENGTH_SHORT).show()
            }
        }

        tv_reg_to_login.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }


    private fun validation(): Boolean {
        val validate: Boolean

        if (et_reg_email.text.toString() != "" && et_reg_name.text.toString() != "" && et_reg_passwd.text.toString() != "") {
            validate = true
        } else {
            validate = false
            Toast.makeText(this, "Rellena todos los campos", Toast.LENGTH_LONG).show()
        }

        return validate
    }

    // Comprueba la conexión a internet
    private fun isNetworkConnected(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null
    }


}
