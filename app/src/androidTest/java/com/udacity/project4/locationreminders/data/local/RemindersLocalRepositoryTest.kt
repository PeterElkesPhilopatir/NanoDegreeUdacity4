package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.util.generateReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase
    private lateinit var repository: RemindersLocalRepository

    @Before
    fun initDb() {

        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()

        repository = RemindersLocalRepository(database.reminderDao())
    }

    @After
    fun closeDB() = database.close()

    @Test
    fun testAddAndGet() = runBlocking {
        val testReminderDTO = generateReminderDTO()

        repository.saveReminder(testReminderDTO)

        val list = repository.getReminder(testReminderDTO.id)

        list as Result.Success
        assertThat(true, `is`(true))

        assertThat(list.data.id, `is`(testReminderDTO.id))
        assertThat(list.data.title, `is`(testReminderDTO.title))
        assertThat(list.data.description, `is`(testReminderDTO.description))
        assertThat(list.data.location, `is`(testReminderDTO.location))
        assertThat(list.data.latitude, `is`(testReminderDTO.latitude))
        assertThat(list.data.longitude, `is`(testReminderDTO.longitude))
    }

    @Test
    fun testCanNotFoundData() = runBlocking {
        val result = repository.getReminder("99999")
        val error = (result is Result.Error)
        assertThat(error, `is`(true))
    }
}