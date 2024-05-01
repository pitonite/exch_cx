package io.github.pitonite.exch_cx.network

import android.annotation.SuppressLint
import java.security.MessageDigest
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

class HCX509TrustManager() : X509TrustManager {

  override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {
    throw CertificateException("cannot check client trusted")
  }

  @SuppressLint("TrustAllX509TrustManager")
  override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {
    // we skip the check entirely since the certificates may fail to renew from time to time
  }

  override fun getAcceptedIssuers(): Array<X509Certificate> {
    return arrayOf()
  }
}
