package github.leavesczy.monitor.internal.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import github.leavesczy.monitor.R
import github.leavesczy.monitor.internal.Utils
import github.leavesczy.monitor.internal.db.MonitorDatabase
import github.leavesczy.monitor.internal.db.buildOverview
import github.leavesczy.monitor.internal.db.buildShareText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


/**
 * @Author: leavesCZY
 * @Date: 2020/11/8 17:04
 * @Desc:
 * @Github：https://github.com/leavesCZY
 */
internal class MonitorDetailsActivity : AppCompatActivity() {

    internal companion object {

        const val keyMonitorId = "keyMonitorId"

    }

    private val monitorId by lazy(mode = LazyThreadSafetyMode.NONE) {
        intent.getLongExtra(keyMonitorId, 0)
    }

    private var mainPageViewState by mutableStateOf(
        value = MonitorDetailPageViewState(
            title = "",
            tabTagList = emptyList()
        )
    )

    private var overviewPageViewState by mutableStateOf(
        value = MonitorDetailOverviewPageViewState(
            overview = emptyList()
        )
    )

    private var requestPageViewState by mutableStateOf(
        value = MonitorDetailRequestPageViewState(
            headers = emptyList(),
            formattedBody = ""
        )
    )

    private var responsePageViewState by mutableStateOf(
        value = MonitorDetailResponsePageViewState(
            headers = emptyList(),
            formattedBody = ""
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MonitorTheme {
                MonitorDetailsPage(
                    mainPageViewState = mainPageViewState,
                    overviewPageViewState = overviewPageViewState,
                    requestPageViewState = requestPageViewState,
                    responsePageViewState = responsePageViewState,
                    onClickBack = ::onClickBack,
                    copyText = ::copyText,
                    shareAsText = ::shareAsText,
                    shareAsFile = ::shareAsFile
                )
            }
        }
        initObserver()
    }

    private fun initObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(state = Lifecycle.State.RESUMED) {
                MonitorDatabase.instance.monitorDao.queryFlow(id = monitorId)
                    .distinctUntilChanged()
                    .collectLatest {
                        mainPageViewState = MonitorDetailPageViewState(
                            title = it.method + " " + it.pathWithQuery,
                            tabTagList = listOf(
                                getString(R.string.monitor_overview),
                                getString(R.string.monitor_request),
                                getString(R.string.monitor_response),
                            )
                        )
                        overviewPageViewState = MonitorDetailOverviewPageViewState(
                            overview = it.buildOverview()
                        )
                        requestPageViewState = MonitorDetailRequestPageViewState(
                            headers = it.requestHeaders,
                            formattedBody = it.requestBodyFormat
                        )
                        responsePageViewState = MonitorDetailResponsePageViewState(
                            headers = it.responseHeaders,
                            formattedBody = it.responseBodyFormat
                        )
                    }
            }
        }
    }

    private fun onClickBack() {
        finish()
    }

    private fun copyText() {
        lifecycleScope.launch(context = Dispatchers.Default) {
            try {
                val shareText = queryMonitorShareText()
                Utils.copyText(context = applicationContext, text = shareText)
                showToast(resId = R.string.monitor_copied)
            } catch (e: Throwable) {
                e.printStackTrace()
                showToast(msg = e.toString())
            }
        }
    }

    private fun shareAsText() {
        lifecycleScope.launch(context = Dispatchers.Default) {
            try {
                val shareText = queryMonitorShareText()
                Utils.shareText(
                    context = applicationContext,
                    text = shareText
                )
            } catch (e: Throwable) {
                e.printStackTrace()
                showToast(msg = e.toString())
            }
        }
    }

    private fun shareAsFile() {
        lifecycleScope.launch(context = Dispatchers.IO) {
            try {
                val shareText = queryMonitorShareText()
                val shareFile = createShareFile()
                shareFile.writeText(text = shareText, charset = Charsets.UTF_8)
                val authority = applicationInfo.packageName + ".monitor.file.provider"
                val shareFileUri =
                    FileProvider.getUriForFile(applicationContext, authority, shareFile)
                Utils.shareFile(context = applicationContext, uri = shareFileUri)
            } catch (e: Throwable) {
                e.printStackTrace()
                showToast(msg = e.toString())
            }
        }
    }

    private fun createShareFile(): File {
        val cacheRootDir = File(cacheDir, "monitor")
        cacheRootDir.mkdirs()
        val currentTime =
            SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault()).format(Date())
        val shareFile = File(cacheRootDir, "monitor_$currentTime.txt")
        shareFile.createNewFile()
        return shareFile
    }

    private suspend fun queryMonitorShareText(): String {
        return MonitorDatabase.instance.monitorDao.query(id = monitorId).buildShareText()
    }

    private suspend fun showToast(@StringRes resId: Int) {
        showToast(msg = getString(resId))
    }

    private suspend fun showToast(msg: String) {
        withContext(context = Dispatchers.Main.immediate) {
            Toast.makeText(this@MonitorDetailsActivity, msg, Toast.LENGTH_SHORT).show()
        }
    }

}