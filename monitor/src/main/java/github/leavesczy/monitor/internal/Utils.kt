package github.leavesczy.monitor.internal

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.ShareCompat
import github.leavesczy.monitor.R

/**
 * @Author: leavesCZY
 * @Date: 2023/11/13 15:44
 * @Desc:
 */
internal object Utils {

    fun copyText(context: Context, text: String) {
        val monitor = context.getString(R.string.monitor_monitor)
        val clipboardManager =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText(monitor, text)
        clipboardManager.setPrimaryClip(clipData)
    }

    fun shareText(context: Context, text: String) {
        val monitor = context.getString(R.string.monitor_monitor)
        val shareIntent = ShareCompat.IntentBuilder(context)
            .setText(text)
            .setType("text/plain")
            .setChooserTitle(monitor)
            .setSubject(monitor)
            .createChooserIntent()
        if (context !is Activity) {
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(shareIntent)
    }

    fun shareFile(context: Context, uri: Uri) {
        val monitor = context.getString(R.string.monitor_monitor)
        val shareIntent = ShareCompat.IntentBuilder(context)
            .setStream(uri)
            .setType(context.contentResolver.getType(uri))
            .setChooserTitle(monitor)
            .setSubject(monitor)
            .intent
        shareIntent.apply {
            clipData = ClipData.newRawUri(monitor, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooserIntent = Intent.createChooser(shareIntent, monitor)
        if (context !is Activity) {
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooserIntent)
    }

}