package com.kitapcepte.core.common

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import org.junit.Test

class DispatcherProviderTest {

    @Test
    fun `DefaultDispatcherProvider returns standard dispatchers`() {
        val provider = DefaultDispatcherProvider()

        assertThat(provider.main).isEqualTo(Dispatchers.Main)
        assertThat(provider.io).isEqualTo(Dispatchers.IO)
        assertThat(provider.default).isEqualTo(Dispatchers.Default)
        assertThat(provider.unconfined).isEqualTo(Dispatchers.Unconfined)
    }
}
