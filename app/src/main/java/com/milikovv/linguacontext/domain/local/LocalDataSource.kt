package com.milikovv.linguacontext.domain.local

/**
 * On-device data source that works without a network and allows writing.
 */
interface LocalDataSource<T> {
    suspend fun set(data: T)
    suspend fun get(): T?
}
