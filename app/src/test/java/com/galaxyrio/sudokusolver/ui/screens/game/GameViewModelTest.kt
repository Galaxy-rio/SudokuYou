package com.galaxyrio.sudokusolver.ui.screens.game

import com.galaxyrio.sudokusolver.data.GameRepository
import com.galaxyrio.sudokusolver.data.settings.AppSettings
import com.galaxyrio.sudokusolver.data.settings.CoordinateNotation
import com.galaxyrio.sudokusolver.domain.game.CandidateCalculator
import com.galaxyrio.sudokusolver.domain.game.HintIssue
import com.galaxyrio.sudokusolver.domain.model.AdvancedNoteColor
import com.galaxyrio.sudokusolver.domain.model.AdvancedNoteEndpoint
import com.galaxyrio.sudokusolver.domain.model.AdvancedNoteLineStyle
import com.galaxyrio.sudokusolver.domain.model.AdvancedNotes
import com.galaxyrio.sudokusolver.domain.model.Cell
import com.galaxyrio.sudokusolver.domain.model.Difficulty
import com.galaxyrio.sudokusolver.domain.model.GameStatistics
import com.galaxyrio.sudokusolver.domain.model.SavedGame
import com.galaxyrio.sudokusolver.domain.model.Sudoku
import com.galaxyrio.sudokusolver.domain.model.SudokuExportFormat
import com.galaxyrio.sudokusolver.domain.model.SudokuSolution
import com.galaxyrio.sudokusolver.domain.solver.CandidateElimination
import com.galaxyrio.sudokusolver.domain.solver.CandidateRef
import com.galaxyrio.sudokusolver.domain.solver.CellRef
import com.galaxyrio.sudokusolver.domain.solver.HumanSolver
import com.galaxyrio.sudokusolver.domain.solver.Placement
import com.galaxyrio.sudokusolver.domain.solver.SolveStep
import com.galaxyrio.sudokusolver.domain.solver.SolverState
import com.galaxyrio.sudokusolver.domain.solver.TechniqueDetector
import com.galaxyrio.sudokusolver.domain.solver.TechniqueId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {

    @Test
    fun newGameUsesRequestedDifficulty() = runViewModelTest {
        val repository = FakeGameRepository()
        val viewModel = GameViewModel(
            gameRepository = repository,
            newGameDifficulty = Difficulty.HARD,
            savedGameId = null,
        )
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(Difficulty.HARD, viewModel.uiState.value.difficulty)
        assertEquals(Difficulty.HARD, repository.createdDifficulty)
    }

    @Test
    fun savedGameKeepsItsPersistedDifficulty() = runViewModelTest {
        val savedGame = SavedGame(
            id = 7,
            difficulty = Difficulty.HARD,
            sudoku = Sudoku(),
        )
        val repository = FakeGameRepository(savedGame)

        val viewModel = GameViewModel(
            gameRepository = repository,
            newGameDifficulty = null,
            savedGameId = savedGame.id,
        )
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(Difficulty.HARD, viewModel.uiState.value.difficulty)
        assertEquals(savedGame.id, viewModel.uiState.value.gameId)
    }

    @Test
    fun missingSavedGameProducesLoadError() = runViewModelTest {
        val viewModel = GameViewModel(
            gameRepository = FakeGameRepository(),
            newGameDifficulty = null,
            savedGameId = 404,
        )
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.hasLoadError)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun enteringNumberUpdatesAndPersistsBoard() = runViewModelTest {
        val savedGame = SavedGame(
            id = 11,
            difficulty = Difficulty.EASY,
            sudoku = Sudoku(),
            solution = SOLUTION.toSolution(),
        )
        val repository = FakeGameRepository(savedGame)
        val viewModel = GameViewModel(
            gameRepository = repository,
            newGameDifficulty = null,
            savedGameId = savedGame.id,
        )
        advanceUntilIdle()

        viewModel.onCellSelected(row = 0, col = 0)
        viewModel.onNumberSelected(5)
        advanceUntilIdle()

        assertEquals(5, viewModel.uiState.value.sudoku.getCell(0, 0).value)
        assertEquals(5, repository.savedGamesHistory.last().sudoku.getCell(0, 0).value)
        assertEquals(savedGame.solution, repository.savedGamesHistory.last().solution)
    }

    @Test
    fun persistenceFailureIsExposedWithoutCrashing() = runViewModelTest {
        val savedGame = SavedGame(
            id = 12,
            difficulty = Difficulty.EASY,
            sudoku = Sudoku(),
        )
        val repository = FakeGameRepository(savedGame).apply {
            failSaves = true
        }
        val viewModel = GameViewModel(
            gameRepository = repository,
            newGameDifficulty = null,
            savedGameId = savedGame.id,
        )
        advanceUntilIdle()

        viewModel.onCellSelected(row = 0, col = 0)
        viewModel.onNumberSelected(5)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.hasPersistenceError)
        assertEquals(5, viewModel.uiState.value.sudoku.getCell(0, 0).value)
    }

    @Test
    fun completingPuzzleKeepsAndPersistsGame() = runViewModelTest {
        val cells = SOLUTION.mapIndexed { index, character ->
            if (index == 0) {
                Cell()
            } else {
                Cell(value = character.digitToInt(), isFixed = true)
            }
        }
        val savedGame = SavedGame(
            id = 15,
            difficulty = Difficulty.MEDIUM,
            sudoku = Sudoku(cells),
        )
        val repository = FakeGameRepository(savedGame)
        val viewModel = GameViewModel(
            gameRepository = repository,
            newGameDifficulty = null,
            savedGameId = savedGame.id,
        )
        advanceUntilIdle()

        viewModel.onCellSelected(row = 0, col = 0)
        viewModel.onNumberSelected(5)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isComplete)
        assertEquals(savedGame.id, viewModel.uiState.value.gameId)
        assertTrue(repository.deletedIds.isEmpty())
        assertTrue(repository.savedGamesHistory.last().sudoku.cells.all { it.isSolved() })
    }

    @Test
    fun activeTimerIsPersistedWhenPaused() = runViewModelTest {
        val savedGame = SavedGame(
            id = 20,
            difficulty = Difficulty.EASY,
            sudoku = Sudoku(),
        )
        val repository = FakeGameRepository(savedGame)
        val viewModel = GameViewModel(
            gameRepository = repository,
            newGameDifficulty = null,
            savedGameId = savedGame.id,
        )
        advanceUntilIdle()

        viewModel.onResume()
        advanceTimeBy(3_000)
        runCurrent()
        viewModel.onPause()
        advanceUntilIdle()

        assertEquals(3, viewModel.uiState.value.timeSpentSeconds)
        assertEquals(3, repository.savedGamesHistory.last().timeSpentSeconds)
    }

    @Test
    fun advancedModeCanBeChangedWithoutChangingTheBoard() = runViewModelTest {
        val savedGame = SavedGame(
            id = 21,
            difficulty = Difficulty.MEDIUM,
            sudoku = Sudoku.fromGridString(PUZZLE),
        )
        val viewModel = GameViewModel(
            gameRepository = FakeGameRepository(savedGame),
            newGameDifficulty = null,
            savedGameId = savedGame.id,
        )
        advanceUntilIdle()

        val originalBoard = viewModel.uiState.value.sudoku
        viewModel.setAdvancedMode(true)

        assertTrue(viewModel.uiState.value.isAdvancedMode)
        assertEquals(originalBoard, viewModel.uiState.value.sudoku)
    }

    @Test
    fun advancedNotesSelectMultipleDigitsWithoutEnteringValues() = runViewModelTest {
        val savedGame = SavedGame(
            id = 210,
            difficulty = Difficulty.MEDIUM,
            sudoku = Sudoku(),
        )
        val repository = FakeGameRepository(savedGame)
        val viewModel = GameViewModel(
            gameRepository = repository,
            newGameDifficulty = null,
            savedGameId = savedGame.id,
        )
        advanceUntilIdle()

        viewModel.setAdvancedMode(true)
        viewModel.toggleAdvancedNoteMode()
        viewModel.onCellSelected(row = 0, col = 0)
        viewModel.onAdvancedNumberSelected(2)
        viewModel.onAdvancedNumberSelected(7)
        advanceUntilIdle()

        assertEquals(setOf(2, 7), viewModel.uiState.value.advancedNotes.highlightedDigits)
        assertEquals(0, viewModel.uiState.value.sudoku.getCell(0, 0).value)
        assertEquals(
            setOf(2, 7),
            repository.savedGamesHistory.last().advancedNotes.highlightedDigits,
        )
    }

    @Test
    fun advancedNoteModeStillAllowsExplicitCandidateNotes() = runViewModelTest {
        val savedGame = SavedGame(
            id = 211,
            difficulty = Difficulty.MEDIUM,
            sudoku = Sudoku(),
        )
        val viewModel = GameViewModel(
            gameRepository = FakeGameRepository(savedGame),
            newGameDifficulty = null,
            savedGameId = savedGame.id,
        )
        advanceUntilIdle()

        viewModel.setAdvancedMode(true)
        viewModel.toggleAdvancedNoteMode()
        viewModel.toggleNoteMode()
        viewModel.onCellSelected(row = 0, col = 0)
        viewModel.onAdvancedNumberSelected(4)

        val cell = viewModel.uiState.value.sudoku.getCell(0, 0)
        assertEquals(0, cell.value)
        assertEquals(setOf(4), cell.candidates)
        assertTrue(cell.isCandidateSetExplicit)
        assertTrue(viewModel.uiState.value.advancedNotes.highlightedDigits.isEmpty())
    }

    @Test
    fun advancedToolsEnableAdvancedNotesAndPersistFeatureFlags() = runViewModelTest {
        val savedGame = SavedGame(
            id = 212,
            difficulty = Difficulty.MEDIUM,
            sudoku = Sudoku(),
        )
        val repository = FakeGameRepository(savedGame)
        val viewModel = GameViewModel(
            gameRepository = repository,
            newGameDifficulty = null,
            savedGameId = savedGame.id,
        )
        advanceUntilIdle()

        viewModel.setAdvancedMode(true)
        viewModel.toggleBivalueHighlights()
        viewModel.toggleFrameHighlights()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isAdvancedNoteMode)
        assertTrue(state.advancedNotes.highlightBivalueCandidates)
        assertTrue(state.advancedNotes.frameHighlightedCells)
        assertEquals(state.advancedNotes, repository.savedGamesHistory.last().advancedNotes)
    }

    @Test
    fun paintColorsSelectedCandidatesAndWhiteClearsTheWholeCell() = runViewModelTest {
        val cells = MutableList(Sudoku.CELL_COUNT) { Cell() }
        cells[0] = Cell(candidates = setOf(2, 7))
        val savedGame = SavedGame(
            id = 213,
            difficulty = Difficulty.MEDIUM,
            sudoku = Sudoku(cells),
        )
        val viewModel = GameViewModel(
            gameRepository = FakeGameRepository(savedGame),
            newGameDifficulty = null,
            savedGameId = savedGame.id,
        )
        advanceUntilIdle()

        viewModel.setAdvancedMode(true)
        viewModel.toggleAdvancedNoteMode()
        viewModel.onAdvancedNumberSelected(2)
        viewModel.onAdvancedNumberSelected(7)
        viewModel.togglePaintTool()
        viewModel.onCellSelected(row = 0, col = 0)

        assertEquals(
            AdvancedNoteColor.RED,
            viewModel.uiState.value.advancedNotes.candidateColor(0, 2),
        )
        assertEquals(
            AdvancedNoteColor.RED,
            viewModel.uiState.value.advancedNotes.candidateColor(0, 7),
        )
        assertTrue(viewModel.uiState.value.advancedNotes.cellColors.isEmpty())

        viewModel.selectAdvancedColor(AdvancedNoteColor.entries.size)
        viewModel.onCellSelected(row = 0, col = 0)

        assertTrue(viewModel.uiState.value.advancedNotes.candidateColors.isEmpty())
        assertTrue(viewModel.uiState.value.advancedNotes.cellColors.isEmpty())
    }

    @Test
    fun lineToolStoresCandidateSpecificEndpoints() = runViewModelTest {
        val cells = MutableList(Sudoku.CELL_COUNT) { Cell() }
        cells[0] = Cell(candidates = setOf(4, 6))
        cells[1] = Cell(candidates = setOf(4, 8))
        val savedGame = SavedGame(
            id = 214,
            difficulty = Difficulty.MEDIUM,
            sudoku = Sudoku(cells),
        )
        val repository = FakeGameRepository(savedGame)
        val viewModel = GameViewModel(
            gameRepository = repository,
            newGameDifficulty = null,
            savedGameId = savedGame.id,
        )
        advanceUntilIdle()

        viewModel.setAdvancedMode(true)
        viewModel.toggleAdvancedNoteMode()
        viewModel.onAdvancedNumberSelected(4)
        viewModel.toggleLineTool(AdvancedNoteLineStyle.SOLID)
        viewModel.onCellSelected(row = 0, col = 0)
        assertEquals(
            AdvancedNoteEndpoint(cellIndex = 0, candidateDigit = 4),
            viewModel.uiState.value.pendingLineStart,
        )
        viewModel.onCellSelected(row = 0, col = 1)
        advanceUntilIdle()

        val line = viewModel.uiState.value.advancedNotes.lines.single()
        assertEquals(AdvancedNoteEndpoint(0, 4), line.start)
        assertEquals(AdvancedNoteEndpoint(1, 4), line.end)
        assertEquals(AdvancedNoteLineStyle.SOLID, line.style)
        assertEquals(AdvancedNoteColor.RED, line.color)
        assertEquals(line, repository.savedGamesHistory.last().advancedNotes.lines.single())
    }

    @Test
    fun savedAdvancedNotesAreRestoredWithTheGame() = runViewModelTest {
        val notes = AdvancedNotes(
            highlightedDigits = setOf(3, 9),
            cellColors = mapOf(8 to AdvancedNoteColor.CYAN),
        )
        val savedGame = SavedGame(
            id = 215,
            difficulty = Difficulty.MEDIUM,
            sudoku = Sudoku(),
            advancedNotes = notes,
        )
        val viewModel = GameViewModel(
            gameRepository = FakeGameRepository(savedGame),
            newGameDifficulty = null,
            savedGameId = savedGame.id,
        )
        advanceUntilIdle()

        assertEquals(notes, viewModel.uiState.value.advancedNotes)
    }

    @Test
    fun undoAndRedoNavigateBoardHistoryAndNewInputClearsRedo() = runViewModelTest {
        val savedGame = SavedGame(
            id = 216,
            difficulty = Difficulty.MEDIUM,
            sudoku = Sudoku(),
        )
        val viewModel = GameViewModel(
            gameRepository = FakeGameRepository(savedGame),
            newGameDifficulty = null,
            savedGameId = savedGame.id,
        )
        advanceUntilIdle()

        viewModel.onCellSelected(0, 0)
        viewModel.onNumberSelected(1)
        viewModel.onCellSelected(0, 1)
        viewModel.onNumberSelected(2)

        viewModel.undo()
        assertEquals(0, viewModel.uiState.value.sudoku.getCell(0, 1).value)
        assertTrue(viewModel.uiState.value.canRedo)

        viewModel.redo()
        assertEquals(2, viewModel.uiState.value.sudoku.getCell(0, 1).value)
        assertFalse(viewModel.uiState.value.canRedo)

        viewModel.undo()
        viewModel.onCellSelected(0, 2)
        viewModel.onNumberSelected(3)
        assertFalse(viewModel.uiState.value.canRedo)
    }

    @Test
    fun deletingDraftClearsColorsButKeepsHighlightModes() = runViewModelTest {
        val notes = AdvancedNotes(
            highlightedDigits = setOf(2, 7),
            highlightBivalueCandidates = true,
            frameHighlightedCells = true,
            cellColors = mapOf(10 to AdvancedNoteColor.ORANGE),
        )
        val savedGame = SavedGame(
            id = 217,
            difficulty = Difficulty.MEDIUM,
            sudoku = Sudoku(),
            advancedNotes = notes,
        )
        val repository = FakeGameRepository(savedGame)
        val viewModel = GameViewModel(
            gameRepository = repository,
            newGameDifficulty = null,
            savedGameId = savedGame.id,
        )
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.hasAdvancedDraft)
        viewModel.deleteAdvancedDraft()
        advanceUntilIdle()

        val clearedNotes = viewModel.uiState.value.advancedNotes
        assertEquals(notes.highlightedDigits, clearedNotes.highlightedDigits)
        assertTrue(clearedNotes.highlightBivalueCandidates)
        assertTrue(clearedNotes.frameHighlightedCells)
        assertTrue(clearedNotes.cellColors.isEmpty())
        assertTrue(clearedNotes.candidateColors.isEmpty())
        assertTrue(clearedNotes.lines.isEmpty())
        assertFalse(viewModel.uiState.value.hasAdvancedDraft)
        assertEquals(clearedNotes, repository.savedGamesHistory.last().advancedNotes)
    }

    @Test
    fun restartRestoresGivensAndClearsProgressDraftHistoryAndTimer() = runViewModelTest {
        val cells = MutableList(Sudoku.CELL_COUNT) { Cell() }
        cells[0] = Cell(value = 5, isFixed = true)
        cells[1] = Cell(value = 3)
        cells[2] = Cell(candidates = setOf(1, 2))
        val savedGame = SavedGame(
            id = 218,
            difficulty = Difficulty.MEDIUM,
            sudoku = Sudoku(cells),
            advancedNotes = AdvancedNotes(
                cellColors = mapOf(0 to AdvancedNoteColor.BLUE),
            ),
            timeSpentSeconds = 125,
        )
        val repository = FakeGameRepository(savedGame)
        val viewModel = GameViewModel(
            gameRepository = repository,
            newGameDifficulty = null,
            savedGameId = savedGame.id,
        )
        advanceUntilIdle()

        viewModel.restartGame()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(5, state.sudoku.getCell(0, 0).value)
        assertTrue(state.sudoku.getCell(0, 0).isFixed)
        assertEquals(0, state.sudoku.getCell(0, 1).value)
        assertTrue(state.sudoku.getCell(0, 2).candidates.isEmpty())
        assertEquals(0, state.timeSpentSeconds)
        assertEquals(AdvancedNotes(), state.advancedNotes)
        assertFalse(state.canUndo)
        assertFalse(state.canRedo)
        assertEquals(state.sudoku, repository.savedGamesHistory.last().sudoku)
        assertEquals(0, repository.savedGamesHistory.last().timeSpentSeconds)
    }

    @Test
    fun exportsUseFileSettingsAndKeepOriginalSeparateFromPlayerInput() = runViewModelTest {
        val cells = MutableList(Sudoku.CELL_COUNT) { Cell() }
        cells[0] = Cell(value = 5, isFixed = true)
        cells[1] = Cell(value = 3)
        val settingsFlow = MutableStateFlow(
            AppSettings(
                exportFormat = SudokuExportFormat.SUSSER,
                includeCandidatesInCurrentExport = false,
            )
        )
        val savedGame = SavedGame(
            id = 219,
            difficulty = Difficulty.MEDIUM,
            sudoku = Sudoku(cells),
        )
        val viewModel = GameViewModel(
            gameRepository = FakeGameRepository(savedGame),
            newGameDifficulty = null,
            savedGameId = savedGame.id,
            settings = settingsFlow,
        )
        advanceUntilIdle()

        assertTrue(viewModel.exportOriginalText().startsWith("5.."))
        assertTrue(viewModel.exportCurrentText().startsWith("5+3."))

        settingsFlow.value = settingsFlow.value.copy(exportFormat = SudokuExportFormat.EXCEL)
        advanceUntilIdle()
        assertTrue('\t' in viewModel.exportCurrentText())
    }

    @Test
    fun coordinateNotationTracksTheCurrentGameSetting() = runViewModelTest {
        val settingsFlow = MutableStateFlow(AppSettings())
        val viewModel = GameViewModel(
            gameRepository = FakeGameRepository(),
            newGameDifficulty = Difficulty.EASY,
            savedGameId = null,
            settings = settingsFlow,
        )
        advanceUntilIdle()

        assertEquals(CoordinateNotation.LOCALIZED, viewModel.uiState.value.coordinateNotation)

        settingsFlow.value = settingsFlow.value.copy(
            coordinateNotation = CoordinateNotation.K9,
        )
        advanceUntilIdle()

        assertEquals(CoordinateNotation.K9, viewModel.uiState.value.coordinateNotation)
    }

    @Test
    fun hintTraceSupportsArbitraryNavigationAndAppliesOnlyTheNextStep() = runViewModelTest {
        val savedGame = SavedGame(
            id = 22,
            difficulty = Difficulty.MEDIUM,
            sudoku = Sudoku.fromGridString(PUZZLE),
        )
        val viewModel = GameViewModel(
            gameRepository = FakeGameRepository(savedGame),
            newGameDifficulty = null,
            savedGameId = savedGame.id,
            solverDispatcher = StandardTestDispatcher(testScheduler),
        )
        advanceUntilIdle()

        viewModel.prepareHintTrace()
        assertTrue(viewModel.uiState.value.isHintLoading)
        advanceUntilIdle()

        val trace = requireNotNull(viewModel.uiState.value.hintTrace)
        assertTrue(trace.steps.size > 1)
        viewModel.selectHintStep(Int.MAX_VALUE)
        assertEquals(trace.steps.lastIndex, viewModel.uiState.value.selectedHintStepIndex)

        val expectedNextState = trace.stateAfterStep(0)
        val firstStep = trace.steps.first()
        assertTrue(viewModel.applyNextHintStep())
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.hintTrace)
        firstStep.placements.forEach { placement ->
            assertEquals(
                expectedNextState.valueAt(placement.cell),
                viewModel.uiState.value.sudoku
                    .getCell(placement.cell.row, placement.cell.col)
                    .value,
            )
        }
        firstStep.eliminations.forEach { elimination ->
            val candidate = elimination.candidate
            assertFalse(
                candidate.digit in viewModel.uiState.value.sudoku
                    .getCell(candidate.cell.row, candidate.cell.col)
                    .candidates
            )
        }
    }

    @Test
    fun hintUsesCurrentSukakuCandidatesWithoutRestoringValidEliminations() = runViewModelTest {
        val constrainedBoard = CandidateCalculator.calculateAllCandidates(
            Sudoku.fromGridString(PUZZLE)
        ).removeCandidate(row = 0, col = 2, candidate = 1)
        val savedGame = SavedGame(
            id = 25,
            difficulty = Difficulty.MEDIUM,
            sudoku = constrainedBoard,
        )
        val viewModel = GameViewModel(
            gameRepository = FakeGameRepository(savedGame),
            newGameDifficulty = null,
            savedGameId = savedGame.id,
            solverDispatcher = StandardTestDispatcher(testScheduler),
        )
        advanceUntilIdle()

        viewModel.prepareHintTrace()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.hintIssue)
        assertFalse(
            requireNotNull(viewModel.uiState.value.hintTrace)
                .initialState
                .hasCandidate(CellRef(0, 2), 1)
        )
    }

    @Test
    fun hintRepairsWrongValuesBeforeMissingCandidates() = runViewModelTest {
        val constrainedBoard = CandidateCalculator.calculateAllCandidates(
            Sudoku.fromGridString(PUZZLE)
        )
            .removeCandidate(row = 0, col = 3, candidate = 6)
            .setCell(row = 0, col = 2, value = 1)
        val savedGame = SavedGame(
            id = 26,
            difficulty = Difficulty.MEDIUM,
            sudoku = constrainedBoard,
        )
        val viewModel = GameViewModel(
            gameRepository = FakeGameRepository(savedGame),
            newGameDifficulty = null,
            savedGameId = savedGame.id,
            solverDispatcher = StandardTestDispatcher(testScheduler),
        )
        advanceUntilIdle()

        viewModel.prepareHintTrace()
        advanceUntilIdle()

        val valueIssue = viewModel.uiState.value.hintIssue as HintIssue.IncorrectValues
        assertEquals(listOf(CellRef(0, 2)), valueIssue.entries.map { it.cell })
        assertNull(viewModel.uiState.value.hintTrace)
        assertTrue(viewModel.applyHintAction())
        assertEquals(0, viewModel.uiState.value.sudoku.getCell(0, 2).value)
        advanceUntilIdle()

        viewModel.prepareHintTrace()
        advanceUntilIdle()

        val candidateIssue = viewModel.uiState.value.hintIssue as HintIssue.MissingCandidates
        assertTrue(CandidateRef(CellRef(0, 3), 6) in candidateIssue.candidates)
        assertTrue(viewModel.applyHintAction())
        assertTrue(6 in viewModel.uiState.value.sudoku.getCell(0, 3).candidates)
    }

    @Test
    fun hiddenHintDetailsRequireAnExplicitReveal() = runViewModelTest {
        val savedGame = SavedGame(
            id = 27,
            difficulty = Difficulty.MEDIUM,
            sudoku = Sudoku.fromGridString(PUZZLE),
            solution = SOLUTION.toSolution(),
        )
        val viewModel = GameViewModel(
            gameRepository = FakeGameRepository(savedGame),
            newGameDifficulty = null,
            savedGameId = savedGame.id,
            solverDispatcher = StandardTestDispatcher(testScheduler),
            settings = MutableStateFlow(AppSettings(showHintDetails = false)),
        )
        advanceUntilIdle()

        viewModel.prepareHintTrace()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.hintTrace?.steps?.isNotEmpty() == true)
        assertFalse(viewModel.uiState.value.areHintDetailsVisible)
        viewModel.revealHintDetails()
        assertTrue(viewModel.uiState.value.areHintDetailsVisible)
    }

    @Test
    fun privateCandidateErrorDismissesWithoutRestoringCandidate() = runViewModelTest {
        val board = CandidateCalculator.calculateAllCandidates(
            Sudoku.fromGridString(PUZZLE)
        ).removeCandidate(row = 0, col = 2, candidate = 4)
        val savedGame = SavedGame(
            id = 28,
            difficulty = Difficulty.MEDIUM,
            sudoku = board,
            solution = SOLUTION.toSolution(),
        )
        val viewModel = GameViewModel(
            gameRepository = FakeGameRepository(savedGame),
            newGameDifficulty = null,
            savedGameId = savedGame.id,
            solverDispatcher = StandardTestDispatcher(testScheduler),
            settings = MutableStateFlow(AppSettings(showErrorDetails = false)),
        )
        advanceUntilIdle()

        viewModel.prepareHintTrace()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.hintIssue is HintIssue.MissingCandidates)

        assertTrue(viewModel.applyHintAction())
        assertNull(viewModel.uiState.value.hintIssue)
        assertFalse(4 in viewModel.uiState.value.sudoku.getCell(0, 2).candidates)
    }

    @Test
    fun immediateErrorOpensForNonConflictingWrongEntry() = runViewModelTest {
        val savedGame = SavedGame(
            id = 29,
            difficulty = Difficulty.MEDIUM,
            sudoku = Sudoku.fromGridString(PUZZLE),
            solution = SOLUTION.toSolution(),
        )
        val viewModel = GameViewModel(
            gameRepository = FakeGameRepository(savedGame),
            newGameDifficulty = null,
            savedGameId = savedGame.id,
            settings = MutableStateFlow(AppSettings(showErrorsImmediately = true)),
        )
        advanceUntilIdle()

        viewModel.onCellSelected(row = 0, col = 2)
        viewModel.onNumberSelected(1)

        val issue = viewModel.uiState.value.hintIssue as HintIssue.IncorrectValues
        assertEquals(listOf(CellRef(0, 2)), issue.entries.map { it.cell })
        assertEquals(1L, viewModel.uiState.value.immediateHintRequestId)
    }

    @Test
    fun immediateErrorLeavesExistingConflictToBoardHighlighting() = runViewModelTest {
        val savedGame = SavedGame(
            id = 30,
            difficulty = Difficulty.MEDIUM,
            sudoku = Sudoku.fromGridString(PUZZLE),
            solution = SOLUTION.toSolution(),
        )
        val viewModel = GameViewModel(
            gameRepository = FakeGameRepository(savedGame),
            newGameDifficulty = null,
            savedGameId = savedGame.id,
            settings = MutableStateFlow(AppSettings(showErrorsImmediately = true)),
        )
        advanceUntilIdle()

        viewModel.onCellSelected(row = 0, col = 2)
        viewModel.onNumberSelected(5)

        assertNull(viewModel.uiState.value.hintIssue)
        assertEquals(0L, viewModel.uiState.value.immediateHintRequestId)
    }

    @Test
    fun immediateErrorOpensWhenSolutionCandidateIsRemoved() = runViewModelTest {
        val savedGame = SavedGame(
            id = 31,
            difficulty = Difficulty.MEDIUM,
            sudoku = CandidateCalculator.calculateAllCandidates(
                Sudoku.fromGridString(PUZZLE)
            ),
            solution = SOLUTION.toSolution(),
        )
        val viewModel = GameViewModel(
            gameRepository = FakeGameRepository(savedGame),
            newGameDifficulty = null,
            savedGameId = savedGame.id,
            settings = MutableStateFlow(AppSettings(showErrorsImmediately = true)),
        )
        advanceUntilIdle()

        viewModel.toggleNoteMode()
        viewModel.onCellSelected(row = 0, col = 2)
        viewModel.onNumberSelected(4)

        val issue = viewModel.uiState.value.hintIssue as HintIssue.MissingCandidates
        assertEquals(listOf(CandidateRef(CellRef(0, 2), 4)), issue.candidates)
        assertEquals(1L, viewModel.uiState.value.immediateHintRequestId)
    }

    @Test
    fun appliedCandidateEliminationIsRetainedForTheNextHint() = runViewModelTest {
        val target = CellRef(0, 0)
        val stagedDetector = object : TechniqueDetector {
            override val technique = TechniqueId.NAKED_PAIR

            override fun find(state: SolverState): SolveStep? = when {
                state.valueAt(target) != 0 -> null
                state.hasCandidate(target, 1) -> SolveStep(
                    technique = TechniqueId.NAKED_PAIR,
                    eliminations = listOf(
                        CandidateElimination(CandidateRef(target, 1))
                    ),
                )
                else -> SolveStep(
                    technique = TechniqueId.NAKED_SINGLE,
                    placements = listOf(Placement(target, 2)),
                )
            }
        }
        val savedGame = SavedGame(
            id = 23,
            difficulty = Difficulty.MEDIUM,
            sudoku = Sudoku(),
        )
        val viewModel = GameViewModel(
            gameRepository = FakeGameRepository(savedGame),
            newGameDifficulty = null,
            savedGameId = savedGame.id,
            humanSolver = HumanSolver(listOf(stagedDetector)),
            solverDispatcher = StandardTestDispatcher(testScheduler),
        )
        advanceUntilIdle()

        viewModel.prepareHintTrace()
        advanceUntilIdle()
        assertEquals(
            TechniqueId.NAKED_PAIR,
            viewModel.uiState.value.hintTrace?.steps?.first()?.technique,
        )
        assertTrue(viewModel.applyNextHintStep())

        viewModel.prepareHintTrace()
        advanceUntilIdle()

        val nextStep = viewModel.uiState.value.hintTrace?.steps?.first()
        assertEquals(TechniqueId.NAKED_SINGLE, nextStep?.technique)
        assertEquals(Placement(target, 2), nextStep?.placements?.single())
    }

    @Test
    fun consecutiveHintEliminationsDoNotRestoreEarlierCandidates() = runViewModelTest {
        val target = CellRef(0, 0)
        val stagedDetector = object : TechniqueDetector {
            override val technique = TechniqueId.NAKED_PAIR

            override fun find(state: SolverState): SolveStep? = when {
                state.hasCandidate(target, 1) -> eliminationStep(target, 1)
                state.hasCandidate(target, 3) -> eliminationStep(target, 3)
                else -> null
            }

            private fun eliminationStep(cell: CellRef, digit: Int) = SolveStep(
                technique = TechniqueId.NAKED_PAIR,
                eliminations = listOf(CandidateElimination(CandidateRef(cell, digit))),
            )
        }
        val savedGame = SavedGame(
            id = 24,
            difficulty = Difficulty.MEDIUM,
            sudoku = Sudoku(),
        )
        val viewModel = GameViewModel(
            gameRepository = FakeGameRepository(savedGame),
            newGameDifficulty = null,
            savedGameId = savedGame.id,
            humanSolver = HumanSolver(listOf(stagedDetector)),
            solverDispatcher = StandardTestDispatcher(testScheduler),
        )
        advanceUntilIdle()

        viewModel.prepareHintTrace()
        advanceUntilIdle()
        assertTrue(viewModel.applyNextHintStep())

        viewModel.prepareHintTrace()
        advanceUntilIdle()
        assertEquals(
            CandidateRef(target, 3),
            viewModel.uiState.value.hintTrace
                ?.steps
                ?.first()
                ?.eliminations
                ?.single()
                ?.candidate,
        )
        assertTrue(viewModel.applyNextHintStep())

        val displayedCandidates = viewModel.uiState.value.sudoku
            .getCell(target.row, target.col)
            .candidates
        assertFalse(1 in displayedCandidates)
        assertFalse(3 in displayedCandidates)
    }

    private fun runViewModelTest(
        block: suspend TestScope.() -> Unit,
    ) = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            block()
        } finally {
            Dispatchers.resetMain()
        }
    }

    private fun String.toSolution(): SudokuSolution = SudokuSolution(map(Char::digitToInt))

    private class FakeGameRepository(
        initialGame: SavedGame? = null,
    ) : GameRepository {
        private var game = initialGame
        private val gamesFlow = MutableStateFlow(listOfNotNull(initialGame))

        override val savedGames: Flow<List<SavedGame>> = gamesFlow
        override val statistics: Flow<List<GameStatistics>> = MutableStateFlow(emptyList())
        val savedGamesHistory = mutableListOf<SavedGame>()
        val deletedIds = mutableListOf<Long>()
        var createdDifficulty: Difficulty? = null
        var failSaves: Boolean = false

        override suspend fun getGame(id: Long): SavedGame? =
            game?.takeIf { it.id == id }

        override suspend fun createGame(difficulty: Difficulty): SavedGame {
            createdDifficulty = difficulty
            return SavedGame(
                id = 1,
                difficulty = difficulty,
                sudoku = Sudoku(),
            ).also {
                game = it
                gamesFlow.value = listOf(it)
            }
        }

        override suspend fun createImportedGame(sudoku: Sudoku): SavedGame? = null

        override suspend fun saveGame(game: SavedGame): Long {
            if (failSaves) error("Simulated persistence failure")
            this.game = game
            savedGamesHistory += game
            gamesFlow.value = listOf(game)
            return game.id
        }

        override suspend fun deleteGame(id: Long) {
            deletedIds += id
            if (game?.id == id) game = null
            gamesFlow.value = listOfNotNull(game)
        }

        override suspend fun deleteGames(ids: Set<Long>) {
            ids.forEach { deleteGame(it) }
        }

        override suspend fun clearStatistics() = Unit
    }

    private companion object {
        const val SOLUTION =
            "534678912" +
                "672195348" +
                "198342567" +
                "859761423" +
                "426853791" +
                "713924856" +
                "961537284" +
                "287419635" +
                "345286179"

        const val PUZZLE =
            "53..7...." +
                "6..195..." +
                ".98....6." +
                "8...6...3" +
                "4..8.3..1" +
                "7...2...6" +
                ".6....28." +
                "...419..5" +
                "....8..79"
    }
}
