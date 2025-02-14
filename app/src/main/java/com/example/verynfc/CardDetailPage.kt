package com.example.verynfc

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.w3c.dom.Text

class CardDetailPage : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_detail_page);
        val button1:LinearLayout=findViewById(R.id.ChangeNameButton);
        val button2:LinearLayout=findViewById(R.id.ChangeAvatarButton);
        val button3:LinearLayout=findViewById(R.id.ChangeCodeButton);
        val CardNameView:TextView=findViewById(R.id.CardBigName);
        val CardPicView:ImageView=findViewById(R.id.CardBigAvatar);
        val CardIDView:TextView=findViewById(R.id.CardBigID);
        val CardName=intent.getStringExtra("showedName");
        val CardCode=intent.getLongExtra("showedCode",0x0000);
        val CardIMG=intent.getIntExtra("showedPic",R.drawable.credit_card);
        button1.setOnClickListener {
            AlertDialog.Builder(this).apply {
                setTitle("Error");
                setMessage("Currently Unavailable");
                setPositiveButton("Confirm",null);
            }.show();
        }
        button2.setOnClickListener {
            AlertDialog.Builder(this).apply {
                setTitle("Error");
                setMessage("Currently Unavailable");
                setPositiveButton("Confirm",null);
            }.show();
        }
        button3.setOnClickListener {
            AlertDialog.Builder(this).apply {
                setTitle("Error");
                setMessage("Currently Unavailable");
                setPositiveButton("Confirm",null);
            }.show();
        }
        CardNameView.text=CardName;
        CardIDView.text=CardCode.toString();
        CardPicView.setImageResource(CardIMG);
    }
}