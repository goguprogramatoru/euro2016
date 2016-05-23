package datamappers

import com.datastax.driver.core.Cluster

object Db {
	val cluster = Cluster.builder().addContactPoint("127.0.0.1").build()
	val session = cluster.connect("euro2016")
}
