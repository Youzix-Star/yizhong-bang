package com.yizhong.bang.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** 下拉框的一项。 */
@Serializable
data class Opt(
    val value: String = "",
    val label: String = "",
)

@Serializable
data class Grade(
    val value: String = "",
    val label: String = "",
    val exams: List<Opt> = emptyList(),
    val classes: List<Opt> = emptyList(),
)

@Serializable
data class Meta(
    val school: String? = null,
    val user: String? = null,
    val grades: List<Grade> = emptyList(),
    val cached: Boolean = false,
)

@Serializable
data class Rank(
    @SerialName("class") val classRank: Int? = null,
    val grade: Int? = null,
)

@Serializable
data class Avg(
    val subjects: List<Double?> = emptyList(),
    val total: Double? = null,
)

@Serializable
data class SheetMeta(
    val term: String? = null,
    val className: String? = null,
    val title: String? = null,
)

@Serializable
data class Student(
    val no: String = "",
    val name: String = "",
    val gradeCount: Double? = null,
    val classSize: Double? = null,
    val scores: List<Double?> = emptyList(),
    val ranks: List<Rank?> = emptyList(),
    val flags: List<Boolean> = emptyList(),
    val total: Double? = null,
    val totalRank: Rank? = null,
    val className: String? = null,
    val title: String? = null,
)

@Serializable
data class Sheet(
    val meta: SheetMeta? = null,
    val subjects: List<String> = emptyList(),
    val full: List<Double?> = emptyList(),
    val classAvg: Avg? = null,
    val gradeAvg: Avg? = null,
    val students: List<Student> = emptyList(),
    val cached: Boolean = false,
    val at: Long = 0L,
)

@Serializable
data class DemoPayload(
    val meta: Meta,
    val sheet: Sheet,
)
