package com.kitapcepte.data.remote.dto

import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.json.Json
import org.junit.Test

class WorkDtoTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `parse string description returns correct text`() {
        val jsonString = """{"title": "The Hobbit", "description": "A great adventure story"}"""
        val work = json.decodeFromString<WorkDto>(jsonString)

        assertThat(work.title).isEqualTo("The Hobbit")
        assertThat(work.descriptionText).isEqualTo("A great adventure story")
    }

    @Test
    fun `parse object value description returns correct text`() {
        val jsonString = """{"title": "The Hobbit", "description": {"type": "/type/text", "value": "Detailed adventure of Bilbo"}}"""
        val work = json.decodeFromString<WorkDto>(jsonString)

        assertThat(work.title).isEqualTo("The Hobbit")
        assertThat(work.descriptionText).isEqualTo("Detailed adventure of Bilbo")
    }

    @Test
    fun `parse null or missing description returns null`() {
        val jsonString = """{"title": "No Description Book"}"""
        val work = json.decodeFromString<WorkDto>(jsonString)

        assertThat(work.descriptionText).isNull()
    }
}
