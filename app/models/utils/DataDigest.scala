package models.utils

import java.security.MessageDigest
import org.apache.commons.codec.binary.Base64
import com.google.common.base.Charsets

class DataDigest {
  val messageDigest = MessageDigest.getInstance("SHA-256")

  def update(v: Boolean) {
    update(if (v) 1 else 0)
  }

  def update(v: Int) {
    messageDigest.update(((v >>> 24) & 0xFF).toByte)
    messageDigest.update(((v >>> 16) & 0xFF).toByte)
    messageDigest.update(((v >>> 8) & 0xFF).toByte)
    messageDigest.update(((v >>> 0) & 0xFF).toByte)
  }

  def update(v: Long) {
    messageDigest.update(((v >>> 56) & 0xFF).toByte)
    messageDigest.update(((v >>> 48) & 0xFF).toByte)
    messageDigest.update(((v >>> 40) & 0xFF).toByte)
    messageDigest.update(((v >>> 32) & 0xFF).toByte)
    messageDigest.update(((v >>> 24) & 0xFF).toByte)
    messageDigest.update(((v >>> 16) & 0xFF).toByte)
    messageDigest.update(((v >>> 8) & 0xFF).toByte)
    messageDigest.update(((v >>> 0) & 0xFF).toByte)
  }

  def update(string: String) {
    messageDigest.update(string.getBytes(Charsets.UTF_8))
  }

  def update(vOption: Option[Any]) {
    vOption.map {
      v =>
        update(true)
        v match {
          case v: Int => update(v)
          case v: Long => update(v)
          case v => update(v.toString)
        }
    }.getOrElse(update(false))
  }

  def base64Digest() = new String(Base64.encodeBase64(messageDigest.digest()), Charsets.UTF_8);
}

object DataDigest {
  def apply() = new DataDigest()
}