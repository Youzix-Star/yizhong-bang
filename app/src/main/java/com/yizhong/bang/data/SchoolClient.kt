package com.yizhong.bang.data

import java.io.IOException
import java.net.URI
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

/**
 * 学校那套 ASP.NET WebForms：cookie 会话 + __VIEWSTATE + 级联 postback。
 * 这里只做四件事：取页面、抓表单、模拟点按钮、导出成绩单。
 */
class SchoolClient(
    private val host: String = DEFAULT_HOST,
    private val port: Int = DEFAULT_PORT,
) {

    companion object {
        const val DEFAULT_HOST = "61.150.95.35"
        const val DEFAULT_PORT = 83
        const val SSQ_PAGE = "/SSQ/ScoreSearch/StudentScoreNotice.aspx"

        private const val UA =
            "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 " +
                "(KHTML, like Gecko) Chrome/131.0.0.0 Mobile Safari/537.36"
        private const val REFERER = "Referer"
        private const val USER_AGENT = "User-Agent"
    }

    private val base = "http://$host:$port"
    private val cookieJar = MemoryCookieJar()
    private val client: OkHttpClient = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .followRedirects(true)
        .followSslRedirects(true)
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /** 登录链接解析出来的人名 + 工号，失败时返回 null。 */
    var user: String? = null
        private set

    /** 成绩页菜单里自带的「试卷分析」入口（含 token），可能为 null。 */
    var answerSheetUrl: String? = null
        private set

    /**
     * 打开任意一个模块链接，自己找到「成绩查询」入口。
     * 返回解析到的人名（没有也不影响后续取数）。
     */
    fun openLink(raw: String): String? {
        val clean = raw.replace(Regex("\\s+"), "").substringBefore("#")
        if (clean.isEmpty()) throw IllegalArgumentException("先把链接粘进来")
        val uri = runCatching { URI(clean) }.getOrElse { throw IllegalArgumentException("链接格式不太对") }
        val path = pathAndQuery(uri)
        if (path.isBlank() || path == "?") throw IllegalArgumentException("链接里没有路径")

        answerSheetUrl = null
        val landing = get(path)
        answerSheetUrl = extractAnswerSheetUrl(landing)
        val landingUser = parseUser(landing)
        if (clean.contains("SysCode=0401") || (uri.rawPath ?: "").contains("/SSQ/")) {
            user = landingUser
            return landingUser
        }

        val link = Regex("href=\"([^\"]*/SSQ/Default\\.aspx\\?[^\"]*)\"", RegexOption.IGNORE_CASE)
            .find(landing)?.groupValues?.get(1)
            ?: throw IllegalStateException("这个链接不是成绩系统，页面上也找不到「成绩查询」入口")
        val fixed = link.replace("&amp;", "&").replace(Regex("\\s+"), "")
        val entry = runCatching { URI(fixed) }.getOrElse { throw IllegalStateException("成绩入口链接解析失败") }
        val entryPage = get(pathAndQuery(entry))
        if (answerSheetUrl == null) answerSheetUrl = extractAnswerSheetUrl(entryPage)
        user = parseUser(entryPage) ?: landingUser
        return user
    }

    fun meta(): Meta {
        val html = get(SSQ_PAGE)
        val schools = Html.options(html, "ddlSchool")
        val grades = Html.options(html, "GradeList").map { grade ->
            val page = postback(SSQ_PAGE, html, target = "GradeList", overrides = mapOf("GradeList" to grade.value))
            Grade(
                value = grade.value,
                label = grade.label,
                exams = Html.options(page, "ddlTestType").filter { it.value.isNotEmpty() },
                classes = Html.options(page, "ddlClass").filter { it.value.isNotEmpty() },
            )
        }
        return Meta(school = schools.firstOrNull()?.label, user = user, grades = grades)
    }

    fun sheet(grade: String, exam: String, clazz: String): Sheet {
        var html = get(SSQ_PAGE)
        html = postback(SSQ_PAGE, html, target = "GradeList", overrides = mapOf("GradeList" to grade))
        html = postback(
            SSQ_PAGE,
            html,
            target = "ddlClass",
            overrides = mapOf("ddlTestType" to exam, "ddlClass" to clazz),
        )
        html = postback(SSQ_PAGE, html, overrides = mapOf("btnExport" to "导出Excel"))
        if (!html.contains("成绩单")) {
            throw IllegalStateException("没取到成绩单，可能这个班没有这次考试的数据")
        }
        val sheet = SheetParser.parse(html)
        if (sheet.students.isEmpty()) throw IllegalStateException("成绩单里没解析出学生")
        return sheet
    }

    fun reset() = cookieJar.clear()

    /** 绑定另一个模块（如试卷分析）的入口链接，只负责拿 cookie 和 UserID。 */
    fun bindModule(rawUrl: String): String? {
        val clean = rawUrl.replace(Regex("\\s+"), "").substringBefore("#")
        if (clean.isEmpty()) throw IllegalArgumentException("先把链接粘进来")
        val uri = runCatching { URI(clean) }.getOrElse { throw IllegalArgumentException("链接格式不太对") }
        get(pathAndQuery(uri))
        return Regex("[?&]UserID=(\\d+)", RegexOption.IGNORE_CASE)
            .find(clean)?.groupValues?.get(1)
    }

    fun getText(path: String): String = get(path)

    private fun extractAnswerSheetUrl(html: String): String? =
        Regex("href=\"([^\"]*/TPTQ/Default\\.aspx\\?[^\"]*token=[^\"]*)\"", RegexOption.IGNORE_CASE)
            .find(html)?.groupValues?.get(1)
            ?.replace("&amp;", "&")
            ?.replace(Regex("\\s+"), "")

    fun getBytes(path: String): ByteArray {
        val request = Request.Builder().url(base + path).header(USER_AGENT, UA).get().build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("上游返回 ${response.code}")
            return response.body?.bytes() ?: ByteArray(0)
        }
    }

    // ── 网络 ─────────────────────────────────────────────
    private fun get(path: String): String =
        execute(Request.Builder().url(base + path).header(USER_AGENT, UA).get().build())

    private fun post(path: String, form: Map<String, String>): String {
        val body = FormBody.Builder().apply {
            form.forEach { (key, value) -> add(key, value) }
        }.build()
        return execute(
            Request.Builder()
                .url(base + path)
                .header(USER_AGENT, UA)
                .header(REFERER, base + path)
                .post(body)
                .build(),
        )
    }

    private fun execute(request: Request): String {
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("上游返回 ${response.code}")
            return decode(response)
        }
    }

    private fun decode(response: Response): String {
        val bytes = response.body?.bytes() ?: ByteArray(0)
        val contentType = response.header("Content-Type").orEmpty()
        val charsetName = Regex("charset=([\\w-]+)", RegexOption.IGNORE_CASE)
            .find(contentType)?.groupValues?.get(1)?.lowercase()
        val charset: Charset = when (charsetName) {
            null -> Charsets.UTF_8
            "gb2312", "gbk", "gb18030" -> Charset.forName("GBK")
            else -> runCatching { Charset.forName(charsetName) }.getOrDefault(Charsets.UTF_8)
        }
        return String(bytes, charset)
    }

    private fun postback(
        path: String,
        html: String,
        target: String = "",
        overrides: Map<String, String> = emptyMap(),
    ): String {
        val form = Html.fields(html)
        form["__EVENTTARGET"] = target
        form["__EVENTARGUMENT"] = ""
        form["__LASTFOCUS"] = ""
        overrides.forEach { (key, value) -> form[key] = value }
        return post(path, form)
    }

    private fun pathAndQuery(uri: URI): String = buildString {
        append(uri.rawPath ?: "")
        uri.rawQuery?.let { append('?').append(it) }
    }

    private fun parseUser(html: String): String? =
        Regex("([\\u4e00-\\u9fa5]{2,4})\\s*\\((\\d+)\\)")
            .find(Html.stripTags(html))
            ?.let { "${it.groupValues[1]} · ${it.groupValues[2]}" }

    private class MemoryCookieJar : CookieJar {
        private val cookies = mutableListOf<Cookie>()

        @Synchronized
        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            this.cookies.removeAll { old -> cookies.any { it.name == old.name } }
            this.cookies.addAll(cookies)
        }

        @Synchronized
        override fun loadForRequest(url: HttpUrl): List<Cookie> =
            cookies.filter { it.matches(url) }

        @Synchronized
        fun clear() = cookies.clear()
    }
}
