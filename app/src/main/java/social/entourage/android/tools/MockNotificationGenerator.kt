package social.entourage.android.tools

import android.content.Context
import social.entourage.android.api.model.notification.PushNotificationMessage
import social.entourage.android.notifications.PushNotificationManager

object MockNotificationGenerator {

    fun createContributionNotification(context: Context) {
        val contributionContent = """
        {
            "extra": {
                "joinable_id": 1,
                "joinable_type": "Entourage",
                "user_id": 123,
                "type": "NEW_CONTRIBUTION",
                "instance": "contributions",
                "instance_id": 1,
                "tracking": "contribution_on_create"
            },
            "message": "Nouvelle contribution à l'événement"
        }
        """.trimIndent()

        val contributionNotification = PushNotificationMessage(
            author = "System",
            msgObject = "Contribution",
            content = contributionContent,
            pushNotificationId = 1,
            pushNotificationTag = "contribution"
        )
        PushNotificationManager.handlePushNotification(contributionNotification, context)
    }

    fun createConversationNotification(context: Context) {
        val conversationContent = """
        {
            "extra": {
                "joinable_id": 2,
                "joinable_type": "Entourage",
                "user_id": 124,
                "type": "NEW_CHAT_MESSAGE",
                "instance": "conversations",
                "instance_id": 2,
                "tracking": "private_chat_message_on_create"
            },
            "message": "Vous avez un nouveau message dans la conversation"
        }
        """.trimIndent()

        val conversationNotification = PushNotificationMessage(
            author = "System",
            msgObject = "Conversation",
            content = conversationContent,
            pushNotificationId = 2,
            pushNotificationTag = "conversation"
        )
        PushNotificationManager.handlePushNotification(conversationNotification, context)
    }

    fun createOutingNotification(context: Context) {
        val outingContent = """
        {
            "extra": {
                "joinable_id": 3,
                "joinable_type": "Entourage",
                "user_id": 125,
                "type": "OUTING",
                "instance": "outings",
                "instance_id": 3,
                "tracking": "outing_on_create"
            },
            "message": "Un nouvel événement a été créé"
        }
        """.trimIndent()

        val outingNotification = PushNotificationMessage(
            author = "System",
            msgObject = "Outing",
            content = outingContent,
            pushNotificationId = 3,
            pushNotificationTag = "outing"
        )
        PushNotificationManager.handlePushNotification(outingNotification, context)
    }

    fun createJoinRequestNotification(context: Context) {
        val joinRequestContent = """
        {
            "extra": {
                "joinable_id": 4,
                "joinable_type": "Entourage",
                "user_id": 126,
                "type": "NEW_JOIN_REQUEST",
                "instance": "neighborhoods",
                "instance_id": 4,
                "tracking": "join_request_on_create"
            },
            "message": "Nouvelle demande pour rejoindre l'événement"
        }
        """.trimIndent()

        val joinRequestNotification = PushNotificationMessage(
            author = "System",
            msgObject = "Join Request",
            content = joinRequestContent,
            pushNotificationId = 4,
            pushNotificationTag = "join_request"
        )
        PushNotificationManager.handlePushNotification(joinRequestNotification, context)
    }

    fun createInvitationNotification(context: Context) {
        val invitationContent = """
        {
            "extra": {
                "joinable_id": 5,
                "joinable_type": "Entourage",
                "user_id": 127,
                "type": "ENTOURAGE_INVITATION",
                "instance": "partners",
                "instance_id": 5,
                "tracking": "invitation_on_create"
            },
            "message": "Vous avez été invité à rejoindre l'entourage"
        }
        """.trimIndent()

        val invitationNotification = PushNotificationMessage(
            author = "System",
            msgObject = "Invitation",
            content = invitationContent,
            pushNotificationId = 5,
            pushNotificationTag = "invitation"
        )
        PushNotificationManager.handlePushNotification(invitationNotification, context)
    }

