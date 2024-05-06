package com.nthlink.android.core.vpn.outline

import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import com.nthlink.android.core.RootVpn
import com.nthlink.android.core.model.Outline
import com.nthlink.android.core.model.Proxy
import com.nthlink.android.core.utils.EMPTY
import com.nthlink.android.core.utils.TAG
import com.nthlink.android.core.vpn.RootVpnClient
import com.nthlink.android.core.vpn.RootVpnService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean


internal class OutlineClient(
    private val vpnService: RootVpnService,
    private val scope: CoroutineScope
) : RootVpnClient, outline.PacketWriter, outline.SocketProtector {

    private var handlePacketsJob: Job? = null
    private var tunFd: ParcelFileDescriptor? = null

    private val isRunning = AtomicBoolean(false)
    private val buffer = ByteBuffer.allocate(1501)

    private lateinit var inputStream: FileInputStream
    private lateinit var outputStream: FileOutputStream

    override fun start(proxies: Array<out Proxy>) {
        scope.launch(Dispatchers.IO) {
            vpnService.updateVpnStatus(RootVpn.Status.CONNECTING)

            tunFd = vpnService.Builder()
                .addAddress("10.255.0.1", 30)
                .addDnsServer("1.1.1.1")
                .addRoute("0.0.0.0", 0)
                // Must add our VPN package to the disallowed list or pass a socket
                // protector to outline to make outgoing traffic bypass the VPN.
                .addDisallowedApplication(vpnService.applicationContext.packageName)
                .establish()

            val tun = tunFd ?: run {
                Log.e(TAG, "Cannot establish tun interface")
                stop()
                return@launch
            }

            // Put TUN FD in blocking mode
            outline.Outline.setNonblock(tun.fd.toLong(), false)

            inputStream = FileInputStream(tun.fileDescriptor)
            outputStream = FileOutputStream(tun.fileDescriptor)

            val proxy = proxies.filterIsInstance(Outline::class.java).random()

            try {
                // packetWriter, socketProtector, address, cipher, secret, prefix
                outline.Outline.start(
                    this@OutlineClient,
                    this@OutlineClient,
                    "${proxy.host}:${proxy.port}",
                    proxy.encryptMethod,
                    proxy.password,
                    EMPTY
                )
            } catch (e: Exception) {
                Log.e(TAG, "Start outline failed: ", e)
                stop()
                return@launch
            }

            isRunning.set(true)

            // Handle IP packet reading in another thread
            handlePackets()
            vpnService.startForegroundWithNotification()
            vpnService.updateVpnStatus(RootVpn.Status.CONNECTED)
        }
    }

    // Implement the PacketWriter interface in order to write back IP packets.
    override fun writePacket(pkt: ByteArray?) {
        try {
            outputStream.write(pkt)
        } catch (e: Exception) {
            Log.e(TAG, "writePacket: failed to write bytes to TUN:", e)
        }
    }

    override fun protect(fd: Long): Boolean {
        return vpnService.protect(fd.toInt())
    }

    override fun stop() {
        scope.launch {
            vpnService.updateVpnStatus(RootVpn.Status.DISCONNECTING)
            isRunning.set(false)

            handlePacketsJob?.cancel()
            handlePacketsJob = null

            try {
                outline.Outline.stop()
            } catch (e: Exception) {
                Log.e(TAG, "Stop outline failed: ", e)
            }

            tunFd?.close()
            tunFd = null

            vpnService.stopForeground(VpnService.STOP_FOREGROUND_REMOVE)
            vpnService.updateVpnStatus(RootVpn.Status.DISCONNECTED)
            vpnService.stopSelf()
        }
    }

    // Read IP packets from TUN FD and feed to outline
    private fun handlePackets() {
        handlePacketsJob = scope.launch(Dispatchers.IO) {
            while (isRunning.get()) {
                try {
                    val n = inputStream.read(buffer.array())
                    if (n > 0) {
                        buffer.limit(n)
                        outline.Outline.writePacket(buffer.array())
                        buffer.clear()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "handlePackets: failed to read bytes from TUN: ", e)
                }
            }
        }
    }
}