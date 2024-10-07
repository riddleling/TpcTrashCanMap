package site.riddleling.tpctrashcanmap

import org.koin.dsl.module

val appModules = module {
    single { MainViewModel() }
}