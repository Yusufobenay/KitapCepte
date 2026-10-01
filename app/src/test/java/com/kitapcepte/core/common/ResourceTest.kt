package com.kitapcepte.core.common

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ResourceTest {

    @Test
    fun `Resource Success returns correct data and state`() {
        val data = "Kitap verisi"
        val resource: Resource<String> = Resource.Success(data)

        assertThat(resource.isSuccess()).isTrue()
        assertThat(resource.isError()).isFalse()
        assertThat(resource.isLoading()).isFalse()
        assertThat(resource.getOrNull()).isEqualTo(data)
    }

    @Test
    fun `Resource Error returns correct message and state`() {
        val errorMessage = UiText.DynamicString("Hata oluştu")
        val resource: Resource<String> = Resource.Error(errorMessage)

        assertThat(resource.isSuccess()).isFalse()
        assertThat(resource.isError()).isTrue()
        assertThat(resource.isLoading()).isFalse()
        assertThat(resource.getOrNull()).isNull()
        assertThat((resource as Resource.Error).message).isEqualTo(errorMessage)
    }

    @Test
    fun `Resource Loading returns correct state`() {
        val resource: Resource<String> = Resource.Loading

        assertThat(resource.isSuccess()).isFalse()
        assertThat(resource.isError()).isFalse()
        assertThat(resource.isLoading()).isTrue()
        assertThat(resource.getOrNull()).isNull()
    }
}
