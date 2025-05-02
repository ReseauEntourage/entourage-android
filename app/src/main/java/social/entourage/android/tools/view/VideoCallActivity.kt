//UNCOMMENT FOR VIDEO CALL FEATURE

/*
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
    private lateinit var roomRef: DatabaseReference
    private lateinit var eglBase: EglBase
    private var videoCapturer: VideoCapturer? = null

    private val roomId = "room2"
    private var isOfferer = false
    private var isAnswerReceived = false

    private val ICE_SERVERS = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
    )
    private val CAMERA_PERMISSION_REQUEST = 1001

    private val retryHandler = Handler(Looper.getMainLooper())
    private val retryInterval = 5000L // toutes les 5 secondes si pas d'answer

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

        Timber.wtf("Vidéo locale démarrée")

        // Déterminer le rôle en fonction de l'existence d'une offre
        checkRoomStatusAndAct()
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
                Timber.wtf("Permissions refusées, impossible d'afficher la caméra")
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
        val options = PeerConnectionFactory.Options()
        peerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(options)
            .setVideoEncoderFactory(DefaultVideoEncoderFactory(eglBase.eglBaseContext, true, true))
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBase.eglBaseContext))
            .createPeerConnectionFactory()

        Timber.wtf("PeerConnectionFactory initialisée")
    }

    private fun initializeSurfaceViews() {
        binding.localView.init(eglBase.eglBaseContext, null)
        binding.remoteView.init(eglBase.eglBaseContext, null)
        binding.localView.setZOrderMediaOverlay(true)
        binding.localView.setMirror(true)
        binding.remoteView.setMirror(false)
        Timber.wtf("SurfaceViews initialisées")
    }

    private fun initializeFirebase() {
        roomRef = FirebaseDatabase.getInstance().reference.child("signaling").child(roomId)
        Timber.wtf("Référence Firebase initialisée (roomId=$roomId)")

        // Test écriture
        roomRef.root.child("testWrite").setValue("HelloWorld")
            .addOnSuccessListener { Timber.wtf("Écriture test réussie") }
            .addOnFailureListener { e -> Timber.wtf("Échec écriture test : ${e.message}") }
    }

    private fun setupPeerConnection() {
        val rtcConfig = PeerConnection.RTCConfiguration(ICE_SERVERS)
        // Mode Unified Plan est par défaut
        val observer = object : PeerConnection.Observer {
            override fun onIceCandidate(candidate: IceCandidate) {
                sendIceCandidate(candidate)
                Timber.wtf("onIceCandidate - Candidat ICE envoyé : ${candidate.sdp}")
            }

            override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {}
            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState) {
                Timber.wtf("ICE Connection State: $state")
            }

            override fun onAddStream(stream: MediaStream) {
                // Déprécié, ne sera pas appelé avec Unified Plan
            }

            override fun onTrack(transceiver: RtpTransceiver) {
                // Appelé pour les pistes distantes dans Unified Plan
                val track = transceiver.receiver.track() as? VideoTrack
                runOnUiThread {
                    track?.addSink(binding.remoteView)
                    Timber.wtf("Piste distante reçue dans onTrack()")
                }
            }

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

    private fun startLocalVideo() {
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

        val audioSource = peerConnectionFactory.createAudioSource(MediaConstraints())
        val localAudioTrack = peerConnectionFactory.createAudioTrack("localAudioTrack", audioSource)

        // Ajout des pistes avec addTrack (Unified Plan)
        peerConnection?.addTrack(localVideoTrack, listOf("streamId"))
        peerConnection?.addTrack(localAudioTrack, listOf("streamId"))
        Timber.wtf("Pistes vidéo et audio locales ajoutées à la PeerConnection (Unified Plan)")
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

    private fun checkRoomStatusAndAct() {
        // On regarde s'il y a déjà une offre dans la room
        roomRef.child("offer").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val offer = snapshot.value as String?
                if (offer == null) {
                    // Pas d'offre -> on devient l'offerer
                    isOfferer = true
                    Timber.wtf("Aucune offre dans la room, on devient offerer et on envoie une offer")
                    listenForRemoteMessagesAsOfferer()
                    sendOffer()
                    // On attend l'answer, si pas reçue on retente après 5s
                    scheduleRetryIfNoAnswer()
                } else {
                    // Une offre existe déjà -> on est answerer
                    isOfferer = false
                    Timber.wtf("Une offre existe déjà, on devient answerer")
                    listenForRemoteMessagesAsAnswerer()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.wtf("Erreur lecture offre : ${error.message}")
            }
        })
    }

    private fun listenForRemoteMessagesAsOfferer() {
        // On écoute l'answer
        roomRef.child("answer").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val answer = snapshot.value as String?
                if (answer != null) {
                    Timber.wtf("Answer reçue : $answer")
                    val sdp = SessionDescription(SessionDescription.Type.ANSWER, answer)
                    peerConnection?.setRemoteDescription(object : SdpObserver {
                        override fun onCreateSuccess(desc: SessionDescription?) {}
                        override fun onSetSuccess() {
                            Timber.wtf("Answer SDP définie avec succès")
                            isAnswerReceived = true
                        }
                        override fun onCreateFailure(p0: String?) {}
                        override fun onSetFailure(p0: String?) {}
                    }, sdp)
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Timber.wtf("Erreur lors de l'écoute des answers : ${error.message}")
            }
        })

        listenForIceCandidates()
    }

    private fun listenForRemoteMessagesAsAnswerer() {
        // On écoute l'offre
        roomRef.child("offer").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val offer = snapshot.value as String?
                if (offer != null && !isOfferer) {
                    Timber.wtf("Offre reçue : $offer")
                    val sdp = SessionDescription(SessionDescription.Type.OFFER, offer)
                    peerConnection?.setRemoteDescription(object : SdpObserver {
                        override fun onCreateSuccess(desc: SessionDescription?) {}
                        override fun onSetSuccess() {
                            // On a l'offre, on crée l'answer
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

        listenForIceCandidates()
    }

    private fun listenForIceCandidates() {
        roomRef.child("iceCandidates").addValueEventListener(object : ValueEventListener {
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
                Timber.wtf("Erreur ICE candidates : ${error.message}")
            }
        })
    }

    private fun sendOffer() {
        peerConnection?.createOffer(object : SdpObserver {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                peerConnection?.setLocalDescription(this, sessionDescription)
                roomRef.child("offer").setValue(sessionDescription.description)
                    .addOnSuccessListener {
                        Timber.wtf("Offre SDP envoyée avec succès")
                    }
                    .addOnFailureListener { e ->
                        Timber.wtf("Échec d'envoi de l'offre SDP : ${e.message}")
                    }
            }
            override fun onSetSuccess() {}
            override fun onCreateFailure(error: String?) {
                Timber.wtf("Échec création offre : $error")
            }
            override fun onSetFailure(error: String?) {}
        }, MediaConstraints())
    }

    private fun sendAnswer() {
        peerConnection?.createAnswer(object : SdpObserver {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                peerConnection?.setLocalDescription(this, sessionDescription)
                roomRef.child("answer").setValue(sessionDescription.description)
                    .addOnSuccessListener {
                        Timber.wtf("Answer SDP envoyée avec succès")
                    }
                    .addOnFailureListener { e ->
                        Timber.wtf("Échec envoi answer SDP : ${e.message}")
                    }
            }

            override fun onSetSuccess() {}
            override fun onCreateFailure(error: String?) {
                Timber.wtf("Échec création answer : $error")
            }
            override fun onSetFailure(error: String?) {}
        }, MediaConstraints())
    }

    private fun sendIceCandidate(candidate: IceCandidate) {
        val map = mapOf(
            "sdpMid" to candidate.sdpMid,
            "sdpMLineIndex" to candidate.sdpMLineIndex,
            "candidate" to candidate.sdp
        )
        roomRef.child("iceCandidates").push().setValue(map)
            .addOnSuccessListener {
                Timber.wtf("Candidat ICE envoyé : ${candidate.sdp}")
            }
            .addOnFailureListener { e ->
                Timber.wtf("Échec envoi ICE candidate : ${e.message}")
            }
    }

    private fun scheduleRetryIfNoAnswer() {
        if (isOfferer) {
            retryHandler.postDelayed({
                if (!isAnswerReceived) {
                    Timber.wtf("Aucune answer reçue, on retente de créer une offre")
                    sendOffer()
                    scheduleRetryIfNoAnswer()
                }
            }, retryInterval)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            videoCapturer?.stopCapture()
        } catch (e: Exception) {
            Timber.wtf("Erreur arrêt capture : ${e.message}")
        }
        peerConnection?.close()
        binding.localView.release()
        binding.remoteView.release()
        eglBase.release()
        Timber.wtf("Activité détruite, ressources libérées")
    }
}
*/
