package github.leavesczy.monitor.internal.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import github.leavesczy.monitor.R
import github.leavesczy.monitor.internal.db.MonitorPair
import kotlinx.coroutines.launch

/**
 * @Author: leavesCZY
 * @Date: 2023/10/27 16:12
 * @Desc:
 */
@Composable
internal fun MonitorDetailsPage(
    mainPageViewState: MonitorDetailPageViewState,
    overviewPageViewState: MonitorDetailOverviewPageViewState,
    requestPageViewState: MonitorDetailRequestPageViewState,
    responsePageViewState: MonitorDetailResponsePageViewState,
    onClickBack: () -> Unit,
    copyText: () -> Unit,
    shareAsText: () -> Unit,
    shareAsFile: () -> Unit
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize(),
        topBar = {
            MonitorDetailsTopBar(
                title = mainPageViewState.title,
                onClickBack = onClickBack,
                copyText = copyText,
                shareAsText = shareAsText,
                shareAsFile = shareAsFile
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = innerPadding)
        ) {
            val coroutineScope = rememberCoroutineScope()
            val pagerState = rememberPagerState(
                initialPage = 0,
                initialPageOffsetFraction = 0f
            ) {
                mainPageViewState.tabTagList.size
            }
            ScrollableTabRow(
                tagList = mainPageViewState.tabTagList,
                selectedTabIndex = pagerState.currentPage,
                scrollToPage = {
                    if (pagerState.currentPage != it) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(page = it)
                        }
                    }
                }
            )
            HorizontalPager(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(weight = 1f),
                state = pagerState
            ) {
                when (it) {
                    0 -> {
                        MonitorDetailsOverviewPage(
                            pageViewState = overviewPageViewState
                        )
                    }

                    1 -> {
                        MonitorDetailsPage(
                            headers = requestPageViewState.headers,
                            bodyFormat = requestPageViewState.formattedBody
                        )
                    }

                    2 -> {
                        MonitorDetailsPage(
                            headers = responsePageViewState.headers,
                            bodyFormat = responsePageViewState.formattedBody
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonitorDetailsTopBar(
    title: String,
    onClickBack: () -> Unit,
    copyText: () -> Unit,
    shareAsText: () -> Unit,
    shareAsFile: () -> Unit
) {
    var menuExpanded by remember {
        mutableStateOf(value = false)
    }
    CenterAlignedTopAppBar(
        modifier = Modifier,
        title = {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = title,
                fontSize = 19.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
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
                        modifier = Modifier
                            .size(size = 26.dp),
                        imageVector = Icons.Filled.Share,
                        contentDescription = null
                    )
                },
                onClick = {
                    menuExpanded = true
                }
            )
            Box(
                modifier = Modifier
                    .padding(end = 10.dp)
            ) {
                TopBarDropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = {
                        menuExpanded = false
                    },
                    copyText = copyText,
                    shareAsText = shareAsText,
                    shareAsFile = shareAsFile
                )
            }
        }
    )
}

@Composable
private fun TopBarDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    copyText: () -> Unit,
    shareAsText: () -> Unit,
    shareAsFile: () -> Unit
) {
    DropdownMenu(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background),
        expanded = expanded,
        onDismissRequest = onDismissRequest
    ) {
        val textStyle = TextStyle(
            fontSize = 18.sp,
            color = if (isSystemInDarkTheme()) {
                Color.White
            } else {
                Color.Black
            }
        )
        DropdownMenuItem(
            text = {
                Text(
                    text = stringResource(id = R.string.monitor_copy),
                    style = textStyle
                )
            },
            onClick = {
                onDismissRequest()
                copyText()
            }
        )
        DropdownMenuItem(
            text = {
                Text(
                    text = stringResource(id = R.string.monitor_share_as_text),
                    style = textStyle
                )
            },
            onClick = {
                onDismissRequest()
                shareAsText()
            }
        )
        DropdownMenuItem(
            text = {
                Text(
                    text = stringResource(id = R.string.monitor_share_as_file),
                    style = textStyle
                )
            },
            onClick = {
                onDismissRequest()
                shareAsFile()
            }
        )
    }
}

@Composable
private fun ScrollableTabRow(
    tagList: List<String>,
    selectedTabIndex: Int,
    scrollToPage: (Int) -> Unit
) {
    TabRow(
        modifier = Modifier.fillMaxWidth(),
        selectedTabIndex = selectedTabIndex,
        indicator = @Composable { tabPositions ->
            if (selectedTabIndex < tabPositions.size) {
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(currentTabPosition = tabPositions[selectedTabIndex]),
                    color = Color.White
                )
            }
        },
        divider = @Composable {

        }
    ) {
        tagList.forEachIndexed { index, item ->
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        scrollToPage(index)
                    }
                    .padding(vertical = 12.dp),
                text = item,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = if (index == selectedTabIndex) {
                    colorResource(id = R.color.monitor_top_bar_tab_text_selected)
                } else {
                    colorResource(id = R.color.monitor_top_bar_tab_text_unselected)
                }
            )
        }
    }
}

@Composable
private fun MonitorDetailsOverviewPage(pageViewState: MonitorDetailOverviewPageViewState) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        state = rememberLazyListState(),
        contentPadding = PaddingValues(
            start = 20.dp,
            top = 15.dp,
            end = 20.dp,
            bottom = 60.dp
        )
    ) {
        items(
            items = pageViewState.overview,
            contentType = {
                "MonitorPairItem"
            }
        ) {
            MonitorPairItem(pair = it)
        }
    }
}

@Composable
private fun MonitorDetailsPage(
    headers: List<MonitorPair>,
    bodyFormat: String
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        state = rememberLazyListState(),
        contentPadding = PaddingValues(
            start = 20.dp,
            top = 15.dp,
            end = 20.dp,
            bottom = 60.dp
        )
    ) {
        items(
            items = headers,
            contentType = {
                "MonitorPairItem"
            }
        ) {
            MonitorPairItem(pair = it)
        }
        if (bodyFormat.isNotBlank()) {
            item(contentType = "bodyFormat") {
                SelectionContainer {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 30.dp),
                        text = bodyFormat,
                        fontSize = 16.sp,
                        lineHeight = 21.sp,
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Normal,
                        color = colorResource(id = R.color.monitor_http_body)
                    )
                }
            }
        }
    }
}

@Composable
private fun MonitorPairItem(pair: MonitorPair) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            modifier = Modifier
                .weight(weight = 3.2f)
                .padding(end = 10.dp),
            text = pair.name,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            color = colorResource(id = R.color.monitor_http_status_successful)
        )
        SelectionContainer(
            modifier = Modifier
                .weight(weight = 5f)
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth(),
                text = pair.value,
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
                color = colorResource(id = R.color.monitor_http_status_successful)
            )
        }
    }
}