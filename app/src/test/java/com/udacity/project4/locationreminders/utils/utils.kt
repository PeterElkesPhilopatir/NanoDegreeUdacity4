package com.udacity.project4.locationreminders.utils

import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

fun createCompleteFakeReminderDataItem(): ReminderDataItem {
    return ReminderDataItem(
        "reminder title", "reminder description location", "reminder location", 0.0, 0.0
    )
}
// creating empty title and empty location
fun createIncompleteReminderDataItem(): ReminderDataItem {
    return ReminderDataItem(
        "", "reminder description location", "", 0.0, 0.0
    )
}