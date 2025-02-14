package com.example.verynfc

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.nfc.NfcAdapter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import java.io.FileNotFoundException
import java.io.IOException
import java.lang.reflect.Field


data class MyCard(
    var avatarId:Int,
    var CardName:String,
    var CardCode:Long,
    var cardType: String,         // 卡片的类型（例如：NFC-A, MIFARE Classic, ISO 15693等）
    var ndefData: String?,        // 可选的NDEF数据，如果卡片支持NDEF格式
    var manufacturer: String?,    // 可选的制造商信息
    var version: String?          // 可选的版本信息
)
public var colorstatus1:Int=0;
public var EmptyStatus:Boolean=false;
public var autoAdderDialogStatus:Int=1;
public object QuickToast {
    private var currentToast: Toast? = null
    fun ShowToast(context: Context, message: String) {
        // 取消之前的 Toast
        currentToast?.cancel();
        // 显示新的 Toast
        currentToast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        currentToast?.show();
    }
}

class HomePage : AppCompatActivity() {

    //This variable is used for temporary card adding process.
    var tempCard:MyCard=MyCard(
        avatarId = 0x0,
        CardName = "",
        CardCode = 0,
        cardType = "",
        ndefData = "",
        manufacturer = "",
        version = ""
    );

    fun isNfcAvailable(context: Context): Boolean {
        val nfcAvailability = NfcAdapter.getDefaultAdapter(context)
        val nfcStatus=NfcAdapter.getDefaultAdapter(context)
        if(nfcAvailability!=null&&nfcStatus.isEnabled){
            return true;
        }
        else return false;
    }

