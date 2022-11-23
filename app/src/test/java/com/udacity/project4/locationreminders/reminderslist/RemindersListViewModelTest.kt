package com.udacity.project4.locationreminders.reminderslist

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.utils.MainCoroutineRule
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.locationreminders.utils.createCompleteFakeReminderDataItem
import com.udacity.project4.locationreminders.utils.createIncompleteReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    private lateinit var viewModel: SaveReminderViewModel

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()


    @Before
    fun initViewModel() {
        viewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(),
            FakeDataSource())
    }

    @Test
    fun testWithError () = runBlockingTest  {
        val result = viewModel.validateEnteredData(createIncompleteReminderDataItem())
        MatcherAssert.assertThat(result, CoreMatchers.`is`(false))
    }


    @Test
    fun testLoading() = runBlockingTest {
        mainCoroutineRule.pauseDispatcher()
        viewModel.saveReminder(createCompleteFakeReminderDataItem())

        MatcherAssert.assertThat(viewModel.showLoading.value, CoreMatchers.`is`(true))

        mainCoroutineRule.resumeDispatcher()
        MatcherAssert.assertThat(viewModel.showLoading.value, CoreMatchers.`is`(false))
    }

}