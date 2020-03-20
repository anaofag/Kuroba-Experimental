package com.github.adamantcheese.model.di

import android.app.Application
import com.github.adamantcheese.model.di.annotation.LoggerTagPrefix
import com.github.adamantcheese.model.di.annotation.OkHttpDns
import com.github.adamantcheese.model.di.annotation.OkHttpProtocols
import com.github.adamantcheese.model.di.annotation.VerboseLogs
import com.github.adamantcheese.model.repository.InlinedFileInfoRepository
import com.github.adamantcheese.model.repository.MediaServiceLinkExtraContentRepository
import com.github.adamantcheese.model.repository.SeenPostRepository
import dagger.BindsInstance
import dagger.Component
import okhttp3.Dns
import javax.inject.Singleton

@Singleton
@Component(
        modules = [
            NetworkModule::class,
            DatabaseModule::class
        ]
)
interface DatabaseComponent {
    fun inject(application: Application)

    fun getMediaServiceLinkExtraContentRepository(): MediaServiceLinkExtraContentRepository
    fun getSeenPostRepository(): SeenPostRepository
    fun getInlinedFileInfoRepository(): InlinedFileInfoRepository

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder
        @BindsInstance
        fun loggerTagPrefix(@LoggerTagPrefix loggerTagPrefix: String): Builder
        @BindsInstance
        fun verboseLogs(@VerboseLogs verboseLogs: Boolean): Builder
        @BindsInstance
        fun okHttpDns(@OkHttpDns dns: Dns): Builder
        @BindsInstance
        fun okHttpProtocols(@OkHttpProtocols okHttpProtocols: NetworkModule.OkHttpProtocolList): Builder

        fun build(): DatabaseComponent
    }

}