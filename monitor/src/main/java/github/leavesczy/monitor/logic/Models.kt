package github.leavesczy.monitor.logic

import androidx.compose.runtime.Stable
import github.leavesczy.monitor.db.MonitorPair

/**
 * @Author: leavesCZY
 * @Date: 2023/10/27 15:41
 * @Desc:
 */
@Stable
internal data class MonitorDetailPageViewState(
    val title: String,
    val tabTagList: List<String>
)

@Stable
internal data class MonitorDetailOverviewPageViewState(
    val overview: List<MonitorPair>
)

@Stable
internal data class MonitorDetailRequestPageViewState(
    val headers: List<MonitorPair>,
    val bodyFormat: String
)

@Stable
internal data class MonitorDetailResponsePageViewState(
    val headers: List<MonitorPair>,
    val bodyFormat: String
)