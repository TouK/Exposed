package org.jetbrains.exposed.sql.tests.shared

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.exists
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.tests.DatabaseTestsBase
import org.junit.Test

class WrapperColumnTest : DatabaseTestsBase() {

    data class StringWrapper(val value: String)
    data class IntWrapper(val value: Int)
    data class IdWrapper(val value: Long)

    object TestTable : Table() {
//        val idWrapper = long("id_wrapper").wrapping(::IdWrapper, IdWrapper::value).autoIncrement().primaryKey()
        val idWrapper = long("id_wrapper").autoIncrement().wrapping(::IdWrapper, IdWrapper::value).primaryKey()
        val stringWrapper = text("string_wrapper").wrapping(::StringWrapper, StringWrapper::value)
        val intWrapper = integer("int_wrapper").wrapping(::IntWrapper, IntWrapper::value).nullable()
    }

    val aStringWrapper = StringWrapper("a string wrapper value")
    val aIntWrapper = IntWrapper(5)

    @Test fun `should create table`() {
        withTables(TestTable) {
            assertEquals(true, TestTable.exists())
        }
    }

    @Test fun `should insert wrapped column`() {
        withTables(TestTable) {
            TestTable.insert {
                val id1 = TestTable.insert {
                    it[TestTable.stringWrapper] = aStringWrapper
                    it[TestTable.intWrapper] = aIntWrapper
                }[TestTable.idWrapper]

                assertEquals(true, id1 is IdWrapper)
                assertEquals(true, id1 != null)

                val id2 = TestTable.insert {
                    it[TestTable.stringWrapper] = aStringWrapper
                }[TestTable.idWrapper]

                assertEquals(true, id2 is IdWrapper)
                assertEquals(true, id2 != null)

                val row1 = TestTable.select { TestTable.idWrapper eq checkNotNull(id1) }.single()

                assertEquals(checkNotNull(id1), row1[TestTable.idWrapper])
                assertEquals(aStringWrapper, row1[TestTable.stringWrapper])
                assertEquals(aIntWrapper, row1[TestTable.intWrapper])

                val row2 = TestTable.select { TestTable.idWrapper eq checkNotNull(id2) }.single()

                assertEquals(checkNotNull(id2), row2[TestTable.idWrapper])
                assertEquals(aStringWrapper, row2[TestTable.stringWrapper])
                assertEquals(null, row2[TestTable.intWrapper])
            }
        }
    }

    @Test fun `should update by wrapped column`() {

    }
}
