package github.leavesczy.monitor.internal.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import github.leavesczy.monitor.internal.MonitorNotificationHandler
import github.leavesczy.monitor.internal.db.Monitor
import github.leavesczy.monitor.internal.db.MonitorDatabase
import kotlinx.coroutines.launch

/**
 * @Author: leavesCZY
 * @Date: 2020/11/8 15:58
 * @Desc:
 * @Github：https://github.com/leavesCZY
 */
internal class MonitorActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MonitorTheme {
                val queryFlow = MonitorDatabase.instance.monitorDao.queryFlow(limit = 300)
                val monitorList by queryFlow.collectAsState(initial = emptyList())
                MonitorPage(
                    onClickBack = ::onClickBack,
                    onClickClear = ::onClickClear,
                    monitorList = monitorList,
                    onClickMonitorItem = ::onClickMonitorItem
                )
            }
        }
    }

    private fun onClickBack() {
        finish()
    }

    private fun onClickClear() {
        lifecycleScope.launch {
            MonitorDatabase.instance.monitorDao.deleteAll()
            MonitorNotificationHandler.clearBuffer()
            MonitorNotificationHandler.dismiss()
        }
    }

    private fun onClickMonitorItem(monitor: Monitor) {
        val intent = Intent(this, MonitorDetailsActivity::class.java)
        intent.putExtra(MonitorDetailsActivity.KEY_ID, monitor.id)
        startActivity(intent)
    }

}