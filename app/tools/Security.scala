package tools

import java.security.MessageDigest

object Security {

	def md5(s: String) = {
		MessageDigest.getInstance("MD5").digest(s.getBytes).map("%02X".format(_)).mkString
	}

}
