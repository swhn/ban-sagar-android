package com.bansagar.app.domain.repository

import com.bansagar.app.data.model.Slang
import com.bansagar.app.data.model.SlangSubmission

interface ContributeRepository {
    suspend fun submitSlang(submission: SlangSubmission): Slang
    suspend fun getUserHistory(userId: String): List<Slang>
    suspend fun checkDuplicates(word: String): List<Slang>
    suspend fun getRequireApproval(): Boolean
}
