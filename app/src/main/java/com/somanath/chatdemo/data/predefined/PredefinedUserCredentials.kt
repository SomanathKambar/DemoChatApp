package com.somanath.chatdemo.data.predefined

import io.getstream.chat.android.models.User

object PredefinedUserCredentials {

    const val API_KEY: String = "f2turvbq7dgq"

    val availableUsers: List<UserCredentials> = listOf(
        UserCredentials(
            apiKey = API_KEY,
            user = User(
                "Alice",
                name = "Alice",
                image = "https://ca.slack-edge.com/T02RM6X6B-U05UD37MA1G-f062f8b7afc2-72",
            ),
            token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX2lkIjoiQWxpY2UifQ.3yXacnI4drfvymGDq4FeSfCqgHnyJL43FSZgbkIdEc0",
        )
    )
}