    fun createJoinRequestAcceptedNotification(context: Context) {
        val joinRequestAcceptedContent = """
        {
            "extra": {
                "joinable_id": 6,
                "joinable_type": "Entourage",
                "user_id": 128,
                "type": "JOIN_REQUEST_ACCEPTED",
                "instance": "neighborhoods",
                "instance_id": 6,
                "tracking": "join_request_accepted"
            },
            "message": "Votre demande pour rejoindre l'entourage a été acceptée"
        }
        """.trimIndent()

        val joinRequestAcceptedNotification = PushNotificationMessage(
            author = "System",
            msgObject = "Join Request Accepted",
            content = joinRequestAcceptedContent,
            pushNotificationId = 6,
            pushNotificationTag = "join_request_accepted"
        )
        PushNotificationManager.handlePushNotification(joinRequestAcceptedNotification, context)
    }

    fun createSolicitationNotification(context: Context) {
        val solicitationContent = """
        {
            "extra": {
                "joinable_id": 7,
                "joinable_type": "Entourage",
                "user_id": 129,
                "type": "NEW_SOLICITATION",
                "instance": "solicitations",
                "instance_id": 7,
                "tracking": "solicitation_on_create"
            },
            "message": "Nouvelle sollicitation reçue"
        }
        """.trimIndent()

        val solicitationNotification = PushNotificationMessage(
            author = "System",
            msgObject = "Solicitation",
            content = solicitationContent,
            pushNotificationId = 7,
            pushNotificationTag = "solicitation"
        )
        PushNotificationManager.handlePushNotification(solicitationNotification, context)
    }

    fun createNeighborhoodPostNotification(context: Context) {
        val neighborhoodPostContent = """
        {
            "extra": {
                "joinable_id": 8,
                "joinable_type": "Entourage",
                "user_id": 130,
                "type": "NEIGHBORHOOD_POST",
                "instance": "neighborhood_post",
                "instance_id": 8,
                "tracking": "post_on_create_to_neighborhood"
            },
            "message": "Nouveau post dans le quartier"
        }
        """.trimIndent()

        val neighborhoodPostNotification = PushNotificationMessage(
            author = "System",
            msgObject = "Neighborhood Post",
            content = neighborhoodPostContent,
            pushNotificationId = 8,
            pushNotificationTag = "neighborhood_post"
        )
        PushNotificationManager.handlePushNotification(neighborhoodPostNotification, context)
    }

    fun createOutingPostNotification(context: Context) {
        val outingPostContent = """
        {
            "extra": {
                "joinable_id": 9,
                "joinable_type": "Entourage",
                "user_id": 131,
                "type": "OUTING_POST",
                "instance": "outing_post",
                "instance_id": 9,
                "tracking": "post_on_create_to_outing"
            },
            "message": "Nouveau post dans l'événement"
        }
        """.trimIndent()

        val outingPostNotification = PushNotificationMessage(
            author = "System",
            msgObject = "Outing Post",
            content = outingPostContent,
            pushNotificationId = 9,
            pushNotificationTag = "outing_post"
        )
        PushNotificationManager.handlePushNotification(outingPostNotification, context)
    }

    fun createWelcomeNotification(context: Context, stage: String, pushNotificationId: Int) {
        val welcomeContent = """
        {
            "extra": {
                "joinable_id": $pushNotificationId,
                "joinable_type": "Entourage",
                "user_id": ${pushNotificationId + 100},
                "type": "WELCOME",
                "instance": "welcome",
                "instance_id": $pushNotificationId,
                "tracking": "",
                "stage": "$stage"
            },
            "message": "Bienvenue au Jour $stage"
        }
        """.trimIndent()

        val welcomeNotification = PushNotificationMessage(
            author = "System",
            msgObject = "Welcome",
            content = welcomeContent,
            pushNotificationId = pushNotificationId,
            pushNotificationTag = "welcome_day$stage"
        )
        PushNotificationManager.handlePushNotification(welcomeNotification, context)
    }

    fun createAllMockNotifications(context: Context) {
        createContributionNotification(context)
        createConversationNotification(context)
        createOutingNotification(context)
        createJoinRequestNotification(context)
        createInvitationNotification(context)
        createJoinRequestAcceptedNotification(context)
        createSolicitationNotification(context)
        createNeighborhoodPostNotification(context)
        createOutingPostNotification(context)
        createWelcomeNotification(context, "h1", 10)
        createWelcomeNotification(context, "j2", 11)
        createWelcomeNotification(context, "j5", 12)
        createWelcomeNotification(context, "j8", 13)
        createWelcomeNotification(context, "j11", 14)
    }
}
