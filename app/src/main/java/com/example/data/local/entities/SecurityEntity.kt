package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "security_settings")
data class SecurityEntity(
    @PrimaryKey val id: Int = 1,
    val pinHash: String = "", // e.g. SHA-256 or simple salt
    val decoyPinHash: String = "",
    val recoverySeedPhrase: String = "", // 24-word cryptographic seed phrase
    val isSetupCompleted: Boolean = false,
    val lockTimeoutMinutes: Int = 5
)
