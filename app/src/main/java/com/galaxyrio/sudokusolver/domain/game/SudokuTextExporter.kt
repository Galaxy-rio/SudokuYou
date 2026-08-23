package com.galaxyrio.sudokusolver.domain.game

import com.galaxyrio.sudokusolver.domain.model.Cell
import com.galaxyrio.sudokusolver.domain.model.Sudoku
import com.galaxyrio.sudokusolver.domain.model.SudokuExportFormat

/** Serializes original and in-progress boards using common interoperable Sudoku text formats. */
object SudokuTextExporter {

    fun exportOriginal(
        currentBoard: Sudoku,
        format: SudokuExportFormat,
    ): String = export(
        sudoku = originalPuzzle(currentBoard),
        format = format,
        includeCandidates = format == SudokuExportFormat.PENCILMARK,
    )

    fun exportCurrent(
        sudoku: Sudoku,
        format: SudokuExportFormat,
        includeCandidates: Boolean,
    ): String = export(sudoku, format, includeCandidates)

    fun originalPuzzle(currentBoard: Sudoku): Sudoku = Sudoku(
        currentBoard.cells.map { cell ->
            if (cell.isFixed) {
                Cell(value = cell.value, isFixed = true)
            } else {
                Cell()
            }
        }
    )

    private fun export(
        sudoku: Sudoku,
        format: SudokuExportFormat,
        includeCandidates: Boolean,
    ): String {
        val legalBoard = CandidateCalculator.calculateAllCandidates(sudoku)
        return when (format) {
            SudokuExportFormat.SUSSER -> susser(
                sudoku = sudoku,
                legalBoard = legalBoard,
                includeCandidates = includeCandidates,
            )

            SudokuExportFormat.MULTILINE -> multiline(sudoku)
            SudokuExportFormat.PENCILMARK -> pencilmark(
                sudoku = sudoku,
                legalBoard = legalBoard,
                includeCandidates = includeCandidates,
            )

            SudokuExportFormat.SUKAKU -> sukaku(
                sudoku = sudoku,
                legalBoard = legalBoard,
                includeExplicitCandidates = includeCandidates,
            )

            SudokuExportFormat.EXCEL -> excel(
                sudoku = sudoku,
                legalBoard = legalBoard,
                includeCandidates = includeCandidates,
            )

            SudokuExportFormat.OPEN_SUDOKU -> openSudoku(sudoku)
            SudokuExportFormat.HODOKU -> hodoku(
                sudoku = sudoku,
                legalBoard = legalBoard,
                includeCandidates = includeCandidates,
            )
        }
    }

    private fun susser(
        sudoku: Sudoku,
        legalBoard: Sudoku,
        includeCandidates: Boolean,
    ): String {
        val board = susserBoard(sudoku)
        if (!includeCandidates) return board
        val deletions = secondaryDeletions(sudoku, legalBoard)
        return if (deletions.isEmpty()) board else "$board:${deletions.joinToString(" ")}"
    }

    private fun susserBoard(sudoku: Sudoku): String = buildString {
        sudoku.cells.forEach { cell ->
            when {
                !cell.isSolved() -> append('.')
                cell.isFixed -> append(cell.value)
                else -> append('+').append(cell.value)
            }
        }
    }

    private fun secondaryDeletions(sudoku: Sudoku, legalBoard: Sudoku): List<String> = buildList {
        sudoku.cells.forEachIndexed { index, cell ->
            if (cell.isSolved() || !cell.isCandidateSetExplicit) return@forEachIndexed
            val row = index / Sudoku.GRID_SIZE + 1
            val col = index % Sudoku.GRID_SIZE + 1
            val deleted = legalBoard.cells[index].candidates - cell.candidates
            deleted.sorted().forEach { digit -> add("$digit$row$col") }
        }
    }

