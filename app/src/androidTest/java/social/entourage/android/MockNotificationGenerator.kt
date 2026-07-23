package social.entourage.android

import android.content.Context
import social.entourage.android.api.model.notification.PushNotificationContent
import social.entourage.android.api.model.notification.PushNotificationMessage
import social.entourage.android.notifications.PushNotificationManager
import social.entourage.android.test.BuildConfig

object MockNotificationGenerator {

    fun createContributionNotification(context: Context): Int {
        val title = "Nouvelle contribution à l'événement"
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
            "message": "$title"
        }
        """.trimIndent()
        val pushNotificationId = 1
        val contributionNotification = PushNotificationMessage(
            author = "System",
            msgObject = "Contribution",
            content = contributionContent,
            pushNotificationId = pushNotificationId,
            pushNotificationTag = "contribution"
        )
        PushNotificationManager.handlePushNotification(contributionNotification, context)
        return pushNotificationId
    }

    fun createConversationNotification(context: Context): Int {
        val messageConversation = "Vous avez un nouveau message dans la conversation"

        val conversationContent = """
        {
            "extra": {
                "joinable_id": 2,
                "joinable_type": "Entourage",
                "user_id": 124,
                "type": "${PushNotificationContent.TYPE_NEW_CHAT_MESSAGE}",
                "instance": "conversations",
                "instance_id": 2,
                "tracking": "private_chat_message_on_create"
            },
            "message": "$messageConversation"
        }
        """.trimIndent()

        val pushNotificationId = 2
        val conversationNotification = PushNotificationMessage(
            author = "System",
            msgObject = "Conversation",
            content = conversationContent,
            pushNotificationId = pushNotificationId,
            pushNotificationTag = "conversation"
        )
        PushNotificationManager.handlePushNotification(conversationNotification, context)
        return pushNotificationId
    }

    fun createOutingNotification(context: Context): Int {
        val title = "Un nouvel événement a été créé"
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
            "message": "$title"
        }
        """.trimIndent()

        val pushNotificationId = 3
        val outingNotification = PushNotificationMessage(
            author = "System",
            msgObject = "Outing",
            content = outingContent,
            pushNotificationId = pushNotificationId,
            pushNotificationTag = "outing"
        )
        PushNotificationManager.handlePushNotification(outingNotification, context)
        return pushNotificationId
    }

    fun createJoinRequestNotification(context: Context): Int {
        val title ="Nouvelle demande pour rejoindre l'événement"
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
            "message": "$title"
        }
        """.trimIndent()
        val pushNotificationId = 4
        val joinRequestNotification = PushNotificationMessage(
            author = "System",
            msgObject = "Join Request",
            content = joinRequestContent,
            pushNotificationId = pushNotificationId,
            pushNotificationTag = "join_request"
        )
        PushNotificationManager.handlePushNotification(joinRequestNotification, context)
        return pushNotificationId
    }

    fun createInvitationNotification(context: Context): Int {
        val title = "Vous avez été invité à rejoindre l'entourage"
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
            "message": "$title"
        }
        """.trimIndent()
        val pushNotificationId = 5
        val invitationNotification = PushNotificationMessage(
            author = "System",
            msgObject = "Invitation",
            content = invitationContent,
            pushNotificationId = pushNotificationId,
            pushNotificationTag = "invitation"
        )
        PushNotificationManager.handlePushNotification(invitationNotification, context)
        return pushNotificationId
    }

    fun createJoinRequestAcceptedNotification(context: Context): Int {
        val title = "Votre demande pour rejoindre l'entourage a été acceptée"
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
            "message": "$title"
        }
        """.trimIndent()
        val pushNotificationId = 6
        val joinRequestAcceptedNotification = PushNotificationMessage(
            author = "System",
            msgObject = "Join Request Accepted",
            content = joinRequestAcceptedContent,
            pushNotificationId = pushNotificationId,
            pushNotificationTag = "join_request_accepted"
        )
        PushNotificationManager.handlePushNotification(joinRequestAcceptedNotification, context)
        return pushNotificationId
    }

    fun createSolicitationNotification(context: Context): Int {
        val title = "Nouvelle sollicitation reçue"
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
            "message": "$title"
        }
        """.trimIndent()
        val pushNotificationId = 7
        val solicitationNotification = PushNotificationMessage(
            author = "System",
            msgObject = "Solicitation",
            content = solicitationContent,
            pushNotificationId = pushNotificationId,
            pushNotificationTag = "solicitation"
        )
        PushNotificationManager.handlePushNotification(solicitationNotification, context)
        return pushNotificationId
    }

    fun createNeighborhoodPostNotification(context: Context): Int {
        val title = "Nouveau post dans le quartier"
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
            "message": "$title"
        }
        """.trimIndent()
        val pushNotificationId = 8
        val neighborhoodPostNotification = PushNotificationMessage(
            author = "System",
            msgObject = "Neighborhood Post",
            content = neighborhoodPostContent,
            pushNotificationId = pushNotificationId,
            pushNotificationTag = "neighborhood_post"
        )
        PushNotificationManager.handlePushNotification(neighborhoodPostNotification, context)
        return pushNotificationId
    }

    fun createOutingPostNotification(context: Context): Int {
        val title = "Nouveau post dans l'événement"
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
            "message": "$title"
        }
        """.trimIndent()
        val pushNotificationId = 9
        val outingPostNotification = PushNotificationMessage(
            author = "System",
            msgObject = "Outing Post",
            content = outingPostContent,
            pushNotificationId = pushNotificationId,
            pushNotificationTag = "outing_post"
        )
        PushNotificationManager.handlePushNotification(outingPostNotification, context)
        return pushNotificationId
    }

    fun createWelcomeNotification(context: Context, stage: String, pushNotificationId: Int): Int {
        val title = "Bienvenue au Jour $stage"
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
            "message": "$title"
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
        return pushNotificationId
    }

    fun createFCMNotification(context: Context): Int {
        PushNotificationManager.displayFCMPushNotification(
            BuildConfig.DEEP_LINKS_SCHEME + "://profile",
            "InApp vers Profil",
            "Doit ouvrir le profil",
            context)
        return PushNotificationMessage.Companion.PushNotificationIds.FCM
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
        createFCMNotification(context)
    }
}