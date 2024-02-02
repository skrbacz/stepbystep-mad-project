package com.example.mad_project.stepcounter_services

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class StepCountViewModel : ViewModel() {
    private val _currentStepsWorker = MutableLiveData<Int>()
    val currentStepsWorker: LiveData<Int> get() = _currentStepsWorker

    fun updateCurrentSteps(newCurrentSteps: Int) {
        _currentStepsWorker.value = newCurrentSteps
    }
}