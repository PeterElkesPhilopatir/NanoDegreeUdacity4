package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

    private var list = mutableListOf<ReminderDTO>()

    override suspend fun getReminders(): Result<List<ReminderDTO>> =
        try {
            Result.Success(list)
        } catch (e: Exception) {
            Result.Error(e.message.toString())
        }


    override suspend fun saveReminder(reminderDTO: ReminderDTO) {
        list.add(reminderDTO)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> =
        try {
            val reminder = list.find { it.id == id }
            if (reminder != null) Result.Success(reminder)
            else throw Exception("Not found $id")
        } catch (e: Exception) {
            Result.Error(e.localizedMessage)
        }


    override suspend fun deleteAllReminders() {
        list.clear()
    }


}