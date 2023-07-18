package com.uns.matur.firebase

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        if(message.notification != null) {
            val title = message.notification!!.title
            val body = message.notification!!.body
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }
}