    var gotoSettingsPage:Intent= Intent();
    var gotoNFCReaderPage:Intent=Intent();
    var gotoDetailPage:Intent=Intent();
    var CardList=ArrayList<MyCard>();
    var Cards:MutableList<MyCard> = mutableListOf();
    lateinit var cardView:RecyclerView;
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        //Basic values initialize
        Log.d(this.toString(),"onCreated.");
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page);
        var CardCode_input:Int=0;
        var CardCode_String:String="";
        var CardName_input:String="";
        Cards  = CardDataBase(this).loadAllCards();
        cardView=findViewById(R.id.MyCardList);
        val toolbar:Toolbar=findViewById(R.id.topbar);
        val emptySign=findViewById<LinearLayout>(R.id.EmptySign);
        val CardAdderButton:com.google.android.material.floatingactionbutton.FloatingActionButton=findViewById(R.id.addCardAction);
        colorstatus1=1;
        Log.d(this.toString(),"Layout generated2.");
        setSupportActionBar(toolbar);
        gotoSettingsPage= Intent(this,SettingsPage::class.java);
        gotoNFCReaderPage=Intent(this,NFCReaderPage::class.java);
        CardList.clear();
        var listLength:Int=Cards.size;
        if(listLength<=0){
            EmptyStatus=true;
        }
        else{
            EmptyStatus=false
        }
        if(EmptyStatus){
            emptySign.isVisible=true;
            cardView.isVisible=false;
        }
        else{
            emptySign.isVisible=false;
            cardView.isVisible=true;
            setCardView(cardView,Cards);
        }
        //The base structure of HomePage has build completely.
        //Now check if the device support NFC function. If not, program will quick immediately.
        val nfcAvailability=isNfcAvailable(this);
        if(!nfcAvailability) {
            AlertDialog.Builder(this).apply {
                setTitle("Unsupported hardware");
                setMessage("Your device does not seem to support NFC, or NFC is not enabled.");
                setCancelable(false);
                setPositiveButton("Quit"){
                    _Parcel,_->finish();
                }
            }.show();
        }
        else{

        }
        val options = arrayOf("Add existing cards via NFC","Add cards manually"); //Two methods of adding cards
        var selectedOption = -1 // 默认选中的选项
        CardAdderButton.setOnClickListener {
            AlertDialog.Builder(this).apply {
                setTitle("Select A Method")
                setSingleChoiceItems(options,selectedOption){dialog, which ->
                    selectedOption = which;
                }
                setPositiveButton("Next"){
                    dialog,_->when(selectedOption)
                    {
                        0->AutoCardAdder();
                        1->TraditionalCardAdder();
                        else->dialog.dismiss();
                    }
                }
                setNegativeButton("Cancel"){
                    dialog,_-> dialog.dismiss();
                }
            }.show();
        }
    }

    fun setCardView(cardView:RecyclerView,Cards: MutableList<MyCard>){
        var count:Int=1;
        var listLength:Int=Cards.size;
        while (count<=listLength){
            CardList.add(Cards[count-1]);
            Log.d(this.toString(),"Card adder complete.");
            count=count+1;
        }
        Log.d(this.toString(),"execute here");
        val layoutManager= LinearLayoutManager(this)
        cardView.layoutManager=layoutManager;
        val adapter=CardViewAdapter(this,
            CardList,
            onSwitchClick={
                    position->
                colorstatus1 = (colorstatus1+1)%2;
                //Toast.makeText(this,"The card will be selected for payment.",Toast.LENGTH_SHORT).show();
            },
            onCardClick = {
                    position->
                gotoDetailPage=Intent(this,CardDetailPage::class.java).apply{
                    putExtra("showedName",Cards[position].CardName);
                    putExtra("showedPic" ,Cards[position].avatarId);
                    putExtra("showedCode",Cards[position].CardCode);
                };
                startActivity(gotoDetailPage);
            },
            onCardLongClick = {
                AlertDialog.Builder(this).apply{
                    setTitle("Deletion");
                    setMessage("Delete this card?");
                    setPositiveButton("Confirm"){
                            _Parcel,_->
                    }
                    setNegativeButton("Cancel"){
                            _Parcel,_->
                    }
                    show();
                }
            });
        cardView.adapter=adapter;
    }

    //Auto card adder function begins
    lateinit var wbDialog:AlertDialog;
    fun AutoCardAdder(){
        val waitingBubble=AlertDialog.Builder(this);
        val processIcon=ProgressBar(this);
        val getNFCInfo:Intent=Intent(this,NFCScanner::class.java);
        processIcon.layoutParams=LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        processIcon.isIndeterminate=true;
        waitingBubble.setCancelable(false);
        waitingBubble.setTitle("Scanning card");
        waitingBubble.setMessage("Please put your card on the back of your phone.");
        waitingBubble.setView(processIcon);
        waitingBubble.setNegativeButton("Cancel"){
            dialog,_->dialog.dismiss();
            QuickToast.ShowToast(this,"Operation aborted");
            autoAdderDialogStatus=1;
        }
        wbDialog=waitingBubble.show();
        //以下开启卡片读取过程
        val handler = Handler(Looper.getMainLooper())
        handler.post {
            startActivityForResult(getNFCInfo,1);
            overridePendingTransition(
                com.google.android.material.R.anim.abc_fade_in,
                com.google.android.material.R.anim.abc_fade_out
            );
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data);
        val readStatus: Boolean = data?.getBooleanExtra("readStatus",false) ?: false;
        if(readStatus){
            val builder = AlertDialog.Builder(this)
            val fdView: View = LayoutInflater.from(this).inflate(R.layout.card_adder_layout_auto, null)
            builder.setView(fdView)

            // 初始化视图和其他变量（如gotCode, gotType, gotName）
            val gotCode: TextView = fdView.findViewById(R.id.gotCardCode)
            val gotType: TextView = fdView.findViewById(R.id.gotCardType)
            val gotName: EditText = fdView.findViewById(R.id.cardname_auto)

            // 设置tempCard的值（根据data）
            tempCard.CardCode = data?.getLongExtra("fetchedCode", 0) ?: 0
            tempCard.ndefData = data?.getStringExtra("fetchNdef")
            tempCard.cardType = data?.getStringExtra("fetchType") ?: "N/A"
            gotCode.text = tempCard.CardCode.toString()
            gotType.text = tempCard.cardType

            // 配置对话框按钮
            builder.setNegativeButton("Cancel") { dialog, _ ->
                QuickToast.ShowToast(this, "Operation aborted")
                wbDialog.dismiss() // 若需关闭其他对话框，请确保wbDialog正确引用
            }
            builder.setPositiveButton("Add",){
                dialog,_->CloseDialog(dialog,false);
                val finalName = gotName.text.toString()
                QuickToast.ShowToast(this, "Added");
                if (finalName.isEmpty()) {
                    gotName.background = getDrawable(R.color.colorError)
                    QuickToast.ShowToast(this, "Please enter a value!")
                    CloseDialog(dialog,false);
                }
                else {
                    tempCard.CardName = finalName
                    tempCard.avatarId = R.drawable.credit_card
                    Cards.add(tempCard)
                    CardDataBase(this).saveAllCards(Cards)
                    CloseDialog(dialog,true);
                    // 关闭相关对话框并更新UI
                    wbDialog.dismiss() // 若需要关闭其他对话框
                    Cards= mutableListOf();//Clear Cardlist Cache
                    Cards=CardDataBase(this).loadAllCards();
                    setCardView(cardView, Cards)
                }
            }
            builder.setCancelable(false)
            val alertDialog = builder.create()
            alertDialog.show()
        }
        else{
            AlertDialog.Builder(this).apply {
                setTitle("Unsupported card type");
                setMessage("\nCurrently we only support Mifare and NFC-A.");
                setPositiveButton("OK"){
                    dialog,_->dialog.dismiss();
                }.show();
            }
            wbDialog.dismiss();
            //QuickToast.ShowToast(this,"Dismissed");
        }
    }
    //Auto card adder function ends


    private fun CloseDialog(dialogInterface: DialogInterface, close: Boolean) {
        try {
            val field: Field = dialogInterface.javaClass.superclass.getDeclaredField("mShowing")
            field.setAccessible(true)
            field.set(dialogInterface, close)
        } catch (e: Exception) {
            e.printStackTrace()
            QuickToast.ShowToast(this,"N/A");
        }
    }



    //Manual card adder function begins
    @SuppressLint("UseCompatLoadingForDrawables")
    fun TraditionalCardAdder()
    {
        //system variables initializing part
        val CardSelectionBubble=AlertDialog.Builder(this);
        var CardAdderBubble=AlertDialog.Builder(this);
        var CardAdderDialog=CardAdderBubble.create();
        //var AdderButton=CardAdderDialog.getButton(AlertDialog.BUTTON_POSITIVE);
        var CardAdderView:View;
        var cardcode_p1:EditText;
        var cardcode_p2:EditText;
        var cardcode_p3:EditText;
        var cardcode_p4:EditText;
        var cardcode_p5:EditText;
        var cardcode_p6:EditText;
        var cardcode_p7:EditText;
        var cardname:EditText;
        CardSelectionBubble.setTitle("Select a card type");
        CardAdderBubble.setTitle("Adding a card");
        val cardsType= arrayOf("ISO 14443-A 4-Bit","ISO 14443-A 7-Bit","ISO 14443-B 4-Bit","ISO 14443-B 7-Bit","NFC Forum","MIFARE Classic","MIFARE DESFire","Felica");
        var selectedType:Int=-1;
        CardSelectionBubble.setSingleChoiceItems(cardsType,selectedType){
            dialog,which->selectedType=which;
        }
        //user inputed variables initializing part
        var CardCode_input:Int
        var CardCode_String:String
        var CardName_input:String
        CardSelectionBubble.setPositiveButton("Next"){
            dialog,_->
            when(selectedType) {
                0->{    //ISO 14443-A 4-Bit
                    CardAdderBubble.setCancelable(false)
                    CardAdderView = layoutInflater.inflate(R.layout.card_adder_layout_4, null)
                    cardcode_p1 = CardAdderView.findViewById(R.id.code4_p1)
                    cardcode_p2 = CardAdderView.findViewById(R.id.code4_p2)
                    cardcode_p3 = CardAdderView.findViewById(R.id.code4_p3)
                    cardcode_p4 = CardAdderView.findViewById(R.id.code4_p4)
                    cardcode_p1.filters = arrayOf(InputFilter.LengthFilter(2))
                    cardcode_p2.filters = arrayOf(InputFilter.LengthFilter(2))
                    cardcode_p3.filters = arrayOf(InputFilter.LengthFilter(2))
                    cardcode_p4.filters = arrayOf(InputFilter.LengthFilter(2))
                    cardname = CardAdderView.findViewById(R.id.card_name_4)

                    CardAdderBubble.setView(CardAdderView)
                    // 取消默认的关闭行为，设置为 null
                    CardAdderBubble.setPositiveButton("Add", null)
                    CardAdderBubble.setNegativeButton("Cancel") { _dialog, _ ->
                        dialog.dismiss()
                        QuickToast.ShowToast(this, "Operation aborted")
                    }

                    CardAdderDialog = CardAdderBubble.create()
                    CardAdderDialog.show(); // 先显示对话框

                    CardAdderDialog.setOnShowListener {
                        // 在这里设置按钮点击事件，确保对话框已完全显示
                        val positiveButton = CardAdderDialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        positiveButton.setOnClickListener {
                            // 你的验证逻辑
                            CardCode_String =
                                cardcode_p1.text.toString() + cardcode_p2.text.toString() + cardcode_p3.text.toString() + cardcode_p4.text.toString()
                            if (cardname.text.isNotEmpty() && CardCode_String.matches(Regex("^[0-9A-Fa-f]{1,4}$"))) {
                                // 输入合法，关闭对话框
                                CardCode_input = CardCode_String.toInt(16)
                                CardName_input = cardname.text.toString()
                                QuickToast.ShowToast(this, CardCode_input.toString())
                                CardAdderDialog.dismiss() // 只有验证通过时才关闭对话框
                            }
                            else if (cardname.text.isEmpty()) {
                                // 输入为空时提示并不关闭对话框
                                cardname.background = getDrawable(R.color.colorError)
                                QuickToast.ShowToast(this, "Please enter a value!")
                                val handler = Handler(Looper.getMainLooper())
                                handler.postDelayed({
                                    cardname.background = getDrawable(R.color.soyoDefault)
                                    cardname.text.clear()
                                }, 1000)
                            }
                            else if (!CardCode_String.matches(Regex("^[0-9A-Fa-f]{1,4}$"))) {
                                // 输入不合法时提示并不关闭对话框
                                cardcode_p1.background = getDrawable(R.color.colorError)
                                cardcode_p2.background = getDrawable(R.color.colorError)
                                cardcode_p3.background = getDrawable(R.color.colorError)
                                cardcode_p4.background = getDrawable(R.color.colorError)
                                QuickToast.ShowToast(this, "Invalid value!")
                                val handler = Handler(Looper.getMainLooper())
                                handler.postDelayed({
                                    cardcode_p1.background = getDrawable(R.color.soyoDefault)
                                    cardcode_p1.text.clear()
                                    cardcode_p2.background = getDrawable(R.color.soyoDefault)
                                    cardcode_p2.text.clear()
                                    cardcode_p3.background = getDrawable(R.color.soyoDefault)
                                    cardcode_p3.text.clear()
                                    cardcode_p4.background = getDrawable(R.color.soyoDefault)
                                    cardcode_p4.text.clear()
                                }, 1000)
                            }
                            else {
                                QuickToast.ShowToast(this, "???")
                            }
                        }
                    }
                };
                else->{ //Unsupported Yet
                    AlertDialog.Builder(this).apply{
                        setTitle("Adding a card");
                        setMessage("Unsupported yet.");
                        setPositiveButton("Confirm"){
                            dialog,_->dialog.dismiss();
                        }.show();
                    }
                }
            }
        }
        CardSelectionBubble.setNegativeButton("Cancel"){
            dialog,_->dialog.dismiss();
        }
        CardSelectionBubble.show();
    }
    //Manual card adder function ends




    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar,menu);
        return true;
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.VNSettings-> startActivity(gotoSettingsPage);
            R.id.gotoNFCmode->
                when(EmptyStatus){
                    false-> startActivity(gotoNFCReaderPage);
                    true->AlertDialog.Builder(this).apply {
                        setTitle("Unable to load Cards");
                        setMessage("Please add a card first!");
                        setPositiveButton("OK"){
                            dialog,_->dialog.dismiss();
                        }.show();
                    }
                }
        }
        return true;
    }

    override fun finish() {
        super.finish()
        CardDataBase(this).saveAllCards(Cards);
    }
}

