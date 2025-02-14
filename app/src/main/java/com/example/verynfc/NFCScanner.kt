package com.example.verynfc

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.nfc.tech.MifareClassic
import android.nfc.tech.MifareUltralight
import android.nfc.tech.Ndef
import android.nfc.tech.NfcA
import android.nfc.tech.NfcB
import android.nfc.tech.NfcF
import android.nfc.tech.NfcV
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.nio.ByteBuffer


class NFCScanner : AppCompatActivity() {
    private lateinit var nfcAdapter: NfcAdapter;
    var paddingIntent : PendingIntent? = null;
    var tempcard:MyCard = MyCard(
        avatarId = 0x0,
        CardName = "",
        CardCode = 0,
        cardType = "",
        ndefData = "",
        manufacturer = "",
        version = ""
    );
    var cardtypeSign:Int=0;
    val fetchBack=Intent();
    override fun onCreate(savedInstanceState: Bundle?) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        super.onCreate(savedInstanceState);
        nfcAdapter=NfcAdapter.getDefaultAdapter(this);
        paddingIntent = PendingIntent.getActivity(this,0,Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE);
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        cardtypeSign=0;
        var tag: Tag?=null;
        if (intent != null) {
            if (intent.action == NfcAdapter.ACTION_TAG_DISCOVERED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13
                    if (intent != null) {
                        tag=intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
                    }
                } else {
                    if (intent != null) {
                        tag=intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
                    }
                }
            }
        }

        if (tag != null) {
            // 读取卡片信息
            val cardType = getCardType(tag)
            var uid:Long=0;
            val ndefData = readNdefData(tag);
            //val mfc: String =tag.techList.toString();
            QuickToast.ShowToast(this,cardType);
            if(cardtypeSign==1){    //Mifare
                uid=getUidM1(tag);
                //QuickToast.ShowToast(this,"got UID");
                fetchBack.putExtra("fetchedCode",uid);
                fetchBack.putExtra("fetchType",cardType);
                fetchBack.putExtra("fetchNdef",ndefData);
                fetchBack.putExtra("readStatus",true);
                //fetchBack.putExtra("readMfc",mfc);
            }
            else if(cardtypeSign==3){   //NFC-A
                uid=getUid(tag);
                fetchBack.putExtra("fetchedCode",uid);
                fetchBack.putExtra("fetchType",cardType);
                fetchBack.putExtra("fetchNdef",ndefData);
                fetchBack.putExtra("readStatus",true);
            }
            else{
                fetchBack.putExtra("readStatus",false);
                finish();
            }
            setResult(RESULT_OK,fetchBack);
            overridePendingTransition(0, 0);
            finish();
        }
        else {
            fetchBack.putExtra("readStatus",false);
            setResult(RESULT_OK,fetchBack);
            finish();
        }
    }
    override fun onResume() {
        super.onResume()
        nfcAdapter?.enableForegroundDispatch(this, this.paddingIntent, null, null)
        if (NfcAdapter.ACTION_TAG_DISCOVERED == intent.action) {
            // 当 NFC 标签被扫描时触发
            onNewIntent(intent);
        }
    }

    override fun onBackPressed() {
        super.onBackPressed();
        fetchBack.putExtra("readStatus",-1);
        setResult(RESULT_OK,fetchBack);
        overridePendingTransition(0, 0);
    }

    //solution for M1 cards begins
    private fun getUidM1(tag: Tag?): Long {
        val uidByteArray = tag?.id
        //Toast.makeText(this,uidByteArray.toString(),Toast.LENGTH_SHORT).show();
        if (uidByteArray != null) {
            if (uidByteArray.size == 4 ) {
                // 将 UID 字节数组转换为十进制 Long
                    val uid:Int=ByteBuffer.wrap(uidByteArray).getInt();
                    return uid.toLong();
            }
            else{
                return -1;
            }
        }
        else{
            return -1;
        }
    }

    //solution for M1 cards ends


    private fun getUid(tag:Tag): Long {
        val uidByteArray = tag.id
        if (uidByteArray.size == 4) {
            // 将 UID 字节数组转换为 Long
            val uidLong = ByteBuffer.wrap(uidByteArray).long
            return uidLong
        }
        else if (uidByteArray.size == 7) {
            val uidString = uidByteArray.joinToString("") { "%02X".format(it) }
            println("UID: $uidString") // 打印或进一步处理 7 字节 UID
            return -1
        }
        else {
            return -1
        }
    }

    private fun getCardType(tag: Tag): String {
        when {
            MifareClassic.get(tag) != null -> {
                cardtypeSign=1;
                return "Mifare_Classic"
            }
            MifareUltralight.get(tag) != null -> {
                cardtypeSign=2;
                return "Mifare_Ultralight"
            }
            NfcA.get(tag) != null -> {
                cardtypeSign=3;
                return "NFC_A"
            }
            NfcB.get(tag) != null -> {
                cardtypeSign=4;
                return "NFC_B"
            }
            NfcF.get(tag) != null -> {
                cardtypeSign=5;
                return "FeliCa"
            }
            NfcV.get(tag) != null -> {
                cardtypeSign=6;
                return "NFC_V"
            }
            IsoDep.get(tag) != null -> {
                cardtypeSign=7;
                return "ISO_14443_4"
            }
            Ndef.get(tag) != null -> {
                cardtypeSign=8;
                return "NDEF"
            }
            else -> {
                cardtypeSign=0;
                return "N/A"
            }
        }
    }

    private fun readNdefData(tag: Tag): String? {
        try {
            val ndef = NfcA.get(tag)  // 使用 NfcA 类来读取数据（假设是 ISO 14443-A 卡片）
            ndef.connect()
            val message = ndef.transceive(byteArrayOf(0x30)) // 示例命令，具体命令根据卡片类型而不同
            ndef.close()
            return message.joinToString(", ") { "%02X".format(it) }
        } catch (e: Exception) {
            return null
        }
    }
}

