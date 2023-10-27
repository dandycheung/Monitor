package github.leavesczy.monitor

import android.app.Application
import android.content.Context
import github.leavesczy.monitor.db.Monitor
import github.leavesczy.monitor.db.MonitorDatabase
import github.leavesczy.monitor.db.MonitorPair
import github.leavesczy.monitor.provider.ContextProvider
import github.leavesczy.monitor.provider.MonitorNotificationHandler
import github.leavesczy.monitor.utils.ResponseUtils
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.internal.http.promisesBody
import okio.Buffer

/**
 * @Author: leavesCZY
 * @Date: 2020/10/20 18:26
 * @Desc:
 * @Github：https://github.com/leavesCZY
 */
class MonitorInterceptor(context: Context) : Interceptor {

    init {
        ContextProvider.inject(context = context.applicationContext as Application)
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var monitorHttp = buildMonitorHttp(request = request)
        monitorHttp = insert(monitor = monitorHttp)
        MonitorNotificationHandler.show(monitor = monitorHttp)
        var response: Response?
        var error: Throwable?
        try {
            response = chain.proceed(request = request)
            error = null
        } catch (throwable: Throwable) {
            response = null
            error = throwable
        }
        try {
            monitorHttp = if (response != null) {
                processResponse(
                    response = response,
                    monitor = monitorHttp
                )
            } else {
                monitorHttp.copy(error = error.toString())
            }
            update(monitor = monitorHttp)
            MonitorNotificationHandler.show(monitor = monitorHttp)
        } catch (_: Throwable) {

        }
        if (error != null) {
            throw error
        }
        return response!!
    }

    private fun buildMonitorHttp(request: Request): Monitor {
        val requestDate = System.currentTimeMillis()
        val requestBody = request.body
        val url = request.url
        val scheme = url.scheme
        val host = url.host
        val path = url.encodedPath
        val query = url.encodedQuery ?: ""
        val method = request.method
        val requestHeaders = request.headers.map {
            MonitorPair(name = it.first, value = it.second)
        }
        val mRequestBody =
            if (requestBody != null && ResponseUtils.bodyHasSupportedEncoding(request.headers)) {
                val buffer = Buffer()
                requestBody.writeTo(buffer)
                if (ResponseUtils.isProbablyUtf8(buffer)) {
                    val charset =
                        requestBody.contentType()?.charset(Charsets.UTF_8) ?: Charsets.UTF_8
                    buffer.readString(charset)
                } else {
                    ""
                }
            } else {
                ""
            }
        val requestContentLength = requestBody?.contentLength() ?: 0
        val requestContentType = requestBody?.contentType()?.toString() ?: ""
        return Monitor(
            id = 0L,
            url = url.toString(),
            scheme = scheme,
            host = host,
            path = path,
            query = query,
            requestDate = requestDate,
            method = method,
            requestHeaders = requestHeaders,
            requestContentLength = requestContentLength,
            requestContentType = requestContentType,
            requestBody = mRequestBody,
            protocol = "",
            responseHeaders = emptyList(),
            responseBody = "",
            responseContentType = "",
            responseContentLength = 0L,
            responseDate = 0L,
            responseTlsVersion = "",
            responseCipherSuite = "",
            responseMessage = "",
            error = null
        )
    }

    private fun processResponse(
        response: Response,
        monitor: Monitor
    ): Monitor {
        val requestHeaders = response.request.headers.map {
            MonitorPair(name = it.first, value = it.second)
        }
        val responseHeaders = response.headers.map {
            MonitorPair(name = it.first, value = it.second)
        }
        val responseBody = response.body
        val responseContentType: String
        var responseContentLength = 0L
        var mResponseBody = "(encoded body omitted)"
        if (responseBody != null) {
            responseContentType = responseBody.contentType()?.toString() ?: ""
            responseContentLength = responseBody.contentLength()
            if (response.promisesBody()) {
                val encodingIsSupported = ResponseUtils.bodyHasSupportedEncoding(response.headers)
                if (encodingIsSupported) {
                    val buffer = ResponseUtils.getNativeSource(response)
                    responseContentLength = buffer.size
                    if (ResponseUtils.isProbablyUtf8(buffer)) {
                        if (responseBody.contentLength() != 0L) {
                            val charset = responseBody.contentType()?.charset(Charsets.UTF_8)
                                ?: Charsets.UTF_8
                            mResponseBody = buffer.clone().readString(charset)
                        }
                    }
                }
            }
        } else {
            responseContentType = ""
        }
        return monitor.copy(
            requestDate = response.sentRequestAtMillis,
            responseDate = response.receivedResponseAtMillis,
            protocol = response.protocol.toString(),
            responseCode = response.code,
            responseMessage = response.message,
            responseTlsVersion = response.handshake?.tlsVersion?.javaName ?: "",
            responseCipherSuite = response.handshake?.cipherSuite?.javaName ?: "",
            requestHeaders = requestHeaders,
            responseHeaders = responseHeaders,
            responseContentType = responseContentType,
            responseContentLength = responseContentLength,
            responseBody = mResponseBody
        )
    }

    private fun insert(monitor: Monitor): Monitor {
        val id = MonitorDatabase.instance.monitorDao.insert(model = monitor)
        return monitor.copy(id = id)
    }

    private fun update(monitor: Monitor) {
        MonitorDatabase.instance.monitorDao.update(model = monitor)
    }

}