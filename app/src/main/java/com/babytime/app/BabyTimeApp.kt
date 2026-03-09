package com.babytime.app

import android.app.Application
import com.babytime.app.data.database.AppDatabase
import com.babytime.app.data.repository.BabyRepository

class BabyTimeApp : Application() {
    val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy { BabyRepository(database) }
}
