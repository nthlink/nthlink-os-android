package com.nthlink.android.client.di

import com.nthlink.android.client.storage.datastore.ApkDownloadDataStore
import com.nthlink.android.client.storage.datastore.CommonDataStore
import com.nthlink.android.client.ui.update.ApkUpdateViewModel
import com.nthlink.android.client.updates.ApkDownloadManager
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    // DataStore
    singleOf(::CommonDataStore)
    singleOf(::ApkDownloadDataStore)

    factoryOf(::ApkDownloadManager)

    viewModelOf(::ApkUpdateViewModel)
}