package com.kitapcepte.core.common

import android.content.Context
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class UiTextTest {

    private val context: Context = mockk()

    @Test
    fun `DynamicString returns raw string directly`() {
        val raw = "Merhaba Dünya"
        val uiText = UiText.DynamicString(raw)

        assertThat(uiText.asString(context)).isEqualTo(raw)
    }

    @Test
    fun `StringResource resolves from context without arguments`() {
        val resId = 1234
        val expected = "Kitap Cepte"
        every { context.getString(resId) } returns expected

        val uiText = UiText.StringResource(resId)
        assertThat(uiText.asString(context)).isEqualTo(expected)
    }

    @Test
    fun `StringResource resolves from context with arguments`() {
        val resId = 5678
        val arg = "Yüzüklerin Efendisi"
        val expected = "$arg kapak görseli"
        every { context.getString(resId, arg) } returns expected

        val uiText = UiText.StringResource(resId, arg)
        assertThat(uiText.asString(context)).isEqualTo(expected)
    }
}
