package com.android.dimitar.mymemorygame

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var auth:FirebaseAuth;
    private lateinit var buttonLogin: Button;

    companion object{
        private const val TAG="LoginActivity"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    auth= FirebaseAuth.getInstance();
        buttonLogin = findViewById<Button>(R.id.buttonLogin);

    }
    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if(currentUser != null){
            Log.i(TAG, "User is logged in. $currentUser");
            val intent= Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    fun login(view: View){
        buttonLogin.isEnabled=false
        val editTextEmailAddress = findViewById<EditText>(R.id.editTextEmailAddress);
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
   val email:String=editTextEmailAddress.text.toString()
        val password:String=editTextPassword.text.toString()
if(email.trim().length > 0 && password.trim().length>0) {
    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }.addOnFailureListener { exception ->
        buttonLogin.isEnabled = true;
        Toast.makeText(applicationContext, exception.localizedMessage, Toast.LENGTH_LONG).show()
    }
} else{
    Toast.makeText(this, "Please enter email and password!", Toast.LENGTH_SHORT).show();
    buttonLogin.isEnabled=true;
}
    }

    fun goToRegister(view: View){
        val intent= Intent(this,RegisterActivity::class.java)
        startActivity(intent)
    }

}