package com.galaxyrio.sudokusolver.domain.tutorial

import com.galaxyrio.sudokusolver.R
import com.galaxyrio.sudokusolver.domain.solver.TechniqueId
import com.galaxyrio.sudokusolver.ui.screens.tutorial.advancedLessonCopy
import com.galaxyrio.sudokusolver.ui.screens.tutorial.lessonCopy
import com.galaxyrio.sudokusolver.ui.screens.tutorial.tutorialCategories
import com.galaxyrio.sudokusolver.ui.screens.tutorial.tutorialEntry
import com.galaxyrio.sudokusolver.ui.util.nameResourceId
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.*
import org.junit.Test

class TutorialCatalogTest {
    private val english = strings("values")
    private val chinese = strings("values-zh-rCN")
    private val resourceNames = R.string::class.java.fields.associate { it.getInt(null) to it.name }

    @Test fun everyHintHasExactlyOneSameNamedEntryAndAStableWorkingRoute() {
        val entries = tutorialCategories.flatMap { it.techniques }
        assertEquals(TechniqueId.entries.size, entries.size)
        assertEquals(entries.size, entries.map { it.id }.distinct().size)
        TechniqueId.entries.forEach { technique ->
            val entry = requireNotNull(tutorialEntry(technique.tutorialId)) { technique.name }
            assertEquals(technique.name, entry.id)
            assertEquals(technique.nameResourceId(), entry.titleResource)
            assertEquals(listOf(technique), entry.solverTechniques)
            assertEquals(technique, TutorialLessons.find(entry.id)!!.technique)
        }
    }

    @Test fun allNamesAliasesRelationshipsAndLessonsArePresentInBothLanguages() {
        fun translated(id: Int) {
            val name = resourceNames.getValue(id)
            assertTrue("English: $name", english[name]?.isNotBlank() == true)
            assertTrue("Chinese: $name", chinese[name]?.isNotBlank() == true)
        }
        tutorialCategories.forEach { category ->
            translated(category.titleResource)
            category.techniques.forEach { entry ->
                translated(entry.titleResource)
                entry.aliasesResource?.let(::translated)
                entry.relationshipResource?.let(::translated)
                val lesson = TutorialLessons.find(entry.id)!!
                if (lesson.examples.firstOrNull()?.isAdvanced == false) {
                    val copy = lessonCopy(entry.technique)
                    translated(copy.first)
                    translated(copy.second)
                } else {
                    val copy = advancedLessonCopy(entry.technique)
                    listOf(copy.rule, copy.reason, copy.tip).forEach(::translated)
                }
            }
        }
        val keys = english.keys.filter { it.startsWith("tutorial_") || it == "game_hint_open_tutorial" }.toSet()
        assertEquals(keys, chinese.keys.filter { it.startsWith("tutorial_") || it == "game_hint_open_tutorial" }.toSet())
        val placeholders = Regex("%[0-9]+\\$[sd]")
        keys.forEach { key ->
            assertEquals(key, placeholders.findAll(english.getValue(key)).map { it.value }.toSet(),
                placeholders.findAll(chinese.getValue(key)).map { it.value }.toSet())
        }
    }

    @Test fun subsetsAndLockedCandidatesStayInDifferentFamilies() {
        fun family(technique: TechniqueId) = tutorialCategories.single { group -> group.techniques.any { it.technique == technique } }.id
        assertEquals(family(TechniqueId.NAKED_PAIR), family(TechniqueId.LOCKED_PAIR))
        assertEquals(family(TechniqueId.NAKED_TRIPLE), family(TechniqueId.LOCKED_TRIPLE))
        assertNotEquals(family(TechniqueId.LOCKED_PAIR), family(TechniqueId.POINTING_PAIR))
        assertEquals(family(TechniqueId.POINTING_PAIR), family(TechniqueId.CLAIMING_TRIPLE))
    }

    @Test fun previouslySavedFamilyRoutesStillOpenTheirLesson() {
        assertEquals(TechniqueId.POINTING_PAIR, TutorialLessons.find("locked_candidate")!!.technique)
        assertEquals(TechniqueId.SIMPLE_COLORING_TYPE_1, TutorialLessons.find("simple_coloring")!!.technique)
        assertEquals(TechniqueId.CELL_FORCING_CHAIN, TutorialLessons.find("cell_region_forcing_chain")!!.technique)
        assertEquals(TechniqueId.CELL_FORCING_NET, TutorialLessons.find("cell_region_forcing_net")!!.technique)
        assertNull(TutorialLessons.find("unknown"))
    }

    @Test fun hintsUseOneCanonicalNameAndTutorialsRetainAliasesWithoutConflatingHigherWings() {
        assertEquals("XY-Wing", english.getValue("technique_xy_wing"))
        assertEquals("Y-Wing", english.getValue("tutorial_alias_xy_wing"))
        assertEquals("4-Y-Wing", english.getValue("technique_wxyz_wing"))
        assertEquals("WXYZ-Wing", english.getValue("tutorial_alias_wxyz_wing"))
        assertEquals("AIC", english.getValue("technique_aic"))
        assertEquals("2-String-Kite", english.getValue("technique_two_string_kite"))
        assertEquals("3D-Medusa", english.getValue("technique_three_d_medusa"))
        assertEquals("Naked Quad", english.getValue("technique_naked_quad"))
        assertNull(tutorialEntry(TechniqueId.FIVE_Y_WING.tutorialId)!!.aliasesResource)
    }

    private fun strings(locale: String): Map<String, String> {
        val directory = listOf(File("src/main/res/$locale"), File("app/src/main/res/$locale")).first { it.isDirectory }
        return directory.listFiles()!!.filter { it.extension == "xml" }.flatMap { file ->
            val nodes = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file).getElementsByTagName("string")
            (0 until nodes.length).map { index ->
                val node = nodes.item(index)
                node.attributes.getNamedItem("name").nodeValue to node.textContent
            }
        }.toMap()
    }
}
