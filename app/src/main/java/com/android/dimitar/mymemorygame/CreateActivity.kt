package com.android.dimitar.mymemorygame

import android.app.Activity
import android.app.AlertDialog
import android.content.ClipData
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.dimitar.mymemorygame.models.BoardSize
import com.android.dimitar.mymemorygame.utils.*
import com.android.dimitar.mymemorygame.utils.BitMapScaler
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

import java.io.ByteArrayOutputStream

class CreateActivity : AppCompatActivity() {

    companion object{
        private const val TAG = "CreateActivity"
        private const val PICK_PHOTO_CODE = 655
        private const val READ_EXTERNAL_PHOTOS_CODE = 248
        private  const val READ_PHOTOS_PERMISSION= android.Manifest.permission.READ_EXTERNAL_STORAGE
        private const val MIN_GAME_NAME_LENGHT=3
        private const val MAX_GAME_NAME_LENGHT=14
    }
    private lateinit var adapter: ImagePickerAdapter
    private  lateinit var rvImagePicker:RecyclerView
    private lateinit var etGameName:EditText
    private lateinit var btnSave:Button
    private lateinit var pbUploading:ProgressBar

   private lateinit var boardSize: BoardSize
   private var numberImagesRequired = -1;
   private val chosenImagesUris = mutableListOf<Uri>()
    private val storage = Firebase.storage
    private val db = Firebase.firestore;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)
        rvImagePicker = findViewById(R.id.rvImagePicker);
        etGameName = findViewById(R.id.etGameName);
        btnSave = findViewById(R.id.btnSave)
        pbUploading = findViewById(R.id.pbUploading)

        supportActionBar?.setDisplayHomeAsUpEnabled(true);
        boardSize = intent.getSerializableExtra(EXTRA_BOARD_SIZE) as BoardSize
      numberImagesRequired = boardSize.getNumPairs()
        supportActionBar?.title = "Choose picks (0 / $numberImagesRequired)"

        btnSave.setOnClickListener{
            saveDataToFirebase();
        }

        etGameName.filters = arrayOf(InputFilter.LengthFilter(MAX_GAME_NAME_LENGHT));
        etGameName.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(p0: Editable?) {
btnSave.isEnabled = shouldEnableSaveButton();
            }

        })
        adapter = ImagePickerAdapter(this, chosenImagesUris, boardSize, object: ImagePickerAdapter.ImageClickListener{
            override fun onPlaceHolderClicked() {
               if(isPermissionGranted(this@CreateActivity,READ_PHOTOS_PERMISSION )) {
                   launchIntentForPhotos();
               }else{
                   requestPermission(this@CreateActivity, READ_PHOTOS_PERMISSION, READ_EXTERNAL_PHOTOS_CODE);
               }
            }

        })
        rvImagePicker.adapter = adapter
        rvImagePicker.setHasFixedSize(true)
        rvImagePicker.layoutManager = GridLayoutManager(this, boardSize.getWidht())
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == READ_EXTERNAL_PHOTOS_CODE){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                launchIntentForPhotos();
            }else{
               val provideAccessPhotos =  getString(R.string.provideAccessPhotos)
                Toast.makeText(this,provideAccessPhotos, Toast.LENGTH_LONG).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(item.itemId == android.R.id.home){
            finish();
            return  true;
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode != PICK_PHOTO_CODE || resultCode != Activity.RESULT_OK || data == null) {
            Log.w(TAG, "Did not get data back from the launched activity user likely cancled flow")
            return
        }
        val selectedUri: Uri? = data.data
        val clipData: ClipData? = data.clipData

        if (clipData != null) {
            Log.i(TAG, "clipData numImages ${clipData.itemCount}: $clipData");
            for (i in 0 until clipData.itemCount) {
                val clipItem = clipData.getItemAt(i);
                if (chosenImagesUris.size < numberImagesRequired) {
                    chosenImagesUris.add(clipItem.uri);
                }
            }
        } else if (selectedUri != null) {
            Log.i(TAG, "Data: $selectedUri")
            chosenImagesUris.add(selectedUri)
        }
    adapter.notifyDataSetChanged()
        val choosePics = getString(R.string.choosePics)
        supportActionBar?.title = "$choosePics (${chosenImagesUris.size} / $numberImagesRequired)"
        
        btnSave.isEnabled = shouldEnableSaveButton();
    }

    private fun shouldEnableSaveButton(): Boolean {
        if(chosenImagesUris.size!=numberImagesRequired) {
            return  false;
            }
       if(etGameName.text.isBlank() || etGameName.text.length < MIN_GAME_NAME_LENGHT){
           return false;
       }

        return true;
    }

    private fun saveDataToFirebase() {
        btnSave.isEnabled=false;
        val customGameName = etGameName.text.toString()
        // Check that we're not overwriting someones else data
        db.collection("games").document(customGameName).get().addOnSuccessListener { document->
            if(document!=null && document.data!=null){
                val nameTaken = getString(R.string.nameTaken)
                val gameAlreadyExists = getString(R.string.gameAlreadyExists, customGameName)
                AlertDialog.Builder(this).setTitle(nameTaken).setMessage(gameAlreadyExists).setPositiveButton("OK", null).show()
           btnSave.isEnabled=true;
            }else{
                handleImageUploading(customGameName);
            }
        }.addOnFailureListener{exception->
            Log.e(TAG, "Encountered error while saving memory game", exception);
            val errorSavingGame = getString(R.string.errorSavingGame)
            Toast.makeText(this, errorSavingGame, Toast.LENGTH_SHORT).show()
            btnSave.isEnabled=true;

        }

        }


    private fun handleImageUploading(gameName: String) {
        pbUploading.visibility = View.VISIBLE
        var didEncounterError = false;
        val uploadedImagesUrls = mutableListOf<String>()
        for ((index, photoUri) in chosenImagesUris.withIndex()) {
            val imageByteArray = getImageByteArray(photoUri);
            val filepath = "images/$gameName/${System.currentTimeMillis()}-${index}.jpg";
            val photoReference = storage.reference.child(filepath);
            photoReference.putBytes(imageByteArray).continueWithTask { photoUpladTask ->
                Log.i(
                    CreateActivity.TAG,
                    "Uploaded bytes: ${photoUpladTask.result?.bytesTransferred}"
                )
                photoReference.downloadUrl
            }.addOnCompleteListener { downloadUrlTask ->
                if (!downloadUrlTask.isSuccessful) {
                    Log.e(
                        CreateActivity.TAG,
                        "Exception with Firebase storage",
                        downloadUrlTask.exception
                    );
                    val failToUplImage = getString(R.string.failToUplImage)
                    Toast.makeText(this, failToUplImage, Toast.LENGTH_SHORT).show()
                    didEncounterError = true;
                    return@addOnCompleteListener;
                }
                if (didEncounterError) {
                    pbUploading.visibility=View.GONE
                    return@addOnCompleteListener
                }
                val downloadUrl = downloadUrlTask.result.toString()
                uploadedImagesUrls.add(downloadUrl)
                pbUploading.progress=uploadedImagesUrls.size *100/ chosenImagesUris.size
                Log.i(
                    CreateActivity.TAG,
                    "Finished uploading $photoUri, num uploaded ${uploadedImagesUrls.size}"
                );
                if (uploadedImagesUrls.size == chosenImagesUris.size) {
                    handleAllImagesUploaded(gameName, uploadedImagesUrls);
                }
            }

        }
    }

    private fun handleAllImagesUploaded(gameName:String, imageUrls: MutableList<String>) {
        db.collection("games").document(gameName).set(mapOf("images" to imageUrls)).addOnCompleteListener{gameCreationTask->
            pbUploading.visibility=View.GONE

            if(!gameCreationTask.isSuccessful){

                Log.e(TAG, "Exception with game creation", gameCreationTask.exception);
                val failGameCreation = getString(R.string.failGameCreation)
                Toast.makeText(this, failGameCreation, Toast.LENGTH_SHORT).show();
                return@addOnCompleteListener
            }
            Log.i(TAG, "Successfully created game $gameName");
            val uploadComplete = getString(R.string.uploadComplete, gameName)
            AlertDialog.Builder(this).setTitle(uploadComplete).setPositiveButton("OK"){ _,_->
                val resultData = Intent()
                resultData.putExtra(EXTRA_GAME_NAME,gameName);
                setResult(Activity.RESULT_OK, resultData)
                finish()
            }.show()
    }
    }


    private fun getImageByteArray(photoUri: Uri): ByteArray {
    val originalBitmap = if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.P){
        val source = ImageDecoder.createSource(contentResolver,photoUri);
       ImageDecoder.decodeBitmap(source)
    }else{
        MediaStore.Images.Media.getBitmap(contentResolver,photoUri);
    }
        Log.i(TAG,"Original width ${originalBitmap.width} and height ${originalBitmap.height}");

        val scaledBitmap = BitMapScaler().scaleToFitWidth(originalBitmap, 250);
        Log.i(TAG,"Scaled width ${scaledBitmap.width} and height ${scaledBitmap.height}");
        val byteOutputStream = ByteArrayOutputStream();
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, byteOutputStream);
        return byteOutputStream.toByteArray();
    }

    private fun launchIntentForPhotos() {
val intent = Intent(Intent.ACTION_PICK)
        intent.type="image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(Intent.createChooser(intent, "Choose pics"), PICK_PHOTO_CODE)

    }
}