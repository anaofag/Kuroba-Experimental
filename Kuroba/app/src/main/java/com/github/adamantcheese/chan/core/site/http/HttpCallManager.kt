/*
 * KurobaEx - *chan browser https://github.com/K1rakishou/Kuroba-Experimental/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.adamantcheese.chan.core.site.http

import com.github.adamantcheese.chan.core.di.NetModule
import com.github.adamantcheese.chan.core.di.NetModule.ProxiedOkHttpClient
import com.github.adamantcheese.chan.utils.Logger
import com.github.adamantcheese.common.ModularResult.Companion.Try
import com.github.adamantcheese.common.suspendCall
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import okhttp3.Request
import java.io.IOException
import javax.inject.Inject

/**
 * Manages the [HttpCall] executions.
 */
class HttpCallManager @Inject constructor(
  private val okHttpClient: ProxiedOkHttpClient
) {
  
  /**
   * Use this one when you want to send a Post request and want to show some progress indicator
   * */
  @OptIn(ExperimentalCoroutinesApi::class)
  suspend fun <T : HttpCall> makePostHttpCallWithProgress(
    httpCall: T
  ): Flow<HttpCall.HttpCallWithProgressResult<T>> {
    return channelFlow {
      val requestBuilder = Request.Builder()
      
      httpCall.setup(requestBuilder, ProgressRequestBody.ProgressRequestListener { percent ->
        offer(HttpCall.HttpCallWithProgressResult.Progress(percent))
      })
      
      httpCall.site.requestModifier().modifyHttpCall(httpCall, requestBuilder)
      
      when (val httpCallResult = makeHttpCallInternal(requestBuilder, httpCall)) {
        is HttpCall.HttpCallResult.Success -> {
          send(HttpCall.HttpCallWithProgressResult.Success(httpCallResult.httpCall))
        }
        is HttpCall.HttpCallResult.Fail -> {
          send(HttpCall.HttpCallWithProgressResult.Fail(httpCallResult.httpCall, httpCallResult.error))
        }
      }
    }
  }
  
  /**
   * Use this one for every other request
   * */
  suspend fun <T : HttpCall> makeHttpCall(httpCall: T): HttpCall.HttpCallResult<T> {
    val requestBuilder = Request.Builder()
    
    httpCall.setup(requestBuilder, null)
    httpCall.site.requestModifier().modifyHttpCall(httpCall, requestBuilder)
    
    return makeHttpCallInternal(requestBuilder, httpCall)
  }
  
  private suspend fun <T : HttpCall> makeHttpCallInternal(
    requestBuilder: Request.Builder,
    httpCall: T
  ): HttpCall.HttpCallResult<T> {
    requestBuilder.header("User-Agent", NetModule.USER_AGENT)
    val request = requestBuilder.build()
    
    val response = Try { okHttpClient.proxiedClient.suspendCall(request) }
      .safeUnwrap { error ->
        Logger.e(TAG, "Error while trying to execute request", error)
        return HttpCall.HttpCallResult.Fail(httpCall, error)
      }
    
    val body = response.body
      ?: return HttpCall.HttpCallResult.Fail(
        httpCall,
        IOException("Response body is null, status = ${response.code}")
      )
    
    return body.use {
      val responseString = it.string()
      httpCall.process(response, responseString)
      
      return@use HttpCall.HttpCallResult.Success(httpCall)
    }
  }
  
  companion object {
    private const val TAG = "HttpCallManager"
  }
  
}