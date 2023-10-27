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
        var monitor = buildMonitor(request = request)
        monitor = insert(monitor = monitor)
        MonitorNotificationHandler.show(monitor = monitor)
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
            monitor = if (response != null) {
                processResponse(
                    response = response,
                    monitor = monitor
                )
            } else {
                monitor.copy(error = error.toString())
            }
            update(monitor = monitor)
            MonitorNotificationHandler.show(monitor = monitor)
        } catch (_: Throwable) {

        }
        if (error != null) {
            throw error
        }
        return response!!
    }

    private fun buildMonitor(request: Request): Monitor {
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
        val mRequestBody = if (requestBody == null) {
            ""
        } else if (ResponseUtils.bodyHasUnknownEncoding(headers = request.headers)) {
            "(encoded body omitted)"
        } else if (requestBody.isDuplex()) {
            "(duplex request body omitted)"
        } else if (requestBody.isOneShot()) {
            "(one-shot body omitted)"
        } else {
            val buffer = Buffer()
            requestBody.writeTo(buffer)
            val contentType = requestBody.contentType()
            val charset = contentType?.charset(Charsets.UTF_8) ?: Charsets.UTF_8
            if (ResponseUtils.isProbablyUtf8(buffer)) {
                buffer.readString(charset)
            } else {
                "(binary body omitted)"
            }
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
        val responseContentType = responseBody?.contentType()?.toString() ?: ""
        val responseContentLength = responseBody?.contentLength() ?: 0
        val mResponseBody = if (responseBody == null || !response.promisesBody()) {
            ""
        } else if (ResponseUtils.bodyHasUnknownEncoding(headers = response.headers)) {
            "(encoded body omitted)"
        } else {
            val buffer = ResponseUtils.getNativeSource(response = response)
            if (ResponseUtils.isProbablyUtf8(buffer)) {
                if (responseContentLength != 0L) {
                    val charset = responseBody.contentType()?.charset(Charsets.UTF_8)
                        ?: Charsets.UTF_8
                    buffer.clone().readString(charset)
                } else {
                    "(encoded body omitted)"
                }
            } else {
                "(binary body omitted)"
            }
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
        val id = MonitorDatabase.instance.monitorDao.insert(monitor = monitor)
        return monitor.copy(id = id)
    }

    private fun update(monitor: Monitor) {
        MonitorDatabase.instance.monitorDao.update(monitor = monitor)
    }

}