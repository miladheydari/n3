package com.miladheydari.n3

import android.Manifest
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.theartofdev.edmodo.cropper.CropImage
import android.app.Activity
import android.content.Intent
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.graphics.Bitmap
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.util.TypedValue
import android.view.Gravity
import android.widget.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.provider.MediaStore.Images.Media.getBitmap
import android.graphics.drawable.BitmapDrawable
import java.io.OutputStream


class MainActivity : AppCompatActivity(), View.OnClickListener {


    lateinit var btnGetImage: Button
    lateinit var btnSave: Button

    lateinit var etN: EditText

    lateinit var linearLayout: LinearLayout

    private var mCropImageUri: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnGetImage = findViewById(R.id.get_image)
        btnSave = findViewById(R.id.save)
        btnSave.visibility = View.GONE
        btnSave.setOnClickListener(this)

        etN = findViewById(R.id.n)
        linearLayout = findViewById(R.id.ll)
        btnGetImage.setOnClickListener(this)

    }


    override fun onClick(p0: View?) {

        when (p0?.id) {
            R.id.get_image -> {
                if (CropImage.isExplicitCameraPermissionRequired(this) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(arrayOf(Manifest.permission.CAMERA), CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE)

                } else {
                    CropImage.startPickImageActivity(this)
                }
            }
            R.id.save -> {

                imgList.reverse()

                val current = System.currentTimeMillis()
                imgList.forEachIndexed { index, bitmap -> saveBitmap(bitmap, String.format("$current-%02d.png", index)) }


            }

        }

    }

    private fun saveBitmap(bitmap: Bitmap, filename: String) {

        val output: OutputStream
        // Find the SD Card path
        val filepath = Environment.getExternalStorageDirectory()

        // Create a new folder in SD Card
        val dir = File(filepath.absolutePath + "/n3/")
        if (!dir.exists())
            dir.mkdirs()

        // Retrieve the image from the res folder

        // Create a name for the saved image
        val file = File(dir, filename)

        try {

            output = FileOutputStream(file)

            // Compress into png format image from 0% - 100%
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output)
            output.flush()
            output.close()

            val values = ContentValues()

            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis())
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/*")
            values.put(MediaStore.MediaColumns.DATA, file.absolutePath)

            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)


//            MediaStore.Images.Media.insertImage(contentResolver, bitmap,
//                    filename, null)
        } catch (e: Exception) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }




        MediaStore.Images.Media.insertImage(contentResolver, bitmap, "", "")
    }

    private var resultUri: Uri? = null

    private var bitmap: Bitmap? = null

    @SuppressLint("NewApi")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // handle result of pick image chooser
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val imageUri = CropImage.getPickImageResultUri(this, data)

            // For API >= 23 we need to check specifically that we have permissions to read external storage.
            if (CropImage.isReadExternalStoragePermissionsRequired(this, imageUri)) {
                // request permissions and handle the result in onRequestPermissionsResult()
                mCropImageUri = imageUri
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE)
            } else {
                // no permissions required or already granted, can start crop image activity
                startCropImageActivity(imageUri)
            }
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val result = CropImage.getActivityResult(data)

            resultUri = result.uri


            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)

                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), -5)
            else btnSave.visibility = View.VISIBLE
            bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, resultUri)
            splitImage()
        }
    }

    private val imgList: MutableList<Bitmap> = ArrayList()


    private fun splitImage() {
        imgList.clear()
        linearLayout.removeAllViews()

        val l = bitmap?.width?.div(3)?.toInt()

        var yCoord = 0
        for (i in 1..y) {
            var xCoord = 0

            val hor = LinearLayout(this)
            hor.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT)

            hor.orientation = LinearLayout.HORIZONTAL
            hor.gravity = Gravity.CENTER

            for (j in 1..3) {
                val bit = Bitmap.createBitmap(bitmap, xCoord, yCoord, l as Int, l)
                imgList.add(bit)
                val imageView = ImageView(this)
                imageView.layoutParams = LinearLayout.LayoutParams(
                        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120f, resources.displayMetrics).toInt()
                        , TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 120f, resources.displayMetrics).toInt())
                imageView.setPadding(5, 5, 5, 5)
                imageView.setImageBitmap(bit)
                hor.addView(imageView)
                xCoord += l
            }
            yCoord += l as Int

            linearLayout.addView(hor)
        }

    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                CropImage.startPickImageActivity(this)
            } else {
                Toast.makeText(this, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show()
            }
        }
        if (requestCode == CropImage.PICK_IMAGE_PERMISSIONS_REQUEST_CODE) {
            if (mCropImageUri != null && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // required permissions granted, start crop image activity
                startCropImageActivity(mCropImageUri!!)
            } else {
                Toast.makeText(this, "Cancelling, required permissions are not granted", Toast.LENGTH_LONG).show()
            }
        }

        if (requestCode == -5) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                btnSave.visibility = View.VISIBLE
            } else {
                btnSave.visibility = View.GONE
            }
        }
    }


    private var y: Int = 1

    private fun startCropImageActivity(imageUri: Uri) {

        if (etN.text.toString().isNotEmpty())
            y = etN.text.toString().toInt()

        CropImage.activity(imageUri).setAspectRatio(3, y)
                .start(this)
    }
}
