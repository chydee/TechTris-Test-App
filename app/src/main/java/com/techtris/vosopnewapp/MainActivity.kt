package com.techtris.vosopnewapp

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.widget.MediaController
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.facebook.FacebookSdk
import com.facebook.share.model.SharePhoto
import com.facebook.share.model.SharePhotoContent
import com.facebook.share.model.ShareVideo
import com.facebook.share.model.ShareVideoContent
import com.facebook.share.widget.ShareDialog
import com.fxn.pix.Options
import com.fxn.pix.Pix
import com.fxn.utility.PermUtil
import com.techtris.vosopnewapp.databinding.ActivityMainBinding
import java.io.File


@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "DEPRECATION")
class MainActivity : AppCompatActivity() {

    private var options: Options? = null
    private var returnValue: ArrayList<String> = ArrayList()

    private lateinit var binding: ActivityMainBinding
    private lateinit var shareDialog: ShareDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        FacebookSdk.setApplicationId(getString(R.string.app_id))
        FacebookSdk.sdkInitialize(applicationContext)
        shareDialog = ShareDialog(this)
        options = Options.init()
            .setRequestCode(100)
            .setFrontfacing(false)
            .setPreSelectedUrls(returnValue)
            .setExcludeVideos(false)
            .setVideoDurationLimitinSeconds(60)
            .setScreenOrientation(Options.SCREEN_ORIENTATION_PORTRAIT)
            .setPath("/chydee/new")
        binding.upload.setOnClickListener {
            options?.preSelectedUrls = returnValue
            Pix.start(this@MainActivity, options);
        }

        val mediaController = MediaController(applicationContext)
        binding.selectedVideo.setMediaController(mediaController)
        mediaController.setAnchorView(binding.selectedVideo)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            100 -> {
                binding.apply {
                    upload.isGone = true
                    shareWithAndroid.isVisible = true
                    shareWithFacebook.isVisible = true
                }
                if (resultCode == Activity.RESULT_OK) {
                    returnValue = data?.getStringArrayListExtra(Pix.IMAGE_RESULTS)!!
                    val f = File(returnValue[0])
                    val bitmap: Bitmap
                    if (f.absolutePath.endsWith("mp4")) {
                        bitmap = BitmapDrawable(applicationContext.resources, f.absolutePath).bitmap
                        val videoUri = FileProvider.getUriForFile(
                            this@MainActivity,
                            applicationContext.packageName + ".fileProvider",
                            f.absoluteFile
                        )
                        binding.apply {
                            selectedImage.isGone = true
                            selectedVideo.isVisible = true
                            selectedVideo.setVideoURI(videoUri)
                            shareWithAndroid.setOnClickListener { shareVideo(videoUri) }
                            shareWithFacebook.setOnClickListener { shareVideoWithFaceBook(uri = videoUri) }
                        }
                    } else {
                        bitmap = BitmapDrawable(applicationContext.resources, f.absolutePath).bitmap
                        binding.apply {
                            selectedVideo.isGone = true
                            selectedImage.setImageBitmap(bitmap)
                            shareWithAndroid.setOnClickListener {
                                shareImage(f.absoluteFile)
                            }
                            shareWithFacebook.setOnClickListener { shareImageWithFacebook(bitmap) }
                        }
                    }

                }
            }
        }
    }

    private fun shareImageWithFacebook(bitmap: Bitmap) {
        val photo: SharePhoto = SharePhoto.Builder()
            .setBitmap(bitmap)
            .build();
        val content: SharePhotoContent = SharePhotoContent.Builder()
            .addPhoto(photo)
            .build()
        shareDialog.show(content)
    }

    private fun shareVideoWithFaceBook(uri: Uri) {
        val vid: ShareVideo = ShareVideo.Builder()
            .setLocalUrl(uri)
            .build();
        val content = ShareVideoContent.Builder()
            .setVideo(vid)
            .build();
        shareDialog.show(content)
    }

    private fun shareImage(file: File) {
        val imageUri = FileProvider.getUriForFile(
            this@MainActivity,
            applicationContext.packageName + ".fileProvider",
            file
        )
        val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_STREAM,
                imageUri
            )
            type = "image/jpeg"
        }
        startActivity(
            Intent.createChooser(
                shareIntent,
                resources.getText(R.string.send_to)
            )
        )
    }

    private fun shareVideo(uri: Uri) {
        val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(
                Intent.EXTRA_STREAM,
                uri
            )
            type = "video/mp4"
        }
        startActivity(
            Intent.createChooser(
                shareIntent,
                resources.getText(R.string.send_to)
            )
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PermUtil.REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Pix.start(this@MainActivity, Options.init().setRequestCode(100))
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "Approve permissions to access camera and gallery",
                        Toast.LENGTH_LONG
                    ).show()
                }
                return
            }
        }
    }
}
