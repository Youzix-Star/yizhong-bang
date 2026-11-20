package com.yizhong.bang.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StatsTest {

    private val sheet = Sheet(
        subjects = listOf("语文", "数学"),
        full = listOf(150.0, 150.0),
        classAvg = Avg(subjects = listOf(85.0, 75.0), total = 611.13),
        gradeAvg = Avg(subjects = listOf(80.0, 70.0), total = 578.59),
        students = listOf(
            Student(no = "1001", name = "张三", total = 600.0, flags = listOf(false, true)),
            Student(no = "1002", name = "李四", total = 550.0, flags = listOf(false, false)),
        ),
    )

    @Test
    fun `差值与人话点评`() {
        val delta = Stats.delta(sheet) ?: 0.0
        assertTrue(delta > 32.0 && delta < 33.0)
        assertEquals("+32.5", Stats.signed(delta))
        assertEquals("611.1", Stats.fmt(Stats.avg(sheet), 1))
        assertTrue(Stats.comment(sheet).contains("比年级均分高"))
    }

    @Test
    fun `归一化与筛选排序`() {
        assertEquals(1f, Stats.pct(150.0, 150.0), 0.001f)
        assertEquals(0f, Stats.pct(null, 150.0), 0.001f)

        assertEquals(listOf("张三", "李四"), Stats.sorted(sheet.students).map { it.name })
        assertEquals(listOf("张三"), Stats.filtered(sheet.students, "张").map { it.name })
        assertEquals(listOf("李四"), Stats.filtered(sheet.students, "1002").map { it.name })
        assertEquals(2, Stats.filtered(sheet.students, " ").size)
    }

    @Test
    fun `偏科计数`() {
        assertEquals(1, Stats.flagCount(sheet.students[0]))
        assertEquals(0, Stats.flagCount(sheet.students[1]))
        assertEquals(2, Stats.subjects(sheet).size)
    }
}
