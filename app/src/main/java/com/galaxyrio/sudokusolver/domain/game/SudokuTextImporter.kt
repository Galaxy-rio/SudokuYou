package com.galaxyrio.sudokusolver.domain.game

import com.galaxyrio.sudokusolver.domain.model.Cell
import com.galaxyrio.sudokusolver.domain.model.Sudoku
import com.galaxyrio.sudokusolver.domain.model.SudokuExportFormat

/** Parses the same interoperable text formats that [SudokuTextExporter] produces. */
object SudokuTextImporter {

    fun import(
        text: String,
        format: SudokuExportFormat,
    ): Sudoku? = runCatching {
        when (format) {
            SudokuExportFormat.SUSSER -> parseSusser(text.substringBefore(':'))
            SudokuExportFormat.MULTILINE -> parseMultiline(text)
            SudokuExportFormat.PENCILMARK -> parsePencilmark(text)
            SudokuExportFormat.SUKAKU -> parseSukaku(text)
            SudokuExportFormat.EXCEL -> parseExcel(text)
            SudokuExportFormat.OPEN_SUDOKU -> parseOpenSudoku(text)
            SudokuExportFormat.HODOKU -> parseHodoku(text)
        }
    }.getOrNull()

    private fun parseSusser(text: String): Sudoku? {
        val cells = mutableListOf<Cell>()
        var index = 0
        while (index < text.length) {
            when (val symbol = text[index]) {
                '.', '0' -> cells += Cell()
                in '1'..'9' -> cells += fixedCell(symbol.digitToInt())
                '+' -> {
                    val value = text.getOrNull(index + 1)?.digitToIntOrNull()
                        ?.takeIf { it in 1..Sudoku.GRID_SIZE }
                        ?: return null
                    cells += fixedCell(value)
                    index += 1
                }

                else -> if (!symbol.isWhitespace()) return null
            }
            index += 1
        }
        return cells.toSudokuOrNull()
    }

    private fun parseMultiline(text: String): Sudoku? {
        val rows = text.lineSequence()
            .filter { line -> line.count { it == '|' } >= 2 }
            .map { line ->
                line.filter { it == '.' || it == '0' || it in '1'..'9' }
            }
            .filter(String::isNotEmpty)
            .toList()
        if (rows.size != Sudoku.GRID_SIZE || rows.any { it.length != Sudoku.GRID_SIZE }) {
            return null
        }
        return rows.joinToString(separator = "")
            .map { symbol ->
                if (symbol == '.' || symbol == '0') Cell() else fixedCell(symbol.digitToInt())
            }
            .toSudokuOrNull()
    }

    private fun parsePencilmark(text: String): Sudoku? {
        val rows = text.lineSequence()
            .filter { line -> line.count { it == '|' } >= 2 }
            .map { line ->
                line.split('|')
                    .drop(1)
                    .dropLast(1)
                    .flatMap { group -> group.trim().split(Regex("\\s+")).filter(String::isNotEmpty) }
            }
            .filter(List<String>::isNotEmpty)
            .toList()
        if (rows.size != Sudoku.GRID_SIZE || rows.any { it.size != Sudoku.GRID_SIZE }) return null

        return rows.flatten().map { token ->
            when {
                token == "." || token == "0" -> Cell()
                token.matches(Regex("<([1-9])>")) -> fixedCell(token[1].digitToInt())
                token.matches(Regex("\\*([1-9])\\*")) -> fixedCell(token[1].digitToInt())
                token.all { it in '1'..'9' } -> Cell(
                    candidates = token.map(Char::digitToInt).toSet(),
                    isCandidateSetExplicit = true,
                )

                else -> return null
            }
        }.toSudokuOrNull()
    }

    private fun parseSukaku(text: String): Sudoku? {
        val symbols = text.filter { !it.isWhitespace() }
        if (symbols.length != Sudoku.CELL_COUNT * Sudoku.GRID_SIZE) return null

        val cells = symbols.chunked(Sudoku.GRID_SIZE).map { candidateSymbols ->
            val candidates = buildSet {
                candidateSymbols.forEachIndexed { offset, symbol ->
                    when {
                        symbol == '.' || symbol == '0' -> Unit
                        symbol == ('1' + offset) -> add(offset + 1)
                        else -> return null
                    }
                }
            }
            Cell(
                candidates = candidates,
                isCandidateSetExplicit = true,
            )
        }
        return cells.toSudokuOrNull()
    }

    private fun parseExcel(text: String): Sudoku? {
        val rows = text.lineSequence()
            .filter { it.isNotBlank() || '\t' in it }
            .map { line -> line.split('\t').take(Sudoku.GRID_SIZE) }
            .toList()
        if (rows.size != Sudoku.GRID_SIZE || rows.any { it.size > Sudoku.GRID_SIZE }) return null

        val cells = rows.flatMap { row ->
            row.map(String::trim).let { it + List(Sudoku.GRID_SIZE - it.size) { "" } }
        }.map { token ->
            when {
                token.isEmpty() || token == "." || token == "0" -> Cell()
                token.length == 1 && token[0] in '1'..'9' -> fixedCell(token[0].digitToInt())
                token.all { it in '1'..'9' } -> Cell(
                    candidates = token.map(Char::digitToInt).toSet(),
                    isCandidateSetExplicit = true,
                )

                else -> return null
            }
        }
        return cells.toSudokuOrNull()
    }

    private fun parseOpenSudoku(text: String): Sudoku? {
        val fields = text.trim().split('|')
        if (fields.size != Sudoku.CELL_COUNT * OPEN_SUDOKU_FIELDS_PER_CELL) return null

        val cells = fields.chunked(OPEN_SUDOKU_FIELDS_PER_CELL).map { cellFields ->
            if (cellFields.any { it.toIntOrNull() == null }) return null
            val value = cellFields[0].toInt()
            when (value) {
                0 -> Cell()
                in 1..Sudoku.GRID_SIZE -> fixedCell(value)
                else -> return null
            }
        }
        return cells.toSudokuOrNull()
    }

    private fun parseHodoku(text: String): Sudoku? = text
        .split(':')
        .firstNotNullOfOrNull(::parseSusser)

    private fun fixedCell(value: Int): Cell = Cell(value = value, isFixed = true)

    private fun List<Cell>.toSudokuOrNull(): Sudoku? =
        takeIf { it.size == Sudoku.CELL_COUNT }?.let(::Sudoku)

    private const val OPEN_SUDOKU_FIELDS_PER_CELL = 3
}
