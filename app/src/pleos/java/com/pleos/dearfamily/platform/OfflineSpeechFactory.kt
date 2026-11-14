package com.pleos.dearfamily.platform

import android.content.Context

object OfflineSpeechFactory {
    fun create(context: Context, listener: OfflineSpeechAdapter.Listener): OfflineSpeechAdapter =
        PleosOfflineSpeechAdapter(context, listener)
}
