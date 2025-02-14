package com.example.verynfc

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

sealed class SettingsItem {
    data class Title(val title: String) : SettingsItem()
    data class Normal(val name: String, val onClick: () -> Unit) : SettingsItem()
    data class Checkbox(
        val name: String,
        var checkStatus: Boolean,
        val onCheckedChange: (Boolean) -> Unit
    ) : SettingsItem()

    data class Switch(
        val name: String,
        var switchStatus: Boolean,
        val onTurned: (Boolean) -> Unit
    ) : SettingsItem()

    data class Dialog(val name: String, var dialogMessage: String) : SettingsItem()
}


class SettingsPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_page);
        val settingsView:RecyclerView=findViewById(R.id.SettingsView);
        settingsView.layoutManager=LinearLayoutManager(this);

        /*Edit your "Settings" page's content here.*/
        val settingsContentList= listOf(
            SettingsItem.Title("BACKUP AND RESTORE"),
            SettingsItem.Normal(
                "Restore cards from device",
                onClick = { AlertDialog.Builder(this).apply {
                    setTitle("Error");
                    setMessage("Service current unavailable");
                    setPositiveButton("OK"){
                            _Parcel,_->
                    }
                }
                    .show()}),
            SettingsItem.Normal(
                "Restore data",
                onClick = { AlertDialog.Builder(this).apply {
                    setTitle("Error");
                    setMessage("Service current unavailable");
                    setPositiveButton("OK"){
                            _Parcel,_->
                    }
                }
                    .show()}),
            SettingsItem.Normal(
                "Backup all cards and settings" ,
                onClick = {
                    QuickToast.ShowToast(this,"Please select a folder!");
                }),
            SettingsItem.Title("GENERAL"),
            SettingsItem.Checkbox("Use compact mode",
                false,
                onCheckedChange = { false }),
            SettingsItem.Switch("Enable encrypted card reader mode",
                false,
                onTurned = {null}),
            SettingsItem.Title("ABOUT"),
            SettingsItem.Normal("About VeryNFC",
                onClick = {
                    val intent=Intent(this,AboutPage::class.java);
                    startActivity(intent);
                })
        );
        /*Edit your "Settings" page's content here.*/

        val settingsPageAdapter=SettingsAdapter(
            settingsContentList,
            onOptionClick = { position ->
            },
            onCheckboxChange = { position, isChecked ->
            },
            onSwitchChanged = { position,isChecked->
            },
        );
        settingsView.adapter=settingsPageAdapter;
    }

}

class SettingsAdapter(
    private val settingsList:List<SettingsItem>,
    private val onOptionClick:(Int)->Unit,
    private val onCheckboxChange:(Int,Boolean)->Unit,
    private val onSwitchChanged:(Int,Boolean)->Unit
):RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    companion object{
        private const val TITLE=0;
        private const val NORMAL=1;
        private const val CHBOX=2;
        private const val SWITCH=3;
        private const val DIALOG=4;
    }

    override fun getItemViewType(position: Int): Int {
        when(settingsList[position]){
            is SettingsItem.Title->return TITLE;
            is SettingsItem.Normal->return NORMAL;
            is SettingsItem.Checkbox->return CHBOX;
            is SettingsItem.Dialog->return DIALOG;
            is SettingsItem.Switch->return SWITCH;
        }
    }

    inner class TitleViewHolder(view:View):RecyclerView.ViewHolder(view){
        private val titleText:TextView=view.findViewById(R.id.titletext);
        fun setTitle(item:SettingsItem.Title){
            titleText.text=item.title;
        }
    }
    inner class NormalViewHolder(view:View):RecyclerView.ViewHolder(view){
        private val normalText:TextView=view.findViewById(R.id.NormalText);
        fun setNormalText(item:SettingsItem.Normal,position:Int){
            normalText.text=item.name;
            itemView.setOnClickListener {
                item.onClick();
            }
        }
    }
    inner class CheckboxViewHolder(view:View):RecyclerView.ViewHolder(view){
        private val ChboxText:TextView=view.findViewById(R.id.checkedText);
        private val checkBox:CheckBox=view.findViewById(R.id.CheckBoxAction);
        fun setCheckBox(item: SettingsItem.Checkbox, position: Int){
            ChboxText.text=item.name;
            checkBox.isChecked=item.checkStatus;
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                item.onCheckedChange(isChecked);
            }
        }
    }
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    inner class SwitchViewHolder(view:View):RecyclerView.ViewHolder(view){
        private val switchText:TextView=view.findViewById(R.id.SwitchText);
        private val switch:Switch=view.findViewById(R.id.switchAction);
        fun setSwitch(item:SettingsItem.Switch,position:Int){
            switchText.text=item.name;
            switch.isChecked=item.switchStatus;
            switch.setOnCheckedChangeListener { _, isChecked ->
                item.onTurned(isChecked);
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
         return when(viewType){
            TITLE->{
                val view=LayoutInflater.from(parent.context).inflate(R.layout.settings_layout_title_1,parent,false);
                TitleViewHolder(view);
            }
            NORMAL->{
                val view=LayoutInflater.from(parent.context).inflate(R.layout.settings_layout_1,parent,false);
                NormalViewHolder(view);
            }
            CHBOX->{
                val view=LayoutInflater.from(parent.context).inflate(R.layout.settings_layout_checked_1,parent,false);
                CheckboxViewHolder(view);
            }
            SWITCH->{
                val view=LayoutInflater.from(parent.context).inflate(R.layout.settings_layout_switched_1,parent,false);
                SwitchViewHolder(view);
            }
            else -> throw IllegalArgumentException("Layout generate exception: NULL");
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item=settingsList[position];
        when(item){
            is SettingsItem.Title->(holder as TitleViewHolder).setTitle(item);
            is SettingsItem.Normal->(holder as NormalViewHolder).setNormalText(item,position);
            is SettingsItem.Switch->(holder as SwitchViewHolder).setSwitch(item,position);
            is SettingsItem.Checkbox->(holder as CheckboxViewHolder).setCheckBox(item,position);
            else -> throw IllegalArgumentException("Layout generate exception: NULL");
        }
    }

    override fun getItemCount(): Int = settingsList.size;
}

