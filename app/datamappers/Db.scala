package datamappers

import com.datastax.driver.core.Cluster
import play.api.Play

object Db {

	val user = Play.current.configuration.getString("cassandra.user").getOrElse("cassandra")
	val password = Play.current.configuration.getString("cassandra.password").getOrElse("cassandra")

	val cluster = Cluster.builder().addContactPoint("127.0.0.1")
			.withCredentials(user, password)
				.build()
	val session = cluster.connect("euro2016")
}
