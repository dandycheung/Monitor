package github.leavesczy.monitor.internal.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
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
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import github.leavesczy.monitor.R
import github.leavesczy.monitor.internal.Utils
import github.leavesczy.monitor.internal.db.MonitorDatabase
import github.leavesczy.monitor.internal.db.MonitorPair
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

@Composable
private fun MonitorDetailsPage(
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
        indicator = { tabPositions ->
            if (selectedTabIndex < tabPositions.size) {
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(currentTabPosition = tabPositions[selectedTabIndex]),
                    color = Color.White
                )
            }
        },
        divider = {

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