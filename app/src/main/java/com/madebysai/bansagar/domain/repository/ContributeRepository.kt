package com.madebysai.bansagar.domain.repository

import com.madebysai.bansagar.data.model.Slang
import com.madebysai.bansagar.data.model.SlangSubmission

interface ContributeRepository {
    suspend fun submitSlang(submission: SlangSubmission): Slang
    suspend fun getUserHistory(userId: String): List<Slang>
    suspend fun checkDuplicates(word: String): List<Slang>
    suspend fun getRequireApproval(): Boolean
}
