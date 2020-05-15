package me.ariel.redditop

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_full_image.*


class FullImageActivity : AppCompatActivity() {

    companion object {

        const val ARG_IMAGE_URL = "ARG_IMAGE_URL"
        fun openWithImageUrl(context: Context, url:String) {
            val intent = Intent(context, FullImageActivity::class.java)
            val bundle = Bundle()
            bundle.putString(ARG_IMAGE_URL, url)
            intent.putExtras(bundle)
            context.startActivity(intent)
        }
    }

    private var scaleFactor = 1f

    private val scaleListener = object : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor

            // Don't let the object get too small or too large.
            scaleFactor = Math.max(1f, Math.min(scaleFactor, 6.0f))

            full_image?.apply {
                scaleX = scaleFactor
                scaleY = scaleFactor
                invalidate()
            }

            return true
        }
    }

    private val panListener = object : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
            full_image?.apply {
                if (scaleX > 1) {
                    scrollX += (distanceX / scaleX).toInt()
                    scrollY += (distanceY / scaleY).toInt()
                } else {
                    scrollX = 0
                    scrollY = 0
                }
            }
            return true
        }

        override fun onDoubleTap(e: MotionEvent?): Boolean {
            resetZoomAndPan()
            return true
        }

    }

    private val scaleDetector by lazy { ScaleGestureDetector(this, scaleListener) }
    private val panDetector by lazy { GestureDetector(this, panListener) }

    private fun resetZoomAndPan() {
        scaleFactor = 1f
        full_image?.apply {
            scrollX = 0
            scrollY = 0
            scaleX = 1f
            scaleY = 1f
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_image)

        val url = intent.extras?.getString(ARG_IMAGE_URL)
        if(url != null) {
            Glide.with(this)
                .load(url)
                .fitCenter()
                .into(full_image)
        } else {
            finish()
        }

        full_image_container.setOnTouchListener { v, event ->
            scaleDetector.onTouchEvent(event) or panDetector.onTouchEvent(event)
        }

        btn_close.setOnClickListener {
            finish()
        }

    }

}