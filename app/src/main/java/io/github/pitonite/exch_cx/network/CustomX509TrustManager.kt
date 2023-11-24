package io.github.pitonite.exch_cx.network

import java.security.MessageDigest
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

class CustomX509TrustManager(
    private val expectedSha256Fingerprint: String,
    private val expectedSha1Fingerprint: String,
) : X509TrustManager {

  override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
    throw CertificateException("cannot check client trusted")
  }

  override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
    if (chain == null || chain.isEmpty())
        throw IllegalArgumentException("Certificate chain is empty")

    val serverCert = chain[0]
    val sha256Fingerprint = getSha256Fingerprint(serverCert)
    val sha1Fingerprint = getSha1Fingerprint(serverCert)

    if (sha256Fingerprint != expectedSha256Fingerprint ||
        sha1Fingerprint != expectedSha1Fingerprint) {
      throw IllegalArgumentException("Certificate fingerprint or hostname not matched")
    }
  }

  override fun getAcceptedIssuers(): Array<X509Certificate> {
    return arrayOf()
  }

  private fun getSha256Fingerprint(cert: X509Certificate): String {
    val md = MessageDigest.getInstance("SHA-256")
    md.update(cert.encoded)
    return bytesToHex(md.digest())
  }

  private fun getSha1Fingerprint(cert: X509Certificate): String {
    val md = MessageDigest.getInstance("SHA-1")
    md.update(cert.encoded)
    return bytesToHex(md.digest())
  }

  private fun bytesToHex(bytes: ByteArray): String {
    val hexChars = "0123456789ABCDEF"
    val result = StringBuilder(bytes.size * 2)
    for (byte in bytes) {
      val intVal = byte.toInt() and 0xff
      result.append(hexChars[intVal ushr 4])
      result.append(hexChars[intVal and 0x0f])
    }
    return result.toString()
  }
}
