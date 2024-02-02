package com.example.mad_project.firestore

data class FireStoreData(
    var email: String,
    var nickname: String,
    var monthlyGoal: Int = 0,
    var dailyGoal: Int=0,
    var amountOfSteps: MutableList<Int> = mutableListOf(),
    var stepsAddedManually: MutableList<Int> = mutableListOf()
)  {
    // Add a no-argument constructor
    constructor() : this("", "", 0 )
}

