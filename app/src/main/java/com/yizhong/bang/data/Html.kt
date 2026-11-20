package com.yizhong.bang.data

/**
 * ASP.NET WebForms 的页面小工具：去标签、解实体、抓表单、抓下拉框。
 * 只依赖标准库，方便在 JVM 单元测试里直接跑。
 */
internal object Html {

    private val I = RegexOption.IGNORE_CASE

    private val TAG = Regex("<[^>]*>")
    private val SPACES = Regex("\\s+")
    private val NUM_ENTITY = Regex("&#(\\d+);")
    private val HEX_ENTITY = Regex("&#x([0-9a-fA-F]+);")

    private val INPUT = Regex("<input\\b([^>]*)>", I)
    private val SELECT = Regex("<select\\b([^>]*)>([\\s\\S]*?)</select>", I)
    private val OPTION = Regex("<option\\b([^>]*)>([\\s\\S]*?)</option>", I)

    private val NAME = Regex("\\bname\\s*=\\s*\"([^\"]*)\"", I)
    private val TYPE = Regex("\\btype\\s*=\\s*\"([^\"]*)\"", I)
    private val VALUE = Regex("\\bvalue\\s*=\\s*\"([^\"]*)\"", I)
    private val CHECKED = Regex("\\bchecked\\b", I)
    private val SELECTED = Regex("\\bselected\\b", I)

    fun attr(attrs: String, name: String): String? =
        Regex("\\b$name\\s*=\\s*\"([^\"]*)\"", I).find(attrs)?.groupValues?.get(1)

    fun unescape(value: String): String {
        var out = NUM_ENTITY.replace(value) { m ->
            m.groupValues[1].toIntOrNull()?.toChar()?.toString() ?: m.value
        }
        out = HEX_ENTITY.replace(out) { m ->
            m.groupValues[1].toIntOrNull(16)?.toChar()?.toString() ?: m.value
        }
        return out
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
    }

    fun stripTags(value: String): String =
        SPACES.replace(unescape(TAG.replace(value, " ")), " ").trim()

    fun cells(row: String): List<String> =
        Regex("<t[hd]\\b[^>]*>([\\s\\S]*?)</t[hd]>", I)
            .findAll(row)
            .map { stripTags(it.groupValues[1]) }
            .toList()

    /** 把页面里的隐藏域和下拉框抓成一份可回传的表单（和 Python 版逐行对应）。 */
    fun fields(html: String): LinkedHashMap<String, String> {
        val out = LinkedHashMap<String, String>()

        INPUT.findAll(html).forEach { m ->
            val attrs = m.groupValues[1]
            val name = NAME.find(attrs)?.groupValues?.get(1) ?: return@forEach
            val type = (TYPE.find(attrs)?.groupValues?.get(1) ?: "text").lowercase()
            if ((type == "checkbox" || type == "radio") && !CHECKED.containsMatchIn(attrs)) {
                return@forEach
            }
            if (type == "button" || type == "reset" || type == "submit" || type == "image") {
                return@forEach
            }
            out[name] = unescape(VALUE.find(attrs)?.groupValues?.get(1) ?: "")
        }

        SELECT.findAll(html).forEach { m ->
            val attrs = m.groupValues[1]
            val body = m.groupValues[2]
            val name = NAME.find(attrs)?.groupValues?.get(1) ?: return@forEach
            var first: String? = null
            var selected: String? = null
            OPTION.findAll(body).forEach { o ->
                val oa = o.groupValues[1]
                val v = unescape(VALUE.find(oa)?.groupValues?.get(1) ?: "")
                if (first == null) first = v
                if (SELECTED.containsMatchIn(oa)) selected = v
            }
            out[name] = selected ?: first ?: ""
        }
        return out
    }

    fun options(html: String, name: String): List<Opt> {
        val select = Regex(
            "<select\\b[^>]*name\\s*=\\s*\"${Regex.escape(name)}\"[^>]*>([\\s\\S]*?)</select>",
            I,
        ).find(html)?.groupValues?.get(1) ?: return emptyList()

        return OPTION.findAll(select).map { o ->
            val attrs = o.groupValues[1]
            Opt(
                value = unescape(VALUE.find(attrs)?.groupValues?.get(1) ?: ""),
                label = stripTags(o.groupValues[2]),
            )
        }.toList()
    }
}
