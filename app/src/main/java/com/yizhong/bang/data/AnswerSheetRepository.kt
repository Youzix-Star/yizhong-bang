package com.yizhong.bang.data

import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 答题卡会话。
 *
 * 学校把「成绩」和「试卷分析」拆成了两个模块，token 也是一次性、分模块的，
 * 所以这里单独持有一个 [SchoolClient]，由用户在登录页额外粘一条试卷分析链接。
 *
 * testImageCode 的规律（高一 2026-2027 上学期期中，已验证）：
 *   4360012151 260104 02 01 0003 01
 *   └─学校───┘ └日期┘ 考试 科目 卷型 页
 * 换考试时卷型/日期会变，所以把模板做成可替换的；当前默认值就是这场期中考试。
 */
class AnswerSheetRepository {

    private var client: SchoolClient? = null
    private var userId: String? = null

    private var codeBase: String = DEFAULT_BASE
    private var paper: String = DEFAULT_PAPER

    val bound: Boolean
        get() = client != null && userId != null

    suspend fun bind(rawUrl: String): String = withContext(Dispatchers.IO) {
        val fresh = SchoolClient()
        val uid = fresh.bindModule(rawUrl)
            ?: throw IllegalArgumentException("链接里没有 UserID，不是试卷系统的链接")
        val page = fresh.getText("/TPTQ/teacher/SeletedAnswerSheet.aspx")
        if (!page.contains("学生答卷查询")) {
            throw IllegalStateException("这不是试卷分析链接，或者链接已经过期")
        }
        client = fresh
        userId = uid
        uid
    }

    /**
     * 拉取某个学生某科的答卷图片（JPEG 原始字节）。
     * 服务端把 Content-Type 错写成 text/html，但字节本身是合法 JPEG，因此只认文件头。
     */
    suspend fun load(
        gradeCode: String,
        studentNo: String,
        studentName: String,
        subject: String,
        subjectIndex: Int,
        className: String,
    ): List<ByteArray> = withContext(Dispatchers.IO) {
        val c = client ?: throw IllegalStateException("还没有绑定答题卡链接")
        val uid = userId ?: throw IllegalStateException("还没有绑定答题卡链接")

        val studentCode = studentNumber(c, uid, gradeCode, studentNo)
            ?: studentNumber(c, uid, gradeCode, studentName)
            ?: throw IllegalStateException("找不到这个学生的答卷编号")

        val pages = mutableListOf<ByteArray>()
        for (page in 1..MAX_PAGES) {
            val code = codeBase + "%02d".format(subjectIndex + 1) + paper + "%02d".format(page)
            val url = "/TPTQ/ShowImage.aspx?testImageCode=$code" +
                "&studentCode=$studentCode&imageCount=$MAX_PAGES" +
                "&CouseText=${enc(subject)}&ClassName=${enc(className)}"
            val bytes = c.getBytes(url)
            if (isJpeg(bytes)) {
                pages += bytes
            } else {
                break
            }
        }
        if (pages.isEmpty()) {
            throw IllegalStateException("这个科目没有找到答题卡（当前内置的是期中考试模板）")
        }
        pages
    }

    /** 用一张现成的 BrowseTestImage 地址校准模板（换考试时用）。 */
    fun setTemplateFromSample(url: String) {
        val code = Regex("[?&]testImageCode=([0-9A-Za-z]+)", RegexOption.IGNORE_CASE)
            .find(url)?.groupValues?.get(1) ?: return
        if (code.length >= 26) {
            codeBase = code.substring(0, 18)
            paper = code.substring(20, 24)
        }
    }

    fun reset() {
        client = null
        userId = null
    }

    private fun studentNumber(
        c: SchoolClient,
        uid: String,
        gradeCode: String,
        query: String,
    ): String? {
        if (query.isBlank()) return null
        val url = "/TPTQ/Handler/StudentPinyinHandler.ashx?userID=$uid" +
            "&gradeCode=${enc(gradeCode)}&rom=${System.nanoTime()}&q=${enc(query)}"
        val text = c.getText(url)
        return Regex("\"SchoolNumber\"\\s*:\\s*\"([0-9A-Fa-f]+)\"")
            .find(text)?.groupValues?.get(1)
    }

    private fun isJpeg(bytes: ByteArray): Boolean =
        bytes.size > 4 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte()

    private fun enc(value: String): String = URLEncoder.encode(value, "UTF-8")

    private companion object {
        const val DEFAULT_BASE = "436001215126010402"
        const val DEFAULT_PAPER = "0003"
        const val MAX_PAGES = 8
    }
}
