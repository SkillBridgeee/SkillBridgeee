package com.android.sample.screen

import com.android.sample.ui.screens.newSkill.NewSkillViewModel
import com.android.sample.ui.screens.newSkill.SkillUIState


import com.android.sample.model.skill.MainSubject
import kotlinx.coroutines.flow.MutableStateFlow
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
        viewModel.setDesc("")
        assertNotNull(viewModel.uiState.value.invalidDescMsg)
        assertFalse(viewModel.uiState.value.isValid)

        viewModel.setDesc("A description")
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
        viewModel.setDesc("D")
        viewModel.setPrice("5")
        assertTrue(viewModel.uiState.value.isValid)
    }

    @Test
    fun `clearErrorMsg via reflection`() {
        val vm = viewModel
        val field = vm.javaClass.getDeclaredField("_uiState")
        field.isAccessible = true
        val stateFlow = field.get(vm) as MutableStateFlow<SkillUIState>
        stateFlow.value = stateFlow.value.copy(errorMsg = "some error")

        assertEquals("some error", vm.uiState.value.errorMsg)
        vm.clearErrorMsg()
        assertNull(vm.uiState.value.errorMsg)
    }
}
