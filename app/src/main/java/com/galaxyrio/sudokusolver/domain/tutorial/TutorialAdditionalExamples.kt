package com.galaxyrio.sudokusolver.domain.tutorial

import com.galaxyrio.sudokusolver.domain.solver.*

/** Additional teaching snapshots, with frozen deductions verified by TutorialAdvancedLessonTest. */
internal val additionalTutorialExamples: List<TutorialExample> = listOf(
    lastDigitExample1(),
    lastDigitExample2(),
    lockedPairExample1(),
    lockedPairExample2(),
    pointingTripleExample1(),
    pointingTripleExample2(),
    lockedTripleExample1(),
    lockedTripleExample2(),
    remotePairExample1(),
    remotePairExample2(),
    chuteRemotePairSingleExample1(),
    chuteRemotePairSingleExample2(),
    chuteRemotePairDoubleExample1(),
    chuteRemotePairDoubleExample2(),
    chuteRemotePairBonusExample1(),
    chuteRemotePairBonusExample2(),
    uniqueRectangleType1mExample1(),
    uniqueRectangleType1mExample2(),
    uniqueRectangleType4mExample1(),
    uniqueRectangleType4mExample2(),
    uniqueRectangleType5pExample1(),
    uniqueRectangleType5pExample2(),
    uniqueRectangleType6Example1(),
    uniqueRectangleType6Example2(),
    uniqueRectangleType7Example1(),
    uniqueRectangleType7Example2(),
    sueDeCoqType1Example1(),
    sueDeCoqType1Example2(),
    sueDeCoqType2Example1(),
    eightYWingExample1(),
    eightYWingExample2(),
    nineYWingExample1(),
    nineYWingExample2(),
    starfishExample1(),
    starfishExample2(),
    whaleExample1(),
    whaleExample2(),
    leviathanExample1(),
    leviathanExample2(),
    nishioForcingNetExample1(),
    nishioForcingNetExample2(),
    digitForcingChainExample1(),
    digitForcingChainExample2(),
    digitForcingNetExample1(),
    digitForcingNetExample2(),
)

// Locally constructed nine-cell wing
private fun lastDigitExample1() = additionalExample(
    givens = "000000000078352960625017038259786103086503729730291685561870392397625810802139576",
    solution = "913468257478352961625917438259786143186543729734291685561874392397625814842139576",
    candidates = "75090c7c143s0a0o200900000000000000090000007c0000080000000000000000000800090000000800000000000008000000000000000000000008000000000000000000000008000800000000000000",
    step = SolveStep(
        technique = TechniqueId.LAST_DIGIT,
        placements = listOf(Placement(cell = cell(34), digit = 4)),
        evidence = StepEvidence(
            causeCells = setOf(cell(34)),
            causeCandidates = setOf(candidate(34, 4)),
            focusDigits = setOf(4),
            houses = listOf(HouseRef(type = HouseType.ROW, index = 3)),
        ),
    ),
)

// Locally constructed chute remote pair
private fun lastDigitExample2() = additionalExample(
    givens = "003468207478302061620017438259786143186543729734201685061874392307625814842139576",
    solution = "913468257478352961625917438259786143186543729734291685561874392397625814842139576",
    candidates = "7k7500000000000g00000000007k0074000000000g7400000000000000000000000000000000000000000000000000000074000000000g0000000000000000007400000000000000000000000000000000",
    step = SolveStep(
        technique = TechniqueId.LAST_DIGIT,
        placements = listOf(Placement(cell = cell(49), digit = 9)),
        evidence = StepEvidence(
            causeCells = setOf(cell(49)),
            causeCandidates = setOf(candidate(49, 9)),
            focusDigits = setOf(9),
            houses = listOf(HouseRef(type = HouseType.ROW, index = 5)),
        ),
    ),
)

// https://sudoku.coach/en/learn/locked-candidate
private fun lockedPairExample1() = additionalExample(
    givens = "900060257408000900000907000000700043106000020004000600060804000000005800000009506",
    solution = "913468257478352961625917438259786143186543729734291685561874392397625814842139576",
    candidates = "0005050d003p000000002f000n0n070011011i0n0n004f000d4l3t42b67m00b70z01000000d0000sbg041s00b45yd2000nbb0700chb52e009j001z001x91771y9b93131z0000917f5i5r1z071z00001x00",
    step = SolveStep(
        technique = TechniqueId.LOCKED_PAIR,
        eliminations = listOf(
            CandidateElimination(candidate = candidate(3, 1)),
            CandidateElimination(candidate = candidate(3, 3)),
            CandidateElimination(candidate = candidate(5, 1)),
            CandidateElimination(candidate = candidate(5, 3)),
            CandidateElimination(candidate = candidate(10, 1)),
            CandidateElimination(candidate = candidate(10, 3)),
            CandidateElimination(candidate = candidate(18, 3)),
            CandidateElimination(candidate = candidate(19, 1)),
            CandidateElimination(candidate = candidate(19, 3)),
            CandidateElimination(candidate = candidate(20, 1)),
            CandidateElimination(candidate = candidate(20, 3)),
        ),
        evidence = StepEvidence(
            causeCells = setOf(cell(1), cell(2)),
            causeCandidates = setOf(candidate(1, 1), candidate(1, 3), candidate(2, 1), candidate(2, 3)),
            focusDigits = setOf(1, 3),
            houses = listOf(HouseRef(type = HouseType.ROW, index = 0), HouseRef(type = HouseType.BOX, index = 0)),
        ),
    ),
)

// https://sudoku.coach/en/learn/locked-candidate
private fun lockedPairExample2() = additionalExample(
    givens = "000000000004000908020006050700300001003900640100800230000400000200009470000005029",
    solution = "697548312514273968328196754762354891853921647149867235971432586285619473436785129",
    candidates = "c4dxdt2bdb5r1x0x321g39002b2f1z000x00as00ch1tct001x002400c8c2001m0a40ao00404000002b1v000028008o8g003c20000028c4dxdt006f5j454h1g00514x0x4l0000001g4s6l692p6d003p0000",
    step = SolveStep(
        technique = TechniqueId.LOCKED_PAIR,
        eliminations = listOf(
            CandidateElimination(candidate = candidate(40, 5)),
            CandidateElimination(candidate = candidate(44, 5)),
            CandidateElimination(candidate = candidate(28, 5)),
            CandidateElimination(candidate = candidate(28, 8)),
            CandidateElimination(candidate = candidate(29, 5)),
            CandidateElimination(candidate = candidate(29, 8)),
            CandidateElimination(candidate = candidate(46, 5)),
            CandidateElimination(candidate = candidate(47, 5)),
        ),
        evidence = StepEvidence(
            causeCells = setOf(cell(36), cell(37)),
            causeCandidates = setOf(candidate(36, 5), candidate(36, 8), candidate(37, 5), candidate(37, 8)),
            focusDigits = setOf(5, 8),
            houses = listOf(HouseRef(type = HouseType.ROW, index = 4), HouseRef(type = HouseType.BOX, index = 3)),
        ),
    ),
)

// https://sudoku.coach/en/learn/locked-candidate
private fun pointingTripleExample1() = additionalExample(
    givens = "000000000004000908020006050700300001003900640100800230000400000200009470000005029",
    solution = "697548312514273968328196754762354891853921647149867235971432586285619473436785129",
    candidates = "c4dxdt2bdb5r1x0x321g39002b2f1z000x00as00ch1tct001x002400c8c2001m0a40ao00404000002b1v000028008o8g003c20000028c4dxdt006f5j454h1g00514x0x4l0000001g4s6d692p6d003p0000",
    step = SolveStep(
        technique = TechniqueId.POINTING_TRIPLE,
        eliminations = listOf(
            CandidateElimination(candidate = candidate(4, 5)),
            CandidateElimination(candidate = candidate(13, 5)),
        ),
        evidence = StepEvidence(
            causeCells = setOf(cell(31), cell(40), cell(49)),
            causeCandidates = setOf(candidate(31, 5), candidate(40, 5), candidate(49, 5)),
            focusDigits = setOf(5),
            houses = listOf(HouseRef(type = HouseType.BOX, index = 4), HouseRef(type = HouseType.COLUMN, index = 4)),
        ),
    ),
)

// https://sudoku.coach/en/learn/x-wing
private fun pointingTripleExample2() = additionalExample(
    givens = "000000000013027060790800200006003709000050000005900000000000000371600800060040100",
    solution = "682319574513427968794865213146283759927154386835976421458731692371692845269548137",
    candidates = "564a3u0t858p0s65654800000o00007s004800000800111l000t0t3v3u000b3l00004b00az3ycq23004r183z4v3v3y0000694r183z4vbe4aay2fclb78s9q3i00000000747m007u0qb600aq2e00b6009i2e",
    step = SolveStep(
        technique = TechniqueId.POINTING_TRIPLE,
        eliminations = listOf(
            CandidateElimination(candidate = candidate(60, 4)),
            CandidateElimination(candidate = candidate(61, 4)),
            CandidateElimination(candidate = candidate(62, 4)),
        ),
        evidence = StepEvidence(
            causeCells = setOf(cell(54), cell(55), cell(56)),
            causeCandidates = setOf(candidate(54, 4), candidate(55, 4), candidate(56, 4)),
            focusDigits = setOf(4),
            houses = listOf(HouseRef(type = HouseType.BOX, index = 6), HouseRef(type = HouseType.ROW, index = 6)),
        ),
    ),
)

// https://sudoku.coach/en/learn/x-wing
private fun lockedTripleExample1() = additionalExample(
    givens = "000160009090005100020040000100004060007000000600000503430706008700400000000090300",
    solution = "374168259896275134521349876183954762957632481642817593435726918719483625268591347",
    candidates = "44604c00005i5m66003o004s3q5i00005q2y440051as00ck685w340040baba5y00ci001uba4800c647avayaz0b003sayaq5fcj00cr0000007n000j00767n00004xc300473r827n1f424x4z42003n002j3f",
    step = SolveStep(
        technique = TechniqueId.LOCKED_TRIPLE,
        eliminations = listOf(
            CandidateElimination(candidate = candidate(36, 3)),
            CandidateElimination(candidate = candidate(36, 5)),
            CandidateElimination(candidate = candidate(36, 8)),
            CandidateElimination(candidate = candidate(72, 5)),
            CandidateElimination(candidate = candidate(72, 8)),
            CandidateElimination(candidate = candidate(1, 5)),
            CandidateElimination(candidate = candidate(1, 8)),
            CandidateElimination(candidate = candidate(2, 3)),
            CandidateElimination(candidate = candidate(2, 5)),
            CandidateElimination(candidate = candidate(2, 8)),
            CandidateElimination(candidate = candidate(11, 3)),
            CandidateElimination(candidate = candidate(11, 8)),
            CandidateElimination(candidate = candidate(20, 3)),
            CandidateElimination(candidate = candidate(20, 5)),
            CandidateElimination(candidate = candidate(20, 8)),
        ),
        evidence = StepEvidence(
            causeCells = setOf(cell(0), cell(9), cell(18)),
            causeCandidates = setOf(
                candidate(0, 3),
                candidate(0, 5),
                candidate(0, 8),
                candidate(9, 3),
                candidate(9, 8),
                candidate(18, 3),
                candidate(18, 5),
                candidate(18, 8),
            ),
            focusDigits = setOf(3, 5, 8),
            houses = listOf(HouseRef(type = HouseType.COLUMN, index = 0), HouseRef(type = HouseType.BOX, index = 0)),
        ),
    ),
)

