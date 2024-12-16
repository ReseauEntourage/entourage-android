package social.entourage.android.tools.view

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import org.webrtc.*
import social.entourage.android.databinding.ActivityVideoCallBinding

class VideoCallActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVideoCallBinding
    private lateinit var peerConnectionFactory: PeerConnectionFactory
    private lateinit var peerConnection: PeerConnection
    private lateinit var firebaseDatabase: DatabaseReference
    private lateinit var eglBase: EglBase
    private var videoCapturer: VideoCapturer? = null

    private val ICE_SERVERS = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer()
    )
    private val TAG = "WebRTC"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialiser ViewBinding
        binding = ActivityVideoCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        eglBase = EglBase.create()
        initializePeerConnectionFactory()
        initializeSurfaceViews()
        initializeFirebase()

        // **IMPORTANT : Initialiser peerConnection en premier**
        setupPeerConnection()

        // Ensuite démarrer la vidéo locale
        startLocalVideo()

        // Enfin écouter les messages et envoyer une offre
        listenForRemoteMessages()
        sendOffer()
    }


    private fun initializePeerConnectionFactory() {
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(applicationContext)
                .setEnableInternalTracer(true)
                .createInitializationOptions()
        )

        peerConnectionFactory = PeerConnectionFactory.builder()
            .setVideoEncoderFactory(DefaultVideoEncoderFactory(eglBase.eglBaseContext, true, true))
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBase.eglBaseContext))
            .createPeerConnectionFactory()
    }

    private fun initializeSurfaceViews() {
        binding.localView.init(eglBase.eglBaseContext, null)
        binding.remoteView.init(eglBase.eglBaseContext, null)
        binding.localView.setMirror(true)
        binding.remoteView.setMirror(false)
    }

    private fun initializeFirebase() {
        firebaseDatabase = FirebaseDatabase.getInstance().reference.child("signaling")
    }

    private fun startLocalVideo() {
        val surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBase.eglBaseContext)
        videoCapturer = createVideoCapturer()

        // Créer la source vidéo
        val videoSource = peerConnectionFactory.createVideoSource(videoCapturer!!.isScreencast)
        videoCapturer?.initialize(surfaceTextureHelper, this, videoSource.capturerObserver)
        videoCapturer?.startCapture(640, 480, 30)

        // Créer une piste vidéo
        val localVideoTrack = peerConnectionFactory.createVideoTrack("localVideoTrack", videoSource)
        localVideoTrack.addSink(binding.localView)

        // Créer une source audio et une piste audio
        val audioSource = peerConnectionFactory.createAudioSource(MediaConstraints())
        val localAudioTrack = peerConnectionFactory.createAudioTrack("localAudioTrack", audioSource)

        // Ajouter les pistes vidéo et audio à la PeerConnection
        peerConnection.addTrack(localVideoTrack, listOf("streamId"))
        peerConnection.addTrack(localAudioTrack, listOf("streamId"))
    }


    private fun setupPeerConnection() {
        val rtcConfig = PeerConnection.RTCConfiguration(ICE_SERVERS)

        val observer = object : PeerConnection.Observer {
            override fun onIceCandidate(candidate: IceCandidate) {
                sendIceCandidate(candidate)
            }

            override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {}

            override fun onAddStream(stream: MediaStream) {
                runOnUiThread {
                    stream.videoTracks[0]?.addSink(binding.remoteView)
                }
            }

            override fun onSignalingChange(state: PeerConnection.SignalingState) {}
            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState) {}
            override fun onIceConnectionReceivingChange(receiving: Boolean) {}
            override fun onIceGatheringChange(state: PeerConnection.IceGatheringState) {}
            override fun onRemoveStream(stream: MediaStream) {}
            override fun onDataChannel(channel: DataChannel) {}
            override fun onRenegotiationNeeded() {}
            override fun onTrack(transceiver: RtpTransceiver) {}
        }

        peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, observer)!!
    }

    private fun sendOffer() {
        peerConnection.createOffer(object : SdpObserver {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                peerConnection.setLocalDescription(this, sessionDescription)
                firebaseDatabase.child("offer").setValue(sessionDescription.description)
            }

            override fun onSetSuccess() {}
            override fun onCreateFailure(error: String?) {
                Log.e(TAG, "Offer creation failed: $error")
            }

            override fun onSetFailure(error: String?) {}
        }, MediaConstraints())
    }

    private fun listenForRemoteMessages() {
        firebaseDatabase.child("offer").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val offer = snapshot.value as String?
                offer?.let {
                    val sdp = SessionDescription(SessionDescription.Type.OFFER, it)
                    peerConnection.setRemoteDescription(object : SdpObserver {
                        override fun onCreateSuccess(p0: SessionDescription?) {}
                        override fun onSetSuccess() {
                            sendAnswer()
                        }

                        override fun onCreateFailure(p0: String?) {}
                        override fun onSetFailure(p0: String?) {}
                    }, sdp)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun sendAnswer() {
        peerConnection.createAnswer(object : SdpObserver {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                peerConnection.setLocalDescription(this, sessionDescription)
                firebaseDatabase.child("answer").setValue(sessionDescription.description)
            }

            override fun onSetSuccess() {}
            override fun onCreateFailure(error: String?) {}
            override fun onSetFailure(error: String?) {}
        }, MediaConstraints())
    }

    private fun sendIceCandidate(candidate: IceCandidate) {
        val map = mapOf(
            "sdpMid" to candidate.sdpMid,
            "sdpMLineIndex" to candidate.sdpMLineIndex,
            "candidate" to candidate.sdp
        )
        firebaseDatabase.child("iceCandidates").push().setValue(map)
    }

    private fun createVideoCapturer(): VideoCapturer {
        return Camera2Enumerator(this).run {
            deviceNames.firstOrNull { isFrontFacing(it) }?.let { createCapturer(it, null) }
        } ?: throw IllegalStateException("No front-facing camera found")
    }

    override fun onDestroy() {
        super.onDestroy()
        videoCapturer?.stopCapture()
        peerConnection.close()
        binding.localView.release()
        binding.remoteView.release()
        eglBase.release()
    }
}
