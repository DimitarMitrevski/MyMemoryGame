package com.android.dimitar.mymemorygame

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.android.dimitar.mymemorygame.models.BoardSize
import kotlin.math.min


class ImagePickerAdapter(
    private val context: Context,
    private val imageUris: List<Uri>,
    private val boardSize: BoardSize,
    private val imageClickListener: ImageClickListener
    ) : RecyclerView.Adapter<ImagePickerAdapter.ViewHolder>() {

    companion object{
        private const val MARGIN_SIZE= 10
    }

    interface ImageClickListener {
        fun onPlaceHolderClicked()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
     val view =  LayoutInflater.from(context).inflate(R.layout.card_image,parent,false)
      val cardWidth =   parent.width / boardSize.getWidht()  - (2* MARGIN_SIZE);
    val cardHeight = parent.height / boardSize.getHeight() - (2* MARGIN_SIZE);
        val cardSideLenght = min(cardWidth,cardHeight);
       val layoutParams =  view.findViewById<ImageView>(R.id.ivCustomImage).layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.width = cardSideLenght
        layoutParams.height = cardSideLenght
        layoutParams.setMargins(
           MARGIN_SIZE,
          MARGIN_SIZE,
          MARGIN_SIZE,
           MARGIN_SIZE
        );
        return  ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
         if(position < imageUris.size){
             holder.bind(imageUris[position])
         }else{
             holder.bind()
         }
    }

    override fun getItemCount() = boardSize.getNumPairs();

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private val ivCustomImage = itemView.findViewById<ImageView>(R.id.ivCustomImage);

     fun bind(uri: Uri){
         ivCustomImage.setImageURI(uri)
         ivCustomImage.setOnClickListener(null)

     }
        fun bind(){
              ivCustomImage.setOnClickListener{

                  imageClickListener.onPlaceHolderClicked()
              }
        }
    }
}