//Data Saving Part
fun SaveCards(card:MyCard){
    try {

    }
    catch (e:IOException){
        e.printStackTrace();
    }
}

//CardView Adapter Part
class CardViewAdapter(
    val context: Context,
    val cardList: List<MyCard>,
    val onCardClick:(Int)->Unit,
    val onSwitchClick: (Int)->Unit,
    val onCardLongClick: (Int) -> Unit ) :
    RecyclerView.Adapter<CardViewAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val avatar: ImageView = view.findViewById(R.id.CardAvatar)
        val name: TextView = view.findViewById(R.id.CardName)
        val details: TextView = view.findViewById(R.id.CardDetail)
        val pmtSwitch: ImageButton = view.findViewById(R.id.paymentSwitch)
        val cardView:com.google.android.material.card.MaterialCardView = view.findViewById(R.id.CardUI)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.cardlist_layout, parent, false)
        return ViewHolder(view);
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val card = cardList[position]

        holder.name.text = card.CardName
        holder.details.text = card.CardCode.toString()
        Glide.with(context).load(card.avatarId).into(holder.avatar)
        holder.pmtSwitch.setOnClickListener {
            if(colorstatus1==1) {
                holder.pmtSwitch.setImageResource(R.drawable.round_nfc_enabled);
            }
            else {
                holder.pmtSwitch.setImageResource(R.drawable.round_nfc_24);
            }
            onSwitchClick(position)
        }
        holder.cardView.setOnLongClickListener {
            onCardLongClick(position)
            true
        }
        holder.cardView.setOnClickListener {
            onCardClick(position);
        }
    }

    override fun getItemCount() = cardList.size
}


