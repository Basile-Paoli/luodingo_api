package fr.ludodingo.repository

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import fr.ludodingo.service.env
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.Transaction

val hikariConfig = HikariConfig().apply {
    jdbcUrl = env.get("DB_URL")
    driverClassName = "org.postgresql.Driver"
    username = env.get("DB_USER")
    password = env.get("DB_PASSWORD")
    maximumPoolSize = 10
    transactionIsolation = "TRANSACTION_REPEATABLE_READ"
}

fun connectToDatabase() = Database.connect(
    datasource = HikariDataSource(hikariConfig),
)


fun Transaction.initTable(table: Table) {
    SchemaUtils.create(table)
    SchemaUtils.addMissingColumnsStatements(table).forEach {
        exec(it)
    }
}
