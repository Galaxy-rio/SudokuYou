package com.galaxyrio.sudokusolver.domain.solver

import com.galaxyrio.sudokusolver.domain.model.Sudoku
import java.util.ArrayDeque

class SimpleColoringDetector : TechniqueDetector {
    override val technique: TechniqueId = TechniqueId.SIMPLE_COLORING_TYPE_1

    override fun find(state: SolverState): SolveStep? {
        for (digit in 1..Sudoku.GRID_SIZE) {
            val graph = state.strongLinkGraph(digit)
            val remaining = graph.keys.toMutableSet()

            while (remaining.isNotEmpty()) {
                val colors = colorComponent(remaining.first(), graph)
                remaining.removeAll(colors.keys)
                if (colors.size < 3) continue

                val groups = listOf(
                    colors.filterValues { it == 0 }.keys,
                    colors.filterValues { it == 1 }.keys,
                )
                val falseGroups = groups.indices.filter { groupIndex ->
                    groups[groupIndex].any { first ->
                        groups[groupIndex].any { second -> first.sees(second) }
                    }
                }

                if (falseGroups.size == 1) {
                    val falseGroup = falseGroups.single()
                    val trueCells = groups[1 - falseGroup]
                    if (trueCells.isNotEmpty()) {
                        return SolveStep(
                            technique = TechniqueId.SIMPLE_COLORING_TYPE_2,
                            placements = trueCells.sortedBy(CellRef::index).map { cell ->
                                Placement(cell, digit)
                            },
                            eliminations = groups[falseGroup].map { cell ->
                                CandidateElimination(CandidateRef(cell, digit))
                            },
                            evidence = coloringEvidence(state, digit, colors.keys, graph),
                        )
                    }
                }

                val targets = state.candidateCells(digit)
                    .filterNot { it in colors }
                    .filter { target ->
                        groups[0].any(target::sees) && groups[1].any(target::sees)
                    }
                if (targets.isNotEmpty()) {
                    return SolveStep(
                        technique = TechniqueId.SIMPLE_COLORING_TYPE_1,
                        eliminations = targets.map { cell ->
                            CandidateElimination(CandidateRef(cell, digit))
                        },
                        evidence = coloringEvidence(state, digit, colors.keys, graph),
                    )
                }
            }
        }
        return null
    }

    private fun colorComponent(
        start: CellRef,
        graph: Map<CellRef, Set<CellRef>>,
    ): Map<CellRef, Int> {
        val colors = mutableMapOf(start to 0)
        val queue = ArrayDeque<CellRef>().apply { add(start) }
        while (queue.isNotEmpty()) {
            val cell = queue.removeFirst()
            val nextColor = 1 - colors.getValue(cell)
            graph[cell].orEmpty().forEach { peer ->
                if (peer !in colors) {
                    colors[peer] = nextColor
                    queue.addLast(peer)
                }
            }
        }
        return colors
    }

    private fun coloringEvidence(
        state: SolverState,
        digit: Int,
        cells: Set<CellRef>,
        graph: Map<CellRef, Set<CellRef>>,
    ): StepEvidence {
        val links = buildList {
            for (cell in cells.sortedBy(CellRef::index)) {
                graph[cell].orEmpty()
                    .filter { it in cells && cell.index < it.index }
                    .forEach { peer ->
                        add(
                            InferenceLink(
                                CandidateRef(cell, digit),
                                CandidateRef(peer, digit),
                                InferenceLinkType.STRONG,
                            )
                        )
                    }
            }
        }
        return StepEvidence(
            causeCells = cells,
            causeCandidates = cells.map { CandidateRef(it, digit) }.toSet(),
            focusDigits = setOf(digit),
            houses = state.strongLinks(digit)
                .filter { it.first in cells && it.second in cells }
                .map(StrongLink::house)
                .distinct(),
            links = links,
        )
    }
}
