package com.example.ipcserver

import android.app.Service
import android.content.Intent
import android.os.*
import android.text.TextUtils

class IPCServerService : Service() {

    companion object {
        var connectionCount: Int = 0
        val NOT_SENT = "Not sent!"

        const val PID = "pid"
        const val MESSAGE = "message"
        const val CONNECTION_COUNT = "connection_count"
        const val PACKAGE_NAME = "package_name"
        const val DATA = "data"
    }


    override fun onBind(intent: Intent?): IBinder? {
        connectionCount++
        return when(intent?.action) {
            "aidlexample" -> aidlBinder
            "messengerexample" -> mMessenger.binder
            else -> null
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        RecentClient.client = null
        return super.onUnbind(intent)
    }

    /* AIDL CONNECTION */
    private val aidlBinder = object : IPCExample.Stub() {

        override fun getPid(): Int = Process.myPid()

        override fun getConnectionCount(): Int = IPCServerService.connectionCount

        override fun setDisplayedValue(packageName: String?, pid: Int, data: String?) {
            val clientData =
                if (data == null || TextUtils.isEmpty(data)) NOT_SENT
                else data

            RecentClient.client = Client(
                packageName ?: NOT_SENT,
                pid.toString(),
                clientData,
                "AIDL"
            )
        }
    }

    // Messenger IPC - Messenger object contains binder to send to client
    private val mMessenger = Messenger(IncomingHandler())

    // Messenger IPC - Message Handler
    internal inner class IncomingHandler : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            // Get message from client. Save recent connected client info.
            val receivedBundle = msg.data
            RecentClient.client = Client(
                receivedBundle.getString(PACKAGE_NAME),
                receivedBundle.getInt(PID).toString(),
                receivedBundle.getString(DATA),
                "Messenger"
            )

            // Send message to the client. The message contains server info
            val message = Message.obtain(this@IncomingHandler, 0)
            val bundle = Bundle()
            bundle.putInt(CONNECTION_COUNT, connectionCount)
            bundle.putInt(PID, Process.myPid())
            bundle.putString(MESSAGE, "Testando texto")
            message.data = bundle
            // The service can save the msg.replyTo object as a local variable
            // so that it can send a message to the client at any time
            msg.replyTo.send(message)
        }
    }

}