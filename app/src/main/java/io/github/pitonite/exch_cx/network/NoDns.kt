package io.github.pitonite.exch_cx.network

import okhttp3.Dns
import java.net.InetAddress

/** Prevent DNS requests. Important when proxying all requests over Tor to not leak DNS queries. */
class NoDns : Dns {
  override fun lookup(hostname: String): List<InetAddress> {
    return listOf(InetAddress.getByAddress(hostname, ByteArray(4)))
  }
}
