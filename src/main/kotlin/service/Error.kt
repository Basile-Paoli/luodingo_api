package fr.ludodingo.service

import io.ktor.http.HttpStatusCode

data class Error(val httpStatus: HttpStatusCode, override val message: String) : Throwable()

sealed class Result<T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Failure<T>(val error: Error) : Result<T>()

    inline fun onFailure(action: (Error) -> Unit): Result<T> {
        if (this is Failure) {
            action(this.error)
        }
        return this
    }

    inline fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) {
            action(this.data)
        }
        return this
    }
    companion object {
        fun <T> ok(data: T): Result<T> = Success(data)

        fun <T> err(httpStatus: HttpStatusCode, message: String): Result<T> = Failure(Error(httpStatus, message))
    }
}