// https://sudoku.coach/en/learn/x-wing
private fun lockedTripleExample2() = additionalExample(
    givens = "600000100000098030000075006706009000003040000010050780001260500400000020030001000",
    solution = "679423158152698437348175296726819345583742619914356782891264573465937821237581964",
    candidates = "00d6d60c060e009kd60j2i2i1500000a002iavayay0d0000ay7c00004a003p3r000e0p0vb6b60069002q828h7n76007e10001200007iaocg000000240094cs00dscwd03o1wbo00clb600cyd43k00bsa0co",
    step = SolveStep(
        technique = TechniqueId.LOCKED_TRIPLE,
        eliminations = listOf(
            CandidateElimination(candidate = candidate(1, 2)),
            CandidateElimination(candidate = candidate(1, 4)),
            CandidateElimination(candidate = candidate(2, 2)),
            CandidateElimination(candidate = candidate(2, 4)),
            CandidateElimination(candidate = candidate(7, 4)),
            CandidateElimination(candidate = candidate(8, 2)),
            CandidateElimination(candidate = candidate(8, 4)),
            CandidateElimination(candidate = candidate(12, 4)),
            CandidateElimination(candidate = candidate(21, 3)),
            CandidateElimination(candidate = candidate(21, 4)),
        ),
        evidence = StepEvidence(
            causeCells = setOf(cell(3), cell(4), cell(5)),
            causeCandidates = setOf(
                candidate(3, 3),
                candidate(3, 4),
                candidate(4, 2),
                candidate(4, 3),
                candidate(5, 2),
                candidate(5, 3),
                candidate(5, 4),
            ),
            focusDigits = setOf(2, 3, 4),
            houses = listOf(HouseRef(type = HouseType.ROW, index = 0), HouseRef(type = HouseType.BOX, index = 1)),
        ),
    ),
)

