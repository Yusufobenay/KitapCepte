package com.kitapcepte.core.designsystem

import com.google.common.truth.Truth.assertThat
import com.kitapcepte.domain.model.BookCategory
import org.junit.Test

class DesignSystemTokensTest {

    @Test
    fun `BookCategory queries are formatted correctly`() {
        assertThat(BookCategory.FANTASY.query).isEqualTo("subject:fantasy")
        assertThat(BookCategory.ROMANCE.query).isEqualTo("subject:romance")
        assertThat(BookCategory.SCI_FI.query).isEqualTo("subject:science_fiction")
        assertThat(BookCategory.MYSTERY.query).isEqualTo("subject:mystery")
        assertThat(BookCategory.HISTORY.query).isEqualTo("subject:history")
        assertThat(BookCategory.HORROR.query).isEqualTo("subject:horror")
        assertThat(BookCategory.BIOGRAPHY.query).isEqualTo("subject:biography")
    }

    @Test
    fun `BookCategory default is Fantasy`() {
        assertThat(BookCategory.DEFAULT).isEqualTo(BookCategory.FANTASY)
    }
}
