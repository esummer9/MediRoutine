package com.ediapp.MediRoutine.model

data class Action(
    val id: Long,
    val actType: String?,
    val actKey: String?,
    val actValue: Int,
    val actRegisteredAt: String?,
    val actCreatedAt: String?,
    val actDeletedAt: String?,
    val actMessage: String?,
    val actStatus: String?,
    val actRef: String?
)
