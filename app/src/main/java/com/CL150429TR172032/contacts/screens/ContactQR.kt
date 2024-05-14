package com.CL150429TR172032.contacts.screens

import android.graphics.Bitmap
import android.graphics.Point
import android.os.Bundle
import android.view.Display
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.appcompat.app.AppCompatActivity
import com.CL150429TR172032.contacts.R

class ContactQR : AppCompatActivity() {

    private lateinit var contactNameLabel: TextView
    private lateinit var qrCode: ImageView
    private lateinit var contactName: String
    private lateinit var contactPhone: String
    lateinit var qrEncoder: QRGEncoder
    lateinit var bitmap: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_qr)

        contactNameLabel = findViewById(R.id.contactNameLabel)
        qrCode = findViewById(R.id.qrCodeImage)

        contactName = intent.getStringExtra("contactName") ?: ""
        contactPhone = intent.getStringExtra("contactPhone") ?: ""

        contactNameLabel.text = contactName

        val windowManager: WindowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val display: Display = windowManager.defaultDisplay

        val point: Point = Point()
        display.getSize(point)

        val width = point.x
        val height = point.y

        var dimen = if (width < height) width else height
        dimen = dimen * 3 / 4

        qrEncoder = QRGEncoder("+503${contactPhone}", null, QRGContents.Type.TEXT, dimen)

        try {
            bitmap = qrEncoder.getBitmap(0)
            qrCode.setImageBitmap(bitmap)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}