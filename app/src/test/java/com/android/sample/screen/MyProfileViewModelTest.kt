package com.android.sample.screen

import com.android.sample.ui.profile.MyProfileViewModel
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MyProfileViewModelTest {

  private lateinit var viewModel: MyProfileViewModel

  @Before
  fun setup() {
    viewModel = MyProfileViewModel()
  }

  @Test
  fun setNameValid() {
    viewModel.setName("Alice")
    val state = viewModel.uiState.value
    assertEquals("Alice", state.name)
    assertNull(state.invalidNameMsg)
  }

  @Test
  fun setNameInvalid() {
    viewModel.setName("")
    val state = viewModel.uiState.value
    assertEquals("Name cannot be empty", state.invalidNameMsg)
  }

  @Test
  fun setEmailValid() {
    viewModel.setEmail("alice@example.com")
    val state = viewModel.uiState.value
    assertEquals("alice@example.com", state.email)
    assertNull(state.invalidEmailMsg)
  }

  @Test
  fun setEmailInvalid() {
    viewModel.setEmail("alice")
    val state = viewModel.uiState.value
    assertEquals("alice", state.email)
    assertEquals("Email is not in the right format", state.invalidEmailMsg)
  }

  @Test
  fun setLocationValid() {
    viewModel.setLocation("EPFL")
    val state = viewModel.uiState.value
    assertEquals("EPFL", state.location?.name)
    assertNull(state.invalidLocationMsg)
  }

  @Test
  fun setLocationInvalid() {
    viewModel.setLocation("")
    val state = viewModel.uiState.value
    assertNull(state.location)
    assertEquals("Location cannot be empty", state.invalidLocationMsg)
  }

  @Test
  fun setDescriptionValid() {
    viewModel.setDescription("Nice person")
    val state = viewModel.uiState.value
    assertEquals("Nice person", state.description)
    assertEquals(null, state.invalidDescMsg)
  }

  @Test
  fun setDescriptionInvalid() {
    viewModel.setDescription("")
    val state = viewModel.uiState.value
    assertEquals("Description cannot be empty", state.invalidDescMsg)
  }

  @Test
  fun checkValidity() {
    viewModel.setName("Alice")
    viewModel.setEmail("alice@example.com")
    viewModel.setLocation("Paris")
    viewModel.setDescription("Desc")
    val state = viewModel.uiState.value
    assertTrue(state.isValid)
  }
}
