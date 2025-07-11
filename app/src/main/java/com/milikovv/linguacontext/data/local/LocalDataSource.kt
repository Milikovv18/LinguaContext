package com.milikovv.linguacontext.data.local

import com.milikovv.linguacontext.domain.local.LocalDataSource
import jakarta.inject.Inject


/**
 * [LocalDataSource] implemented as in-memory data source.
 * @param T type of data to save
 */
class MemoryLocalDataSource<T> @Inject constructor() : LocalDataSource<T> {
    private var data: T? = null

    override suspend fun set(data: T) {
        this.data = data
    }

    override suspend fun get(): T? {
        return this.data
    }
}
