package com.milikovv.linguacontext.domain.local

interface LocalDataSource<T> {
    suspend fun set(data: T)
    suspend fun get(): T?
}
