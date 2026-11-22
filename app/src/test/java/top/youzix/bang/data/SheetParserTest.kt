package top.youzix.bang.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SheetParserTest {

    private fun table(name: String, no: String, score1: Int, score2: Int, total: Int) = """
        <table>
        <tr><td colspan="6">2026-2027学年上学期期中考试成绩单(1班)</td></tr>
        <tr><td>姓名</td><td>项目</td><td>语文</td><td>数学</td><td>总分</td></tr>
        <tr><td>$name</td><td>1497</td><td>满分</td><td>150</td><td>150</td><td>300</td></tr>
        <tr><td></td><td>级均分</td><td>80</td><td>70</td><td>150</td></tr>
        <tr><td></td><td>班均分</td><td>85</td><td>75</td><td>160</td></tr>
        <tr><td></td><td>得分</td><td>$score1</td><td>$score2</td><td>$total</td></tr>
        <tr><td>$no</td><td>58</td><td>位次</td><td>1/500</td><td>2/500</td><td>1/500</td></tr>
        <tr><td></td><td>偏科</td><td>—</td><td>●</td><td></td></tr>
        </table>
    """.trimIndent()

    private val html = "<html><body>2026-2027学年上学期" +
        table("张三", "2026100", 120, 130, 250) +
        table("李四", "2026101", 90, 60, 150) +
        "</body></html>"

    @Test
    fun `解析表头、均分与两位学生`() {
        val sheet = SheetParser.parse(html)

        assertEquals(listOf("语文", "数学"), sheet.subjects)
        assertEquals(listOf(150.0, 150.0), sheet.full)
        assertEquals(160.0, sheet.classAvg?.total ?: -1.0, 0.001)
        assertEquals(listOf(85.0, 75.0), sheet.classAvg?.subjects)
        assertEquals(150.0, sheet.gradeAvg?.total ?: -1.0, 0.001)
        assertEquals("2026-2027学年上学期", sheet.meta?.term)
        assertEquals("1班", sheet.meta?.className)

        assertEquals(2, sheet.students.size)
        val first = sheet.students[0]
        assertEquals("张三", first.name)
        assertEquals("2026100", first.no)
        assertEquals(250.0, first.total ?: -1.0, 0.001)
        assertEquals(listOf(120.0, 130.0), first.scores)
        assertEquals(1, first.totalRank?.classRank)
        assertEquals(500, first.totalRank?.grade)
        assertEquals(listOf(false, true), first.flags)
        assertEquals(2, first.ranks[1]?.classRank)
        assertEquals(58.0, first.classSize ?: -1.0, 0.001)
        assertEquals(1497.0, first.gradeCount ?: -1.0, 0.001)
    }

    @Test
    fun `字段与下拉框解析`() {
        val page = """
            <input type="hidden" name="__VIEWSTATE" value="abc&amp;def" />
            <input type="checkbox" name="unchecked" value="1" />
            <input type="checkbox" name="checked" value="1" checked />
            <input type="submit" name="btn" value="go" />
            <select name="ddlClass">
              <option value="">--</option>
              <option value="1" selected>1班</option>
            </select>
        """.trimIndent()

        val fields = Html.fields(page)
        assertEquals("abc&def", fields["__VIEWSTATE"])
        assertFalse(fields.containsKey("unchecked"))
        assertEquals("1", fields["checked"])
        assertFalse(fields.containsKey("btn"))
        assertEquals("1", fields["ddlClass"])

        val options = Html.options(page, "ddlClass")
        assertEquals(2, options.size)
        assertEquals("1班", options[1].label)
        assertEquals("1", options[1].value)
    }

    @Test
    fun `实体与标签清理`() {
        assertTrue(Html.stripTags("<b>语文</b>&nbsp;90").startsWith("语文"))
        assertEquals("a&b", Html.unescape("a&amp;b"))
        assertEquals("<b>", Html.unescape("&lt;b&gt;"))
    }
}
