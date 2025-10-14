package com.android.sample.screen

import com.android.sample.model.skill.MainSubject
import com.android.sample.ui.screens.newSkill.NewSkillViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class NewSkillViewModelTest {

  private lateinit var viewModel: NewSkillViewModel

  @Before
  fun setup() {
    viewModel = NewSkillViewModel()
  }

  @Test
  fun `setTitle blank and valid`() {
    viewModel.setTitle("")
    assertNotNull(viewModel.uiState.value.invalidTitleMsg)
    assertFalse(viewModel.uiState.value.isValid)

    viewModel.setTitle("My title")
    assertNull(viewModel.uiState.value.invalidTitleMsg)
  }

  @Test
  fun `setDesc blank and valid`() {
    viewModel.setDescription("")
    assertNotNull(viewModel.uiState.value.invalidDescMsg)
    assertFalse(viewModel.uiState.value.isValid)

    viewModel.setDescription("A description")
    assertNull(viewModel.uiState.value.invalidDescMsg)
  }

  @Test
  fun `setPrice blank non-number negative and valid`() {
    viewModel.setPrice("")
    assertEquals("Price cannot be empty", viewModel.uiState.value.invalidPriceMsg)
    assertFalse(viewModel.uiState.value.isValid)

    viewModel.setPrice("abc")
    assertEquals("Price must be a positive number", viewModel.uiState.value.invalidPriceMsg)

    viewModel.setPrice("-1")
    assertEquals("Price must be a positive number", viewModel.uiState.value.invalidPriceMsg)

    viewModel.setPrice("10.5")
    assertNull(viewModel.uiState.value.invalidPriceMsg)
  }

  @Test
  fun `setSubject`() {
    val subject = MainSubject.entries.firstOrNull()
    if (subject != null) {
      viewModel.setSubject(subject)
      assertEquals(subject, viewModel.uiState.value.subject)
    }
  }

  @Test
  fun `isValid becomes true when all fields valid`() {
    viewModel.setTitle("T")
    viewModel.setDescription("D")
    viewModel.setPrice("5")
    viewModel.setSubject(MainSubject.TECHNOLOGY)
    assertTrue(viewModel.uiState.value.isValid)
  }

  @Test
  fun `setError sets all errors when fields are empty`() {
    viewModel.setTitle("")
    viewModel.setDescription("")
    viewModel.setPrice("")
    viewModel.setError()

    assertEquals("Title cannot be empty", viewModel.uiState.value.invalidTitleMsg)
    assertEquals("Description cannot be empty", viewModel.uiState.value.invalidDescMsg)
    assertEquals("Price cannot be empty", viewModel.uiState.value.invalidPriceMsg)
    assertEquals("You must choose a subject", viewModel.uiState.value.invalidSubjectMsg)
    assertFalse(viewModel.uiState.value.isValid)
  }

  @Test
  fun `setError sets price invalid message for non numeric or negative`() {

    viewModel.setTitle("Valid")
    viewModel.setDescription("Valid")
    viewModel.setPrice("abc") // non-numeric
    viewModel.setError()

    assertNull(viewModel.uiState.value.invalidTitleMsg)
    assertNull(viewModel.uiState.value.invalidDescMsg)
    assertEquals("Price must be a positive number", viewModel.uiState.value.invalidPriceMsg)
    assertEquals("You must choose a subject", viewModel.uiState.value.invalidSubjectMsg)
    assertFalse(viewModel.uiState.value.isValid)
  }

  @Test
  fun `setError clears errors when all fields valid`() {
    viewModel.setTitle("T")
    viewModel.setDescription("D")
    viewModel.setPrice("10")
    viewModel.setSubject(MainSubject.TECHNOLOGY)

    viewModel.setError()

    assertNull(viewModel.uiState.value.invalidTitleMsg)
    assertNull(viewModel.uiState.value.invalidDescMsg)
    assertNull(viewModel.uiState.value.invalidPriceMsg)
    assertNull(viewModel.uiState.value.invalidSubjectMsg)
    assertTrue(viewModel.uiState.value.isValid)
  }

  @Test
  fun `addProfile withInvallid data`() {
    viewModel.setTitle("T")

    viewModel.addProfile(userId = "")

    assertEquals("Description cannot be empty", viewModel.uiState.value.invalidDescMsg)
    assertEquals("Price cannot be empty", viewModel.uiState.value.invalidPriceMsg)
    assertEquals("You must choose a subject", viewModel.uiState.value.invalidSubjectMsg)
    assertFalse(viewModel.uiState.value.isValid)
  }
}
