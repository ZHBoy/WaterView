package com.hao.idelhandlerusedemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.os.MessageQueue
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginBottom
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity() {

    private var tvAddView: Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvAddView = findViewById(R.id.tvAddView)

        tvAddView?.setOnClickListener {
            startAddViewOnTimeFree()
            for (i in 0..5) {
                addViewToMain("当前UI线程在忙...")
            }
        }
    }

    private fun startAddViewOnTimeFree() {
        if (!isFinishing) {
            Looper.myQueue().addIdleHandler(idleHandler.value)
        }
    }

    /**
     * 添加view到页面
     */
    private fun addViewToMain(title: String) {
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
        )
        val textView = TextView(this)
        textView.textSize = 20f
        textView.text = title

        lp.topMargin = 20
        (tvAddView?.parent as ViewGroup).addView(textView, lp)
    }

    private val idleHandler = lazy {
        AnimStarterHandler(this)
    }

    /**
     * 在UI线程不忙的时候去添加view
     */
    class AnimStarterHandler(fmt: MainActivity) :
        MessageQueue.IdleHandler {
        private val activityRef = WeakReference(fmt)

        override fun queueIdle(): Boolean {
            val mActivity = activityRef.get()
            if (mActivity != null && !mActivity.isFinishing) {
                mActivity.addViewToMain("UI线程空闲时，添加的view")
            }
            return false
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (idleHandler.isInitialized()) {
            Looper.myQueue().removeIdleHandler(idleHandler.value)
        }
    }

}