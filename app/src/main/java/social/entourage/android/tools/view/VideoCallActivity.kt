package social.entourage.android.tools.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.*
import org.webrtc.*
import social.entourage.android.databinding.ActivityVideoCallBinding
import timber.log.Timber

class VideoCallActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVideoCallBinding
    private lateinit var peerConnectionFactory: PeerConnectionFactory
    private var peerConnection: PeerConnection? = null
    private lateinit var firebaseDatabase: DatabaseReference
    private lateinit var eglBase: EglBase
    private var videoCapturer: VideoCapturer? = null
    private var connectedUserId: String? = null

    // Identifiant local de l'utilisateur. On le déclare "hostUser" par exemple.
    private val localUserId = "hostUser"

    private val ICE_SERVERS = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
    )

    private val CAMERA_PERMISSION_REQUEST = 1001
    private val retryHandler = Handler(Looper.getMainLooper())
    private val retryInterval = 5000L // 5 secondes

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityVideoCallBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Timber.wtf("onCreate - Vue initialisée")

        if (!checkPermissions()) {
            requestPermissions()
        } else {
            initializeEverything()
        }
    }

    private fun initializeEverything() {
        eglBase = EglBase.create()
        Timber.wtf("EglBase initialisé")

        initializePeerConnectionFactory()
        initializeSurfaceViews()
        initializeFirebase()

        setupPeerConnection()
        startLocalVideo()
        Timber.wtf("Capture vidéo locale et audio démarrées immédiatement")

        // On déclare notre utilisateur local dans la base
        firebaseDatabase.child("users").child(localUserId)
            .setValue(mapOf("isAlive" to true, "isBusy" to false))
            .addOnSuccessListener {
                Timber.wtf("Utilisateur $localUserId créé/activé dans Firebase")

                // Maintenant qu'on existe, on essaie de trouver un autre utilisateur
                startFindingUser()
            }
            .addOnFailureListener { e ->
                Timber.wtf("Échec de création d'utilisateur $localUserId : ${e.message}")
            }
    }

    private fun checkPermissions(): Boolean {
        val cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        val audioPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
        return cameraPermission == PackageManager.PERMISSION_GRANTED && audioPermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO),
            CAMERA_PERMISSION_REQUEST
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                initializeEverything()
                Timber.wtf("Permissions accordées")
            } else {
                Timber.wtf("Permissions non accordées, impossible d'afficher la caméra")
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun initializePeerConnectionFactory() {
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(applicationContext)
                .setEnableInternalTracer(true)
                .createInitializationOptions()
        )
        peerConnectionFactory = PeerConnectionFactory.builder()
            .setVideoEncoderFactory(
                DefaultVideoEncoderFactory(
                    eglBase.eglBaseContext,
                    true,
                    true
                )
            )
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBase.eglBaseContext))
            .createPeerConnectionFactory()

        Timber.wtf("PeerConnectionFactory initialisée")
    }

    private fun initializeSurfaceViews() {
        binding.localView.init(eglBase.eglBaseContext, null)
        binding.remoteView.init(eglBase.eglBaseContext, null)

        binding.localView.setZOrderMediaOverlay(true)
        binding.localView.setEnableHardwareScaler(true)
        binding.localView.setMirror(true)

        binding.remoteView.setEnableHardwareScaler(true)
        binding.remoteView.setMirror(false)

        Timber.wtf("SurfaceViews initialisées")
    }

    private fun initializeFirebase() {
        firebaseDatabase = FirebaseDatabase.getInstance().reference
        Timber.wtf("Référence Firebase initialisée")

        // Test d'écriture simple
        firebaseDatabase.child("testWrite").setValue("HelloWorld")
            .addOnSuccessListener { Timber.wtf("Écriture test réussie") }
            .addOnFailureListener { e -> Timber.wtf("Échec écriture test : ${e.message}") }
    }

    private fun startFindingUser() {
        findAvailableUser { userId ->
            if (userId != null) {
                // On a trouvé un autre utilisateur, on se connecte à lui
                Timber.wtf("Utilisateur disponible trouvé : $userId")
                connectedUserId = userId
                connectToUser(userId)
            } else {
                // Aucun autre utilisateur trouvé. On est donc seul.
                // On se met en "host" : c'est-à-dire qu'on reste dispo
                // et on envoie une offre "dans le vide".
                Timber.wtf("Aucun utilisateur disponible, on devient host (en attente d'un autre)")

                // Ici on crée un noeud signaling avec notre propre userId
                firebaseDatabase = firebaseDatabase.child("signaling").child(localUserId)
                Timber.wtf("Référence Firebase pour signaling initialisée: ${firebaseDatabase.toString()}")

                // On envoie une offre immédiatement. Ainsi, si quelqu'un se connecte plus tard,
                // il recevra cette offre et pourra y répondre.
                sendOffer()

                // On écoute également les messages entrants au cas où quelqu'un répond.
                listenForRemoteMessages()
            }
        }
    }

    private fun findAvailableUser(onUserFound: (String?) -> Unit) {
        firebaseDatabase.child("users").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Timber.wtf("Aucun noeud 'users' trouvé dans la base")
                } else {
                    Timber.wtf("Nombre d'utilisateurs trouvés : ${snapshot.childrenCount}")
                }

                var availableUserId: String? = null
                for (userSnapshot in snapshot.children) {
                    val userId = userSnapshot.key ?: continue
                    if (userId == localUserId) continue // On ignore nous-même
                    val isAlive = userSnapshot.child("isAlive").getValue(Boolean::class.java) ?: false
                    val isBusy = userSnapshot.child("isBusy").getValue(Boolean::class.java) ?: true

                    Timber.wtf("Utilisateur $userId: isAlive=$isAlive, isBusy=$isBusy")
                    if (isAlive && !isBusy) {
                        availableUserId = userId
                        break
                    }
                }
                onUserFound(availableUserId)
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.wtf("Erreur Firebase lors de la recherche d'utilisateur : ${error.message}")
                onUserFound(null)
            }
        })
    }

    private fun connectToUser(userId: String) {
        markUserBusy(userId)
        Timber.wtf("Marqué utilisateur $userId comme occupé")

        // Maintenant qu'on s'est connecté à cet utilisateur, on passe sur son noeud signaling
        firebaseDatabase = firebaseDatabase.child("signaling").child(userId)
        Timber.wtf("Référence Firebase pour signaling initialisée: ${firebaseDatabase.toString()}")

        listenForRemoteMessages()
        sendOffer()
    }

    private fun listenForRemoteMessages() {
        // Écoute des offres
        firebaseDatabase.child("offer").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val offer = snapshot.value as String?
                if (offer == null) {
                    Timber.wtf("Aucune offre reçue")
                } else {
                    Timber.wtf("Offre reçue : $offer")
                    val sdp = SessionDescription(SessionDescription.Type.OFFER, offer)
                    peerConnection?.setRemoteDescription(object : SdpObserver {
                        override fun onCreateSuccess(p0: SessionDescription?) {}
                        override fun onSetSuccess() {
                            sendAnswer()
                        }
                        override fun onCreateFailure(p0: String?) {}
                        override fun onSetFailure(p0: String?) {}
                    }, sdp)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.wtf("Erreur lors de l'écoute des offres : ${error.message}")
            }
        })

        // Écoute des réponses
        firebaseDatabase.child("answer").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val answer = snapshot.value as String?
                if (answer == null) {
                    Timber.wtf("Aucune réponse reçue")
                } else {
                    Timber.wtf("Réponse reçue : $answer")
                    val sdp = SessionDescription(SessionDescription.Type.ANSWER, answer)
                    peerConnection?.setRemoteDescription(object : SdpObserver {
                        override fun onCreateSuccess(p0: SessionDescription?) {}
                        override fun onSetSuccess() {
                            Timber.wtf("Réponse SDP définie avec succès")
                        }
                        override fun onCreateFailure(p0: String?) {}
                        override fun onSetFailure(p0: String?) {}
                    }, sdp)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.wtf("Erreur lors de l'écoute des réponses : ${error.message}")
            }
        })

        // Écoute des candidats ICE
        firebaseDatabase.child("iceCandidates").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Timber.wtf("Aucun ICE candidate reçu")
                    return
                }

                for (child in snapshot.children) {
                    val iceCandidateData = child.value as? Map<String, Any> ?: continue
                    val sdpMid = iceCandidateData["sdpMid"] as? String ?: continue
                    val sdpMLineIndex = (iceCandidateData["sdpMLineIndex"] as? Long)?.toInt() ?: -1
                    val candidateStr = iceCandidateData["candidate"] as? String ?: continue

                    val candidate = IceCandidate(sdpMid, sdpMLineIndex, candidateStr)
                    peerConnection?.addIceCandidate(candidate)
                    Timber.wtf("Candidat ICE ajouté : ${candidate.sdp}")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.wtf("Erreur lors de l'écoute des candidats ICE : ${error.message}")
            }
        })
    }

    private fun sendAnswer() {
        Timber.wtf("Création de la réponse SDP")
        peerConnection?.createAnswer(object : SdpObserver {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                peerConnection?.setLocalDescription(this, sessionDescription)
                Timber.wtf("Réponse SDP créée et définie")
                firebaseDatabase.child("answer").setValue(sessionDescription.description)
                    .addOnSuccessListener {
                        Timber.wtf("Réponse SDP envoyée avec succès : ${sessionDescription.description}")
                    }
                    .addOnFailureListener { e ->
                        Timber.wtf("Échec de l'envoi de la réponse SDP : ${e.message}")
                    }
            }

            override fun onSetSuccess() {}
            override fun onCreateFailure(error: String?) {
                Timber.wtf("Échec de la création de la réponse SDP : $error")
            }

            override fun onSetFailure(error: String?) {}
        }, MediaConstraints())
    }

    private fun markUserBusy(userId: String) {
        FirebaseDatabase.getInstance().reference
            .child("users").child(userId).child("isBusy").setValue(true)
            .addOnSuccessListener {
                Timber.wtf("Utilisateur $userId marqué comme occupé (écriture réussie)")
            }
            .addOnFailureListener { e ->
                Timber.wtf("Échec d'écriture isBusy=true pour $userId : ${e.message}")
            }
    }

    private fun releaseUser(userId: String) {
        FirebaseDatabase.getInstance().reference
            .child("users").child(userId).child("isBusy").setValue(false)
            .addOnSuccessListener {
                Timber.wtf("Utilisateur $userId libéré (écriture réussie)")
            }
            .addOnFailureListener { e ->
                Timber.wtf("Échec d'écriture isBusy=false pour $userId : ${e.message}")
            }
    }

    private fun startLocalVideo() {
        Timber.wtf("Début de la capture vidéo locale et audio")
        val surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBase.eglBaseContext)
        videoCapturer = createVideoCapturer()

        if (videoCapturer == null) {
            Timber.wtf("Impossible d'initialiser le VideoCapturer")
            return
        }

        val videoSource = peerConnectionFactory.createVideoSource(videoCapturer!!.isScreencast)
        videoCapturer?.initialize(surfaceTextureHelper, this, videoSource.capturerObserver)

        try {
            videoCapturer?.startCapture(640, 480, 30)
            Timber.wtf("Capture vidéo démarrée")
        } catch (e: Exception) {
            Timber.wtf("Erreur lors de la capture vidéo : ${e.message}")
        }

        val localVideoTrack = peerConnectionFactory.createVideoTrack("localVideoTrack", videoSource)
        localVideoTrack.addSink(binding.localView)
        Timber.wtf("Piste vidéo locale ajoutée au localView")

        val audioSource = peerConnectionFactory.createAudioSource(MediaConstraints())
        val localAudioTrack = peerConnectionFactory.createAudioTrack("localAudioTrack", audioSource)

        peerConnection?.addTrack(localVideoTrack, listOf("streamId"))
        peerConnection?.addTrack(localAudioTrack, listOf("streamId"))
        Timber.wtf("Pistes vidéo et audio ajoutées à la PeerConnection")
    }

    private fun sendIceCandidate(candidate: IceCandidate) {
        val map = mapOf(
            "sdpMid" to candidate.sdpMid,
            "sdpMLineIndex" to candidate.sdpMLineIndex,
            "candidate" to candidate.sdp
        )
        firebaseDatabase.child("iceCandidates").push().setValue(map)
            .addOnSuccessListener {
                Timber.wtf("Candidat ICE envoyé avec succès : ${candidate.sdp}")
            }
            .addOnFailureListener { e ->
                Timber.wtf("Échec d'envoi ICE candidate : ${e.message}")
            }
    }

    private fun setupPeerConnection() {
        val rtcConfig = PeerConnection.RTCConfiguration(ICE_SERVERS)
        val observer = object : PeerConnection.Observer {
            override fun onIceCandidate(candidate: IceCandidate) {
                sendIceCandidate(candidate)
                Timber.wtf("onIceCandidate - Candidat ICE envoyé : ${candidate.sdp}")
            }

            override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}
            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState) {
                Timber.wtf("onIceConnectionChange - État : $state")
                if (state == PeerConnection.IceConnectionState.FAILED || state == PeerConnection.IceConnectionState.DISCONNECTED) {
                    attemptReconnect()
                }
            }

            override fun onAddStream(stream: MediaStream) {
                runOnUiThread {
                    if (stream.videoTracks.isNotEmpty()) {
                        stream.videoTracks[0]?.addSink(binding.remoteView)
                        Timber.wtf("Stream distant ajouté au remoteView")
                    }
                }
            }

            override fun onTrack(transceiver: RtpTransceiver) {}
            override fun onSignalingChange(state: PeerConnection.SignalingState) {}
            override fun onIceConnectionReceivingChange(receiving: Boolean) {}
            override fun onIceGatheringChange(state: PeerConnection.IceGatheringState) {}
            override fun onRemoveStream(stream: MediaStream) {}
            override fun onDataChannel(channel: DataChannel) {}
            override fun onRenegotiationNeeded() {}
        }

        peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, observer)
        Timber.wtf("PeerConnection créée")
    }

    private fun sendOffer() {
        peerConnection?.createOffer(object : SdpObserver {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                peerConnection?.setLocalDescription(this, sessionDescription)
                firebaseDatabase.child("offer").setValue(sessionDescription.description)
                    .addOnSuccessListener {
                        Timber.wtf("Offre SDP envoyée avec succès : ${sessionDescription.description}")
                    }
                    .addOnFailureListener { e ->
                        Timber.wtf("Échec de l'envoi de l'offre SDP : ${e.message}")
                    }
            }

            override fun onSetSuccess() {}
            override fun onCreateFailure(error: String?) {
                Timber.wtf("Échec de création de l'offre SDP : $error")
            }

            override fun onSetFailure(error: String?) {}
        }, MediaConstraints())
    }

    private fun attemptReconnect() {
        peerConnection?.restartIce()
        sendOffer()
        Timber.wtf("Tentative de reconnexion initiée")
    }

    private fun createVideoCapturer(): VideoCapturer? {
        val enumerator = Camera1Enumerator(false)
        for (deviceName in enumerator.deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                Timber.wtf("Caméra frontale détectée : $deviceName")
                return enumerator.createCapturer(deviceName, null)
            }
        }
        Timber.wtf("Aucune caméra frontale détectée")
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            videoCapturer?.stopCapture()
        } catch (e: Exception) {
            Timber.wtf("Erreur lors de l'arrêt de la capture : ${e.message}")
        }
        peerConnection?.close()
        binding.localView.release()
        binding.remoteView.release()
        eglBase.release()
        connectedUserId?.let { releaseUser(it) }
        Timber.wtf("Activité détruite et ressources libérées")
    }
}
