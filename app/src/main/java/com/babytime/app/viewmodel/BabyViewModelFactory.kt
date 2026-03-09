package com.babytime.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.babytime.app.data.repository.BabyRepository

class BabyViewModelFactory(private val repository: BabyRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BabyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BabyViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
