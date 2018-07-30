package com.miladheydari.n3

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.*
import com.theartofdev.edmodo.cropper.CropImage
import ir.tapsell.sdk.*
import ir.tapsell.sdk.bannerads.TapsellBannerType
import ir.tapsell.sdk.bannerads.TapsellBannerView
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class MainActivity : AppCompatActivity(), View.OnClickListener {


    lateinit var btnGetImage: Button
    lateinit var btnSave: Button

    lateinit var etN: EditText

    lateinit var tapsellBannerView: TapsellBannerView
    lateinit var imgHelp: ImageView
    lateinit var lblHelp: TextView
    lateinit var linearLayout: LinearLayout

    private var mCropImageUri: Uri? = null

    private var tapsellAd: TapsellAd? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnGetImage = findViewById(R.id.get_image)
        tapsellBannerView = findViewById(R.id.banner1)

        imgHelp = findViewById(R.id.help)
        lblHelp = findViewById(R.id.lbl_help)

        imgHelp.setOnClickListener(this)
        lblHelp.setOnClickListener(this)

        btnSave = findViewById(R.id.save)
        btnSave.visibility = View.GONE
        btnSave.setOnClickListener(this)

        etN = findViewById(R.id.n)
        linearLayout = findViewById(R.id.ll)
        btnGetImage.setOnClickListener(this)

        if (!isStoragePermissionGranted())
            Toast.makeText(this, "ما برای ذخیره عکس به این دسترسی نیاز داریم", Toast.LENGTH_LONG).show()

        Tapsell.setRewardListener { tapsellAd, b ->


            if (forSaving) {
                forSaving = false
                if (b) {
                    imgList.reverse()

                    val current = System.currentTimeMillis()
                    imgList.forEachIndexed { index, bitmap -> saveBitmap(bitmap, String.format("$current-%02d.png", index)) }

                    Toast.makeText(this@MainActivity, "عکس ها ذخیره شد", Toast.LENGTH_LONG).show()

                } else {
                    Toast.makeText(this@MainActivity, "عکس ها ذخیره نشد", Toast.LENGTH_LONG).show()
                }
            }

        }
        tapsellBannerView.loadAd(this, BuildConfig.tapsellStandardBannerZoneId, TapsellBannerType.BANNER_320x100)


    }

    private fun requestAd(zoneId: String, cacheType: Int) {
        Tapsell.requestAd(this, zoneId,
                TapsellAdRequestOptions(cacheType),
                object : TapsellAdRequestListener {
                    override fun onAdAvailable(p0: TapsellAd?) {
                        tapsellAd = p0


                        if (CropImage.isExplicitCameraPermissionRequired(this@MainActivity) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(arrayOf(Manifest.permission.CAMERA), CropImage.CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE)

                        } else {
                            CropImage.startPickImageActivity(this@MainActivity)
                        }
                    }

                    override fun onExpiring(p0: TapsellAd?) {
                        Toast.makeText(this@MainActivity, "expire ad", Toast.LENGTH_LONG).show()
                        requestAd(zoneId, cacheType)
                    }

                    override fun onNoAdAvailable() {
                        Toast.makeText(this@MainActivity, "no ad available", Toast.LENGTH_LONG).show()

                    }

                    override fun onError(p0: String?) {
                        Toast.makeText(this@MainActivity, "error ad $p0", Toast.LENGTH_LONG).show()

                    }

                    override fun onNoNetwork() {
                        Toast.makeText(this@MainActivity, "لطفا به اینترنت وصل شده و دوباره تلاش کنید", Toast.LENGTH_LONG).show()

                    }
                })


    }

    private var forSaving: Boolean = false

    override fun onClick(p0: View?) {

        when (p0?.id) {
            R.id.help, R.id.lbl_help -> {


                Tapsell.requestAd(this, BuildConfig.tapsellVideoRewardZoneId,
                        TapsellAdRequestOptions(TapsellAdRequestOptions.CACHE_TYPE_STREAMED),
                        object : TapsellAdRequestListener {
                            override fun onAdAvailable(p0: TapsellAd?) {


                                val showOptions = TapsellShowOptions()
                                showOptions.isBackDisabled = false
                                showOptions.isImmersiveMode = true
                                showOptions.rotationMode = TapsellShowOptions.ROTATION_UNLOCKED
                                showOptions.isShowDialog = true

                                showOptions.warnBackPressedDialogMessage = "درصورت خروج عکس ها ذخیره نمی‌شود. ویدیو را ادامه میدهید؟"
                                showOptions.warnBackPressedDialogMessageTextColor = Color.RED

                                showOptions.warnBackPressedDialogPositiveButtonText = "بله"
                                showOptions.warnBackPressedDialogNegativeButtonText = "خیر"

                                showOptions.warnBackPressedDialogPositiveButtonTextColor = Color.RED
                                showOptions.warnBackPressedDialogNegativeButtonTextColor = Color.GREEN

                                showOptions.backDisabledToastMessage = "لطفا جهت بازگشت تا انتهای پخش ویدیو صبر کنید."

                                p0?.show(this@MainActivity, showOptions, object : TapsellAdShowListener {
                                    override fun onOpened(p0: TapsellAd?) {

//                                        Toast.makeText(this@MainActivity, "open", Toast.LENGTH_LONG).show()
                                    }

                                    override fun onClosed(p0: TapsellAd?) {
//                                        Toast.makeText(this@MainActivity, "close", Toast.LENGTH_LONG).show()
                                    }
                                })

                            }

                            override fun onExpiring(p0: TapsellAd?) {
                                Toast.makeText(this@MainActivity, "expire ad", Toast.LENGTH_LONG).show()

                            }

                            override fun onNoAdAvailable() {
                                Toast.makeText(this@MainActivity, "no ad available", Toast.LENGTH_LONG).show()

                            }

                            override fun onError(p0: String?) {
                                Toast.makeText(this@MainActivity, "error ad $p0", Toast.LENGTH_LONG).show()

                            }

                            override fun onNoNetwork() {
                                Toast.makeText(this@MainActivity, "لطفا به اینترنت وصل شده و دوباره تلاش کنید", Toast.LENGTH_LONG).show()

                            }
                        })


            }
            R.id.get_image -> {
                requestAd(BuildConfig.tapsellVideoRewardZoneId, TapsellAdRequestOptions.CACHE_TYPE_STREAMED)
            }
            R.id.save -> {

                forSaving = true

                if (tapsellAd != null) {

                    val showOptions = TapsellShowOptions()
                    showOptions.isBackDisabled = true
                    showOptions.isImmersiveMode = true
                    showOptions.rotationMode = TapsellShowOptions.ROTATION_UNLOCKED
                    showOptions.isShowDialog = true

                    showOptions.warnBackPressedDialogMessage = "درصورت خروج عکس ها ذخیره نمی‌شود. ویدیو را ادامه میدهید؟"
                    showOptions.warnBackPressedDialogMessageTextColor = Color.RED

                    showOptions.warnBackPressedDialogPositiveButtonText = "بله"
                    showOptions.warnBackPressedDialogNegativeButtonText = "خیر"

                    showOptions.warnBackPressedDialogPositiveButtonTextColor = Color.RED
                    showOptions.warnBackPressedDialogNegativeButtonTextColor = Color.GREEN

                    showOptions.backDisabledToastMessage = "لطفا جهت بازگشت تا انتهای پخش ویدیو صبر کنید."

                    tapsellAd?.show(this@MainActivity, showOptions, object : TapsellAdShowListener {
                        override fun onOpened(p0: TapsellAd?) {

//                            Toast.makeText(this@MainActivity, "open", Toast.LENGTH_LONG).show()
                        }

                        override fun onClosed(p0: TapsellAd?) {
//                            Toast.makeText(this@MainActivity, "close", Toast.LENGTH_LONG).show()

                            forSaving = false
                        }
                    })

                }


            }

        }

    }

    private fun isStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("N3", "Permission is granted")
                true
            } else {

                Log.v("N3", "Permission is revoked")
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 5)
                false
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v("N3", "Permission is granted")
            true
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

        val l = bitmap?.width?.div(3)

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

        if (requestCode == 5) {
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
