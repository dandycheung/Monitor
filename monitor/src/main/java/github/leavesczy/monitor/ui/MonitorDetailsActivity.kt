package github.leavesczy.monitor.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import github.leavesczy.monitor.R
import github.leavesczy.monitor.db.MonitorDatabase
import github.leavesczy.monitor.utils.FormatUtils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * @Author: leavesCZY
 * @Date: 2020/11/8 17:04
 * @Desc:
 * @Github：https://github.com/leavesCZY
 */
internal class MonitorDetailsActivity : AppCompatActivity() {

    internal companion object {

        const val KEY_ID = "keyId"

    }

    private val id by lazy(mode = LazyThreadSafetyMode.NONE) {
        intent.getLongExtra(KEY_ID, 0)
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
            bodyFormat = ""
        )
    )

    private var responsePageViewState by mutableStateOf(
        value = MonitorDetailResponsePageViewState(
            headers = emptyList(),
            bodyFormat = ""
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MonitorDetailsPage(
                mainPageViewState = mainPageViewState,
                overviewPageViewState = overviewPageViewState,
                requestPageViewState = requestPageViewState,
                responsePageViewState = responsePageViewState,
                onClickBack = ::onClickBack,
                onClickShare = ::onClickShare
            )
        }
        initObserver()
    }

    private fun initObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(state = Lifecycle.State.RESUMED) {
                MonitorDatabase.instance.monitorDao.queryFlow(id = id)
                    .distinctUntilChanged()
                    .collectLatest {
                        mainPageViewState = MonitorDetailPageViewState(
                            title = String.format("%s  %s", it.method, it.pathWithQuery),
                            tabTagList = listOf(
                                "Overview",
                                "Request",
                                "Response"
                            )
                        )
                        overviewPageViewState = MonitorDetailOverviewPageViewState(
                            overview = FormatUtils.buildOverview(
                                monitor = it
                            )
                        )
                        requestPageViewState = MonitorDetailRequestPageViewState(
                            headers = it.requestHeaders,
                            bodyFormat = it.requestBodyFormat
                        )
                        responsePageViewState = MonitorDetailResponsePageViewState(
                            headers = it.responseHeaders,
                            bodyFormat = it.responseBodyFormat
                        )
                    }
            }
        }
    }

    private fun onClickBack() {
        finish()
    }

    private fun onClickShare() {
        lifecycleScope.launch {
            try {
                val monitorHttp = MonitorDatabase.instance.monitorDao.query(id = id)
                share(
                    context = applicationContext,
                    content = FormatUtils.buildShareText(monitor = monitorHttp)
                )
            } catch (_: ActivityNotFoundException) {

            }
        }
    }

    private fun share(context: Context, content: String) {
        val sendIntent = Intent()
        sendIntent.putExtra(Intent.EXTRA_TEXT, content)
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.type = "text/plain"
        val chooserIntent =
            Intent.createChooser(sendIntent, getString(R.string.monitor_library_name))
        if (context !is Activity) {
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooserIntent)
    }

}