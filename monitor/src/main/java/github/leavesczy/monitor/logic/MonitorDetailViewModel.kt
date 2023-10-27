package github.leavesczy.monitor.logic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import github.leavesczy.monitor.db.MonitorDatabase
import github.leavesczy.monitor.utils.FormatUtils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * @Author: leavesCZY
 * @Date: 2023/8/17 14:41
 * @Desc:
 * @Github：https://github.com/leavesCZY
 */
internal class MonitorDetailViewModel(id: Long) : ViewModel() {

    var mainPageViewState by mutableStateOf(
        value = MonitorDetailPageViewState(
            title = "",
            tabTagList = emptyList()
        )
    )
        private set

    var overviewPageViewState by mutableStateOf(
        value = MonitorDetailOverviewPageViewState(
            overview = emptyList()
        )
    )
        private set

    var requestPageViewState by mutableStateOf(
        value = MonitorDetailRequestPageViewState(
            headers = emptyList(),
            bodyFormat = ""
        )
    )
        private set

    var responsePageViewState by mutableStateOf(
        value = MonitorDetailResponsePageViewState(
            headers = emptyList(),
            bodyFormat = ""
        )
    )
        private set

    init {
        viewModelScope.launch {
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