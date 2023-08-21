package github.leavesczy.monitor.samples

import okhttp3.Interceptor
import java.io.IOException

/**
 * @Author: leavesCZY
 * @Desc:
 * @Github：https://github.com/leavesCZY
 */
class FilterInterceptor : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val request = chain.request()
        val url = request.url
        if (url.host == "restapi.amap.com") {
            val httpBuilder = url.newBuilder()
            httpBuilder.addEncodedQueryParameter("key", "fb0a1b0d89f3b93adca639f0a29dbf23")
            val requestBuilder = request.newBuilder()
                .url(httpBuilder.build())
            return chain.proceed(requestBuilder.build())
        }
        return chain.proceed(request)
    }

}