package com.android.dimitar.mymemorygame

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private  lateinit var auth:FirebaseAuth;
    private  lateinit var buttonRegister:Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance();
        buttonRegister = findViewById<Button>(R.id.buttonRegister);
    }

    fun register(view: View){
        buttonRegister.isEnabled=false
       val editTextEmailAddress =  findViewById<EditText>(R.id.editTextEmailAddress)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        val email=editTextEmailAddress.text.toString()
        val password=editTextPassword.text.toString()

        auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener { task ->
            if(task.isSuccessful){
                val intent= Intent(this,MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }.addOnFailureListener { exception ->
            buttonRegister.isEnabled=true
            Toast.makeText(applicationContext,exception.localizedMessage,Toast.LENGTH_LONG).show()
        }
    }

    fun goToLogin(view: View){
        val intent= Intent(this,LoginActivity::class.java)
        startActivity(intent)
    }
}