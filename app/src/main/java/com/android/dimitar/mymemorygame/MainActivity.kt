package com.android.dimitar.mymemorygame

import android.animation.ArgbEvaluator
import android.app.ActionBar
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.dimitar.mymemorygame.models.BoardSize
import com.android.dimitar.mymemorygame.models.MemoryGame
import com.android.dimitar.mymemorygame.models.UserImageList
import com.android.dimitar.mymemorygame.utils.EXTRA_BOARD_SIZE
import com.android.dimitar.mymemorygame.utils.EXTRA_GAME_NAME
import com.firebase.ui.auth.AuthUI
import com.github.jinatonic.confetti.CommonConfetti
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity";
        private const val CREATE_REQUEST_CODE = 248
    }

    private lateinit var clRoot:CoordinatorLayout
 private lateinit var memoryGame: MemoryGame;
private lateinit var adapter:MemoryBoardAdapter;
 private lateinit var rvBoard: RecyclerView;
 private  lateinit var tvNumMoves: TextView;
 private  lateinit var tvNumPairs: TextView;

    private  var boardSize: BoardSize = BoardSize.EASY
    private val db = Firebase.firestore
    private  var gameName:String? = null
    private  var customGameImages: List<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        clRoot = findViewById(R.id.clRoot)
        rvBoard = findViewById(R.id.rvBoard);
        tvNumMoves = findViewById(R.id.tvNumMoves);
        tvNumPairs = findViewById(R.id.tvNumPairs);


        setupBoard();
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
      menuInflater.inflate(R.menu.menu_main,menu)
        return  true;
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.mi_refresh -> {
                //setup the game again
                if(memoryGame.getNumMoves() > 0 && !memoryGame.haveWonGame()) {
                showAlertDialog("Quit your game?", null,View.OnClickListener {
                    setupBoard()
                })
                }else {
                    setupBoard();
                }
                return  true;
                }
            R.id.mi_new_size ->{
                showNewSizeDialog();
                return true;
            }
            R.id.mi_custom ->{
                showCreationDialog();
                return true;
            }
            R.id.mi_download->{
                showeDownloadDialog();
                return true;
            }
            R.id.mi_signout->{
                AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener {
                        val intent = Intent(this, FirebaseUIActivity::class.java)
                        startActivity(intent)
                    }
            }
            }
    return super.onOptionsItemSelected(item)
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if(requestCode== CREATE_REQUEST_CODE && resultCode==Activity.RESULT_OK){
            val  customGameNam:String? = data?.getStringExtra(EXTRA_GAME_NAME);
            if(customGameNam==null){
                Log.e(TAG, "Got null custom game from CreateActivity");
                return;
            }
            downloadGame(customGameNam)
        }
        super.onActivityResult(requestCode, resultCode, data)

    }

    private fun showeDownloadDialog() {
val boardDownloadView = LayoutInflater.from(this).inflate(R.layout.dialog_download_board,null)
        showAlertDialog("Fetch memory game", boardDownloadView, View.OnClickListener {
            // Grab the text of the game name.
            val etDownloadGame = boardDownloadView.findViewById<EditText>(R.id.etDownloadGame).text.toString().trim();
            downloadGame(etDownloadGame);


        })
    }

    private fun downloadGame(customGameName: String){
        db.collection("games").document(customGameName).get().addOnSuccessListener {
            document->
            var userImageList:UserImageList? = document.toObject(UserImageList::class.java)
            if(userImageList?.images==null){
                Log.e(TAG, "Invalid custom game data from firestore.");
                Snackbar.make(clRoot, "Sorry, we couldn't find such game, '$customGameName'", Snackbar.LENGTH_LONG).show()
                    return@addOnSuccessListener
            }
            val numCards = userImageList.images!!.size*2
boardSize = BoardSize.getByValue(numCards);
            customGameImages = userImageList.images!!;
            for (imageUrl in userImageList.images!!) {
            Picasso.get().load(imageUrl).fetch()
            }
            Snackbar.make(clRoot, "Your playing this $customGameName", Snackbar.LENGTH_SHORT).show();
            gameName = customGameName
            setupBoard()
        }.addOnFailureListener{exception->
            Log.e(TAG, "Exception when retriving game", exception)
        }

    }

    private fun showCreationDialog() {
        val boardSizeView= LayoutInflater.from(this).inflate(R.layout.dialog_board_size,null)
        var radioGroupSize = boardSizeView
            .findViewById<RadioGroup>(R.id.radioGroup)

        when(boardSize) {
            BoardSize.EASY -> radioGroupSize.check(R.id.rbEasy)
            BoardSize.MEDIUM -> radioGroupSize.check(R.id.rbMedium)
            BoardSize.HARD -> radioGroupSize.check(R.id.rbHard)
        }
        showAlertDialog("Create your own memory board", boardSizeView, View.OnClickListener {
            // Set a new value of the board size.
            val desiredBoardSize = when(radioGroupSize.checkedRadioButtonId){
                R.id.rbEasy-> BoardSize.EASY
                R.id.rbMedium->BoardSize.MEDIUM
                else->BoardSize.HARD
            }
            // Navigate the user to a new activity
            val intent = Intent(this, CreateActivity::class.java)
            intent.putExtra(EXTRA_BOARD_SIZE, desiredBoardSize)
            startActivityForResult(intent,CREATE_REQUEST_CODE)
        })

    }

    private fun showNewSizeDialog() {
       val boardSizeView= LayoutInflater.from(this).inflate(R.layout.dialog_board_size,null)
        var radioGroupSize = boardSizeView
            .findViewById<RadioGroup>(R.id.radioGroup)
      when(boardSize) {
          BoardSize.EASY -> radioGroupSize.check(R.id.rbEasy)
          BoardSize.MEDIUM -> radioGroupSize.check(R.id.rbMedium)
          BoardSize.HARD -> radioGroupSize.check(R.id.rbHard)
      }
        showAlertDialog("Choose new size", boardSizeView, View.OnClickListener {
            // Set a new value of the board size.
            boardSize = when(radioGroupSize.checkedRadioButtonId){
                R.id.rbEasy-> BoardSize.EASY
                    R.id.rbMedium->BoardSize.MEDIUM
                   else->BoardSize.HARD
            }
            gameName=null;
            customGameImages = null;
            setupBoard()
        })
    }

    private fun showAlertDialog(title:String,view: View?, positiveClickListener:View.OnClickListener) {
  MaterialAlertDialogBuilder(this).setTitle(title).setView(view).setNegativeButton("Cancel",null).setPositiveButton("OK"){
      _,_ ->
 positiveClickListener.onClick(null)
  }.show()
}
    private fun setupBoard() {
        supportActionBar?.title = gameName?: getString(R.string.app_name);
        when(boardSize){
            BoardSize.EASY -> {
                tvNumMoves.text = "Easy 4 x 2"
                tvNumPairs.text = "Pairs 0 / 4"
            }
            BoardSize.MEDIUM -> {
                tvNumMoves.text = "Easy 6 x 3"
                tvNumPairs.text = "Pairs 0 / 9"

            }
            BoardSize.HARD -> {
                tvNumMoves.text = "Easy 6 x 4"
                tvNumPairs.text = "Pairs 0 / 12"
            }
        }

        tvNumPairs.setTextColor(ContextCompat.getColor(this,R.color.color_progress_none))

        memoryGame = MemoryGame(boardSize, customGameImages)

        adapter = MemoryBoardAdapter(this, boardSize, memoryGame.cards, object:MemoryBoardAdapter.CardClickListener{
            override fun onCardClicked(position: Int) {
                updateGameWithFlip(position);

            }

        });
        rvBoard.adapter = adapter;
        rvBoard.setHasFixedSize(true);
        rvBoard.layoutManager = GridLayoutManager(this, boardSize.getWidht());
    }


    private fun updateGameWithFlip(position: Int) {
    if(memoryGame.haveWonGame()){
        Snackbar.make(clRoot, "You already won!", Snackbar.LENGTH_LONG).show();
        return;
    }
        if(memoryGame.isCardFaceUp(position)){
            Snackbar.make(clRoot, "Invalid move!", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if(memoryGame.flipCard(position)){
            Log.i(TAG,"Found a match! Num pairs found: ${memoryGame.numPairsFound}")
            val color = ArgbEvaluator().evaluate(
                memoryGame.numPairsFound.toFloat()/boardSize.getNumPairs(),
                ContextCompat.getColor(this,R.color.color_progress_none),
                ContextCompat.getColor(this,R.color.color_progress_full)
            ) as Int
            tvNumPairs.setTextColor(color)
            tvNumPairs.text="Pairs: ${memoryGame.numPairsFound} / ${boardSize.getNumPairs()}"
            if(memoryGame.haveWonGame()){

                Snackbar.make(clRoot, "You won! Congratulations.",Snackbar.LENGTH_LONG).show();
                CommonConfetti.rainingConfetti(clRoot, intArrayOf(Color.YELLOW, Color.BLUE, Color.DKGRAY)).oneShot()
            }
        }
        tvNumMoves.text = "Moves: ${memoryGame.getNumMoves()}"
        adapter.notifyDataSetChanged();
    }
}