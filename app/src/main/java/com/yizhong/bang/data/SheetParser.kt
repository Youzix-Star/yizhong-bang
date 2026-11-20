package com.yizhong.bang.data

/** 把「导出 Excel」返回的 HTML 成绩单解析成 [Sheet]。逻辑与 Python 版一致。 */
object SheetParser {

    private const val I = RegexOption.IGNORE_CASE

    private val TABLE = Regex("<table\\b[^>]*>([\\s\\S]*?)</table>", I)
    private val TR = Regex("<tr\\b", I)
    private val TERM = Regex("(\\d{4}-\\d{4}学年第?[上下]学期)")
    private val NUMBER = Regex("-?\\d+(?:\\.\\d+)?")
    private val RANK = Regex("^\\s*(\\d+)\\s*/\\s*(\\d+)\\s*$")
    private val CLASS_NAME = Regex("\\(([^)]+)\\)\\s*$")

    private data class Pick(val row: List<String>, val at: Int)

    fun parse(html: String): Sheet {
        var term: String? = null
        TERM.find(Html.stripTags(html))?.let { term = it.groupValues[1] }

        var subjects: List<String> = emptyList()
        var full: List<Double?> = emptyList()
        var classAvg: Avg? = null
        var gradeAvg: Avg? = null
        val students = mutableListOf<Student>()

        TABLE.findAll(html).forEach { tableMatch ->
            val table = tableMatch.groupValues[1]
            if (!table.contains("姓名") || !table.contains("得分")) return@forEach

            val chunks = TR.split(table)
                .drop(1)
                .map { Html.cells(it) }
                .filter { it.isNotEmpty() }
            if (chunks.isEmpty()) return@forEach

            val head = chunks.firstOrNull { "项目" in it && "总分" in it } ?: return@forEach
            val ip = head.indexOf("项目")
            val totalIndex = head.indexOf("总分")
            if (ip < 0 || totalIndex <= ip) return@forEach
            if (subjects.isEmpty()) subjects = head.subList(ip + 1, totalIndex)
            val n = subjects.size

            val rFull = pick(chunks, "满分")
            val rGrade = pick(chunks, "级均分")
            val rClass = pick(chunks, "班均分")
            val rScore = pick(chunks, "得分") ?: return@forEach
            val rRank = pick(chunks, "位次") ?: return@forEach
            val rFlag = pick(chunks, "偏科")

            val vFull = tail(rFull)
            val vGrade = tail(rGrade)
            val vClass = tail(rClass)
            val vScore = tail(rScore)
            val vRank = tail(rRank)
            val vFlag = tail(rFlag)

            if (classAvg == null && rClass != null) {
                classAvg = Avg(vClass.take(n).map(::num), num(vClass.getOrNull(n)))
            }
            if (gradeAvg == null && rGrade != null) {
                gradeAvg = Avg(vGrade.take(n).map(::num), num(vGrade.getOrNull(n)))
            }
            if (full.isEmpty() && rFull != null) {
                full = vFull.take(n).map(::num)
            }

            val name = rFull?.row?.firstOrNull() ?: ""
            if (name.isEmpty()) return@forEach

            val title = chunks[0].firstOrNull { "成绩单" in it } ?: ""
            val className = CLASS_NAME.find(title)?.groupValues?.get(1)

            students += Student(
                no = rRank.row.firstOrNull() ?: "",
                name = name,
                gradeCount = num(rFull?.row?.getOrNull(1)),
                classSize = num(rRank.row.getOrNull(1)),
                scores = vScore.take(n).map(::num),
                ranks = (0 until n).map { rank(vRank.getOrNull(it)) },
                flags = (0 until n).map { vFlag.getOrNull(it)?.contains("●") == true },
                total = num(vScore.getOrNull(n)),
                totalRank = rank(vRank.getOrNull(n)),
                className = className,
                title = title,
            )
        }

        return Sheet(
            meta = SheetMeta(
                term = term,
                className = students.firstOrNull()?.className,
                title = students.firstOrNull()?.title,
            ),
            subjects = subjects,
            full = full,
            classAvg = classAvg,
            gradeAvg = gradeAvg,
            students = students,
        )
    }

    private fun pick(chunks: List<List<String>>, label: String): Pick? {
        for (row in chunks) {
            val at = row.indexOf(label)
            if (at >= 0) return Pick(row, at)
        }
        return null
    }

    private fun tail(pick: Pick?): List<String> {
        if (pick == null) return emptyList()
        return pick.row.drop(pick.at + 1)
    }

    private fun num(value: String?): Double? =
        value?.let { NUMBER.find(it)?.value?.toDoubleOrNull() }

    private fun rank(value: String?): Rank? {
        val m = RANK.find(value?.trim() ?: "") ?: return null
        return Rank(
            classRank = m.groupValues[1].toIntOrNull(),
            grade = m.groupValues[2].toIntOrNull(),
        )
    }
}
