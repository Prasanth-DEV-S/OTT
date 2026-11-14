package com.dealshot.app.presentation.mobile.autoplay

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AutoplayFeatureTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: AutoplayViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = AutoplayViewModel(SavedStateHandle())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialSetup_loadsNationalCategoryWith6Videos() {
        val state = viewModel.uiState.value
        assertEquals("national", state.selectedCategoryId)
        assertEquals("National", state.selectedCategoryName)
        assertEquals(6, state.categoryVideos.size)
        assertEquals(0, state.currentIndex)
        assertEquals("Parliament Session Highlights", state.currentVideo?.title)
        assertTrue(state.isAutoplayEnabled)
    }

    @Test
    fun onVideoEnded_starts5SecondCountdownForNextVideoInSameCategory() {
        viewModel.onVideoEnded()

        val state = viewModel.uiState.value
        assertTrue(state.showNextOverlay)
        assertEquals("Budget 2026: Key Announcements", state.nextVideo?.title)
    }

    @Test
    fun playNextVideoNow_advancesToNextVideoInSameCategory() {
        viewModel.onVideoEnded()
        viewModel.playNextVideoNow()

        val state = viewModel.uiState.value
        assertFalse(state.showNextOverlay)
        assertEquals(1, state.currentIndex)
        assertEquals("Budget 2026: Key Announcements", state.currentVideo?.title)
        assertEquals("national", state.currentVideo?.categoryId)
    }

    @Test
    fun cancelCountdown_dismissesNextOverlayWithoutChangingVideo() {
        viewModel.onVideoEnded()
        viewModel.cancelCountdown()

        val state = viewModel.uiState.value
        assertFalse(state.showNextOverlay)
        assertEquals(0, state.currentIndex)
        assertEquals("Parliament Session Highlights", state.currentVideo?.title)
    }

    @Test
    fun selectVideo_playsSelectedVideoImmediately() {
        val targetVideo = AutoplayDemoRepository.videosByCategory["national"]!![3] // New Metro Line Inaugurated
        viewModel.selectVideo(targetVideo)

        val state = viewModel.uiState.value
        assertEquals(3, state.currentIndex)
        assertEquals("New Metro Line Inaugurated", state.currentVideo?.title)
        assertFalse(state.showNextOverlay)
    }

    @Test
    fun autoplayDisabled_videoEndDoesNotTriggerCountdown() {
        viewModel.toggleAutoplay(false)
        viewModel.onVideoEnded()

        val state = viewModel.uiState.value
        assertFalse(state.showNextOverlay)
        assertEquals(0, state.currentIndex)
    }

    @Test
    fun lastVideoEnded_showsCategoryCompletedOverlay() {
        // Jump to last video (index 5: Independence Day Preparations)
        val lastVideo = AutoplayDemoRepository.videosByCategory["national"]!!.last()
        viewModel.selectVideo(lastVideo)

        // Reaching end of last video in category
        viewModel.onVideoEnded()

        val state = viewModel.uiState.value
        assertFalse(state.showNextOverlay)
        assertTrue(state.showCategoryCompletedOverlay)
    }

    @Test
    fun selectOtherCategory_swapsPlaylistToCategoryScope() {
        viewModel.loadCategory("sports")

        val state = viewModel.uiState.value
        assertEquals("sports", state.selectedCategoryId)
        assertEquals("Sports", state.selectedCategoryName)
        assertEquals(6, state.categoryVideos.size)
        assertEquals(0, state.currentIndex)
        assertEquals("India vs Australia T20 Thriller Highlights", state.currentVideo?.title)
    }
}
