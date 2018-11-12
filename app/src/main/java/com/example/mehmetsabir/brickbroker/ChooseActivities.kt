package com.example.mehmetsabir.brickbroker

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.InputFilter
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast

class ChooseActivities : AppCompatActivity() {
    private var radioGroup : RadioGroup? = null
    private var btnOk :  Button? = null
    private var btnCancel :  Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_activities)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        openDialogScreen()
    }


    private fun openDialogScreen() {


        val inflater = this.layoutInflater
        val view = inflater.inflate(R.layout.alertlayout, null)

        radioGroup = view.findViewById(R.id.rbtnGrpLevel)
        btnOk = view.findViewById(R.id.btnOkay)
        btnCancel = view.findViewById(R.id.btnCancel)

        val alert = AlertDialog.Builder(this)
        alert.setView(view)
        alert.setCancelable(false)
        val dialog = alert.create()

        btnOk?.setOnClickListener {

            changeLevel(radioGroup?.checkedRadioButtonId!!)

            dialog.cancel()
        }
        btnCancel?.setOnClickListener {
            dialog.cancel()
        }


        dialog.show()

    }

    private fun changeLevel(position : Int)  {

        val intent : Intent = Intent(this@ChooseActivities,BrickBroker::class.java)
        var level : Int = 0

        when (position) {
            R.id.rbEasy -> {
                level=2
            }
            R.id.rbMedium ->{
                level=4
            }
            R.id.rbHard ->{
                level=6
            }
            else -> {
                level=2
                Toast.makeText(this@ChooseActivities,"Seçilmedi", Toast.LENGTH_LONG).show()
            }
        }

        intent.putExtra("level",level)
        startActivity(intent)
    }
}
