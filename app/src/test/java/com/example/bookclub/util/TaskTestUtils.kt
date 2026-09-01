package com.example.bookclub.util

import com.google.android.gms.tasks.Task
import io.mockk.every
import io.mockk.mockk

fun <T> successTask(result: T): Task<T> {
    val task: Task<T> = mockk(relaxed = true)
    every { task.isComplete } returns true
    every { task.isCanceled } returns false
    every { task.exception } returns null
    every { task.result } returns result
    return task
}

fun <T> failedTask(error: Exception): Task<T> {
    val task: Task<T> = mockk(relaxed = true)
    every { task.isComplete } returns true
    every { task.isCanceled } returns false
    every { task.exception } returns error
    return task
}
