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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private var dbHandler: DataBaseHandler? = null
    private lateinit var auth: FirebaseAuth
    private var dbFirebase = FirebaseFirestore.getInstance()
    private var usersLogin = dbFirebase.collection("usersLogin")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Inicializa Firebase Auth
        auth = FirebaseAuth.getInstance()

        // init db
        dbHandler = DataBaseHandler(this)


        if (dbHandler!!.getLoginTableUserLogin()) {
            progressBar_login.visibility = View.VISIBLE
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)

            //  Modifica la transicion de un activity a otro
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        } else {
            btn_login.setOnClickListener {
                if (validation()) {
                    if (isNetworkConnected()) {
                        progressBar_login.visibility = View.VISIBLE
                        auth.signInWithEmailAndPassword(et_login_email.text.toString(), et_login_passwd.text.toString())
                            .addOnCompleteListener(this) { task ->
                                if (task.isSuccessful) {

                                    val user = auth.currentUser

                                    user?.let {

                                        val uid = user.uid
                                        var userExist = false

                                        //  Comprueba si el usuario está registrado en el dispositivo
                                        for (i in dbHandler!!.getAllUsersId()){
                                            if (uid == i){
                                                userExist = true
                                            }
                                        }

                                        // Una vez iniciada la sesión, comprobamos que el usuario exista en el dispositivo
                                        if (userExist){
                                            // Actualiza la tabla usersLogin con login = 1 cuando se inicie sesion
                                            usersLogin.document(uid)
                                                .update("login", 1)
                                                .addOnSuccessListener {
                                                    // Loguea al usuario e inicia sesion, la cuenta seguirá abierta hasta que el usuario la cierre
                                                    dbHandler!!.updateLoginTableUserLogin(et_login_email.text.toString())

                                                    val intent = Intent(this, MainActivity::class.java)
                                                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                    startActivity(intent)
                                                }
                                        } else {
                                            progressBar_login.visibility = View.GONE
                                            Toast.makeText(
                                                this,
                                                "Debes haberte registrado desde este dispositivo para poder iniciar sesión",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }

                                } else {
                                    progressBar_login.visibility = View.GONE
                                    if (task.exception.toString().contains("FirebaseAuthInvalidCredentialsException")){
                                        Toast.makeText(
                                            baseContext,
                                            "La contraseña es incorrecta.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    if (task.exception.toString().contains("FirebaseAuthInvalidUserException")){
                                        Toast.makeText(
                                            baseContext,
                                            "El email es incorrecto o no se encuentra registrado.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            }

                    } else {
                        Toast.makeText(
                            baseContext,
                            "Debes tener conexión a internet para iniciar sesión.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            tv_login_to_register.setOnClickListener {
                val intent = Intent(this, RegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }
    }

    private fun validation(): Boolean {
        val validate: Boolean

        if (et_login_email.text.toString() != "" && et_login_passwd.text.toString() != "") {
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
