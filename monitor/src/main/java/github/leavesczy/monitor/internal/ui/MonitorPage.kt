package github.leavesczy.monitor.internal.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import github.leavesczy.monitor.R
import github.leavesczy.monitor.internal.db.Monitor
import github.leavesczy.monitor.internal.db.MonitorStatus

/**
 * @Author: leavesCZY
 * @Date: 2023/10/27 16:18
 * @Desc:
 */
@Composable
internal fun MonitorPage(
    onClickBack: () -> Unit,
    onClickClear: () -> Unit,
    monitorList: List<Monitor>,
    onClickMonitorItem: (Monitor) -> Unit
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            MonitorTopBar(
                onClickBack = onClickBack,
                onClickClear = onClickClear
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = innerPadding),
            state = rememberLazyListState(),
            contentPadding = PaddingValues(bottom = 60.dp)
        ) {
            items(
                items = monitorList,
                key = {
                    it.id
                },
                contentType = {
                    "monitor"
                }
            ) {
                MonitorItem(monitor = it, onClick = onClickMonitorItem)
            }
        }
    }
}

@Composable
private fun MonitorItem(monitor: Monitor, onClick: (Monitor) -> Unit) {
    val titleColor: Int
    val subtitleColor: Int
    when (monitor.httpStatus) {
        MonitorStatus.Requesting -> {
            titleColor = R.color.monitor_http_status_requesting
            subtitleColor = R.color.monitor_http_status_requesting_sub
        }

        MonitorStatus.Complete -> {
            titleColor = if (monitor.responseCode == 200) {
                R.color.monitor_http_status_successful
            } else {
                R.color.monitor_http_status_unsuccessful
            }
            subtitleColor = if (monitor.responseCode == 200) {
                R.color.monitor_http_status_successful_sub
            } else {
                R.color.monitor_http_status_unsuccessful_sub
            }
        }

        MonitorStatus.Failed -> {
            titleColor = R.color.monitor_http_status_unsuccessful
            subtitleColor = R.color.monitor_http_status_unsuccessful_sub
        }
    }
    val titleTextStyle =
        MaterialTheme.typography.titleMedium.copy(color = colorResource(id = titleColor))
    val subtitleTextStyle =
        MaterialTheme.typography.bodySmall.copy(color = colorResource(id = subtitleColor))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onClick(monitor)
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 14.dp, top = 10.dp)
        ) {
            Text(
                modifier = Modifier
                    .width(width = 40.dp),
                text = monitor.responseCodeFormat,
                style = titleTextStyle
            )
            Column(
                modifier = Modifier
                    .weight(weight = 1f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        modifier = Modifier.weight(weight = 1f),
                        text = monitor.pathWithQuery,
                        style = titleTextStyle
                    )
                    Text(
                        modifier = Modifier,
                        text = monitor.id.toString(),
                        style = titleTextStyle
                    )
                }
                Text(
                    modifier = Modifier
                        .padding(
                            top = 3.dp,
                            bottom = 3.dp
                        ),
                    text = String.format("%s://%s", monitor.scheme, monitor.host),
                    style = subtitleTextStyle
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        modifier = Modifier,
                        text = monitor.requestDateMDHMS,
                        style = subtitleTextStyle
                    )
                    Text(
                        modifier = Modifier,
                        text = monitor.requestDurationFormat,
                        style = subtitleTextStyle
                    )
                    Text(
                        modifier = Modifier,
                        text = monitor.totalSizeFormat,
                        style = subtitleTextStyle
                    )
                }
            }
        }
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
        )
    }
}

@Composable
private fun MonitorTopBar(
    onClickBack: () -> Unit,
    onClickClear: () -> Unit
) {
    TopAppBar(
        modifier = Modifier,
        title = {
            Text(
                modifier = Modifier,
                text = stringResource(id = R.string.monitor_library_name)
            )
        },
        navigationIcon = {
            IconButton(
                content = {
                    Icon(
                        modifier = Modifier
                            .size(size = 26.dp),
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = null
                    )
                },
                onClick = onClickBack
            )
        },
        actions = {
            IconButton(
                content = {
                    Icon(
                        modifier = Modifier.size(size = 26.dp),
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = null
                    )
                },
                onClick = onClickClear
            )
        }
    )
}