    private fun multiline(sudoku: Sudoku): String {
        val separator = ":-------+-------+-------:"
        return buildList {
            add(".-------.-------.-------.")
            repeat(Sudoku.GRID_SIZE) { row ->
                val tokens = List(Sudoku.GRID_SIZE) { col ->
                    sudoku.getCell(row, col).value.takeIf { it != 0 }?.toString() ?: "."
                }
                add(
                    "| ${tokens.take(3).joinToString(" ")} | " +
                        "${tokens.subList(3, 6).joinToString(" ")} | " +
                        "${tokens.takeLast(3).joinToString(" ")} |"
                )
                if (row == 2 || row == 5) add(separator)
            }
            add("'-------'-------'-------'")
        }.joinToString("\n")
    }

    private fun pencilmark(
        sudoku: Sudoku,
        legalBoard: Sudoku,
        includeCandidates: Boolean,
    ): String {
        val tokens = sudoku.cells.mapIndexed { index, cell ->
            when {
                cell.isFixed -> "<${cell.value}>"
                cell.isSolved() -> "*${cell.value}*"
                !includeCandidates -> "."
                else -> candidatesForExport(cell, legalBoard.cells[index]).joinToString("")
                    .ifEmpty { "." }
            }
        }
        val cellWidth = maxOf(3, tokens.maxOf(String::length))
        val segment = "-".repeat(cellWidth * Sudoku.BOX_SIZE + Sudoku.BOX_SIZE - 1)
        return buildList {
            add(".$segment.$segment.$segment.")
            repeat(Sudoku.GRID_SIZE) { row ->
                val rowTokens = tokens.subList(
                    row * Sudoku.GRID_SIZE,
                    (row + 1) * Sudoku.GRID_SIZE,
                )
                fun group(start: Int): String = rowTokens
                    .subList(start, start + Sudoku.BOX_SIZE)
                    .joinToString(" ") { it.padEnd(cellWidth) }
                add("| ${group(0)} | ${group(3)} | ${group(6)} |")
                if (row == 2 || row == 5) add(":$segment+$segment+$segment:")
            }
            add("'$segment'$segment'$segment'")
        }.joinToString("\n")
    }

    private fun sukaku(
        sudoku: Sudoku,
        legalBoard: Sudoku,
        includeExplicitCandidates: Boolean,
    ): String = buildString(capacity = Sudoku.CELL_COUNT * Sudoku.GRID_SIZE) {
        sudoku.cells.forEachIndexed { index, cell ->
            val candidates = when {
                cell.isSolved() -> setOf(cell.value)
                includeExplicitCandidates -> candidatesForExport(cell, legalBoard.cells[index])
                else -> legalBoard.cells[index].candidates
            }
            repeat(Sudoku.GRID_SIZE) { offset ->
                val digit = offset + 1
                append(if (digit in candidates) digit else '.')
            }
        }
    }

    private fun excel(
        sudoku: Sudoku,
        legalBoard: Sudoku,
        includeCandidates: Boolean,
    ): String = buildList {
        repeat(Sudoku.GRID_SIZE) { row ->
            add(
                List(Sudoku.GRID_SIZE) { col ->
                    val index = row * Sudoku.GRID_SIZE + col
                    val cell = sudoku.cells[index]
                    when {
                        cell.isSolved() -> cell.value.toString()
                        includeCandidates -> candidatesForExport(
                            cell,
                            legalBoard.cells[index],
                        ).joinToString("")
                        else -> ""
                    }
                }.joinToString("\t")
            )
        }
    }.joinToString("\n")

    private fun openSudoku(sudoku: Sudoku): String = sudoku.cells
        .flatMap { cell ->
            if (cell.isSolved()) listOf(cell.value, 0, 0) else listOf(0, 0, 1)
        }
        .joinToString("|")

    private fun hodoku(
        sudoku: Sudoku,
        legalBoard: Sudoku,
        includeCandidates: Boolean,
    ): String {
        val deletions = if (includeCandidates) {
            secondaryDeletions(sudoku, legalBoard).joinToString(" ")
        } else {
            ""
        }
        return ":0000:x:${susserBoard(sudoku)}:$deletions:::"
    }

    private fun candidatesForExport(cell: Cell, legalCell: Cell): Set<Int> =
        if (cell.isCandidateSetExplicit) cell.candidates else legalCell.candidates
}
