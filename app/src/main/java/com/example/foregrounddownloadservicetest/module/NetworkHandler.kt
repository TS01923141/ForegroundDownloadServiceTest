/**
 * Copyright (C) 2020 Fernando Cejas Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.foregrounddownloadservicetest.module

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log

object NetworkHandler {
    /**
     * @param context
     * @return true: 有網路 , false: 沒網路
     */
    fun checkInternet(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return try {
            val activeNetwork = cm.activeNetworkInfo
            if (activeNetwork != null) {
                return true
            }
            false
        } catch (e: NullPointerException) {
            false
        }
    }
}
