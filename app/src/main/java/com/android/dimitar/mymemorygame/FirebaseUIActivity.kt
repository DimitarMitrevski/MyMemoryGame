package com.android.dimitar.mymemorygame

import android.content.Intent
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging


class FirebaseUIActivity : AppCompatActivity() {
    private  var signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res ->
        this.onSignInResult(res)
    }
    private val providers = arrayListOf(
        AuthUI.IdpConfig.EmailBuilder().build(),
        AuthUI.IdpConfig.GoogleBuilder().build(),
        AuthUI.IdpConfig.FacebookBuilder().build(),
        AuthUI.IdpConfig.AnonymousBuilder().build()
    )

    private val signInIntent = AuthUI.getInstance()
        .createSignInIntentBuilder()
        .setAvailableProviders(providers)
        .setLogo(R.drawable.icon)
        .build()

    companion object{
        private const val TAG="FirebaseUIActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FacebookSdk.setClientToken("dbf5c0de64bc1aa234dd64c0619d6b5f")
        FacebookSdk.sdkInitialize(getApplicationContext());
        //setContentView(R.layout.activity_firebase_ui_activity)
        // Choose authentication providers
        signInLauncher.launch(signInIntent)

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            val msg = token
            Log.d(TAG, "Token is $msg")
            //Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode==0){
            signInLauncher.launch(signInIntent);
        }
        Log.i(TAG,"This has been called $resultCode ${data?.extras}" );
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
            // ...
        } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            // ...
            Toast.makeText(this, "An error occured while trying to log in.",Toast.LENGTH_LONG);
        }
    }

}