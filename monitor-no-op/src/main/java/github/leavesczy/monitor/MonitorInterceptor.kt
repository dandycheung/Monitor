package github.leavesczy.monitor

import okhttp3.Interceptor
import okhttp3.Response

/**
 * @Author: leavesCZY
 * @Desc:
 * @Github：https://github.com/leavesCZY
 */
class MonitorInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        return chain.proceed(request)
    }

}