// https://sudoku.coach/en/learn/swordfish
private fun remotePairExample1() = additionalExample(
    givens = "875020013249100780316780290001000027730251960962847531120370059003090170097010340",
    solution = "875629413249135786316784295451963827738251964962847531124378659583496172697512348",
    candidates = "00000088008814000000000000100k00001c00000000000o00000o0o40008010783s000000003s00000000003s00000000000000000000003s00004o4g00001k40001k004i00004i1c00001c003m00003m",
    step = SolveStep(
        technique = TechniqueId.REMOTE_PAIR,
        eliminations = listOf(CandidateElimination(candidate = candidate(60, 8))),
        evidence = StepEvidence(
            causeCells = setOf(cell(33), cell(44), cell(38), cell(56)),
            causeCandidates = setOf(
                candidate(33, 4),
                candidate(33, 8),
                candidate(44, 4),
                candidate(44, 8),
                candidate(38, 4),
                candidate(38, 8),
                candidate(56, 4),
                candidate(56, 8),
            ),
            focusDigits = setOf(4, 8),
            houses = listOf(
                HouseRef(type = HouseType.BOX, index = 5),
                HouseRef(type = HouseType.ROW, index = 4),
                HouseRef(type = HouseType.COLUMN, index = 2),
            ),
            links = listOf(
                InferenceLink(from = candidate(33, 4), to = candidate(33, 8), type = InferenceLinkType.DUAL),
                InferenceLink(from = candidate(44, 4), to = candidate(44, 8), type = InferenceLinkType.DUAL),
                InferenceLink(from = candidate(38, 4), to = candidate(38, 8), type = InferenceLinkType.DUAL),
                InferenceLink(from = candidate(56, 4), to = candidate(56, 8), type = InferenceLinkType.DUAL),
                InferenceLink(from = candidate(33, 4), to = candidate(44, 4), type = InferenceLinkType.WEAK),
                InferenceLink(from = candidate(44, 8), to = candidate(38, 8), type = InferenceLinkType.WEAK),
                InferenceLink(from = candidate(38, 4), to = candidate(56, 4), type = InferenceLinkType.WEAK),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/jellyfish
private fun remotePairExample2() = additionalExample(
    givens = "098506400245019608000080050830190006600000000050670000500042890920800160083961205",
    solution = "398526471245719638176483952837194526619235784452678319561342897924857163783961245",
    candidates = "1x0000000600001z1v0000001w0000001w001x2p2p0a002490008z00002200000o282200001t950a0m4c9g5p9509007f00003w783z77000x0x1w000000001w000020000k2c000024200000000000002000",
    step = SolveStep(
        technique = TechniqueId.REMOTE_PAIR,
        eliminations = listOf(
            CandidateElimination(candidate = candidate(8, 7)),
            CandidateElimination(candidate = candidate(26, 7)),
            CandidateElimination(candidate = candidate(79, 7)),
        ),
        evidence = StepEvidence(
            causeCells = setOf(cell(16), cell(12), cell(57), cell(62)),
            causeCandidates = setOf(
                candidate(16, 3),
                candidate(16, 7),
                candidate(12, 3),
                candidate(12, 7),
                candidate(57, 3),
                candidate(57, 7),
                candidate(62, 3),
                candidate(62, 7),
            ),
            focusDigits = setOf(3, 7),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 1),
                HouseRef(type = HouseType.COLUMN, index = 3),
                HouseRef(type = HouseType.ROW, index = 6),
            ),
            links = listOf(
                InferenceLink(from = candidate(16, 3), to = candidate(16, 7), type = InferenceLinkType.DUAL),
                InferenceLink(from = candidate(12, 3), to = candidate(12, 7), type = InferenceLinkType.DUAL),
                InferenceLink(from = candidate(57, 3), to = candidate(57, 7), type = InferenceLinkType.DUAL),
                InferenceLink(from = candidate(62, 3), to = candidate(62, 7), type = InferenceLinkType.DUAL),
                InferenceLink(from = candidate(16, 3), to = candidate(12, 3), type = InferenceLinkType.WEAK),
                InferenceLink(from = candidate(12, 7), to = candidate(57, 7), type = InferenceLinkType.WEAK),
                InferenceLink(from = candidate(57, 3), to = candidate(62, 3), type = InferenceLinkType.WEAK),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/locked-candidate
private fun chuteRemotePairSingleExample1() = additionalExample(
    givens = "400701008100052374570483019604807000750309080009005007005008746047506820000074005",
    solution = "463791258198652374572483619624817593751369482839245167315928746947536821286174935",
    candidates = "0086120080001e1c0000bk4g80000000000000000y0000000y00000007000003007k7k070000030014001400033q3r000z17001510007a0700777b00000000780000007900000005au4n4n770000757800",
    step = SolveStep(
        technique = TechniqueId.CHUTE_REMOTE_PAIR_SINGLE,
        eliminations = listOf(CandidateElimination(candidate = candidate(28, 1))),
        evidence = StepEvidence(
            causeCells = setOf(cell(31), cell(38), cell(51), cell(52), cell(53)),
            causeCandidates = setOf(candidate(31, 1), candidate(31, 2), candidate(38, 1), candidate(38, 2)),
            focusDigits = setOf(1, 2),
            houses = listOf(
                HouseRef(type = HouseType.BOX, index = 3),
                HouseRef(type = HouseType.BOX, index = 4),
                HouseRef(type = HouseType.BOX, index = 5),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/x-wing
private fun chuteRemotePairSingleExample2() = additionalExample(
    givens = "600009000013027960794860200006083759907050000005970000000000000371690800060040100",
    solution = "682319574513427968794865213146283759927154386835976421458731692371692845269548137",
    candidates = "00423m0s05000o5l614000000o000000004800000000000h00050l0b0a00030000000000003q00030014183z4v3n3q00000014183z4v4a4aaq2c05431g923a00000000000i000a0q4200aq2c004200922e",
    step = SolveStep(
        technique = TechniqueId.CHUTE_REMOTE_PAIR_SINGLE,
        eliminations = listOf(
            CandidateElimination(candidate = candidate(3, 4)),
            CandidateElimination(candidate = candidate(17, 4)),
        ),
        evidence = StepEvidence(
            causeCells = setOf(cell(6), cell(12), cell(18), cell(19), cell(20)),
            causeCandidates = setOf(candidate(6, 4), candidate(6, 5), candidate(12, 4), candidate(12, 5)),
            focusDigits = setOf(4, 5),
            houses = listOf(
                HouseRef(type = HouseType.BOX, index = 0),
                HouseRef(type = HouseType.BOX, index = 1),
                HouseRef(type = HouseType.BOX, index = 2),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/skyscraper
private fun chuteRemotePairDoubleExample1() = additionalExample(
    givens = "004178020529643178718592364001927406270064010496015702140209607902736041007401290",
    solution = "364178925529643178718592364831927456275364819496815732143259687982736541657481293",
    candidates = "1010000000007k007k0000000000000000000000000000000000003o4400000000000k0000000k3o0000b4007o0000003o0000003o000000040040000040000040000000004000004g4w0000400000000k",
    step = SolveStep(
        technique = TechniqueId.CHUTE_REMOTE_PAIR_DOUBLE,
        eliminations = listOf(CandidateElimination(candidate = candidate(80, 5))),
        evidence = StepEvidence(
            causeCells = setOf(cell(69), cell(76), cell(54), cell(55), cell(56)),
            causeCandidates = setOf(candidate(69, 5), candidate(69, 8), candidate(76, 5), candidate(76, 8)),
            focusDigits = setOf(5, 8),
            houses = listOf(
                HouseRef(type = HouseType.BOX, index = 6),
                HouseRef(type = HouseType.BOX, index = 7),
                HouseRef(type = HouseType.BOX, index = 8),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/x-chain
private fun chuteRemotePairDoubleExample2() = additionalExample(
    givens = "104305080502004319003201045039508174801403590000019038916857423000932861328146957",
    solution = "164395782582674319793281645239568174871423596645719238916857423457932861328146957",
    candidates = "009s000080002q000y004g002o68000000002oao0000ao002o00000y0000000y00000000001s00002q0000000y2y0o282o00000y0000000000000000000000200o28000000000000000000000000000000",
    step = SolveStep(
        technique = TechniqueId.CHUTE_REMOTE_PAIR_DOUBLE,
        eliminations = listOf(CandidateElimination(candidate = candidate(48, 6))),
        evidence = StepEvidence(
            causeCells = setOf(cell(31), cell(51), cell(36), cell(37), cell(38)),
            causeCandidates = setOf(candidate(31, 2), candidate(31, 6), candidate(51, 2), candidate(51, 6)),
            focusDigits = setOf(2, 6),
            houses = listOf(
                HouseRef(type = HouseType.BOX, index = 3),
                HouseRef(type = HouseType.BOX, index = 4),
                HouseRef(type = HouseType.BOX, index = 5),
            ),
        ),
    ),
)

// Locally constructed chute remote pair
private fun chuteRemotePairBonusExample1() = additionalExample(
    givens = "003468207478302061620017438259786143186543729734201685061874392307625814842139576",
    solution = "913468257478352961625917438259786143186543729734291685561874392397625814842139576",
    candidates = "7k7500000000000g00000000007k0074000000000g7400000000000000000000000000000000000000000000000000000074000000000g0000000000000000007400000000000000000000000000000000",
    step = SolveStep(
        technique = TechniqueId.CHUTE_REMOTE_PAIR_BONUS,
        eliminations = listOf(CandidateElimination(candidate = candidate(1, 9))),
        evidence = StepEvidence(
            causeCells = setOf(cell(0), cell(13), cell(24), cell(25), cell(26)),
            causeCandidates = setOf(candidate(0, 5), candidate(0, 9), candidate(13, 5), candidate(13, 9)),
            focusDigits = setOf(5, 9),
            houses = listOf(
                HouseRef(type = HouseType.BOX, index = 0),
                HouseRef(type = HouseType.BOX, index = 1),
                HouseRef(type = HouseType.BOX, index = 2),
            ),
        ),
    ),
)

// Locally constructed chute remote pair
private fun chuteRemotePairBonusExample2() = additionalExample(
    givens = "003468207478302061620017438209786143186543729734201685061874392397625814842139576",
    solution = "913468257478352961625917438259786143186543729734291685561874392397625814842139576",
    candidates = "7k0h00000000000g00000000007k0074000000000g740000000000000g000000000000000000000000000000000000000074000000000g0000000000000000000000000000000000000000000000000000",
    step = SolveStep(
        technique = TechniqueId.CHUTE_REMOTE_PAIR_BONUS,
        eliminations = listOf(CandidateElimination(candidate = candidate(1, 5))),
        evidence = StepEvidence(
            causeCells = setOf(cell(0), cell(13), cell(24), cell(25), cell(26)),
            causeCandidates = setOf(candidate(0, 5), candidate(0, 9), candidate(13, 5), candidate(13, 9)),
            focusDigits = setOf(5, 9),
            houses = listOf(
                HouseRef(type = HouseType.BOX, index = 0),
                HouseRef(type = HouseType.BOX, index = 1),
                HouseRef(type = HouseType.BOX, index = 2),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/swordfish
private fun uniqueRectangleType1mExample1() = additionalExample(
    givens = "428165739019804652560020481040006200200400006006012040635201890192008360874693125",
    solution = "428165739319874652567329481941536278253487916786912543635241897192758364874693125",
    candidates = "0000000000000000001w0000001w0000000000001s90008w0000008w00059g5w00001t3o004005005g8w7k1t008w40009000007k003o000000002000000020000000282800000020000000000000000000",
    step = SolveStep(
        technique = TechniqueId.UNIQUE_RECTANGLE_TYPE_1M,
        eliminations = listOf(CandidateElimination(candidate = candidate(67, 7))),
        evidence = StepEvidence(
            causeCells = setOf(cell(58), cell(62), cell(67), cell(71)),
            causeCandidates = setOf(
                candidate(58, 4),
                candidate(58, 7),
                candidate(62, 4),
                candidate(62, 7),
                candidate(67, 7),
                candidate(71, 4),
                candidate(71, 7),
            ),
            focusDigits = setOf(4, 7),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 6),
                HouseRef(type = HouseType.ROW, index = 7),
                HouseRef(type = HouseType.COLUMN, index = 4),
                HouseRef(type = HouseType.COLUMN, index = 8),
                HouseRef(type = HouseType.BOX, index = 7),
                HouseRef(type = HouseType.BOX, index = 8),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/finned-x-wing
private fun uniqueRectangleType1mExample2() = additionalExample(
    givens = "364295187820007640001600023682501700407029006903760002248956371030002060006003200",
    solution = "364295187829317645571648923682531794417829536953764812248956371735182469196473258",
    candidates = "00000000000000000000007k05050000007k288w00003s3s7k0000000000000c0000787c000h003o0000400k00000h0000003s3s0h0000000000000000000029007k3t5d007s00bc298w003t5d00007kbc",
    step = SolveStep(
        technique = TechniqueId.UNIQUE_RECTANGLE_TYPE_1M,
        eliminations = listOf(CandidateElimination(candidate = candidate(43, 5))),
        evidence = StepEvidence(
            causeCells = setOf(cell(37), cell(43), cell(46), cell(52)),
            causeCandidates = setOf(
                candidate(37, 1),
                candidate(37, 5),
                candidate(43, 5),
                candidate(46, 1),
                candidate(46, 5),
                candidate(52, 1),
                candidate(52, 5),
            ),
            focusDigits = setOf(1, 5),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 4),
                HouseRef(type = HouseType.ROW, index = 5),
                HouseRef(type = HouseType.COLUMN, index = 1),
                HouseRef(type = HouseType.COLUMN, index = 7),
                HouseRef(type = HouseType.BOX, index = 3),
                HouseRef(type = HouseType.BOX, index = 5),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/locked-candidate
private fun uniqueRectangleType4mExample1() = additionalExample(
    givens = "000548002004200908020096054762354891003900647149867235000400580205009470400005129",
    solution = "697548312514273968328196754762354891853921647149867235971432586285619473436785129",
    candidates = "84912p0000001w0x001g2d00001x05000x003o005d1t00001w000000000000000000000040400000030300000000000000000000000084912p001y07000010003p000x3p00000010005g682o5g00000000",
    step = SolveStep(
        technique = TechniqueId.UNIQUE_RECTANGLE_TYPE_4M,
        eliminations = listOf(CandidateElimination(candidate = candidate(59, 1))),
        evidence = StepEvidence(
            causeCells = setOf(cell(40), cell(41), cell(58), cell(59)),
            causeCandidates = setOf(
                candidate(40, 1),
                candidate(40, 2),
                candidate(41, 1),
                candidate(41, 2),
                candidate(58, 2),
                candidate(58, 3),
                candidate(58, 7),
                candidate(59, 1),
                candidate(59, 2),
                candidate(59, 3),
            ),
            focusDigits = setOf(1, 2),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 4),
                HouseRef(type = HouseType.ROW, index = 6),
                HouseRef(type = HouseType.COLUMN, index = 4),
                HouseRef(type = HouseType.COLUMN, index = 5),
                HouseRef(type = HouseType.BOX, index = 4),
                HouseRef(type = HouseType.BOX, index = 7),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/locked-candidate
private fun uniqueRectangleType4mExample2() = additionalExample(
    givens = "400701008100052374570483019604807000750309080009005007005008746047506820000074005",
    solution = "463791258198652374572483619624817593751369482839245167315928746947536821286174935",
    candidates = "0086120080001e1c0000bk4g80000000000000000y0000000y00000007000003007k7k070000030014001400033q3r000z0b001510007a0700777b00000000780000007900000005au4n4n770000757800",
    step = SolveStep(
        technique = TechniqueId.UNIQUE_RECTANGLE_TYPE_4M,
        eliminations = listOf(CandidateElimination(candidate = candidate(51, 6))),
        evidence = StepEvidence(
            causeCells = setOf(cell(40), cell(42), cell(49), cell(51)),
            causeCandidates = setOf(
                candidate(40, 4),
                candidate(40, 6),
                candidate(42, 4),
                candidate(42, 6),
                candidate(49, 1),
                candidate(49, 2),
                candidate(49, 4),
                candidate(51, 1),
                candidate(51, 4),
                candidate(51, 6),
            ),
            focusDigits = setOf(4, 6),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 4),
                HouseRef(type = HouseType.ROW, index = 5),
                HouseRef(type = HouseType.COLUMN, index = 4),
                HouseRef(type = HouseType.COLUMN, index = 6),
                HouseRef(type = HouseType.BOX, index = 4),
                HouseRef(type = HouseType.BOX, index = 5),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/unique-rectangle
private fun uniqueRectangleType5pExample1() = additionalExample(
    givens = "002037004903401287407092310005086400708320001000900008209048100004003800830009040",
    solution = "182637954963451287457892316395186472748325691621974538279548163514763829836219745",
    candidates = "1d41004g0000808g00001c00001c000000000040004w000000001c0576001t000000927a007c0000000o8g8g00110a0x001t0o282e0000340034000000381g1d3500372p0000aa8200000x1e350034001e",
    step = SolveStep(
        technique = TechniqueId.UNIQUE_RECTANGLE_TYPE_5P,
        eliminations = listOf(CandidateElimination(candidate = candidate(52, 5))),
        evidence = StepEvidence(
            causeCells = setOf(cell(6), cell(7), cell(42), cell(43)),
            causeCandidates = setOf(
                candidate(6, 6),
                candidate(6, 9),
                candidate(7, 5),
                candidate(7, 6),
                candidate(7, 9),
                candidate(42, 5),
                candidate(42, 6),
                candidate(42, 9),
                candidate(43, 5),
                candidate(43, 6),
                candidate(43, 9),
            ),
            focusDigits = setOf(6, 9),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 0),
                HouseRef(type = HouseType.ROW, index = 4),
                HouseRef(type = HouseType.COLUMN, index = 6),
                HouseRef(type = HouseType.COLUMN, index = 7),
                HouseRef(type = HouseType.BOX, index = 2),
                HouseRef(type = HouseType.BOX, index = 5),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/unique-rectangle
private fun uniqueRectangleType5pExample2() = additionalExample(
    givens = "607000042840600000020405670006008004102340060004060130400006000260904007070000496",
    solution = "657193842841672359923485671736518924182349765594267138419756283268934517375821496",
    candidates = "007p003lat05b8000000007p001u1u7o0h7p78007900at000000at9g7o002929008y0i0000b40000008wcw00b49cao002a008y0000b4007pb95s5w000m420l0000450044000k41000k0044434705000000",
    step = SolveStep(
        technique = TechniqueId.UNIQUE_RECTANGLE_TYPE_5P,
        eliminations = listOf(CandidateElimination(candidate = candidate(42, 5))),
        evidence = StepEvidence(
            causeCells = setOf(cell(37), cell(44), cell(46), cell(53)),
            causeCandidates = setOf(
                candidate(37, 5),
                candidate(37, 8),
                candidate(37, 9),
                candidate(44, 5),
                candidate(44, 8),
                candidate(44, 9),
                candidate(46, 8),
                candidate(46, 9),
                candidate(53, 5),
                candidate(53, 8),
                candidate(53, 9),
            ),
            focusDigits = setOf(8, 9),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 4),
                HouseRef(type = HouseType.ROW, index = 5),
                HouseRef(type = HouseType.COLUMN, index = 1),
                HouseRef(type = HouseType.COLUMN, index = 8),
                HouseRef(type = HouseType.BOX, index = 3),
                HouseRef(type = HouseType.BOX, index = 5),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/two-string-kite
private fun uniqueRectangleType6Example1() = additionalExample(
    givens = "487095013392080000651030000769040000815376942234908000073829460026407000948063027",
    solution = "487695213392184756651732894769241538815376942234958671173829465526417389948563127",
    candidates = "0000000y00000y00000000002p000934281k0000001u000a5eaoaw0000000j0003453o41000000000000000000000000000h0035281d0h000000000000000h0h0000000h003oasao0000000h00000h0000",
    step = SolveStep(
        technique = TechniqueId.UNIQUE_RECTANGLE_TYPE_6,
        eliminations = listOf(
            CandidateElimination(candidate = candidate(26, 9)),
            CandidateElimination(candidate = candidate(70, 9)),
            CandidateElimination(candidate = candidate(25, 8)),
            CandidateElimination(candidate = candidate(71, 8)),
        ),
        evidence = StepEvidence(
            causeCells = setOf(cell(25), cell(26), cell(70), cell(71)),
            causeCandidates = setOf(
                candidate(25, 8),
                candidate(25, 9),
                candidate(26, 8),
                candidate(26, 9),
                candidate(70, 8),
                candidate(70, 9),
                candidate(71, 8),
                candidate(71, 9),
            ),
            focusDigits = setOf(8, 9),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 2),
                HouseRef(type = HouseType.ROW, index = 7),
                HouseRef(type = HouseType.COLUMN, index = 7),
                HouseRef(type = HouseType.COLUMN, index = 8),
                HouseRef(type = HouseType.BOX, index = 2),
                HouseRef(type = HouseType.BOX, index = 8),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/y-wing
private fun uniqueRectangleType6Example2() = additionalExample(
    givens = "904000180300100904610000702200081509849050210531200807400912378193876425700500691",
    solution = "954723186327168954618495732276381549849657213531249867465912378193876425782534691",
    candidates = "002a002o062c000010005u5u000y5s001c000000400c7cbc000k00002o2o0c0000000c000000002o001w00001000000000887c001400001c1c000000000000000000000000000000003m3m000c0c000000",
    step = SolveStep(
        technique = TechniqueId.UNIQUE_RECTANGLE_TYPE_6,
        eliminations = listOf(
            CandidateElimination(candidate = candidate(23, 9)),
            CandidateElimination(candidate = candidate(49, 9)),
            CandidateElimination(candidate = candidate(22, 4)),
            CandidateElimination(candidate = candidate(50, 4)),
        ),
        evidence = StepEvidence(
            causeCells = setOf(cell(22), cell(23), cell(49), cell(50)),
            causeCandidates = setOf(
                candidate(22, 4),
                candidate(22, 9),
                candidate(23, 4),
                candidate(23, 9),
                candidate(49, 4),
                candidate(49, 9),
                candidate(50, 4),
                candidate(50, 9),
            ),
            focusDigits = setOf(4, 9),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 2),
                HouseRef(type = HouseType.ROW, index = 5),
                HouseRef(type = HouseType.COLUMN, index = 4),
                HouseRef(type = HouseType.COLUMN, index = 5),
                HouseRef(type = HouseType.BOX, index = 1),
                HouseRef(type = HouseType.BOX, index = 4),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/locked-candidate
private fun uniqueRectangleType7Example1() = additionalExample(
    givens = "000756381567030924318294576895403000003080400000900835032010040450372008781649253",
    solution = "249756381567138924318294576895463712123587469674921835932815647456372198781649253",
    candidates = "760a7c0000000000000000003l003l000000000000000000000000000000000y002p0x1u0z1u000h002900818y0z2214000y1t0000008000004000402o008w0000800000000x8100000000000000000000",
    step = SolveStep(
        technique = TechniqueId.UNIQUE_RECTANGLE_TYPE_7,
        eliminations = listOf(CandidateElimination(candidate = candidate(69, 6))),
        evidence = StepEvidence(
            causeCells = setOf(cell(33), cell(34), cell(69), cell(70)),
            causeCandidates = setOf(
                candidate(33, 1),
                candidate(33, 6),
                candidate(34, 1),
                candidate(34, 6),
                candidate(69, 1),
                candidate(69, 6),
                candidate(70, 1),
                candidate(70, 6),
            ),
            focusDigits = setOf(1, 6),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 3),
                HouseRef(type = HouseType.ROW, index = 7),
                HouseRef(type = HouseType.COLUMN, index = 6),
                HouseRef(type = HouseType.COLUMN, index = 7),
                HouseRef(type = HouseType.BOX, index = 5),
                HouseRef(type = HouseType.BOX, index = 8),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/locked-candidate
private fun uniqueRectangleType7Example2() = additionalExample(
    givens = "841063200206051000507004100000400018168072004304010020000007800085149032000600000",
    solution = "841763259296851473537294186972436518168572394354918627419327865685149732723685941",
    candidates = "0000008w0000009c9c007800cg000094co1w007800aqaq0000bk108w2a7600781cac00000000007o00007o7k00002800b4004wa800a888037a0m0600008o8h2o00000000002o0000941v7a003q409k9k9d",
    step = SolveStep(
        technique = TechniqueId.UNIQUE_RECTANGLE_TYPE_7,
        eliminations = listOf(CandidateElimination(candidate = candidate(74, 2))),
        evidence = StepEvidence(
            causeCells = setOf(cell(56), cell(58), cell(74), cell(76)),
            causeCandidates = setOf(
                candidate(56, 2),
                candidate(56, 3),
                candidate(58, 2),
                candidate(58, 3),
                candidate(74, 2),
                candidate(74, 3),
                candidate(74, 9),
                candidate(76, 2),
                candidate(76, 3),
            ),
            focusDigits = setOf(2, 3),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 6),
                HouseRef(type = HouseType.ROW, index = 8),
                HouseRef(type = HouseType.COLUMN, index = 2),
                HouseRef(type = HouseType.COLUMN, index = 4),
                HouseRef(type = HouseType.BOX, index = 6),
                HouseRef(type = HouseType.BOX, index = 7),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/finned-swordfish
private fun sueDeCoqType1Example1() = additionalExample(
    givens = "700090600006700009059106370000870060070600050163245897001908706690307082007060900",
    solution = "782593641316784529459126378524879163978631254163245897241958736695317482837462915",
    candidates = "003z3u0o000e000b493y3z0000460e0p0b003u0000003m0000003s7u0a0q0000790b0005ay003m0005790b000d0000000000000000000u0e00000i00000c0000000o000h000p00003o3q000o000b00050h",
    step = SolveStep(
        technique = TechniqueId.SUE_DE_COQ_TYPE_1,
        eliminations = listOf(
            CandidateElimination(candidate = candidate(1, 2)),
            CandidateElimination(candidate = candidate(1, 4)),
            CandidateElimination(candidate = candidate(10, 2)),
            CandidateElimination(candidate = candidate(10, 4)),
            CandidateElimination(candidate = candidate(54, 3)),
        ),
        evidence = StepEvidence(
            causeCells = setOf(cell(55), cell(73), cell(28), cell(72)),
            causeCandidates = setOf(
                candidate(55, 2),
                candidate(55, 3),
                candidate(55, 4),
                candidate(73, 2),
                candidate(73, 3),
                candidate(73, 8),
                candidate(28, 2),
                candidate(28, 4),
                candidate(72, 3),
                candidate(72, 8),
            ),
            focusDigits = setOf(2, 3, 4, 8),
            houses = listOf(HouseRef(type = HouseType.COLUMN, index = 1), HouseRef(type = HouseType.BOX, index = 6)),
            wingCells = setOf(cell(28), cell(72)),
        ),
    ),
)

// https://sudoku.coach/en/learn/wxyz-wing
private fun sueDeCoqType1Example2() = additionalExample(
    givens = "005400006000956007976218435089600002060520800200890000000749003000385020093162708",
    solution = "815473296432956187976218435789634512364521879251897364128749653647385921593162748",
    candidates = "3l0300001w1w76ao000c0c3m000000033l000000000000000000002d0000001w250l2h0025002100002500957d000t210000251h3d094x0j3m0000001d1d002x092100000081007d0o0000000000000o00",
    step = SolveStep(
        technique = TechniqueId.SUE_DE_COQ_TYPE_1,
        eliminations = listOf(
            CandidateElimination(candidate = candidate(46, 1)),
            CandidateElimination(candidate = candidate(54, 5)),
            CandidateElimination(candidate = candidate(63, 4)),
            CandidateElimination(candidate = candidate(65, 4)),
        ),
        evidence = StepEvidence(
            causeCells = setOf(cell(55), cell(64), cell(1), cell(72)),
            causeCandidates = setOf(
                candidate(55, 1),
                candidate(55, 2),
                candidate(55, 5),
                candidate(64, 1),
                candidate(64, 4),
                candidate(1, 1),
                candidate(1, 2),
                candidate(72, 4),
                candidate(72, 5),
            ),
            focusDigits = setOf(1, 2, 4, 5),
            houses = listOf(HouseRef(type = HouseType.COLUMN, index = 1), HouseRef(type = HouseType.BOX, index = 6)),
            wingCells = setOf(cell(1), cell(72)),
        ),
    ),
)

// https://sudoku.coach/en/learn/locked-candidate
private fun sueDeCoqType2Example1() = additionalExample(
    givens = "841063200206051000507004100000400018168072004304010020000007800085149032000600000",
    solution = "841763259296851473537294186972436518168572394354918627419327865685149732723685941",
    candidates = "0000008w0000009c9c007800cg000094co90007800aqaq0000bk848w2a7600781cac00000000007o00007o7k00002800b4004wa800a888037a0m0600008o8h2o00000000002o0000941v7a003q409k9k9d",
    step = SolveStep(
        technique = TechniqueId.SUE_DE_COQ_TYPE_2,
        eliminations = listOf(
            CandidateElimination(candidate = candidate(15, 7)),
            CandidateElimination(candidate = candidate(78, 7)),
            CandidateElimination(candidate = candidate(53, 5)),
            CandidateElimination(candidate = candidate(53, 9)),
        ),
        evidence = StepEvidence(
            causeCells = setOf(cell(33), cell(42), cell(51), cell(69), cell(43)),
            causeCandidates = setOf(
                candidate(33, 3),
                candidate(33, 5),
                candidate(33, 6),
                candidate(33, 7),
                candidate(33, 9),
                candidate(42, 3),
                candidate(42, 5),
                candidate(42, 9),
                candidate(51, 5),
                candidate(51, 6),
                candidate(51, 7),
                candidate(51, 9),
                candidate(69, 6),
                candidate(69, 7),
                candidate(43, 5),
                candidate(43, 9),
            ),
            focusDigits = setOf(3, 5, 6, 7, 9),
            houses = listOf(HouseRef(type = HouseType.COLUMN, index = 6), HouseRef(type = HouseType.BOX, index = 5)),
            wingCells = setOf(cell(69), cell(43)),
        ),
    ),
)

// https://sudoku.coach/en/learn/wxyz-wing
private fun eightYWingExample1() = additionalExample(
    givens = "000900010091078300030100409940700138087340050003800004308000007004680003509000001",
    solution = "756934812491278365832156479945762138287341956613895724368519247124687593579423681",
    candidates = "6i2a1e001i1q5c001e1600000q0000000y1e5c001e001e1e005c0000001e001e1e0000000z000000008382000y0z0j0000758j9u9u00000z000q757v8i8a001v1v0000009d7m7600002q000a06244i4q00",
    step = SolveStep(
        technique = TechniqueId.EIGHT_Y_WING,
        eliminations = listOf(CandidateElimination(candidate = candidate(59, 2))),
        evidence = StepEvidence(
            causeCells = setOf(cell(77), cell(75), cell(76), cell(23), cell(32), cell(41), cell(50), cell(68)),
            causeCandidates = setOf(
                candidate(77, 3),
                candidate(77, 4),
                candidate(77, 7),
                candidate(75, 2),
                candidate(75, 4),
                candidate(76, 2),
                candidate(76, 3),
                candidate(23, 2),
                candidate(23, 5),
                candidate(23, 6),
                candidate(32, 2),
                candidate(32, 5),
                candidate(32, 6),
                candidate(41, 1),
                candidate(41, 2),
                candidate(41, 6),
                candidate(41, 9),
                candidate(50, 1),
                candidate(50, 2),
                candidate(50, 5),
                candidate(50, 6),
                candidate(50, 9),
                candidate(68, 1),
                candidate(68, 5),
                candidate(68, 7),
                candidate(68, 9),
            ),
            focusDigits = setOf(1, 2, 3, 4, 5, 6, 7, 9),
            houses = listOf(HouseRef(type = HouseType.ROW, index = 8), HouseRef(type = HouseType.COLUMN, index = 5)),
        ),
    ),
)

// https://sudoku.coach/en/learn/w-wing
private fun eightYWingExample2() = additionalExample(
    givens = "078026300630000000050400068890000402300000510005200890006800000500049000003502000",
    solution = "478126359632985147159437268891653472324798516765214893246871935587349621913562784",
    candidates = "090000750000000o7t00007f8x40408z229503007700901w8z000000001t10392d001w000016228wcg5k00002o211500001x2500002s970b00001x1x8z2m9p003n1v1000002r5i1t953t00002p009t5k95",
    step = SolveStep(
        technique = TechniqueId.EIGHT_Y_WING,
        eliminations = listOf(CandidateElimination(candidate = candidate(61, 7))),
        evidence = StepEvidence(
            causeCells = setOf(cell(70), cell(34), cell(60), cell(69), cell(71), cell(78), cell(79), cell(80)),
            causeCandidates = setOf(
                candidate(70, 2),
                candidate(70, 3),
                candidate(70, 7),
                candidate(70, 8),
                candidate(34, 3),
                candidate(34, 7),
                candidate(60, 1),
                candidate(60, 2),
                candidate(60, 7),
                candidate(60, 9),
                candidate(69, 1),
                candidate(69, 2),
                candidate(69, 6),
                candidate(69, 7),
                candidate(71, 1),
                candidate(71, 7),
                candidate(78, 1),
                candidate(78, 6),
                candidate(78, 7),
                candidate(78, 9),
                candidate(79, 4),
                candidate(79, 7),
                candidate(79, 8),
                candidate(80, 1),
                candidate(80, 4),
                candidate(80, 7),
                candidate(80, 9),
            ),
            focusDigits = setOf(1, 2, 3, 4, 6, 7, 8, 9),
            houses = listOf(HouseRef(type = HouseType.COLUMN, index = 7), HouseRef(type = HouseType.BOX, index = 8)),
        ),
    ),
)

// https://sudoku.coach/en/learn/crane
private fun nineYWingExample1() = additionalExample(
    givens = "000400900060000200097000008082009706000060100000800002003207000000050000070908030",
    solution = "125486973368795214497321568582139746734562189619874352853247691941653827276918435",
    candidates = "470n41005j1j00352d4d00492dcl0l002h2l0v00001h071j1o1l000t00000l0d00000o009o0s7s2c000u00bc7wal0t8p00250t0s7s00c90p0000090054c97tbv0bbt1100194odn951n001l0009001k000p",
    step = SolveStep(
        technique = TechniqueId.NINE_Y_WING,
        eliminations = listOf(CandidateElimination(candidate = candidate(22, 1))),
        evidence = StepEvidence(
            causeCells = setOf(cell(4), cell(58), cell(76), cell(5), cell(12), cell(13), cell(14), cell(21), cell(23)),
            causeCandidates = setOf(
                candidate(4, 1),
                candidate(4, 2),
                candidate(4, 3),
                candidate(4, 7),
                candidate(4, 8),
                candidate(58, 1),
                candidate(58, 4),
                candidate(76, 1),
                candidate(76, 4),
                candidate(5, 1),
                candidate(5, 2),
                candidate(5, 3),
                candidate(5, 5),
                candidate(5, 6),
                candidate(12, 1),
                candidate(12, 3),
                candidate(12, 5),
                candidate(12, 7),
                candidate(13, 1),
                candidate(13, 3),
                candidate(13, 7),
                candidate(13, 8),
                candidate(13, 9),
                candidate(14, 1),
                candidate(14, 3),
                candidate(14, 5),
                candidate(21, 1),
                candidate(21, 3),
                candidate(21, 5),
                candidate(21, 6),
                candidate(23, 1),
                candidate(23, 2),
                candidate(23, 3),
                candidate(23, 5),
                candidate(23, 6),
            ),
            focusDigits = setOf(1, 2, 3, 4, 5, 6, 7, 8, 9),
            houses = listOf(HouseRef(type = HouseType.COLUMN, index = 4), HouseRef(type = HouseType.BOX, index = 1)),
        ),
    ),
)

// Locally constructed nine-cell wing
private fun nineYWingExample2() = additionalExample(
    givens = "000000000078352960625017038259786103086503729730291685561870392397625810802139576",
    solution = "913468257478352961625917438259786143186543729734291685561874392397625814842139576",
    candidates = "75090c7c143s0a0o200900000000000000090000007c0000080000000000000000000800090000000800000000000008000000000000000000000008000000000000000000000008000800000000000000",
    step = SolveStep(
        technique = TechniqueId.NINE_Y_WING,
        eliminations = listOf(CandidateElimination(candidate = candidate(1, 4))),
        evidence = StepEvidence(
            causeCells = setOf(cell(0), cell(2), cell(3), cell(4), cell(5), cell(6), cell(7), cell(8), cell(9)),
            causeCandidates = setOf(
                candidate(0, 1),
                candidate(0, 9),
                candidate(2, 3),
                candidate(2, 4),
                candidate(3, 4),
                candidate(3, 9),
                candidate(4, 4),
                candidate(4, 6),
                candidate(5, 4),
                candidate(5, 8),
                candidate(6, 2),
                candidate(6, 4),
                candidate(7, 4),
                candidate(7, 5),
                candidate(8, 4),
                candidate(8, 7),
                candidate(9, 1),
                candidate(9, 4),
            ),
            focusDigits = setOf(1, 2, 3, 4, 5, 6, 7, 8, 9),
            houses = listOf(HouseRef(type = HouseType.ROW, index = 0), HouseRef(type = HouseType.COLUMN, index = 0)),
        ),
    ),
)

// https://sudoku.coach/en/learn/x-wing
private fun starfishExample1() = additionalExample(
    givens = "098003562003090178000008934340080759085907340907435820509071483130850697870309215",
    solution = "498713562263594178751268934342186759685927341917435826529671483134852697876349215",
    candidates = "2000001t0900000000161e000i00140000002q1d0x2a0y000000000000030x000y0000000y000000030000000x000x0000000000000x000y000y000000000000000a00000a000000000014001400000000",
    step = SolveStep(
        technique = TechniqueId.STARFISH,
        eliminations = listOf(
            CandidateElimination(candidate = candidate(18, 6)),
            CandidateElimination(candidate = candidate(19, 6)),
        ),
        evidence = StepEvidence(
            causeCells = setOf(
                cell(9),
                cell(10),
                cell(14),
                cell(30),
                cell(32),
                cell(36),
                cell(44),
                cell(46),
                cell(53),
                cell(55),
                cell(57),
            ),
            causeCandidates = setOf(
                candidate(9, 6),
                candidate(10, 6),
                candidate(14, 6),
                candidate(30, 6),
                candidate(32, 6),
                candidate(36, 6),
                candidate(44, 6),
                candidate(46, 6),
                candidate(53, 6),
                candidate(55, 6),
                candidate(57, 6),
            ),
            focusDigits = setOf(6),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 1),
                HouseRef(type = HouseType.ROW, index = 3),
                HouseRef(type = HouseType.ROW, index = 4),
                HouseRef(type = HouseType.ROW, index = 5),
                HouseRef(type = HouseType.ROW, index = 6),
                HouseRef(type = HouseType.COLUMN, index = 0),
                HouseRef(type = HouseType.COLUMN, index = 1),
                HouseRef(type = HouseType.COLUMN, index = 3),
                HouseRef(type = HouseType.COLUMN, index = 5),
                HouseRef(type = HouseType.COLUMN, index = 8),
            ),
            baseHouses = listOf(
                HouseRef(type = HouseType.ROW, index = 1),
                HouseRef(type = HouseType.ROW, index = 3),
                HouseRef(type = HouseType.ROW, index = 4),
                HouseRef(type = HouseType.ROW, index = 5),
                HouseRef(type = HouseType.ROW, index = 6),
            ),
            coverHouses = listOf(
                HouseRef(type = HouseType.COLUMN, index = 0),
                HouseRef(type = HouseType.COLUMN, index = 1),
                HouseRef(type = HouseType.COLUMN, index = 3),
                HouseRef(type = HouseType.COLUMN, index = 5),
                HouseRef(type = HouseType.COLUMN, index = 8),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/x-wing
private fun starfishExample2() = additionalExample(
    givens = "008000060003090100000008904000000759000007300900400000500071080100050600070300200",
    solution = "498713562263594178751268934342186759685927341917435826529671483134852697876349215",
    candidates = "227v002b0f0u0g002e2y1m0036001m001u5u2q1f37371300001y004u4v174j4n120000004q571nc34j00000b4j005337004n1i3k034j008e8a82000008000400b27eaq007e00981w4o0088004o88007d0h",
    step = SolveStep(
        technique = TechniqueId.STARFISH,
        eliminations = listOf(
            CandidateElimination(candidate = candidate(28, 3)),
            CandidateElimination(candidate = candidate(31, 3)),
            CandidateElimination(candidate = candidate(32, 3)),
        ),
        evidence = StepEvidence(
            causeCells = setOf(
                cell(4),
                cell(5),
                cell(8),
                cell(22),
                cell(25),
                cell(46),
                cell(49),
                cell(50),
                cell(55),
                cell(62),
                cell(64),
                cell(70),
                cell(71),
            ),
            causeCandidates = setOf(
                candidate(4, 3),
                candidate(5, 3),
                candidate(8, 3),
                candidate(22, 3),
                candidate(25, 3),
                candidate(46, 3),
                candidate(49, 3),
                candidate(50, 3),
                candidate(55, 3),
                candidate(62, 3),
                candidate(64, 3),
                candidate(70, 3),
                candidate(71, 3),
            ),
            focusDigits = setOf(3),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 0),
                HouseRef(type = HouseType.ROW, index = 2),
                HouseRef(type = HouseType.ROW, index = 5),
                HouseRef(type = HouseType.ROW, index = 6),
                HouseRef(type = HouseType.ROW, index = 7),
                HouseRef(type = HouseType.COLUMN, index = 1),
                HouseRef(type = HouseType.COLUMN, index = 4),
                HouseRef(type = HouseType.COLUMN, index = 5),
                HouseRef(type = HouseType.COLUMN, index = 7),
                HouseRef(type = HouseType.COLUMN, index = 8),
            ),
            baseHouses = listOf(
                HouseRef(type = HouseType.ROW, index = 0),
                HouseRef(type = HouseType.ROW, index = 2),
                HouseRef(type = HouseType.ROW, index = 5),
                HouseRef(type = HouseType.ROW, index = 6),
                HouseRef(type = HouseType.ROW, index = 7),
            ),
            coverHouses = listOf(
                HouseRef(type = HouseType.COLUMN, index = 1),
                HouseRef(type = HouseType.COLUMN, index = 4),
                HouseRef(type = HouseType.COLUMN, index = 5),
                HouseRef(type = HouseType.COLUMN, index = 7),
                HouseRef(type = HouseType.COLUMN, index = 8),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/xy-chain
private fun whaleExample1() = additionalExample(
    givens = "004007090080900500000050600079010800000082360000000000650000000002009000100000054",
    solution = "534267198786941532921853647379416825415782369268395471657124983842539716193678254",
    candidates = "0m13004n120003003r1y002t001a1900271z927b1x3z003x005r5j0u00001o001o000a0i0o090h2g000000009d4e1b513ga41o97239f00005g5r263x8z5jcn5o0c007130001t5h6d00785g6e2u4k8y0000",
    step = SolveStep(
        technique = TechniqueId.WHALE,
        eliminations = listOf(
            CandidateElimination(candidate = candidate(45, 4)),
            CandidateElimination(candidate = candidate(46, 4)),
            CandidateElimination(candidate = candidate(48, 4)),
            CandidateElimination(candidate = candidate(49, 4)),
            CandidateElimination(candidate = candidate(50, 4)),
            CandidateElimination(candidate = candidate(52, 4)),
        ),
        evidence = StepEvidence(
            causeCells = setOf(
                cell(13),
                cell(14),
                cell(16),
                cell(21),
                cell(23),
                cell(25),
                cell(27),
                cell(30),
                cell(32),
                cell(34),
                cell(36),
                cell(37),
                cell(39),
                cell(57),
                cell(58),
                cell(59),
                cell(63),
                cell(64),
                cell(66),
                cell(67),
            ),
            causeCandidates = setOf(
                candidate(13, 4),
                candidate(14, 4),
                candidate(16, 4),
                candidate(21, 4),
                candidate(23, 4),
                candidate(25, 4),
                candidate(27, 4),
                candidate(30, 4),
                candidate(32, 4),
                candidate(34, 4),
                candidate(36, 4),
                candidate(37, 4),
                candidate(39, 4),
                candidate(57, 4),
                candidate(58, 4),
                candidate(59, 4),
                candidate(63, 4),
                candidate(64, 4),
                candidate(66, 4),
                candidate(67, 4),
            ),
            focusDigits = setOf(4),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 1),
                HouseRef(type = HouseType.ROW, index = 2),
                HouseRef(type = HouseType.ROW, index = 3),
                HouseRef(type = HouseType.ROW, index = 4),
                HouseRef(type = HouseType.ROW, index = 6),
                HouseRef(type = HouseType.ROW, index = 7),
                HouseRef(type = HouseType.COLUMN, index = 0),
                HouseRef(type = HouseType.COLUMN, index = 1),
                HouseRef(type = HouseType.COLUMN, index = 3),
                HouseRef(type = HouseType.COLUMN, index = 4),
                HouseRef(type = HouseType.COLUMN, index = 5),
                HouseRef(type = HouseType.COLUMN, index = 7),
            ),
            baseHouses = listOf(
                HouseRef(type = HouseType.ROW, index = 1),
                HouseRef(type = HouseType.ROW, index = 2),
                HouseRef(type = HouseType.ROW, index = 3),
                HouseRef(type = HouseType.ROW, index = 4),
                HouseRef(type = HouseType.ROW, index = 6),
                HouseRef(type = HouseType.ROW, index = 7),
            ),
            coverHouses = listOf(
                HouseRef(type = HouseType.COLUMN, index = 0),
                HouseRef(type = HouseType.COLUMN, index = 1),
                HouseRef(type = HouseType.COLUMN, index = 3),
                HouseRef(type = HouseType.COLUMN, index = 4),
                HouseRef(type = HouseType.COLUMN, index = 5),
                HouseRef(type = HouseType.COLUMN, index = 7),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/jellyfish
private fun whaleExample2() = additionalExample(
    givens = "071000006008030071020761004006350127200107560000206003000020005594073012002510700",
    solution = "471892356968435271325761984846359127239147568157286493713928645594673812682514739",
    candidates = "7g0000awawbeaub800881k007c007u76000078007o000000asb800aw08000000aw000000000c7800aw000000aocp0p9c00aw00awaw006d111wbs00awbwb0000000004g00004g00004k4k000000aw00b0ao",
    step = SolveStep(
        technique = TechniqueId.WHALE,
        eliminations = listOf(
            CandidateElimination(candidate = candidate(72, 8)),
            CandidateElimination(candidate = candidate(40, 8)),
            CandidateElimination(candidate = candidate(77, 8)),
            CandidateElimination(candidate = candidate(79, 8)),
        ),
        evidence = StepEvidence(
            causeCells = setOf(
                cell(3),
                cell(4),
                cell(5),
                cell(6),
                cell(7),
                cell(24),
                cell(25),
                cell(27),
                cell(32),
                cell(45),
                cell(49),
                cell(51),
                cell(52),
                cell(54),
                cell(57),
                cell(59),
                cell(60),
                cell(61),
                cell(66),
                cell(69),
            ),
            causeCandidates = setOf(
                candidate(3, 8),
                candidate(4, 8),
                candidate(5, 8),
                candidate(6, 8),
                candidate(7, 8),
                candidate(24, 8),
                candidate(25, 8),
                candidate(27, 8),
                candidate(32, 8),
                candidate(45, 8),
                candidate(49, 8),
                candidate(51, 8),
                candidate(52, 8),
                candidate(54, 8),
                candidate(57, 8),
                candidate(59, 8),
                candidate(60, 8),
                candidate(61, 8),
                candidate(66, 8),
                candidate(69, 8),
            ),
            focusDigits = setOf(8),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 0),
                HouseRef(type = HouseType.ROW, index = 2),
                HouseRef(type = HouseType.ROW, index = 3),
                HouseRef(type = HouseType.ROW, index = 5),
                HouseRef(type = HouseType.ROW, index = 6),
                HouseRef(type = HouseType.ROW, index = 7),
                HouseRef(type = HouseType.COLUMN, index = 0),
                HouseRef(type = HouseType.COLUMN, index = 3),
                HouseRef(type = HouseType.COLUMN, index = 4),
                HouseRef(type = HouseType.COLUMN, index = 5),
                HouseRef(type = HouseType.COLUMN, index = 6),
                HouseRef(type = HouseType.COLUMN, index = 7),
            ),
            baseHouses = listOf(
                HouseRef(type = HouseType.ROW, index = 0),
                HouseRef(type = HouseType.ROW, index = 2),
                HouseRef(type = HouseType.ROW, index = 3),
                HouseRef(type = HouseType.ROW, index = 5),
                HouseRef(type = HouseType.ROW, index = 6),
                HouseRef(type = HouseType.ROW, index = 7),
            ),
            coverHouses = listOf(
                HouseRef(type = HouseType.COLUMN, index = 0),
                HouseRef(type = HouseType.COLUMN, index = 3),
                HouseRef(type = HouseType.COLUMN, index = 4),
                HouseRef(type = HouseType.COLUMN, index = 5),
                HouseRef(type = HouseType.COLUMN, index = 6),
                HouseRef(type = HouseType.COLUMN, index = 7),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/jellyfish
private fun leviathanExample1() = additionalExample(
    givens = "040195026261304000500620413050041209418269537000053041034500002075932004900410300",
    solution = "843195726261374958597628413356741289418269537729853641634587192175932864982416375",
    candidates = "5g001w0000005c0000000000001s00cgcw4000ao8w00005c0000002s002s5c0000004g000000000000000000002o769u5c00004g00004h0000005c68dddc004h00000000004h4g00003m0y000068006o40",
    step = SolveStep(
        technique = TechniqueId.LEVIATHAN,
        eliminations = listOf(
            CandidateElimination(candidate = candidate(54, 8)),
            CandidateElimination(candidate = candidate(59, 8)),
            CandidateElimination(candidate = candidate(60, 8)),
            CandidateElimination(candidate = candidate(61, 8)),
        ),
        evidence = StepEvidence(
            causeCells = setOf(
                cell(0),
                cell(6),
                cell(15),
                cell(16),
                cell(17),
                cell(19),
                cell(23),
                cell(30),
                cell(34),
                cell(48),
                cell(51),
                cell(63),
                cell(69),
                cell(70),
                cell(73),
                cell(77),
                cell(79),
                cell(80),
            ),
            causeCandidates = setOf(
                candidate(0, 8),
                candidate(6, 8),
                candidate(15, 8),
                candidate(16, 8),
                candidate(17, 8),
                candidate(19, 8),
                candidate(23, 8),
                candidate(30, 8),
                candidate(34, 8),
                candidate(48, 8),
                candidate(51, 8),
                candidate(63, 8),
                candidate(69, 8),
                candidate(70, 8),
                candidate(73, 8),
                candidate(77, 8),
                candidate(79, 8),
                candidate(80, 8),
            ),
            focusDigits = setOf(8),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 0),
                HouseRef(type = HouseType.ROW, index = 1),
                HouseRef(type = HouseType.ROW, index = 2),
                HouseRef(type = HouseType.ROW, index = 3),
                HouseRef(type = HouseType.ROW, index = 5),
                HouseRef(type = HouseType.ROW, index = 7),
                HouseRef(type = HouseType.ROW, index = 8),
                HouseRef(type = HouseType.COLUMN, index = 0),
                HouseRef(type = HouseType.COLUMN, index = 1),
                HouseRef(type = HouseType.COLUMN, index = 3),
                HouseRef(type = HouseType.COLUMN, index = 5),
                HouseRef(type = HouseType.COLUMN, index = 6),
                HouseRef(type = HouseType.COLUMN, index = 7),
                HouseRef(type = HouseType.COLUMN, index = 8),
            ),
            baseHouses = listOf(
                HouseRef(type = HouseType.ROW, index = 0),
                HouseRef(type = HouseType.ROW, index = 1),
                HouseRef(type = HouseType.ROW, index = 2),
                HouseRef(type = HouseType.ROW, index = 3),
                HouseRef(type = HouseType.ROW, index = 5),
                HouseRef(type = HouseType.ROW, index = 7),
                HouseRef(type = HouseType.ROW, index = 8),
            ),
            coverHouses = listOf(
                HouseRef(type = HouseType.COLUMN, index = 0),
                HouseRef(type = HouseType.COLUMN, index = 1),
                HouseRef(type = HouseType.COLUMN, index = 3),
                HouseRef(type = HouseType.COLUMN, index = 5),
                HouseRef(type = HouseType.COLUMN, index = 6),
                HouseRef(type = HouseType.COLUMN, index = 7),
                HouseRef(type = HouseType.COLUMN, index = 8),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/swordfish
private fun leviathanExample2() = additionalExample(
    givens = "271000806050862179896070025500038090920050080608000507069080000105007068082306050",
    solution = "271593846354862179896471325517638492923754681648129537469285713135947268782316954",
    candidates = "0000007s747w000c000c000c00000000000000000009000d0c00000009202z00001600030000242x0009180005000d007f777d000d002400000r000p260d07000c007e7e007i0000200000007d00940009",
    step = SolveStep(
        technique = TechniqueId.LEVIATHAN,
        eliminations = listOf(
            CandidateElimination(candidate = candidate(72, 4)),
            CandidateElimination(candidate = candidate(64, 4)),
            CandidateElimination(candidate = candidate(66, 4)),
            CandidateElimination(candidate = candidate(69, 4)),
            CandidateElimination(candidate = candidate(78, 4)),
        ),
        evidence = StepEvidence(
            causeCells = setOf(
                cell(3),
                cell(5),
                cell(7),
                cell(9),
                cell(11),
                cell(21),
                cell(23),
                cell(24),
                cell(28),
                cell(29),
                cell(30),
                cell(33),
                cell(38),
                cell(39),
                cell(41),
                cell(42),
                cell(46),
                cell(48),
                cell(50),
                cell(52),
                cell(54),
                cell(57),
                cell(59),
                cell(60),
                cell(61),
            ),
            causeCandidates = setOf(
                candidate(3, 4),
                candidate(5, 4),
                candidate(7, 4),
                candidate(9, 4),
                candidate(11, 4),
                candidate(21, 4),
                candidate(23, 4),
                candidate(24, 4),
                candidate(28, 4),
                candidate(29, 4),
                candidate(30, 4),
                candidate(33, 4),
                candidate(38, 4),
                candidate(39, 4),
                candidate(41, 4),
                candidate(42, 4),
                candidate(46, 4),
                candidate(48, 4),
                candidate(50, 4),
                candidate(52, 4),
                candidate(54, 4),
                candidate(57, 4),
                candidate(59, 4),
                candidate(60, 4),
                candidate(61, 4),
            ),
            focusDigits = setOf(4),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 0),
                HouseRef(type = HouseType.ROW, index = 1),
                HouseRef(type = HouseType.ROW, index = 2),
                HouseRef(type = HouseType.ROW, index = 3),
                HouseRef(type = HouseType.ROW, index = 4),
                HouseRef(type = HouseType.ROW, index = 5),
                HouseRef(type = HouseType.ROW, index = 6),
                HouseRef(type = HouseType.COLUMN, index = 0),
                HouseRef(type = HouseType.COLUMN, index = 1),
                HouseRef(type = HouseType.COLUMN, index = 2),
                HouseRef(type = HouseType.COLUMN, index = 3),
                HouseRef(type = HouseType.COLUMN, index = 5),
                HouseRef(type = HouseType.COLUMN, index = 6),
                HouseRef(type = HouseType.COLUMN, index = 7),
            ),
            baseHouses = listOf(
                HouseRef(type = HouseType.ROW, index = 0),
                HouseRef(type = HouseType.ROW, index = 1),
                HouseRef(type = HouseType.ROW, index = 2),
                HouseRef(type = HouseType.ROW, index = 3),
                HouseRef(type = HouseType.ROW, index = 4),
                HouseRef(type = HouseType.ROW, index = 5),
                HouseRef(type = HouseType.ROW, index = 6),
            ),
            coverHouses = listOf(
                HouseRef(type = HouseType.COLUMN, index = 0),
                HouseRef(type = HouseType.COLUMN, index = 1),
                HouseRef(type = HouseType.COLUMN, index = 2),
                HouseRef(type = HouseType.COLUMN, index = 3),
                HouseRef(type = HouseType.COLUMN, index = 5),
                HouseRef(type = HouseType.COLUMN, index = 6),
                HouseRef(type = HouseType.COLUMN, index = 7),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/w-wing
private fun nishioForcingNetExample1() = additionalExample(
    givens = "056900300000003100007005900000008060401000708200000000000070000040100200080000003",
    solution = "856917342924863157317425986579238461431659728268741539695372814743186295182594673",
    candidates = "3l0000003v23005m22ao76ay6i4q0000623e3p07004q4r00003u169g907o2m7z000o007v0084001i8m82007q00009wb83g8ta10o7x7t8l877q5a008a54bd8pac007o00c48000cwa8a9007m1m8q8a1k9l00",
    step = SolveStep(
        technique = TechniqueId.NISHIO_FORCING_NET,
        eliminations = listOf(CandidateElimination(candidate = candidate(33, 5))),
        evidence = StepEvidence(
            causeCells = setOf(cell(33), cell(27), cell(29), cell(47)),
            causeCandidates = setOf(
                candidate(33, 5),
                candidate(27, 5),
                candidate(29, 5),
                candidate(47, 5),
                candidate(47, 8),
            ),
            focusDigits = setOf(5, 8),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 3),
                HouseRef(type = HouseType.BOX, index = 3),
                HouseRef(type = HouseType.ROW, index = 5),
            ),
            links = listOf(
                InferenceLink(
                    from = candidate(33, 5),
                    to = candidate(27, 5),
                    type = InferenceLinkType.WEAK,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(33, 5),
                    to = candidate(29, 5),
                    type = InferenceLinkType.WEAK,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(27, 5),
                    to = candidate(47, 5),
                    type = InferenceLinkType.STRONG,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(29, 5),
                    to = candidate(47, 5),
                    type = InferenceLinkType.STRONG,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(47, 5),
                    to = candidate(47, 8),
                    type = InferenceLinkType.WEAK,
                    branchId = 0,
                ),
            ),
            inferenceGraph = InferenceGraph(
                nodes = listOf(
                    InferenceNode(
                        id = 0,
                        candidate = candidate(33, 5),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = true,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 1,
                        candidate = candidate(27, 5),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 2,
                        candidate = candidate(29, 5),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 3,
                        candidate = candidate(47, 5),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 4,
                        candidate = candidate(47, 8),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = true,
                    ),
                ),
                edges = listOf(
                    InferenceEdge(
                        fromNodeId = 0,
                        toNodeId = 1,
                        type = InferenceLinkType.WEAK,
                        house = HouseRef(type = HouseType.ROW, index = 3),
                    ),
                    InferenceEdge(
                        fromNodeId = 0,
                        toNodeId = 2,
                        type = InferenceLinkType.WEAK,
                        house = HouseRef(type = HouseType.ROW, index = 3),
                    ),
                    InferenceEdge(
                        fromNodeId = 1,
                        toNodeId = 3,
                        type = InferenceLinkType.STRONG,
                        house = HouseRef(type = HouseType.BOX, index = 3),
                    ),
                    InferenceEdge(
                        fromNodeId = 2,
                        toNodeId = 3,
                        type = InferenceLinkType.STRONG,
                        house = HouseRef(type = HouseType.BOX, index = 3),
                    ),
                    InferenceEdge(fromNodeId = 3, toNodeId = 4, type = InferenceLinkType.WEAK),
                ),
                premise = InferencePremise(type = InferencePremiseType.NISHIO, candidates = listOf(candidate(33, 5))),
                contradiction = InferenceContradiction(
                    type = InferenceContradictionType.EMPTY_HOUSE,
                    house = HouseRef(type = HouseType.ROW, index = 5),
                ),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/unique-rectangle
private fun nishioForcingNetExample2() = additionalExample(
    givens = "000900000870000050000046020900400006005060040046280000000095208000000100009030000",
    solution = "562978413874312659193546827921457386785163942346289571417695238638724195259831764",
    candidates = "1r1j0f002b5j6k6d2500000f0503078c007h0l7p055x0000ck0091003r5j00291x5w5h001z3r001x0091ck00931x00000000919g919h3111252p0000002s003i525q681u5m009w9o3f4z0069005n3c2o2g",
    step = SolveStep(
        technique = TechniqueId.NISHIO_FORCING_NET,
        eliminations = listOf(CandidateElimination(candidate = candidate(12, 1))),
        evidence = StepEvidence(
            causeCells = setOf(cell(12), cell(13), cell(4), cell(31)),
            causeCandidates = setOf(
                candidate(12, 1),
                candidate(13, 1),
                candidate(4, 1),
                candidate(31, 1),
                candidate(31, 5),
            ),
            focusDigits = setOf(1, 5),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 1),
                HouseRef(type = HouseType.BOX, index = 1),
                HouseRef(type = HouseType.COLUMN, index = 4),
                HouseRef(type = HouseType.BOX, index = 4),
            ),
            links = listOf(
                InferenceLink(
                    from = candidate(12, 1),
                    to = candidate(13, 1),
                    type = InferenceLinkType.WEAK,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(12, 1),
                    to = candidate(4, 1),
                    type = InferenceLinkType.WEAK,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(4, 1),
                    to = candidate(31, 1),
                    type = InferenceLinkType.STRONG,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(13, 1),
                    to = candidate(31, 1),
                    type = InferenceLinkType.STRONG,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(31, 1),
                    to = candidate(31, 5),
                    type = InferenceLinkType.WEAK,
                    branchId = 0,
                ),
            ),
            inferenceGraph = InferenceGraph(
                nodes = listOf(
                    InferenceNode(
                        id = 0,
                        candidate = candidate(12, 1),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = true,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 1,
                        candidate = candidate(13, 1),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 2,
                        candidate = candidate(4, 1),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 3,
                        candidate = candidate(31, 1),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 4,
                        candidate = candidate(31, 5),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = true,
                    ),
                ),
                edges = listOf(
                    InferenceEdge(
                        fromNodeId = 0,
                        toNodeId = 1,
                        type = InferenceLinkType.WEAK,
                        house = HouseRef(type = HouseType.ROW, index = 1),
                    ),
                    InferenceEdge(
                        fromNodeId = 0,
                        toNodeId = 2,
                        type = InferenceLinkType.WEAK,
                        house = HouseRef(type = HouseType.BOX, index = 1),
                    ),
                    InferenceEdge(
                        fromNodeId = 2,
                        toNodeId = 3,
                        type = InferenceLinkType.STRONG,
                        house = HouseRef(type = HouseType.COLUMN, index = 4),
                    ),
                    InferenceEdge(
                        fromNodeId = 1,
                        toNodeId = 3,
                        type = InferenceLinkType.STRONG,
                        house = HouseRef(type = HouseType.COLUMN, index = 4),
                    ),
                    InferenceEdge(fromNodeId = 3, toNodeId = 4, type = InferenceLinkType.WEAK),
                ),
                premise = InferencePremise(type = InferencePremiseType.NISHIO, candidates = listOf(candidate(12, 1))),
                contradiction = InferenceContradiction(
                    type = InferenceContradictionType.EMPTY_HOUSE,
                    house = HouseRef(type = HouseType.BOX, index = 4),
                ),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/unique-rectangle
private fun digitForcingChainExample1() = additionalExample(
    givens = "020000000807000060030007201200004000700013090601500004100008076000000002000060050",
    solution = "524136987817942563936857241293684715745213698681579324159328476468795132372461859",
    candidates = "7s008obxbg8hd83wd0007t007j7y7n7w007o7s008obsbc00003s0000b4b8dccg006t3p5w0048484i00004w004000ao0000ci765g3q00007s7y7i7y007g00007we0cc999o7lb13x007gcob29b0077b100as",
    step = SolveStep(
        technique = TechniqueId.DIGIT_FORCING_CHAIN,
        placements = listOf(Placement(cell = cell(46), digit = 8)),
        evidence = StepEvidence(
            causeCells = setOf(cell(46), cell(50), cell(52)),
            causeCandidates = setOf(
                candidate(46, 8),
                candidate(46, 9),
                candidate(50, 9),
                candidate(50, 2),
                candidate(52, 2),
            ),
            focusDigits = setOf(8, 9, 2),
            houses = listOf(HouseRef(type = HouseType.ROW, index = 5), HouseRef(type = HouseType.COLUMN, index = 7)),
            links = listOf(
                InferenceLink(
                    from = candidate(46, 8),
                    to = candidate(46, 9),
                    type = InferenceLinkType.STRONG,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(46, 9),
                    to = candidate(50, 9),
                    type = InferenceLinkType.WEAK,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(50, 9),
                    to = candidate(50, 2),
                    type = InferenceLinkType.STRONG,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(50, 2),
                    to = candidate(52, 2),
                    type = InferenceLinkType.WEAK,
                    branchId = 0,
                ),
            ),
            inferenceGraph = InferenceGraph(
                nodes = listOf(
                    InferenceNode(
                        id = 0,
                        candidate = candidate(46, 8),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = true,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 1,
                        candidate = candidate(46, 9),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 2,
                        candidate = candidate(50, 9),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 3,
                        candidate = candidate(50, 2),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 4,
                        candidate = candidate(52, 2),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = true,
                    ),
                ),
                edges = listOf(
                    InferenceEdge(fromNodeId = 0, toNodeId = 1, type = InferenceLinkType.STRONG),
                    InferenceEdge(
                        fromNodeId = 1,
                        toNodeId = 2,
                        type = InferenceLinkType.WEAK,
                        house = HouseRef(type = HouseType.ROW, index = 5),
                    ),
                    InferenceEdge(fromNodeId = 2, toNodeId = 3, type = InferenceLinkType.STRONG),
                    InferenceEdge(
                        fromNodeId = 3,
                        toNodeId = 4,
                        type = InferenceLinkType.WEAK,
                        house = HouseRef(type = HouseType.ROW, index = 5),
                    ),
                ),
                premise = InferencePremise(type = InferencePremiseType.DIGIT, candidates = listOf(candidate(46, 8))),
                contradiction = InferenceContradiction(
                    type = InferenceContradictionType.EMPTY_HOUSE,
                    house = HouseRef(type = HouseType.COLUMN, index = 7),
                ),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/finned-jellyfish
private fun digitForcingChainExample2() = additionalExample(
    givens = "000025001020380600000600003004008006060090307010000000200001000600500200309000000",
    solution = "936425871127389654458617923794238516562194387813756492245961738681573249379842165",
    candidates = "cocs6c940000coco009l0029000094009k7sd5d45t002194d4d6009c9g001v2d007l7n004000420b000a004b00cw005y223g32bcbebe00605sco3000d4e4bc005k5d00249800ctaw0060005m2w2y616x48",
    step = SolveStep(
        technique = TechniqueId.DIGIT_FORCING_CHAIN,
        placements = listOf(Placement(cell = cell(41), digit = 4)),
        evidence = StepEvidence(
            causeCells = setOf(cell(41), cell(38), cell(47), cell(53)),
            causeCandidates = setOf(
                candidate(41, 4),
                candidate(41, 2),
                candidate(38, 2),
                candidate(47, 2),
                candidate(53, 2),
            ),
            focusDigits = setOf(4, 2),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 4),
                HouseRef(type = HouseType.COLUMN, index = 2),
                HouseRef(type = HouseType.ROW, index = 5),
                HouseRef(type = HouseType.COLUMN, index = 8),
            ),
            links = listOf(
                InferenceLink(
                    from = candidate(41, 4),
                    to = candidate(41, 2),
                    type = InferenceLinkType.STRONG,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(41, 2),
                    to = candidate(38, 2),
                    type = InferenceLinkType.WEAK,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(38, 2),
                    to = candidate(47, 2),
                    type = InferenceLinkType.STRONG,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(47, 2),
                    to = candidate(53, 2),
                    type = InferenceLinkType.WEAK,
                    branchId = 0,
                ),
            ),
            inferenceGraph = InferenceGraph(
                nodes = listOf(
                    InferenceNode(
                        id = 0,
                        candidate = candidate(41, 4),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = true,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 1,
                        candidate = candidate(41, 2),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 2,
                        candidate = candidate(38, 2),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 3,
                        candidate = candidate(47, 2),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 4,
                        candidate = candidate(53, 2),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = true,
                    ),
                ),
                edges = listOf(
                    InferenceEdge(fromNodeId = 0, toNodeId = 1, type = InferenceLinkType.STRONG),
                    InferenceEdge(
                        fromNodeId = 1,
                        toNodeId = 2,
                        type = InferenceLinkType.WEAK,
                        house = HouseRef(type = HouseType.ROW, index = 4),
                    ),
                    InferenceEdge(
                        fromNodeId = 2,
                        toNodeId = 3,
                        type = InferenceLinkType.STRONG,
                        house = HouseRef(type = HouseType.COLUMN, index = 2),
                    ),
                    InferenceEdge(
                        fromNodeId = 3,
                        toNodeId = 4,
                        type = InferenceLinkType.WEAK,
                        house = HouseRef(type = HouseType.ROW, index = 5),
                    ),
                ),
                premise = InferencePremise(type = InferencePremiseType.DIGIT, candidates = listOf(candidate(41, 4))),
                contradiction = InferenceContradiction(
                    type = InferenceContradictionType.EMPTY_HOUSE,
                    house = HouseRef(type = HouseType.COLUMN, index = 8),
                ),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/unique-rectangle
private fun digitForcingNetExample1() = additionalExample(
    givens = "000900000870000050000046020900400006005060040046280000000095208000000100009030000",
    solution = "562978413874312659193546827921457386785163942346289571417695238638724195259831764",
    candidates = "1r1j0f002b5j6k6d2500000f0503078c007h0l7p055x0000ck0091003r5j00291x5w5h001z3r001x0091ck00931x00000000919g919h3111252p0000002s003i525q681u5m009w9o3f4z0069005n3c2o2g",
    step = SolveStep(
        technique = TechniqueId.DIGIT_FORCING_NET,
        placements = listOf(Placement(cell = cell(12), digit = 3)),
        evidence = StepEvidence(
            causeCells = setOf(cell(12), cell(13), cell(4), cell(31)),
            causeCandidates = setOf(
                candidate(12, 3),
                candidate(12, 1),
                candidate(13, 1),
                candidate(4, 1),
                candidate(31, 1),
                candidate(31, 5),
            ),
            focusDigits = setOf(3, 1, 5),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 1),
                HouseRef(type = HouseType.BOX, index = 1),
                HouseRef(type = HouseType.COLUMN, index = 4),
                HouseRef(type = HouseType.BOX, index = 4),
            ),
            links = listOf(
                InferenceLink(
                    from = candidate(12, 3),
                    to = candidate(12, 1),
                    type = InferenceLinkType.STRONG,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(12, 1),
                    to = candidate(13, 1),
                    type = InferenceLinkType.WEAK,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(12, 1),
                    to = candidate(4, 1),
                    type = InferenceLinkType.WEAK,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(4, 1),
                    to = candidate(31, 1),
                    type = InferenceLinkType.STRONG,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(13, 1),
                    to = candidate(31, 1),
                    type = InferenceLinkType.STRONG,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(31, 1),
                    to = candidate(31, 5),
                    type = InferenceLinkType.WEAK,
                    branchId = 0,
                ),
            ),
            inferenceGraph = InferenceGraph(
                nodes = listOf(
                    InferenceNode(
                        id = 0,
                        candidate = candidate(12, 3),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = true,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 1,
                        candidate = candidate(12, 1),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 2,
                        candidate = candidate(13, 1),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 3,
                        candidate = candidate(4, 1),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 4,
                        candidate = candidate(31, 1),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 5,
                        candidate = candidate(31, 5),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = true,
                    ),
                ),
                edges = listOf(
                    InferenceEdge(fromNodeId = 0, toNodeId = 1, type = InferenceLinkType.STRONG),
                    InferenceEdge(
                        fromNodeId = 1,
                        toNodeId = 2,
                        type = InferenceLinkType.WEAK,
                        house = HouseRef(type = HouseType.ROW, index = 1),
                    ),
                    InferenceEdge(
                        fromNodeId = 1,
                        toNodeId = 3,
                        type = InferenceLinkType.WEAK,
                        house = HouseRef(type = HouseType.BOX, index = 1),
                    ),
                    InferenceEdge(
                        fromNodeId = 3,
                        toNodeId = 4,
                        type = InferenceLinkType.STRONG,
                        house = HouseRef(type = HouseType.COLUMN, index = 4),
                    ),
                    InferenceEdge(
                        fromNodeId = 2,
                        toNodeId = 4,
                        type = InferenceLinkType.STRONG,
                        house = HouseRef(type = HouseType.COLUMN, index = 4),
                    ),
                    InferenceEdge(fromNodeId = 4, toNodeId = 5, type = InferenceLinkType.WEAK),
                ),
                premise = InferencePremise(type = InferencePremiseType.DIGIT, candidates = listOf(candidate(12, 3))),
                contradiction = InferenceContradiction(
                    type = InferenceContradictionType.EMPTY_HOUSE,
                    house = HouseRef(type = HouseType.BOX, index = 4),
                ),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/locked-candidate
private fun digitForcingNetExample2() = additionalExample(
    givens = "841063200206051000507004100000400018168072004304010020000007800085149032000600000",
    solution = "841763259296851473537294186972436518168572394354918627419327865685149732723685941",
    candidates = "0000008w0000009c9c007800cg000094co90007800aqaq0000bk848w2a7600781cac00000000007o00007o7k00002800b4004wa800a888037a0m0600008o8h2o00000000002o0000941v7a003q409k9k9d",
    step = SolveStep(
        technique = TechniqueId.DIGIT_FORCING_NET,
        placements = listOf(Placement(cell = cell(3), digit = 7)),
        evidence = StepEvidence(
            causeCells = setOf(cell(3), cell(39), cell(48), cell(31), cell(27), cell(29)),
            causeCandidates = setOf(
                candidate(3, 7),
                candidate(3, 9),
                candidate(39, 9),
                candidate(48, 9),
                candidate(31, 9),
                candidate(27, 9),
                candidate(29, 9),
            ),
            focusDigits = setOf(7, 9),
            houses = listOf(
                HouseRef(type = HouseType.COLUMN, index = 3),
                HouseRef(type = HouseType.BOX, index = 4),
                HouseRef(type = HouseType.ROW, index = 3),
                HouseRef(type = HouseType.BOX, index = 3),
            ),
            links = listOf(
                InferenceLink(
                    from = candidate(3, 7),
                    to = candidate(3, 9),
                    type = InferenceLinkType.STRONG,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(3, 9),
                    to = candidate(39, 9),
                    type = InferenceLinkType.WEAK,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(3, 9),
                    to = candidate(48, 9),
                    type = InferenceLinkType.WEAK,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(39, 9),
                    to = candidate(31, 9),
                    type = InferenceLinkType.STRONG,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(48, 9),
                    to = candidate(31, 9),
                    type = InferenceLinkType.STRONG,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(31, 9),
                    to = candidate(27, 9),
                    type = InferenceLinkType.WEAK,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(31, 9),
                    to = candidate(29, 9),
                    type = InferenceLinkType.WEAK,
                    branchId = 0,
                ),
            ),
            inferenceGraph = InferenceGraph(
                nodes = listOf(
                    InferenceNode(
                        id = 0,
                        candidate = candidate(3, 7),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = true,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 1,
                        candidate = candidate(3, 9),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 2,
                        candidate = candidate(39, 9),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 3,
                        candidate = candidate(48, 9),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 4,
                        candidate = candidate(31, 9),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 5,
                        candidate = candidate(27, 9),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = true,
                    ),
                    InferenceNode(
                        id = 6,
                        candidate = candidate(29, 9),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = true,
                    ),
                ),
                edges = listOf(
                    InferenceEdge(fromNodeId = 0, toNodeId = 1, type = InferenceLinkType.STRONG),
                    InferenceEdge(
                        fromNodeId = 1,
                        toNodeId = 2,
                        type = InferenceLinkType.WEAK,
                        house = HouseRef(type = HouseType.COLUMN, index = 3),
                    ),
                    InferenceEdge(
                        fromNodeId = 1,
                        toNodeId = 3,
                        type = InferenceLinkType.WEAK,
                        house = HouseRef(type = HouseType.COLUMN, index = 3),
                    ),
                    InferenceEdge(
                        fromNodeId = 2,
                        toNodeId = 4,
                        type = InferenceLinkType.STRONG,
                        house = HouseRef(type = HouseType.BOX, index = 4),
                    ),
                    InferenceEdge(
                        fromNodeId = 3,
                        toNodeId = 4,
                        type = InferenceLinkType.STRONG,
                        house = HouseRef(type = HouseType.BOX, index = 4),
                    ),
                    InferenceEdge(
                        fromNodeId = 4,
                        toNodeId = 5,
                        type = InferenceLinkType.WEAK,
                        house = HouseRef(type = HouseType.ROW, index = 3),
                    ),
                    InferenceEdge(
                        fromNodeId = 4,
                        toNodeId = 6,
                        type = InferenceLinkType.WEAK,
                        house = HouseRef(type = HouseType.ROW, index = 3),
                    ),
                ),
                premise = InferencePremise(type = InferencePremiseType.DIGIT, candidates = listOf(candidate(3, 7))),
                contradiction = InferenceContradiction(
                    type = InferenceContradictionType.EMPTY_HOUSE,
                    house = HouseRef(type = HouseType.BOX, index = 3),
                ),
            ),
        ),
    ),
)

// Cell indices are zero-based, in row-major order.
private fun cell(index: Int) = CellRef.fromIndex(index)
private fun candidate(index: Int, digit: Int) = CandidateRef(cell(index), digit)
private fun additionalExample(givens: String, solution: String, candidates: String, step: SolveStep) = TutorialExample(
    technique = step.technique, givens = givens, solution = solution,
    house = step.evidence.houses.firstOrNull() ?: HouseRef(HouseType.ROW, step.evidence.causeCells.first().row),
    patternCells = step.evidence.causeCells, digits = step.evidence.focusDigits,
    candidateMasks = candidates, recordedDeduction = step,
)
