package com.omeryalap.myapplication


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.omeryalap.myapplication.EncodeAndDecodeHelper.Decode
import com.omeryalap.myapplication.EncodeAndDecodeHelper.Encode
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var encodeButton: Button
    private lateinit var decodeButton: Button
    private lateinit var encodedImage: Bitmap


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        encodeButton = findViewById(R.id.encodeButton)
        decodeButton = findViewById(R.id.decodeButton)

        // Orijinal boyutları almak için options kullan
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        val drawableId = Constants.SOURCE_IMAGE_RESOURCE
        BitmapFactory.decodeResource(resources, drawableId, options)
        val originalWidth = options.outWidth
        val originalHeight = options.outHeight
        println("Orijinal Boyut: $originalWidth x $originalHeight")

        // Hedef genişlik ve yükseklik
        val desiredWidth = originalWidth
        val desiredHeight = originalHeight

        // Orijinal boyutları kullanarak gerçek bir Bitmap oluştur
        val originalImageOptions = BitmapFactory.Options()

        // Önceki boyutları kullanma, sıfırdan başla
        originalImageOptions.inJustDecodeBounds = false

        // Hedef boyutlara uygun inSampleSize hesapla
        originalImageOptions.inSampleSize =
            calculateInSampleSize(originalWidth, originalHeight, desiredWidth, desiredHeight)

        val originalImage: Bitmap = BitmapFactory.decodeResource(
            resources,
            drawableId,
            originalImageOptions
        )

        // Oluşturulan Bitmap'i bir ImageView'e atayabilirsiniz
        imageView.setImageBitmap(originalImage)
        val width = originalWidth // İstenen genişlik
        val height = originalHeight // İstenen yükseklik
        val scaledImage = Bitmap.createScaledBitmap(originalImage, width, height, true)
        imageView.setImageBitmap(scaledImage)

        if (checkPermission()) {
            // İzin zaten verilmişse, dosya işlemine devam edebilirsiniz.
            // Burada dosyayı oluşturabilir veya kaydedebilirsiniz.
        } else {
            requestPermission()
        }

        encodeButton.setOnClickListener {
            encodedImage = Encode(scaledImage, Constants.TEXT_TO_HIDE)
            imageView.setImageBitmap(encodedImage)
        }

        decodeButton.setOnClickListener {
            val drawable = imageView.drawable
            var bitmap: Bitmap? = null
            bitmap = readBitmap()
            if (drawable is BitmapDrawable) {
                var extractedText = Decode(bitmap)
                println("Çıkarılan Metin: $extractedText")
            }
        }
    }

    // İzin kontrolü
    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    // İzin iste
    @RequiresApi(Build.VERSION_CODES.R)
    private fun requestPermission() {
        if (!Environment.isExternalStorageManager()) {
            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            val launcher =
                registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    if (checkPermission()) {
                        Toast.makeText(
                            this,
                            "İzin verildi. Artık dosya işlemlerine devam edebilirsiniz.",
                            Toast.LENGTH_LONG
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            "İzin verilmedi. Dosyayı kaydetmek için izin gereklidir.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            launcher.launch(intent)
        }
    }

    private fun readBitmap(): Bitmap? {
        val fileName = Constants.getFullFilePath()
        return BitmapFactory.decodeFile(fileName)
    }

    private fun saveBitmapToFile(bitmap: Bitmap, fileName: String): File {
        val file = File(externalCacheDir, fileName)
        try {
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file
    }

    private fun calculateInSampleSize(
        originalWidth: Int,
        originalHeight: Int,
        desiredWidth: Int,
        desiredHeight: Int
    ): Int {
        var inSampleSize = 1

        if (originalWidth > desiredWidth || originalHeight > desiredHeight) {
            val halfWidth = originalWidth / 2
            val halfHeight = originalHeight / 2

            while ((halfWidth / inSampleSize) >= desiredWidth && (halfHeight / inSampleSize) >= desiredHeight) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }
}

