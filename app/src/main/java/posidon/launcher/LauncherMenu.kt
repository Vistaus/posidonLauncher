package posidon.launcher

import android.app.ActivityOptions
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.view.Gravity
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import android.view.View
import android.view.View.OnLongClickListener
import android.view.Window
import android.view.animation.PathInterpolator
import com.google.android.material.bottomsheet.BottomSheetBehavior
import posidon.launcher.customizations.Customizations
import posidon.launcher.external.WidgetManager
import posidon.launcher.storage.Settings
import posidon.launcher.tools.Device
import posidon.launcher.tools.Tools
import posidon.launcher.tools.vibrate
import posidon.launcher.wall.Gallery
import java.util.*

class LauncherMenu(private val context: Context, private val window: Window) : OnLongClickListener {

    override fun onLongClick(v: View): Boolean {
        if (!isActive) menu(context, window)
        return true
    }

    internal class PinchListener(private val context: Context, private val window: Window) : SimpleOnScaleGestureListener() {
        override fun onScale(d: ScaleGestureDetector) = true
        override fun onScaleEnd(d: ScaleGestureDetector) {
            if (!isActive) menu(context, window)
        }
    }

    companion object {
        var isActive = false
        var dialog: Dialog? = null
        private fun menu(context: Context, window: Window) {
            isActive = true
            context.vibrate()
            val homescreen = window.decorView.findViewById<View>(android.R.id.content)
            val page = homescreen.findViewById<View>(R.id.desktop)
            page.animate().scaleX(0.65f).scaleY(0.65f).translationY(page.height * -0.05f).setInterpolator(PathInterpolator(0.245f, 1.275f, 0.405f, 1.005f)).duration = 450L
            val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(homescreen.findViewById<View>(R.id.drawer))
            behavior.isHideable = true
            behavior.state = BottomSheetBehavior.STATE_HIDDEN
            dialog = Dialog(context, R.style.longpressmenusheet)
            dialog!!.setContentView(R.layout.menu)
            dialog!!.window!!.setGravity(Gravity.BOTTOM)
            dialog!!.findViewById<View>(R.id.custombtn).setOnClickListener {
                val i = Intent(context, Customizations::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                context.startActivity(i, ActivityOptions.makeCustomAnimation(context, R.anim.slideup, R.anim.home_exit).toBundle())
                dialog!!.dismiss()
            }
            dialog!!.findViewById<View>(R.id.wallbtn).setOnClickListener {
                val i = Intent(context, Gallery::class.java)
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                context.startActivity(i, ActivityOptions.makeCustomAnimation(context, R.anim.slideup, R.anim.home_exit).toBundle())
                dialog!!.dismiss()
            }
            dialog!!.findViewById<View>(R.id.widgetpickerbtn).setOnClickListener {
                WidgetManager.selectWidget()
                dialog!!.dismiss()
            }
            page.setBackgroundResource(R.drawable.page)
            if (Tools.canBlurWall(context)) window.setBackgroundDrawable(LayerDrawable(arrayOf(BitmapDrawable(context.resources, Tools.blurredWall(context, Settings["blurradius", 15f])), context.getDrawable(R.drawable.black_gradient)))) else window.setBackgroundDrawableResource(R.drawable.black_gradient)
            homescreen.setOnClickListener { dialog!!.dismiss() }
            dialog!!.setOnDismissListener { exit(homescreen, window, behavior) }
            dialog!!.show()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val list = ArrayList<Rect>()
                homescreen.systemGestureExclusionRects = list
            }
        }

        private fun exit(homescreen: View, window: Window, behavior: BottomSheetBehavior<*>) {
            behavior.state = BottomSheetBehavior.STATE_COLLAPSED
            val page = homescreen.findViewById<View>(R.id.desktop)
            page.animate().scaleX(1f).scaleY(1f).translationY(0f).duration = 400L
            page.setBackgroundColor(0x0)
            window.setBackgroundDrawableResource(android.R.color.transparent)
            behavior.isHideable = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val list = ArrayList<Rect>()
                list.add(Rect(0, 0, Device.displayWidth, Device.displayHeight))
                homescreen.systemGestureExclusionRects = list
            }
            isActive = false
        }
    }
}