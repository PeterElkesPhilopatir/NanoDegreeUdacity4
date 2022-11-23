package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.util.generateReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var mDatabase: RemindersDatabase

    @Before
    fun initDb() {
        mDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = mDatabase.close()

    @Test
    fun testAddAndGetData() = runBlockingTest {
        val testReminder = generateReminderDTO()

        mDatabase.reminderDao().saveReminder(testReminder)

        val list = mDatabase.reminderDao().getReminders()

        assertThat(list.size, `is`(1))

        assertThat(list[0].id, `is`(testReminder.id))
        assertThat(list[0].title, `is`(testReminder.title))
        assertThat(list[0].description, `is`(testReminder.description))
        assertThat(list[0].location, `is`(testReminder.location))
        assertThat(list[0].latitude, `is`(testReminder.latitude))
        assertThat(list[0].longitude, `is`(testReminder.longitude))
    }
}