package mobi.sevenwinds.app.budget

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Join
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object BudgetService {
    suspend fun addRecord(body: BudgetRecord): BudgetRecord = withContext(Dispatchers.IO) {
        transaction {
            val entity = BudgetEntity.new {
                this.year = body.year
                this.month = body.month
                this.amount = body.amount
                this.type = body.type
                this.authorId = body.authorId
            }

            return@transaction entity.toResponse()
        }
    }

    suspend fun getYearStats(param: BudgetYearParam): BudgetYearStatsResponse = withContext(Dispatchers.IO) {
        transaction {
            val query = BudgetTable
                .select { BudgetTable.year eq param.year }

            val total = query.count()
            val data = BudgetEntity.wrapRows(query).map { it.toResponse() }

            val sumByType = data.groupBy { it.type.name }.mapValues { it.value.sumOf { v -> v.amount } }

            val slicedData = if (param.offset == 0 && param.limit == 0) {
                data.sortedByDescending { it.amount }.sortedBy { it.month }
            } else {
                data.slice(param.offset..param.limit).sortedByDescending { it.amount }.sortedBy { it.month }
            }

            slicedData.forEach { budget ->
                if (budget.authorId != null) {
                    val authorQuery = AuthorTable
                        .select{ AuthorTable.id eq budget.authorId}
                    val authorData = AuthorEntity.wrapRows(authorQuery).map { it.toDto() }

                    budget.authorFullName = authorData.first().fullName
                    budget.authorRecordCreatedAt = authorData.first().createdAt
                }
            }


            return@transaction BudgetYearStatsResponse(
                total = total,
                totalByType = sumByType,
                items = slicedData
            )
        }
    }
}