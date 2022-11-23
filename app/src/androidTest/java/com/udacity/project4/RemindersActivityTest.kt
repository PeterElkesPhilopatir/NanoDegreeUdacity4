package com.udacity.project4

import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.generateReminderDTO
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    private lateinit var dataSource: ReminderDataSource
    private lateinit var context: Application

    @Before
    fun initialize() {
        stopKoin()
        context = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    context,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    context,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) }
            single { LocalDB.createRemindersDao(context) }
        }
        startKoin {
            modules(listOf(myModule))
        }
        dataSource = get()

        runBlocking {
            dataSource.deleteAllReminders()
        }
    }

    @Test
    fun testRemindersActivity() {
        val testReminder = generateReminderDTO()

        runBlocking {
            dataSource.saveReminder(testReminder)
        }

        val scenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(scenario)

        onView(withText(testReminder.title)).check(matches(isDisplayed()))
        onView(withText(testReminder.description)).check(matches(isDisplayed()))
        onView(withText(testReminder.location)).check(matches(isDisplayed()))
    }

    @Test
    fun testAddReminder() {
        val scenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(scenario)
        onView(withId(R.id.noDataTextView)).check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.selectLocation)).perform(click())
        onView(withId(R.id.btn_save)).perform(click())
        onView(withId(R.id.reminderTitle)).perform(typeText("reminder title"))
        onView(withId(R.id.reminderDescription)).perform(typeText("reminder description"))
        closeSoftKeyboard()
        onView(withId(R.id.saveReminder)).perform(click())
        onView(withId(R.id.noDataTextView)).check(matches(withEffectiveVisibility(Visibility.GONE)))
        onView(withText("Title")).check(matches(isDisplayed()))
        onView(withText("Description")).check(matches(isDisplayed()))
    }
}
