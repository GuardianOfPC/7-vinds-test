package mobi.sevenwinds.app.budget

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.IntIdTable
import org.jetbrains.exposed.sql.stringLiteral
import org.jetbrains.exposed.sql.stringParam
import java.time.LocalDateTime

object AuthorTable : IntIdTable("author") {
    val fullName = text("full_name")
    val createdAt = date("created_at")
}

class AuthorEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AuthorEntity>(AuthorTable)

    var fullName by AuthorTable.fullName
    var createdAt by AuthorTable.createdAt

    fun toResponse(): AuthorRecord {
        return AuthorRecord(fullName)
    }

    fun toDto(): AuthorDto {
        return AuthorDto(fullName, createdAt)
    }
}