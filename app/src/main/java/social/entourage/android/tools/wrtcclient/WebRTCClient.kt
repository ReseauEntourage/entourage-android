//UNCOMMENT FOR VIDEO CALL FEATURE


/*
package social.entourage.android.tools.wrtcclient

import android.content.Context
import org.webrtc.*

class WebRTCClient(
    private val context: Context,
    private val eglBase: EglBase
) {
    private var peerConnectionFactory: PeerConnectionFactory
    private var localVideoTrack: VideoTrack? = null
    private var localAudioTrack: AudioTrack? = null
    private var localSurfaceView: SurfaceViewRenderer? = null
    private var remoteSurfaceView: SurfaceViewRenderer? = null
    private var peerConnection: PeerConnection? = null

    init {
        // Initialisation de WebRTC
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)

        // Création de l'usine PeerConnection
        peerConnectionFactory = PeerConnectionFactory.builder()
            .setVideoEncoderFactory(DefaultVideoEncoderFactory(eglBase.eglBaseContext, true, true))
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBase.eglBaseContext))
            .createPeerConnectionFactory()
    }

    fun startLocalVideo(localView: SurfaceViewRenderer) {
        localSurfaceView = localView
        localSurfaceView?.init(eglBase.eglBaseContext, null)

        // Configuration de la caméra
        val videoCapturer = createVideoCapturer()
        val surfaceTextureHelper = SurfaceTextureHelper.create("CaptureThread", eglBase.eglBaseContext)
        val videoSource = peerConnectionFactory.createVideoSource(videoCapturer.isScreencast)

        videoCapturer.initialize(surfaceTextureHelper, context, videoSource.capturerObserver)
        videoCapturer.startCapture(640, 480, 30)

        // Création de la piste vidéo locale
        localVideoTrack = peerConnectionFactory.createVideoTrack("videoTrack", videoSource)
        localVideoTrack?.addSink(localSurfaceView)

        // Création de la piste audio
        val audioSource = peerConnectionFactory.createAudioSource(MediaConstraints())
        localAudioTrack = peerConnectionFactory.createAudioTrack("audioTrack", audioSource)
    }

    fun setRemoteSurface(remoteView: SurfaceViewRenderer) {
        remoteSurfaceView = remoteView
        remoteSurfaceView?.init(eglBase.eglBaseContext, null)
    }

    private fun createVideoCapturer(): VideoCapturer {
        return Camera2Enumerator(context).run {
            deviceNames.firstOrNull { isFrontFacing(it) }?.let { createCapturer(it, null) }
        } ?: throw IllegalStateException("No front facing camera found")
    }

    fun createPeerConnection(observer: PeerConnection.Observer) {
        val iceServers = listOf(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer())
        peerConnection = peerConnectionFactory.createPeerConnection(iceServers, observer)
    }
}
*/