class CardDataBase(private val context: Context) {
    private val fileName = "my_cards.txt"
    private val gson = Gson()
    fun saveCard(card: MyCard) {
        val jsonString = gson.toJson(card)
        context.openFileOutput(fileName, Context.MODE_APPEND).use { output ->
            output.write("$jsonString\n".toByteArray())
        }
    }
    fun saveAllCards(cards: MutableList<MyCard>) {
        val jsonStrings = cards.map { gson.toJson(it) }
        context.openFileOutput(fileName, Context.MODE_PRIVATE).use { output ->
            output.write(jsonStrings.joinToString("\n").toByteArray())
        }
    }
    fun loadAllCards(): MutableList<MyCard> {
        return try {
            context.openFileInput(fileName).bufferedReader().useLines { lines ->
                lines.map { gson.fromJson(it, MyCard::class.java) }
                    .toMutableList()
            }
        } catch (e: FileNotFoundException) {
            mutableListOf()
        }
    }
    fun clearData() {
        context.deleteFile(fileName)
    }
}


class CardDataBaseSQL(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "MyCards.db"
        private const val DATABASE_VERSION = 1

        // 定义表结构
        const val TABLE_CARDS = "cards"
        const val COLUMN_ID = "_id"
        const val COLUMN_AVATAR_ID = "avatar_id"
        const val COLUMN_CARD_NAME = "card_name"
        const val COLUMN_CARD_CODE = "card_code"
        const val COLUMN_CARD_TYPE = "card_type"
        const val COLUMN_NDEF_DATA = "ndef_data"
        const val COLUMN_MANUFACTURER = "manufacturer"
        const val COLUMN_VERSION = "version"
    }
    override fun onCreate(db: SQLiteDatabase) {
        // 创建表的SQL语句
        val createTable = """
            CREATE TABLE $TABLE_CARDS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_AVATAR_ID INTEGER NOT NULL,
                $COLUMN_CARD_NAME TEXT NOT NULL,
                $COLUMN_CARD_CODE INTEGER NOT NULL,
                $COLUMN_CARD_TYPE TEXT NOT NULL,
                $COLUMN_NDEF_DATA TEXT,
                $COLUMN_MANUFACTURER TEXT,
                $COLUMN_VERSION TEXT
            )
        """.trimIndent()
        db.execSQL(createTable)
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CARDS")
        onCreate(db)
    }
    // 插入卡片
    fun addCard(card: MyCard): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_AVATAR_ID, card.avatarId)
            put(COLUMN_CARD_NAME, card.CardName)
            put(COLUMN_CARD_CODE, card.CardCode)
            put(COLUMN_CARD_TYPE, card.cardType)
            put(COLUMN_NDEF_DATA, card.ndefData)
            put(COLUMN_MANUFACTURER, card.manufacturer)
            put(COLUMN_VERSION, card.version)
        }
        val id = db.insert(TABLE_CARDS, null, values)
        db.close()
        return id
    }
    // 获取所有卡片
    fun getAllCards(): List<MyCard> {
        val cards = mutableListOf<MyCard>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_CARDS,
            null,
            null,
            null,
            null,
            null,
            null
        )
        while (cursor.moveToNext()) {
            val card = MyCard(
                avatarId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AVATAR_ID)),
                CardName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CARD_NAME)),
                CardCode = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CARD_CODE)),
                cardType = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CARD_TYPE)),
                ndefData = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NDEF_DATA)),
                manufacturer = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MANUFACTURER)),
                version = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VERSION))
            )
            cards.add(card)
        }
        cursor.close()
        db.close()
        return cards
    }
}

