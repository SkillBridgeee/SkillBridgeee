package com.android.sample.model

import android.content.Context

abstract class RepositoryProvider<T> {
  @Volatile protected var _repository: T? = null

  val repository: T
    get() =
        _repository
            ?: error(
                "${this::class.simpleName} not initialized. Call init(...) first or setForTests(...) in tests.")

  abstract fun init(context: Context, useEmulator: Boolean = false)

  fun setForTests(repository: T) {
    _repository = repository
  }
}
