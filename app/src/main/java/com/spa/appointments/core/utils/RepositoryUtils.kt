package com.spa.appointments.core.utils

import kotlin.coroutines.cancellation.CancellationException

inline fun <T> runCatchingSuspend(block: () -> T): Result<T> {
    return try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        Result.failure(e)
    }
}
