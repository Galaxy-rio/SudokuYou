package com.galaxyrio.sudokusolver.domain.tutorial

import com.galaxyrio.sudokusolver.domain.solver.*

/** Numeric teaching positions from Sudoku Coach; explanations and rendering are local.
 * Each snapshot and recorded deduction is validated by TutorialAdvancedLessonTest. See NOTICE. */
internal val advancedTutorialExamples: List<TutorialExample> = listOf(
    pointingPairExample1(),
    pointingPairExample2(),
    claimingPairExample1(),
    claimingPairExample2(),
    claimingTripleExample1(),
    claimingTripleExample2(),
    xWingExample1(),
    xWingExample2(),
    skyscraperExample1(),
    skyscraperExample2(),
    twoStringKiteExample1(),
    twoStringKiteExample2(),
    turbotCraneExample1(),
    turbotCraneExample2(),
    xyWingExample1(),
    xyWingExample2(),
    xyzWingExample1(),
    xyzWingExample2(),
    wWingExample1(),
    wWingExample2(),
    finnedXWingExample1(),
    finnedXWingExample2(),
    simpleColoringType1Example1(),
    simpleColoringType1Example2(),
    simpleColoringType2Example1(),
    simpleColoringType2Example2(),
    emptyRectangleExample1(),
    emptyRectangleExample2(),
    swordfishExample1(),
    swordfishExample2(),
    uniqueRectangleType1Example1(),
    uniqueRectangleType1Example2(),
    uniqueRectangleType2Example1(),
    uniqueRectangleType2Example2(),
    uniqueRectangleType3Example1(),
    uniqueRectangleType3Example2(),
    uniqueRectangleType4Example1(),
    uniqueRectangleType4Example2(),
    uniqueRectangleType5Example1(),
    uniqueRectangleType5Example2(),
    jellyfishExample1(),
    jellyfishExample2(),
    finnedSwordfishExample1(),
    finnedSwordfishExample2(),
    finnedJellyfishExample1(),
    finnedJellyfishExample2(),
    bugPlusOneExample1(),
    bugPlusOneExample2(),
    xChainExample1(),
    xChainExample2(),
    xChainLoopExample1(),
    xChainLoopExample2(),
    xChainOneEndpointExample1(),
    xChainOneEndpointExample2(),
    groupedXChainExample1(),
    groupedXChainExample2(),
    wxyzWingExample1(),
    wxyzWingExample2(),
    fiveYWingExample1(),
    fiveYWingExample2(),
    sixYWingExample1(),
    sixYWingExample2(),
    sevenYWingExample1(),
    xyChainExample1(),
    xyChainExample2(),
    xyChainLoopExample1(),
    threeDMedusaExample1(),
    threeDMedusaExample2(),
    aicExample1(),
    aicExample2(),
    nishioForcingChainExample1(),
    nishioForcingChainExample2(),
    cellForcingChainExample1(),
    cellForcingNetExample1(),
    cellForcingNetExample2(),
    regionForcingChainExample1(),
    regionForcingChainExample2(),
    regionForcingNetExample1(),
    regionForcingNetExample2(),
)

// https://sudoku.coach/en/learn/locked-candidate
private fun pointingPairExample1() = advancedExample(
    givens = "900060257408000900000907000000700043106000020004000600060804000000005800000009506",
    solution = "913468257478352961625917438259786143186543729734291685561874392397625814842139576",
    candidates = "0005050d003p000000002f000n0n070011011i0n0n004f000d4l3t42b67m00b70z01000000d0000sbg041s00b45yd2000nbb0700chb52e009j001z001x91771y9b93131z0000917f5i5r1z071z00001x00",
    step = SolveStep(
        technique = TechniqueId.POINTING_PAIR,
        eliminations = listOf(CandidateElimination(candidate = candidate(22, 4))),
        evidence = StepEvidence(
            causeCells = setOf(cell(24), cell(26)),
            causeCandidates = setOf(candidate(24, 4), candidate(26, 4)),
            focusDigits = setOf(4),
            houses = listOf(HouseRef(type = HouseType.BOX, index = 2), HouseRef(type = HouseType.ROW, index = 2)),
        ),
    ),
)

// https://sudoku.coach/en/learn/locked-candidate
private fun pointingPairExample2() = advancedExample(
    givens = "000548002004200908020096054762354891003900647149867235000400580205009470400005129",
    solution = "697548312514273968328196754762354891853921647149867235971432586285619473436785129",
    candidates = "84912p0000001w0x001g2d00001x05000x003o005d1t00001w000000000000000000000040400000030300000000000000000000000084912p001z07000010003p000x3p00000010005g682o5g00000000",
    step = SolveStep(
        technique = TechniqueId.POINTING_PAIR,
        eliminations = listOf(
            CandidateElimination(candidate = candidate(9, 3)),
            CandidateElimination(candidate = candidate(10, 3)),
        ),
        evidence = StepEvidence(
            causeCells = setOf(cell(13), cell(14)),
            causeCandidates = setOf(candidate(13, 3), candidate(14, 3)),
            focusDigits = setOf(3),
            houses = listOf(HouseRef(type = HouseType.BOX, index = 1), HouseRef(type = HouseType.ROW, index = 1)),
        ),
    ),
)

// https://sudoku.coach/en/learn/locked-candidate
private fun claimingPairExample1() = advancedExample(
    givens = "000756381567030924318294576895403000003080400000900835032010040450372008781649253",
    solution = "249756381567138924318294576895463712123587469674921835932815647456372198781649253",
    candidates = "760a7c0000000000000000003l003l000000000000000000000000000000000y002p0x1u0z1u000h002900818y0z2214000y1t0000008000004000402o008w0000800000000x8100000000000000000000",
    step = SolveStep(
        technique = TechniqueId.CLAIMING_PAIR,
        eliminations = listOf(CandidateElimination(candidate = candidate(43, 1))),
        evidence = StepEvidence(
            causeCells = setOf(cell(33), cell(34)),
            causeCandidates = setOf(candidate(33, 1), candidate(34, 1)),
            focusDigits = setOf(1),
            houses = listOf(HouseRef(type = HouseType.ROW, index = 3), HouseRef(type = HouseType.BOX, index = 5)),
        ),
    ),
)

// https://sudoku.coach/en/learn/locked-candidate
private fun claimingPairExample2() = advancedExample(
    givens = "000400020217956483540300070000039048004000300305004200002690154450000800600045700",
    solution = "863417925217956483549382671126739548984521367375864219732698154451273896698145732",
    candidates = "aobobo005d5d8g008h0000000000000000000000bk003n3n8000811t2q0x2b00001c0000chde005v6b5f0081a900dc005d690000819t5c5g0000003o0000000000791v1v1z00848200asat3n0000007876",
    step = SolveStep(
        technique = TechniqueId.CLAIMING_PAIR,
        eliminations = listOf(CandidateElimination(candidate = candidate(29, 1))),
        evidence = StepEvidence(
            causeCells = setOf(cell(27), cell(36)),
            causeCandidates = setOf(candidate(27, 1), candidate(36, 1)),
            focusDigits = setOf(1),
            houses = listOf(HouseRef(type = HouseType.COLUMN, index = 0), HouseRef(type = HouseType.BOX, index = 3)),
        ),
    ),
)

// https://sudoku.coach/en/learn/locked-candidate
private fun claimingTripleExample1() = advancedExample(
    givens = "900060257408000900000907000000700043106000020004000600060804000000005800000009506",
    solution = "913468257478352961625917438259786143186543729734291685561874392397625814842139576",
    candidates = "0005050d003p000000002f000n0n070011011i0n0n004f000d4l3t42b67m00b70z01000000d0000sbg041s00b45yd2000nbb0700chb52e009j001z001x91771y9b93131z0000917f5i5r1z071z00001x00",
    step = SolveStep(
        technique = TechniqueId.CLAIMING_TRIPLE,
        eliminations = listOf(
            CandidateElimination(candidate = candidate(54, 7)),
            CandidateElimination(candidate = candidate(63, 7)),
            CandidateElimination(candidate = candidate(64, 7)),
            CandidateElimination(candidate = candidate(72, 7)),
            CandidateElimination(candidate = candidate(73, 7)),
        ),
        evidence = StepEvidence(
            causeCells = setOf(cell(56), cell(65), cell(74)),
            causeCandidates = setOf(candidate(56, 7), candidate(65, 7), candidate(74, 7)),
            focusDigits = setOf(7),
            houses = listOf(HouseRef(type = HouseType.COLUMN, index = 2), HouseRef(type = HouseType.BOX, index = 6)),
        ),
    ),
)

// https://sudoku.coach/en/learn/locked-candidate
private fun claimingTripleExample2() = advancedExample(
    givens = "000400020217956483540300070000039048004000300305004200002690154450000800600045700",
    solution = "863417925217956483549382671126739548984521367375864219732698154451273896698145732",
    candidates = "aobobo005d5d8g008h0000000000000000000000bk003n3n8000811t2q0x2b00001c0000chde005v6b5f0081a900dc005d690000819t5c5g0000005g0000000000791v1v1z00848200asat3n0000007876",
    step = SolveStep(
        technique = TechniqueId.CLAIMING_TRIPLE,
        eliminations = listOf(CandidateElimination(candidate = candidate(59, 7))),
        evidence = StepEvidence(
            causeCells = setOf(cell(66), cell(67), cell(68)),
            causeCandidates = setOf(candidate(66, 7), candidate(67, 7), candidate(68, 7)),
            focusDigits = setOf(7),
            houses = listOf(HouseRef(type = HouseType.ROW, index = 7), HouseRef(type = HouseType.BOX, index = 7)),
        ),
    ),
)

// https://sudoku.coach/en/learn/x-wing
private fun xWingExample1() = advancedExample(
    givens = "600009000013027960794860200006083759907050000005970000000000000371690800060040100",
    solution = "682319574513427968794865213146283759927154386835976421458731692371692845269548137",
    candidates = "00423m0s05000o5l614000000o000000004800000000000h00050l0b0a00030000000000003q00030014183z4v3n3q00000014183z4v4a4aaq2c05431g923a00000000000i000a0q4200aq2c004200922e",
    step = SolveStep(
        technique = TechniqueId.X_WING,
        eliminations = listOf(
            CandidateElimination(candidate = candidate(59, 5)),
            CandidateElimination(candidate = candidate(77, 5)),
            CandidateElimination(candidate = candidate(8, 5)),
            CandidateElimination(candidate = candidate(17, 5)),
            CandidateElimination(candidate = candidate(62, 5)),
            CandidateElimination(candidate = candidate(80, 5)),
        ),
        evidence = StepEvidence(
            causeCells = setOf(cell(23), cell(26), cell(68), cell(71)),
            causeCandidates = setOf(candidate(23, 5), candidate(26, 5), candidate(68, 5), candidate(71, 5)),
            focusDigits = setOf(5),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 2),
                HouseRef(type = HouseType.ROW, index = 7),
                HouseRef(type = HouseType.COLUMN, index = 5),
                HouseRef(type = HouseType.COLUMN, index = 8),
            ),
            baseHouses = listOf(HouseRef(type = HouseType.ROW, index = 2), HouseRef(type = HouseType.ROW, index = 7)),
            coverHouses = listOf(
                HouseRef(type = HouseType.COLUMN, index = 5),
                HouseRef(type = HouseType.COLUMN, index = 8),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/x-wing
private fun xWingExample2() = advancedExample(
    givens = "098003562003090178000008934340080759085907340907435820509071483130850697870309215",
    solution = "498713562263594178751268934342186759685927341917435826529671483134852697876349215",
    candidates = "2000001t0900000000161e001e00160000002q1f0x360y0000000000000z0x000y0000000y0000000z0000000x000x0000000000000x000y000y000000000000000a00000a000000000014001400000000",
    step = SolveStep(
        technique = TechniqueId.X_WING,
        eliminations = listOf(CandidateElimination(candidate = candidate(14, 2))),
        evidence = StepEvidence(
            causeCells = setOf(cell(29), cell(32), cell(65), cell(68)),
            causeCandidates = setOf(candidate(29, 2), candidate(32, 2), candidate(65, 2), candidate(68, 2)),
            focusDigits = setOf(2),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 3),
                HouseRef(type = HouseType.ROW, index = 7),
                HouseRef(type = HouseType.COLUMN, index = 2),
                HouseRef(type = HouseType.COLUMN, index = 5),
            ),
            baseHouses = listOf(HouseRef(type = HouseType.ROW, index = 3), HouseRef(type = HouseType.ROW, index = 7)),
            coverHouses = listOf(
                HouseRef(type = HouseType.COLUMN, index = 2),
                HouseRef(type = HouseType.COLUMN, index = 5),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/skyscraper
private fun skyscraperExample1() = advancedExample(
    givens = "364295187820007640001600023682501700407029006903760002248956371030002060006003200",
    solution = "364295187829317645571648923682531794417829536953764812248956371735182469196473258",
    candidates = "00000000000000000000007k05050000007k288w00003s3s7k0000000000000c0000787c000h003o0000400l00000h0000003s3s0h0000000000000000000029007k3t5d007s00bc298w003t5d00007kbc",
    step = SolveStep(
        technique = TechniqueId.SKYSCRAPER,
        eliminations = listOf(
            CandidateElimination(candidate = candidate(65, 9)),
            CandidateElimination(candidate = candidate(79, 9)),
            CandidateElimination(candidate = candidate(80, 9)),
        ),
        evidence = StepEvidence(
            causeCells = setOf(cell(73), cell(19), cell(24), cell(69)),
            causeCandidates = setOf(candidate(73, 9), candidate(19, 9), candidate(24, 9), candidate(69, 9)),
            focusDigits = setOf(9),
            houses = listOf(
                HouseRef(type = HouseType.COLUMN, index = 1),
                HouseRef(type = HouseType.COLUMN, index = 6),
                HouseRef(type = HouseType.ROW, index = 2),
            ),
            links = listOf(
                InferenceLink(from = candidate(73, 9), to = candidate(19, 9), type = InferenceLinkType.STRONG),
                InferenceLink(from = candidate(19, 9), to = candidate(24, 9), type = InferenceLinkType.WEAK),
                InferenceLink(from = candidate(24, 9), to = candidate(69, 9), type = InferenceLinkType.STRONG),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/skyscraper
private fun skyscraperExample2() = advancedExample(
    givens = "002300600060200030173659842200067309600920704007105286320006100046700028781592463",
    solution = "852374691964281537173659842218467359635928714497135286329846175546713928781592463",
    candidates = "bc7k00005d3t008w29bc007s005d3t7k0029000000000000000000000h3s3s0000000h00000l4000003o000h007c7800000c0000000000007k3s3s00008w287k00000005057k0000000000000000000000",
    step = SolveStep(
        technique = TechniqueId.SKYSCRAPER,
        eliminations = listOf(
            CandidateElimination(candidate = candidate(61, 9)),
            CandidateElimination(candidate = candidate(63, 9)),
        ),
        evidence = StepEvidence(
            causeCells = setOf(cell(56), cell(11), cell(15), cell(69)),
            causeCandidates = setOf(candidate(56, 9), candidate(11, 9), candidate(15, 9), candidate(69, 9)),
            focusDigits = setOf(9),
            houses = listOf(
                HouseRef(type = HouseType.COLUMN, index = 2),
                HouseRef(type = HouseType.COLUMN, index = 6),
                HouseRef(type = HouseType.ROW, index = 1),
            ),
            links = listOf(
                InferenceLink(from = candidate(56, 9), to = candidate(11, 9), type = InferenceLinkType.STRONG),
                InferenceLink(from = candidate(11, 9), to = candidate(15, 9), type = InferenceLinkType.WEAK),
                InferenceLink(from = candidate(15, 9), to = candidate(69, 9), type = InferenceLinkType.STRONG),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/two-string-kite
private fun twoStringKiteExample1() = advancedExample(
    givens = "740296013290813047301000902970542001000007020000008750809001435000009170017300290",
    solution = "745296813296813547381475962973542681158967324624138759869721435432659178517384296",
    candidates = "00004000000040000000001c0000001c0000004g0020280o004g000000100000004k4g000p403s75100010007c15121a75100000007c000y002o1u000000001k1i1a14420000004g1k000000400o00004g",
    step = SolveStep(
        technique = TechniqueId.TWO_STRING_KITE,
        eliminations = listOf(CandidateElimination(candidate = candidate(29, 6))),
        evidence = StepEvidence(
            causeCells = setOf(cell(11), cell(15), cell(25), cell(34)),
            causeCandidates = setOf(candidate(11, 6), candidate(15, 6), candidate(25, 6), candidate(34, 6)),
            focusDigits = setOf(6),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 1),
                HouseRef(type = HouseType.BOX, index = 2),
                HouseRef(type = HouseType.COLUMN, index = 7),
            ),
            links = listOf(
                InferenceLink(from = candidate(11, 6), to = candidate(15, 6), type = InferenceLinkType.STRONG),
                InferenceLink(from = candidate(15, 6), to = candidate(25, 6), type = InferenceLinkType.WEAK),
                InferenceLink(from = candidate(25, 6), to = candidate(34, 6), type = InferenceLinkType.STRONG),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/two-string-kite
private fun twoStringKiteExample2() = advancedExample(
    givens = "000040060010096020008015090000530916106409835593681742000168274400053089000904050",
    solution = "759842361314796528628315497842537916176429835593681742935168274467253189281974653",
    candidates = "922e9e5i001u0l005h1w000o5g00000o005g2u32001y00000c001w5e5m2200001u000000001u00001u00000000000000000000000000780k7k000000000000002q1v1u00000x00006a6a1v001u00110005",
    step = SolveStep(
        technique = TechniqueId.TWO_STRING_KITE,
        eliminations = listOf(CandidateElimination(candidate = candidate(1, 2))),
        evidence = StepEvidence(
            causeCells = setOf(cell(37), cell(40), cell(32), cell(5)),
            causeCandidates = setOf(candidate(37, 2), candidate(40, 2), candidate(32, 2), candidate(5, 2)),
            focusDigits = setOf(2),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 4),
                HouseRef(type = HouseType.BOX, index = 4),
                HouseRef(type = HouseType.COLUMN, index = 5),
            ),
            links = listOf(
                InferenceLink(from = candidate(37, 2), to = candidate(40, 2), type = InferenceLinkType.STRONG),
                InferenceLink(from = candidate(40, 2), to = candidate(32, 2), type = InferenceLinkType.WEAK),
                InferenceLink(from = candidate(32, 2), to = candidate(5, 2), type = InferenceLinkType.STRONG),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/crane
private fun turbotCraneExample1() = advancedExample(
    givens = "200050179500174000871029504120930400705042013300001200650290041402010000910403000",
    solution = "243856179569174832871329564126935487785642913394781256657298341432517698918463725",
    candidates = "0018184k004g0000000084840000004k4m4i00000010000000100000004g000028004w6o00bk004g0000bk000000bsbs284g0000c06o00005g00005c5g0000003o0028006odgas4g00005c004g00680i0i",
    step = SolveStep(
        technique = TechniqueId.TURBOT_CRANE,
        eliminations = listOf(CandidateElimination(candidate = candidate(42, 6))),
        evidence = StepEvidence(
            causeCells = setOf(cell(39), cell(49), cell(76), cell(78)),
            causeCandidates = setOf(candidate(39, 6), candidate(49, 6), candidate(76, 6), candidate(78, 6)),
            focusDigits = setOf(6),
            houses = listOf(
                HouseRef(type = HouseType.BOX, index = 4),
                HouseRef(type = HouseType.COLUMN, index = 4),
                HouseRef(type = HouseType.ROW, index = 8),
            ),
            links = listOf(
                InferenceLink(from = candidate(39, 6), to = candidate(49, 6), type = InferenceLinkType.STRONG),
                InferenceLink(from = candidate(49, 6), to = candidate(76, 6), type = InferenceLinkType.WEAK),
                InferenceLink(from = candidate(76, 6), to = candidate(78, 6), type = InferenceLinkType.STRONG),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/crane
private fun turbotCraneExample2() = advancedExample(
    givens = "213940870897030004465800309906400007301200400724000000172309048549028700638704902",
    solution = "213945876897632154465817329956481237381276495724593681172369548549128763638754912",
    candidates = "00000000001c00001c0000001d001f1f1f00000000001t1v000300004000003l0l0j470000400000dc3400b41c0000001dao1h1dao05000000001c001c00000000000x0000001105000000000h00000h00",
    step = SolveStep(
        technique = TechniqueId.TURBOT_CRANE,
        eliminations = listOf(CandidateElimination(candidate = candidate(40, 6))),
        evidence = StepEvidence(
            causeCells = setOf(cell(44), cell(51), cell(60), cell(58)),
            causeCandidates = setOf(candidate(44, 6), candidate(51, 6), candidate(60, 6), candidate(58, 6)),
            focusDigits = setOf(6),
            houses = listOf(
                HouseRef(type = HouseType.BOX, index = 5),
                HouseRef(type = HouseType.COLUMN, index = 6),
                HouseRef(type = HouseType.ROW, index = 6),
            ),
            links = listOf(
                InferenceLink(from = candidate(44, 6), to = candidate(51, 6), type = InferenceLinkType.STRONG),
                InferenceLink(from = candidate(51, 6), to = candidate(60, 6), type = InferenceLinkType.WEAK),
                InferenceLink(from = candidate(60, 6), to = candidate(58, 6), type = InferenceLinkType.STRONG),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/y-wing
private fun xyWingExample1() = advancedExample(
    givens = "000900410201800600070100008010009800300081700000026901906004180584613279107098000",
    solution = "865937412291845637473162598612759843359481726748326951936274185584613279127598364",
    candidates = "4g1c4400101u00001u007c00000c2800782814007800140i0k7q002w000i2k2800001q1q008o760o0000001m1m5k0o402k0000000s000006002a280000000k0000000000000000000006000i00000k1414",
    step = SolveStep(
        technique = TechniqueId.XY_WING,
        eliminations = listOf(
            CandidateElimination(candidate = candidate(10, 4)),
            CandidateElimination(candidate = candidate(27, 4)),
            CandidateElimination(candidate = candidate(45, 4)),
        ),
        evidence = StepEvidence(
            causeCells = setOf(cell(1), cell(18), cell(46)),
            causeCandidates = setOf(
                candidate(1, 5),
                candidate(1, 6),
                candidate(18, 4),
                candidate(18, 6),
                candidate(46, 4),
                candidate(46, 5),
            ),
            focusDigits = setOf(6, 5, 4),
            links = listOf(
                InferenceLink(from = candidate(18, 4), to = candidate(18, 6), type = InferenceLinkType.DUAL),
                InferenceLink(from = candidate(18, 6), to = candidate(1, 6), type = InferenceLinkType.WEAK),
                InferenceLink(from = candidate(1, 6), to = candidate(1, 5), type = InferenceLinkType.DUAL),
                InferenceLink(from = candidate(1, 5), to = candidate(46, 5), type = InferenceLinkType.WEAK),
                InferenceLink(from = candidate(46, 5), to = candidate(46, 4), type = InferenceLinkType.DUAL),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/y-wing
private fun xyWingExample2() = advancedExample(
    givens = "904000180300100904610000702200081509849050210531200807400912378193876425700500691",
    solution = "954723186327168954618495732276381549849657213531249867465912378193876425782534691",
    candidates = "002a002o062c000010005u5u000y5s001c000000400c7cbc000k00002o2o0c0000000c000000002o001w00001000000000887c001400001c1c000000000000000000000000000000003m3m000c0c000000",
    step = SolveStep(
        technique = TechniqueId.XY_WING,
        eliminations = listOf(
            CandidateElimination(candidate = candidate(3, 6)),
            CandidateElimination(candidate = candidate(16, 6)),
        ),
        evidence = StepEvidence(
            causeCells = setOf(cell(4), cell(8), cell(13)),
            causeCandidates = setOf(
                candidate(4, 2),
                candidate(4, 3),
                candidate(8, 3),
                candidate(8, 6),
                candidate(13, 2),
                candidate(13, 6),
            ),
            focusDigits = setOf(3, 2, 6),
            links = listOf(
                InferenceLink(from = candidate(8, 6), to = candidate(8, 3), type = InferenceLinkType.DUAL),
                InferenceLink(from = candidate(8, 3), to = candidate(4, 3), type = InferenceLinkType.WEAK),
                InferenceLink(from = candidate(4, 3), to = candidate(4, 2), type = InferenceLinkType.DUAL),
                InferenceLink(from = candidate(4, 2), to = candidate(13, 2), type = InferenceLinkType.WEAK),
                InferenceLink(from = candidate(13, 2), to = candidate(13, 6), type = InferenceLinkType.DUAL),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/xyz-wing
private fun xyzWingExample1() = advancedExample(
    givens = "630008070082037006704000138843700625009300847567842300008670004076000083000980760",
    solution = "631458279982137456754296138843719625129365847567842391298673514476521983315984762",
    candidates = "00000h0p7l007e00767500000900007s7k00007k000i8i8g000000000000007575000000030300001c1c0000000000000000000075757b770000000l7m7l007d00000j0j0p7k00000c0j0h00000c000003",
    step = SolveStep(
        technique = TechniqueId.XYZ_WING,
        eliminations = listOf(CandidateElimination(candidate = candidate(4, 1))),
        evidence = StepEvidence(
            causeCells = setOf(cell(3), cell(2), cell(12)),
            causeCandidates = setOf(
                candidate(3, 1),
                candidate(3, 4),
                candidate(3, 5),
                candidate(2, 1),
                candidate(2, 5),
                candidate(12, 1),
                candidate(12, 4),
            ),
            focusDigits = setOf(1, 4, 5),
        ),
    ),
)

// https://sudoku.coach/en/learn/xyz-wing
private fun xyzWingExample2() = advancedExample(
    givens = "020830050501670082000215000657123894294758000010496275102587030970341028080962007",
    solution = "726834951541679382839215746657123894294758163318496275162587439975341628483962517",
    candidates = "20008000007ca10081000c0000007c7g00005k18bk000000a41484000000000000000000000000000000110x113o003o00000000000000140000000088008000001c0000001c00000c000k0000000p0900",
    step = SolveStep(
        technique = TechniqueId.XYZ_WING,
        eliminations = listOf(CandidateElimination(candidate = candidate(18, 4))),
        evidence = StepEvidence(
            causeCells = setOf(cell(19), cell(10), cell(25)),
            causeCandidates = setOf(
                candidate(19, 3),
                candidate(19, 4),
                candidate(19, 6),
                candidate(10, 3),
                candidate(10, 4),
                candidate(25, 4),
                candidate(25, 6),
            ),
            focusDigits = setOf(3, 4, 6),
        ),
    ),
)

// https://sudoku.coach/en/learn/w-wing
private fun wWingExample1() = advancedExample(
    givens = "078026300630000000050400068890000402300000510005200890006800000500049000003502000",
    solution = "478126359632985147159437268891653472324798516765214893246871935587349621913562784",
    candidates = "090000750000000o7t00007f8x40408z229503007700901w8z000000001t10392d001w000016228wcg5k00002o211500001x2500002s970b00001x1x8z2m9p003n1v1000002r5i1t953t00002p009t5k95",
    step = SolveStep(
        technique = TechniqueId.W_WING,
        eliminations = listOf(CandidateElimination(candidate = candidate(65, 1))),
        evidence = StepEvidence(
            causeCells = setOf(cell(29), cell(71), cell(34), cell(44), cell(53)),
            causeCandidates = setOf(
                candidate(29, 1),
                candidate(29, 7),
                candidate(71, 1),
                candidate(71, 7),
                candidate(34, 7),
                candidate(44, 7),
                candidate(53, 7),
            ),
            focusDigits = setOf(1, 7),
            houses = listOf(HouseRef(type = HouseType.BOX, index = 5)),
            wingCells = setOf(cell(29), cell(71)),
        ),
    ),
)

// https://sudoku.coach/en/learn/w-wing
private fun wWingExample2() = advancedExample(
    givens = "510207830200000000009100502002410000105003204600052010357020600000070020420000008",
    solution = "516247839243589167879136542732418956195763284684952713357824691968371425421695378",
    candidates = "000014008800000080006k4sc4bwc095a09t5c6k00004s4g002w00cgck000000bk904w1c00cg00dcbk00004g0000cs3wcg0000900090000000ao00ax007c75aobk4h1g001l0d000l00000x8k848h919c00",
    step = SolveStep(
        technique = TechniqueId.W_WING,
        eliminations = listOf(CandidateElimination(candidate = candidate(25, 6))),
        evidence = StepEvidence(
            causeCells = setOf(cell(23), cell(43), cell(13), cell(22), cell(40)),
            causeCandidates = setOf(
                candidate(23, 6),
                candidate(23, 8),
                candidate(43, 6),
                candidate(43, 8),
                candidate(13, 8),
                candidate(22, 8),
                candidate(40, 8),
            ),
            focusDigits = setOf(6, 8),
            houses = listOf(HouseRef(type = HouseType.COLUMN, index = 4)),
            wingCells = setOf(cell(23), cell(43)),
        ),
    ),
)

// https://sudoku.coach/en/learn/finned-x-wing
private fun finnedXWingExample1() = advancedExample(
    givens = "200079500000034920900256100001302490420500300800040200102005843080423719340018652",
    solution = "213879564568134927974256138751382496426597381839641275192765843685423719347918652",
    candidates = "00114s3l0000004k4o34356o3l0000000068001w5o000000005g5k343400004g0000006o00009s00bk1t00686900acac80001t002o35008w009s80000000001c001c00000000000000008w8w0000000000",
    step = SolveStep(
        technique = TechniqueId.FINNED_X_WING,
        eliminations = listOf(CandidateElimination(candidate = candidate(48, 9))),
        evidence = StepEvidence(
            causeCells = setOf(cell(38), cell(40), cell(74), cell(75)),
            causeCandidates = setOf(candidate(38, 9), candidate(40, 9), candidate(74, 9), candidate(75, 9)),
            focusDigits = setOf(9),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 4),
                HouseRef(type = HouseType.ROW, index = 8),
                HouseRef(type = HouseType.COLUMN, index = 2),
                HouseRef(type = HouseType.COLUMN, index = 3),
                HouseRef(type = HouseType.BOX, index = 4),
            ),
            baseHouses = listOf(HouseRef(type = HouseType.ROW, index = 4), HouseRef(type = HouseType.ROW, index = 8)),
            coverHouses = listOf(
                HouseRef(type = HouseType.COLUMN, index = 2),
                HouseRef(type = HouseType.COLUMN, index = 3),
            ),
            finCandidates = setOf(candidate(40, 9)),
        ),
    ),
)

// https://sudoku.coach/en/learn/finned-x-wing
private fun finnedXWingExample2() = advancedExample(
    givens = "500000063000603204003400708000704000006500030802006005318260000000840301000319000",
    solution = "524178963781693254963452718135784692496521837872936145318265479659847321247319586",
    candidates = "005m20755e3m7500008xcg8x00cw00007l00838200007m03007l00757o7l003q00blar8295940000aq3naw008y0024007578007d95000000000000287s948w9uaa9c000028001u00222i2g0000004w5e2q",
    step = SolveStep(
        technique = TechniqueId.FINNED_X_WING,
        eliminations = listOf(CandidateElimination(candidate = candidate(33, 1))),
        evidence = StepEvidence(
            causeCells = setOf(cell(3), cell(6), cell(48), cell(51), cell(52)),
            causeCandidates = setOf(
                candidate(3, 1),
                candidate(6, 1),
                candidate(48, 1),
                candidate(51, 1),
                candidate(52, 1),
            ),
            focusDigits = setOf(1),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 0),
                HouseRef(type = HouseType.ROW, index = 5),
                HouseRef(type = HouseType.COLUMN, index = 3),
                HouseRef(type = HouseType.COLUMN, index = 6),
                HouseRef(type = HouseType.BOX, index = 5),
            ),
            baseHouses = listOf(HouseRef(type = HouseType.ROW, index = 0), HouseRef(type = HouseType.ROW, index = 5)),
            coverHouses = listOf(
                HouseRef(type = HouseType.COLUMN, index = 3),
                HouseRef(type = HouseType.COLUMN, index = 6),
            ),
            finCandidates = setOf(candidate(52, 1)),
        ),
    ),
)

// https://sudoku.coach/en/learn/locked-candidate
private fun simpleColoringType1Example1() = advancedExample(
    givens = "000548002004200908020096054762354891003900647149867235000400580205009470400005129",
    solution = "697548312514273968328196754762354891853921647149867235971432586285619473436785129",
    candidates = "84912p0000001w0x001g2d00001x05000x003o005d1t00001w000000000000000000000040400000030300000000000000000000000084912p001z07000010003p000x3p00000010005g682o5g00000000",
    step = SolveStep(
        technique = TechniqueId.SIMPLE_COLORING_TYPE_1,
        eliminations = listOf(
            CandidateElimination(candidate = candidate(9, 3)),
            CandidateElimination(candidate = candidate(10, 3)),
        ),
        evidence = StepEvidence(
            causeCells = setOf(cell(14), cell(59), cell(13)),
            causeCandidates = setOf(candidate(14, 3), candidate(59, 3), candidate(13, 3)),
            focusDigits = setOf(3),
            houses = listOf(HouseRef(type = HouseType.COLUMN, index = 5), HouseRef(type = HouseType.BOX, index = 1)),
            links = listOf(
                InferenceLink(from = candidate(13, 3), to = candidate(14, 3), type = InferenceLinkType.STRONG),
                InferenceLink(from = candidate(14, 3), to = candidate(59, 3), type = InferenceLinkType.STRONG),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/locked-candidate
private fun simpleColoringType1Example2() = advancedExample(
    givens = "000400020217956483540300070000039048004000300305004200002690154450000800600045700",
    solution = "863417925217956483549382671126739548984521367375864219732698154451273896698145732",
    candidates = "aobobo005d5d8g008h0000000000000000000000bk003n3n8000811t2q0x2b00001c0000chde005v6b5f0081a900dc005d690000819t5c5g0000005g0000000000791v1v1z00848200asat3n0000007876",
    step = SolveStep(
        technique = TechniqueId.SIMPLE_COLORING_TYPE_1,
        eliminations = listOf(CandidateElimination(candidate = candidate(29, 1))),
        evidence = StepEvidence(
            causeCells = setOf(cell(74), cell(75), cell(65)),
            causeCandidates = setOf(candidate(74, 1), candidate(75, 1), candidate(65, 1)),
            focusDigits = setOf(1),
            houses = listOf(HouseRef(type = HouseType.ROW, index = 8), HouseRef(type = HouseType.BOX, index = 6)),
            links = listOf(
                InferenceLink(from = candidate(65, 1), to = candidate(74, 1), type = InferenceLinkType.STRONG),
                InferenceLink(from = candidate(74, 1), to = candidate(75, 1), type = InferenceLinkType.STRONG),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/skyscraper
private fun simpleColoringType2Example1() = advancedExample(
    givens = "004178020529643178718592364001927406270064010496015702140209607902736041007401290",
    solution = "364178925529643178718592364831927456275364819496815732143259687982736541657481293",
    candidates = "1010000000007k007k0000000000000000000000000000000000003o4400000000000k0000000k3o0000b4007o0000003o0000003o0000000k0040000044000040000000004000004k500000400000000k",
    step = SolveStep(
        technique = TechniqueId.SIMPLE_COLORING_TYPE_2,
        placements = listOf(Placement(cell = cell(56), digit = 3), Placement(cell = cell(80), digit = 3)),
        eliminations = listOf(
            CandidateElimination(candidate = candidate(61, 3)),
            CandidateElimination(candidate = candidate(38, 3)),
            CandidateElimination(candidate = candidate(44, 3)),
        ),
        evidence = StepEvidence(
            causeCells = setOf(cell(56), cell(61), cell(38), cell(80), cell(44)),
            causeCandidates = setOf(
                candidate(56, 3),
                candidate(61, 3),
                candidate(38, 3),
                candidate(80, 3),
                candidate(44, 3),
            ),
            focusDigits = setOf(3),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 6),
                HouseRef(type = HouseType.COLUMN, index = 2),
                HouseRef(type = HouseType.COLUMN, index = 8),
                HouseRef(type = HouseType.BOX, index = 8),
            ),
            links = listOf(
                InferenceLink(from = candidate(38, 3), to = candidate(56, 3), type = InferenceLinkType.STRONG),
                InferenceLink(from = candidate(44, 3), to = candidate(80, 3), type = InferenceLinkType.STRONG),
                InferenceLink(from = candidate(56, 3), to = candidate(61, 3), type = InferenceLinkType.STRONG),
                InferenceLink(from = candidate(61, 3), to = candidate(80, 3), type = InferenceLinkType.STRONG),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/skyscraper
private fun simpleColoringType2Example2() = advancedExample(
    givens = "005901034019004200408205179607000040094000001501642790970853410840000300153426987",
    solution = "725981634319764258468235179637519842294378561581642793972853416846197325153426987",
    candidates = "1u0y000068004g00001w00001w4g00001c4w001000001000000000003q000l75ao40003q0600002c1w5c4w0y00003o0000000000003o00000y00000000000y00000y1t758w001e1e000000000000000000",
    step = SolveStep(
        technique = TechniqueId.SIMPLE_COLORING_TYPE_2,
        placements = listOf(Placement(cell = cell(9), digit = 3), Placement(cell = cell(22), digit = 3)),
        eliminations = listOf(
            CandidateElimination(candidate = candidate(12, 3)),
            CandidateElimination(candidate = candidate(36, 3)),
            CandidateElimination(candidate = candidate(19, 3)),
            CandidateElimination(candidate = candidate(40, 3)),
        ),
        evidence = StepEvidence(
            causeCells = setOf(cell(9), cell(12), cell(36), cell(19), cell(22), cell(40)),
            causeCandidates = setOf(
                candidate(9, 3),
                candidate(12, 3),
                candidate(36, 3),
                candidate(19, 3),
                candidate(22, 3),
                candidate(40, 3),
            ),
            focusDigits = setOf(3),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 1),
                HouseRef(type = HouseType.ROW, index = 2),
                HouseRef(type = HouseType.COLUMN, index = 0),
                HouseRef(type = HouseType.COLUMN, index = 4),
                HouseRef(type = HouseType.BOX, index = 0),
                HouseRef(type = HouseType.BOX, index = 1),
            ),
            links = listOf(
                InferenceLink(from = candidate(9, 3), to = candidate(12, 3), type = InferenceLinkType.STRONG),
                InferenceLink(from = candidate(9, 3), to = candidate(36, 3), type = InferenceLinkType.STRONG),
                InferenceLink(from = candidate(9, 3), to = candidate(19, 3), type = InferenceLinkType.STRONG),
                InferenceLink(from = candidate(12, 3), to = candidate(22, 3), type = InferenceLinkType.STRONG),
                InferenceLink(from = candidate(19, 3), to = candidate(22, 3), type = InferenceLinkType.STRONG),
                InferenceLink(from = candidate(22, 3), to = candidate(40, 3), type = InferenceLinkType.STRONG),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/empty-rectangle
private fun emptyRectangleExample1() = advancedExample(
    givens = "832509160715060390694103025251007030348000050967350040573006419429715683186934572",
    solution = "832549167715268394694173825251487936348692751967351248573826419429715683186934572",
    candidates = "0000000020000000200000003u003m00003s000000005c005c00000000004oaw00ao004g0000000y76038y002p00000000003n3m003l0000003m3m00000000000000000000000000000000000000000000",
    step = SolveStep(
        technique = TechniqueId.EMPTY_RECTANGLE,
        eliminations = listOf(CandidateElimination(candidate = candidate(51, 8))),
        evidence = StepEvidence(
            causeCells = setOf(cell(14), cell(50), cell(17), cell(24)),
            causeCandidates = setOf(candidate(14, 8), candidate(50, 8), candidate(17, 8), candidate(24, 8)),
            focusDigits = setOf(8),
            houses = listOf(
                HouseRef(type = HouseType.COLUMN, index = 5),
                HouseRef(type = HouseType.ROW, index = 1),
                HouseRef(type = HouseType.BOX, index = 2),
                HouseRef(type = HouseType.COLUMN, index = 6),
            ),
            links = listOf(
                InferenceLink(from = candidate(14, 8), to = candidate(50, 8), type = InferenceLinkType.STRONG),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/empty-rectangle
private fun emptyRectangleExample2() = advancedExample(
    givens = "612750000953402067874906002485200601391600025267501000136875249548329006729164008",
    solution = "612758394953412867874936152485293671391687425267541983136875249548329716729164538",
    candidates = "00000000003ob0as0c000000003l003l00000000000005000l0l0000000000781w009000000000003s5c200000000000007g00b0as0c0000000000000000000000000000001t1t000000000000000k0k00",
    step = SolveStep(
        technique = TechniqueId.EMPTY_RECTANGLE,
        eliminations = listOf(CandidateElimination(candidate = candidate(49, 3))),
        evidence = StepEvidence(
            causeCells = setOf(cell(8), cell(53), cell(5), cell(22)),
            causeCandidates = setOf(candidate(8, 3), candidate(53, 3), candidate(5, 3), candidate(22, 3)),
            focusDigits = setOf(3),
            houses = listOf(
                HouseRef(type = HouseType.COLUMN, index = 8),
                HouseRef(type = HouseType.ROW, index = 0),
                HouseRef(type = HouseType.BOX, index = 1),
                HouseRef(type = HouseType.COLUMN, index = 4),
            ),
            links = listOf(
                InferenceLink(from = candidate(8, 3), to = candidate(53, 3), type = InferenceLinkType.STRONG),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/swordfish
private fun swordfishExample1() = advancedExample(
    givens = "082053000007080050000417800061700005500140009700506000900071506000004000870005241",
    solution = "482653917617289354395417862261798435538142679749536128924371586156824793873965241",
    candidates = "150000800000a19t20197h008200768d000c107o8k0000000086060a0000007aaq0c3q0000063o00003m2o2o00007eaw0006000d3r3y000e0c3q0000003o00130j1g3q0y0090ck5g000010788000000000",
    step = SolveStep(
        technique = TechniqueId.SWORDFISH,
        eliminations = listOf(
            CandidateElimination(candidate = candidate(46, 2)),
            CandidateElimination(candidate = candidate(64, 2)),
            CandidateElimination(candidate = candidate(66, 2)),
            CandidateElimination(candidate = candidate(32, 2)),
        ),
        evidence = StepEvidence(
            causeCells = setOf(cell(12), cell(14), cell(37), cell(41), cell(55), cell(57)),
            causeCandidates = setOf(
                candidate(12, 2),
                candidate(14, 2),
                candidate(37, 2),
                candidate(41, 2),
                candidate(55, 2),
                candidate(57, 2),
            ),
            focusDigits = setOf(2),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 1),
                HouseRef(type = HouseType.ROW, index = 4),
                HouseRef(type = HouseType.ROW, index = 6),
                HouseRef(type = HouseType.COLUMN, index = 1),
                HouseRef(type = HouseType.COLUMN, index = 3),
                HouseRef(type = HouseType.COLUMN, index = 5),
            ),
            baseHouses = listOf(
                HouseRef(type = HouseType.ROW, index = 1),
                HouseRef(type = HouseType.ROW, index = 4),
                HouseRef(type = HouseType.ROW, index = 6),
            ),
            coverHouses = listOf(
                HouseRef(type = HouseType.COLUMN, index = 1),
                HouseRef(type = HouseType.COLUMN, index = 3),
                HouseRef(type = HouseType.COLUMN, index = 5),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/swordfish
private fun swordfishExample2() = advancedExample(
    givens = "875020013249100780316780290001000027730251960962847531120370059003090170097010340",
    solution = "875629413249135786316784295451963827738251964962847531124378659583496172697512348",
    candidates = "00000088008814000000000000100k00001c00000000000o00000o0o40008010783s000000003s00000000003s00000000000000000000003s00004o4g00001k40001k004i00004i1c00001c003m00003m",
    step = SolveStep(
        technique = TechniqueId.SWORDFISH,
        eliminations = listOf(CandidateElimination(candidate = candidate(5, 4))),
        evidence = StepEvidence(
            causeCells = setOf(cell(23), cell(26), cell(38), cell(44), cell(56), cell(59)),
            causeCandidates = setOf(
                candidate(23, 4),
                candidate(26, 4),
                candidate(38, 4),
                candidate(44, 4),
                candidate(56, 4),
                candidate(59, 4),
            ),
            focusDigits = setOf(4),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 2),
                HouseRef(type = HouseType.ROW, index = 4),
                HouseRef(type = HouseType.ROW, index = 6),
                HouseRef(type = HouseType.COLUMN, index = 2),
                HouseRef(type = HouseType.COLUMN, index = 5),
                HouseRef(type = HouseType.COLUMN, index = 8),
            ),
            baseHouses = listOf(
                HouseRef(type = HouseType.ROW, index = 2),
                HouseRef(type = HouseType.ROW, index = 4),
                HouseRef(type = HouseType.ROW, index = 6),
            ),
            coverHouses = listOf(
                HouseRef(type = HouseType.COLUMN, index = 2),
                HouseRef(type = HouseType.COLUMN, index = 5),
                HouseRef(type = HouseType.COLUMN, index = 8),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/unique-rectangle
private fun uniqueRectangleType1Example1() = advancedExample(
    givens = "620470095008000406054000073096700304380946000047001609060290040009104562412007938",
    solution = "621473895738519426954862173196725384385946217247381659563298741879134562412657938",
    candidates = "00000500003o3l00008x1w000k0n7m0003007500004g4j763n00000j000000420i00410000000h0000001v0j1t0i000044460000400040000k00003o1t001t5c1w00003o000000000000001c1c00000000",
    step = SolveStep(
        technique = TechniqueId.UNIQUE_RECTANGLE_TYPE_1,
        eliminations = listOf(
            CandidateElimination(candidate = candidate(42, 1)),
            CandidateElimination(candidate = candidate(42, 7)),
        ),
        evidence = StepEvidence(
            causeCells = setOf(cell(42), cell(44), cell(60), cell(62)),
            causeCandidates = setOf(
                candidate(42, 1),
                candidate(42, 7),
                candidate(44, 1),
                candidate(44, 7),
                candidate(60, 1),
                candidate(60, 7),
                candidate(62, 1),
                candidate(62, 7),
            ),
            focusDigits = setOf(1, 7),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 4),
                HouseRef(type = HouseType.ROW, index = 6),
                HouseRef(type = HouseType.COLUMN, index = 6),
                HouseRef(type = HouseType.COLUMN, index = 8),
                HouseRef(type = HouseType.BOX, index = 5),
                HouseRef(type = HouseType.BOX, index = 8),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/unique-rectangle
private fun uniqueRectangleType1Example2() = advancedExample(
    givens = "509861002602094080108200900921400070365002800784059210250900400410020000897040020",
    solution = "579861342632794185148235967921486573365172894784359216256917438413628759897543621",
    candidates = "0024000000001w0c00001w002c00002d002d002400001w2c001o3c000000003o4k1g001g0000001t1t00007c7c000000100000000010000010005d5c00105d0000102s006s388kds0000001h001g1h001d",
    step = SolveStep(
        technique = TechniqueId.UNIQUE_RECTANGLE_TYPE_1,
        eliminations = listOf(
            CandidateElimination(candidate = candidate(70, 3)),
            CandidateElimination(candidate = candidate(70, 6)),
        ),
        evidence = StepEvidence(
            causeCells = setOf(cell(56), cell(61), cell(65), cell(70)),
            causeCandidates = setOf(
                candidate(56, 3),
                candidate(56, 6),
                candidate(61, 3),
                candidate(61, 6),
                candidate(65, 3),
                candidate(65, 6),
                candidate(70, 3),
                candidate(70, 6),
            ),
            focusDigits = setOf(3, 6),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 6),
                HouseRef(type = HouseType.ROW, index = 7),
                HouseRef(type = HouseType.COLUMN, index = 2),
                HouseRef(type = HouseType.COLUMN, index = 7),
                HouseRef(type = HouseType.BOX, index = 6),
                HouseRef(type = HouseType.BOX, index = 8),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/unique-rectangle
private fun uniqueRectangleType2Example1() = advancedExample(
    givens = "182537946657914823900682517009050004310049005405000098001490682200108059090000401",
    solution = "182537946657914823934682517729856134318749265465321798571493682246178359893265471",
    candidates = "000000000000000000000000000000000000000c0c0000000000005c2q005i000x07100000004g5e00001u2o00002q001y2q111z0000281w0000000k000000001818002o001w000040004k1y2q1g001w00",
    step = SolveStep(
        technique = TechniqueId.UNIQUE_RECTANGLE_TYPE_2,
        eliminations = listOf(
            CandidateElimination(candidate = candidate(67, 6)),
            CandidateElimination(candidate = candidate(74, 6)),
        ),
        evidence = StepEvidence(
            causeCells = setOf(cell(19), cell(20), cell(64), cell(65)),
            causeCandidates = setOf(
                candidate(19, 3),
                candidate(19, 4),
                candidate(20, 3),
                candidate(20, 4),
                candidate(64, 3),
                candidate(64, 4),
                candidate(64, 6),
                candidate(65, 3),
                candidate(65, 4),
                candidate(65, 6),
            ),
            focusDigits = setOf(3, 4),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 2),
                HouseRef(type = HouseType.ROW, index = 7),
                HouseRef(type = HouseType.COLUMN, index = 1),
                HouseRef(type = HouseType.COLUMN, index = 2),
                HouseRef(type = HouseType.BOX, index = 0),
                HouseRef(type = HouseType.BOX, index = 6),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/unique-rectangle
private fun uniqueRectangleType2Example2() = advancedExample(
    givens = "040008920900742081000900000090000000053209100000050209109004000000100090600897410",
    solution = "547318926936742581812965347291683754453279168768451239189534672374126895625897413",
    candidates = "2c00291g110000002o00101c0000000k00005y035f00111g383g2w22002z185c115w2k64200000005c00002w6g5k0x6h180011002400005c001g120068386u0s5c0o00121g68006u00060i00000000000k",
    step = SolveStep(
        technique = TechniqueId.UNIQUE_RECTANGLE_TYPE_2,
        eliminations = listOf(
            CandidateElimination(candidate = candidate(24, 6)),
            CandidateElimination(candidate = candidate(61, 6)),
            CandidateElimination(candidate = candidate(62, 6)),
            CandidateElimination(candidate = candidate(71, 6)),
        ),
        evidence = StepEvidence(
            causeCells = setOf(cell(55), cell(60), cell(64), cell(69)),
            causeCandidates = setOf(
                candidate(55, 7),
                candidate(55, 8),
                candidate(60, 6),
                candidate(60, 7),
                candidate(60, 8),
                candidate(64, 7),
                candidate(64, 8),
                candidate(69, 6),
                candidate(69, 7),
                candidate(69, 8),
            ),
            focusDigits = setOf(7, 8),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 6),
                HouseRef(type = HouseType.ROW, index = 7),
                HouseRef(type = HouseType.COLUMN, index = 1),
                HouseRef(type = HouseType.COLUMN, index = 6),
                HouseRef(type = HouseType.BOX, index = 6),
                HouseRef(type = HouseType.BOX, index = 8),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/unique-rectangle
private fun uniqueRectangleType3Example1() = advancedExample(
    givens = "608703000900060087170048600260070008530000046089030500396000005012000803850320100",
    solution = "628793451945261387173548629264175938531982746789436512396814275412657893857329164",
    candidates = "000a00007k007e7l7d000a0k0j000j0e000000000k7m0000007q760000097t007t78790000001tar3l778y0000200000170017001v030000003t3l21221u002000008o7kag009s000000200000a000807c",
    step = SolveStep(
        technique = TechniqueId.UNIQUE_RECTANGLE_TYPE_3,
        eliminations = listOf(CandidateElimination(candidate = candidate(42, 9))),
        evidence = StepEvidence(
            causeCells = setOf(cell(1), cell(6), cell(10), cell(15), cell(33)),
            causeCandidates = setOf(
                candidate(1, 2),
                candidate(1, 4),
                candidate(6, 2),
                candidate(6, 4),
                candidate(10, 2),
                candidate(10, 4),
                candidate(15, 2),
                candidate(15, 4),
                candidate(33, 3),
                candidate(33, 9),
            ),
            focusDigits = setOf(2, 4),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 0),
                HouseRef(type = HouseType.ROW, index = 1),
                HouseRef(type = HouseType.COLUMN, index = 1),
                HouseRef(type = HouseType.COLUMN, index = 6),
                HouseRef(type = HouseType.BOX, index = 0),
                HouseRef(type = HouseType.BOX, index = 2),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/unique-rectangle
private fun uniqueRectangleType3Example2() = advancedExample(
    givens = "008400507054700002706305040970602800800179000000508790509014370487963000010057409",
    solution = "238491567154786932796325148971632854845179623362548791529814376487963215613257489",
    candidates = "077a0000760x00100005000000ao0x804k0000760000aq0075003l00000l000c00000h0t000c0k0000000y0i1o121a07000c0000000d000y003m000000004g000000000000030j0h1200063m0000004g00",
    step = SolveStep(
        technique = TechniqueId.UNIQUE_RECTANGLE_TYPE_3,
        eliminations = listOf(
            CandidateElimination(candidate = candidate(26, 1)),
            CandidateElimination(candidate = candidate(44, 5)),
        ),
        evidence = StepEvidence(
            causeCells = setOf(cell(31), cell(35), cell(49), cell(53), cell(71)),
            causeCandidates = setOf(
                candidate(31, 3),
                candidate(31, 4),
                candidate(35, 3),
                candidate(35, 4),
                candidate(49, 3),
                candidate(49, 4),
                candidate(53, 3),
                candidate(53, 4),
                candidate(71, 1),
                candidate(71, 5),
            ),
            focusDigits = setOf(3, 4),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 3),
                HouseRef(type = HouseType.ROW, index = 5),
                HouseRef(type = HouseType.COLUMN, index = 4),
                HouseRef(type = HouseType.COLUMN, index = 8),
                HouseRef(type = HouseType.BOX, index = 4),
                HouseRef(type = HouseType.BOX, index = 5),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/unique-rectangle
private fun uniqueRectangleType4Example1() = advancedExample(
    givens = "182537946657914823900682517009050004310049005405000098001490682200108059090000401",
    solution = "182537946657914823934682517729856134318749265465321798571493682246178359893265471",
    candidates = "000000000000000000000000000000000000000c0c0000000000005c2q005i000x07100000004g5e00001u2o00002q001y2q111z0000281w0000000k000000001818001s0004000040003o1y2q1g001w00",
    step = SolveStep(
        technique = TechniqueId.UNIQUE_RECTANGLE_TYPE_4,
        eliminations = listOf(
            CandidateElimination(candidate = candidate(64, 3)),
            CandidateElimination(candidate = candidate(65, 3)),
        ),
        evidence = StepEvidence(
            causeCells = setOf(cell(19), cell(20), cell(64), cell(65)),
            causeCandidates = setOf(
                candidate(19, 3),
                candidate(19, 4),
                candidate(20, 3),
                candidate(20, 4),
                candidate(64, 3),
                candidate(64, 4),
                candidate(64, 6),
                candidate(65, 3),
                candidate(65, 4),
                candidate(65, 6),
            ),
            focusDigits = setOf(3, 4),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 2),
                HouseRef(type = HouseType.ROW, index = 7),
                HouseRef(type = HouseType.COLUMN, index = 1),
                HouseRef(type = HouseType.COLUMN, index = 2),
                HouseRef(type = HouseType.BOX, index = 0),
                HouseRef(type = HouseType.BOX, index = 6),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/unique-rectangle
private fun uniqueRectangleType4Example2() = advancedExample(
    givens = "608703000900060087170048600260070008530000046089030500396000005012000803850320100",
    solution = "628793451945261387173548629264175938531982746789436512396814275412657893857329164",
    candidates = "000a00007k007e7l7d000a0k0j000j0e000000000k7m0000007q760000097t007t78790000001tar3l771u0000200000170017001v030000003t3l21221u002000008o7kag009s000000200000a000807c",
    step = SolveStep(
        technique = TechniqueId.UNIQUE_RECTANGLE_TYPE_4,
        eliminations = listOf(
            CandidateElimination(candidate = candidate(39, 1)),
            CandidateElimination(candidate = candidate(57, 1)),
        ),
        evidence = StepEvidence(
            causeCells = setOf(cell(39), cell(40), cell(57), cell(58)),
            causeCandidates = setOf(
                candidate(39, 1),
                candidate(39, 2),
                candidate(39, 8),
                candidate(39, 9),
                candidate(40, 1),
                candidate(40, 8),
                candidate(57, 1),
                candidate(57, 4),
                candidate(57, 8),
                candidate(58, 1),
                candidate(58, 8),
            ),
            focusDigits = setOf(1, 8),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 4),
                HouseRef(type = HouseType.ROW, index = 6),
                HouseRef(type = HouseType.COLUMN, index = 3),
                HouseRef(type = HouseType.COLUMN, index = 4),
                HouseRef(type = HouseType.BOX, index = 4),
                HouseRef(type = HouseType.BOX, index = 7),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/unique-rectangle
private fun uniqueRectangleType5Example1() = advancedExample(
    givens = "000576003309241070070893120007310049000759001913460750095137000020685007730924010",
    solution = "281576493359241678476893125567312849842759361913468752695137284124685937738924516",
    candidates = "033s03000000awao00004w000000001c004w1k001400000000001k1e1c0000003m4i00004q143u00000012100000000000003m00003m4g00000000000a4g0a09000900000078780000004g0000004w001c",
    step = SolveStep(
        technique = TechniqueId.UNIQUE_RECTANGLE_TYPE_5,
        eliminations = listOf(CandidateElimination(candidate = candidate(6, 8))),
        evidence = StepEvidence(
            causeCells = setOf(cell(15), cell(17), cell(78), cell(80)),
            causeCandidates = setOf(
                candidate(15, 5),
                candidate(15, 6),
                candidate(17, 5),
                candidate(17, 6),
                candidate(17, 8),
                candidate(78, 5),
                candidate(78, 6),
                candidate(78, 8),
                candidate(80, 5),
                candidate(80, 6),
            ),
            focusDigits = setOf(5, 6),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 1),
                HouseRef(type = HouseType.ROW, index = 8),
                HouseRef(type = HouseType.COLUMN, index = 6),
                HouseRef(type = HouseType.COLUMN, index = 8),
                HouseRef(type = HouseType.BOX, index = 2),
                HouseRef(type = HouseType.BOX, index = 8),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/unique-rectangle
private fun uniqueRectangleType5Example2() = advancedExample(
    givens = "980510460502640918461089070795008146148006320326104800854061030600400081210800604",
    solution = "987512463532647918461389275795238146148756329326194857854961732679423581213875694",
    candidates = "00001w00001y000006001w0000001w0000000000000600000i000m0000000606000000000000008w280000009c000000009c00007k280000008w00001u0076001w9000920m28000000009000900k007k00",
    step = SolveStep(
        technique = TechniqueId.UNIQUE_RECTANGLE_TYPE_5,
        eliminations = listOf(
            CandidateElimination(candidate = candidate(39, 9)),
            CandidateElimination(candidate = candidate(52, 9)),
        ),
        evidence = StepEvidence(
            causeCells = setOf(cell(40), cell(44), cell(49), cell(53)),
            causeCandidates = setOf(
                candidate(40, 5),
                candidate(40, 7),
                candidate(44, 5),
                candidate(44, 7),
                candidate(44, 9),
                candidate(49, 5),
                candidate(49, 7),
                candidate(49, 9),
                candidate(53, 5),
                candidate(53, 7),
            ),
            focusDigits = setOf(5, 7),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 4),
                HouseRef(type = HouseType.ROW, index = 5),
                HouseRef(type = HouseType.COLUMN, index = 4),
                HouseRef(type = HouseType.COLUMN, index = 8),
                HouseRef(type = HouseType.BOX, index = 4),
                HouseRef(type = HouseType.BOX, index = 5),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/jellyfish
private fun jellyfishExample1() = advancedExample(
    givens = "670000351219537600053610927520169730397400162061273095932001506186000003745306019",
    solution = "674892351219537648853614927528169734397458162461273895932741586186925473745386219",
    candidates = "00003saoay3u000000000000000000003s3s3s000000003s00000000003s00000000003s0000000040400000003s00000000003s00000000005c3s00005k000000008w7u0q0a2000000000003m003m0000",
    step = SolveStep(
        technique = TechniqueId.JELLYFISH,
        eliminations = listOf(
            CandidateElimination(candidate = candidate(4, 8)),
            CandidateElimination(candidate = candidate(58, 8)),
            CandidateElimination(candidate = candidate(5, 8)),
        ),
        evidence = StepEvidence(
            causeCells = setOf(cell(18), cell(23), cell(40), cell(41), cell(45), cell(51), cell(76), cell(78)),
            causeCandidates = setOf(
                candidate(18, 8),
                candidate(23, 8),
                candidate(40, 8),
                candidate(41, 8),
                candidate(45, 8),
                candidate(51, 8),
                candidate(76, 8),
                candidate(78, 8),
            ),
            focusDigits = setOf(8),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 2),
                HouseRef(type = HouseType.ROW, index = 4),
                HouseRef(type = HouseType.ROW, index = 5),
                HouseRef(type = HouseType.ROW, index = 8),
                HouseRef(type = HouseType.COLUMN, index = 0),
                HouseRef(type = HouseType.COLUMN, index = 4),
                HouseRef(type = HouseType.COLUMN, index = 5),
                HouseRef(type = HouseType.COLUMN, index = 6),
            ),
            baseHouses = listOf(
                HouseRef(type = HouseType.ROW, index = 2),
                HouseRef(type = HouseType.ROW, index = 4),
                HouseRef(type = HouseType.ROW, index = 5),
                HouseRef(type = HouseType.ROW, index = 8),
            ),
            coverHouses = listOf(
                HouseRef(type = HouseType.COLUMN, index = 0),
                HouseRef(type = HouseType.COLUMN, index = 4),
                HouseRef(type = HouseType.COLUMN, index = 5),
                HouseRef(type = HouseType.COLUMN, index = 6),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/jellyfish
private fun jellyfishExample2() = advancedExample(
    givens = "040195026261304000500620413050041209418269537000053041034500002075932004900410300",
    solution = "843195726261374958597628413356741289418269537729853641634587192175932864982416375",
    candidates = "3o001w0000005c0000000000005c00cg7k4000ao8w00005c0000002s00105c0000004g000000000000000000002o76765c00004g00004h0000005c68758w003l00000000004h4g00003m0y000068002840",
    step = SolveStep(
        technique = TechniqueId.JELLYFISH,
        eliminations = listOf(
            CandidateElimination(candidate = candidate(54, 8)),
            CandidateElimination(candidate = candidate(15, 8)),
        ),
        evidence = StepEvidence(
            causeCells = setOf(cell(0), cell(6), cell(30), cell(34), cell(48), cell(51), cell(63), cell(69), cell(70)),
            causeCandidates = setOf(
                candidate(0, 8),
                candidate(6, 8),
                candidate(30, 8),
                candidate(34, 8),
                candidate(48, 8),
                candidate(51, 8),
                candidate(63, 8),
                candidate(69, 8),
                candidate(70, 8),
            ),
            focusDigits = setOf(8),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 0),
                HouseRef(type = HouseType.ROW, index = 3),
                HouseRef(type = HouseType.ROW, index = 5),
                HouseRef(type = HouseType.ROW, index = 7),
                HouseRef(type = HouseType.COLUMN, index = 0),
                HouseRef(type = HouseType.COLUMN, index = 3),
                HouseRef(type = HouseType.COLUMN, index = 6),
                HouseRef(type = HouseType.COLUMN, index = 7),
            ),
            baseHouses = listOf(
                HouseRef(type = HouseType.ROW, index = 0),
                HouseRef(type = HouseType.ROW, index = 3),
                HouseRef(type = HouseType.ROW, index = 5),
                HouseRef(type = HouseType.ROW, index = 7),
            ),
            coverHouses = listOf(
                HouseRef(type = HouseType.COLUMN, index = 0),
                HouseRef(type = HouseType.COLUMN, index = 3),
                HouseRef(type = HouseType.COLUMN, index = 6),
                HouseRef(type = HouseType.COLUMN, index = 7),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/finned-swordfish
private fun finnedSwordfishExample1() = advancedExample(
    givens = "410057023005903147000241508001000354530400816004135279040500701050714032172390405",
    solution = "416857923285963147397241568721689354539472816864135279643528791958714632172396485",
    candidates = "0000bk4g00008000003m4i00004g000000001w801w0000000080008y76004g68bk00000000008w001u760000004g4g00000000000000840010004i4i00bk00ao00bk00000080000000000000004g004g00",
    step = SolveStep(
        technique = TechniqueId.FINNED_SWORDFISH,
        eliminations = listOf(CandidateElimination(candidate = candidate(58, 6))),
        evidence = StepEvidence(
            causeCells = setOf(cell(10), cell(13), cell(19), cell(25), cell(77), cell(79)),
            causeCandidates = setOf(
                candidate(10, 6),
                candidate(13, 6),
                candidate(19, 6),
                candidate(25, 6),
                candidate(77, 6),
                candidate(79, 6),
            ),
            focusDigits = setOf(6),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 1),
                HouseRef(type = HouseType.ROW, index = 2),
                HouseRef(type = HouseType.ROW, index = 8),
                HouseRef(type = HouseType.COLUMN, index = 1),
                HouseRef(type = HouseType.COLUMN, index = 4),
                HouseRef(type = HouseType.COLUMN, index = 7),
                HouseRef(type = HouseType.BOX, index = 7),
            ),
            baseHouses = listOf(
                HouseRef(type = HouseType.ROW, index = 1),
                HouseRef(type = HouseType.ROW, index = 2),
                HouseRef(type = HouseType.ROW, index = 8),
            ),
            coverHouses = listOf(
                HouseRef(type = HouseType.COLUMN, index = 1),
                HouseRef(type = HouseType.COLUMN, index = 4),
                HouseRef(type = HouseType.COLUMN, index = 7),
            ),
            finCandidates = setOf(candidate(77, 6)),
        ),
    ),
)

// https://sudoku.coach/en/learn/finned-swordfish
private fun finnedSwordfishExample2() = advancedExample(
    givens = "200900051003001760070400000600000040000000005508309000009005007000002380702000500",
    solution = "286973451453821769971456238627518943394267815518349672849635127165792384732184596",
    candidates = "004o14001w1w3s0000awbc004242000000ayap000h004y4gaq7aau007b1t5v5v5car00au7h7j216b6j6gbn9300000b00002z000z1v0y3x4t004h4t00170300091l1d2pa100000088004k004hbx4s007588",
    step = SolveStep(
        technique = TechniqueId.FINNED_SWORDFISH,
        eliminations = listOf(CandidateElimination(candidate = candidate(58, 4))),
        evidence = StepEvidence(
            causeCells = setOf(cell(2), cell(38), cell(41), cell(77), cell(6), cell(60)),
            causeCandidates = setOf(
                candidate(2, 4),
                candidate(38, 4),
                candidate(41, 4),
                candidate(77, 4),
                candidate(6, 4),
                candidate(60, 4),
            ),
            focusDigits = setOf(4),
            houses = listOf(
                HouseRef(type = HouseType.COLUMN, index = 2),
                HouseRef(type = HouseType.COLUMN, index = 5),
                HouseRef(type = HouseType.COLUMN, index = 6),
                HouseRef(type = HouseType.ROW, index = 0),
                HouseRef(type = HouseType.ROW, index = 4),
                HouseRef(type = HouseType.ROW, index = 6),
                HouseRef(type = HouseType.BOX, index = 7),
            ),
            baseHouses = listOf(
                HouseRef(type = HouseType.COLUMN, index = 2),
                HouseRef(type = HouseType.COLUMN, index = 5),
                HouseRef(type = HouseType.COLUMN, index = 6),
            ),
            coverHouses = listOf(
                HouseRef(type = HouseType.ROW, index = 0),
                HouseRef(type = HouseType.ROW, index = 4),
                HouseRef(type = HouseType.ROW, index = 6),
            ),
            finCandidates = setOf(candidate(77, 4)),
        ),
    ),
)

// https://sudoku.coach/en/learn/finned-jellyfish
private fun finnedJellyfishExample1() = advancedExample(
    givens = "036425001120380600400610023004238016062194307013756002200961030601503200309802160",
    solution = "936425871127389654458617923794238516562194387813756492245961738681573249379842165",
    candidates = "8w0000000000cgcg0000002800008w009k7s00cw5s00008w9c00009c8w000000007k0000400000000000004000ao0000000000awaw0000605s0000002g0048005k000020000094aw002g0000200000000o",
    step = SolveStep(
        technique = TechniqueId.FINNED_JELLYFISH,
        eliminations = listOf(CandidateElimination(candidate = candidate(16, 7))),
        evidence = StepEvidence(
            causeCells = setOf(
                cell(0),
                cell(6),
                cell(7),
                cell(27),
                cell(28),
                cell(64),
                cell(67),
                cell(70),
                cell(73),
                cell(76),
            ),
            causeCandidates = setOf(
                candidate(0, 7),
                candidate(6, 7),
                candidate(7, 7),
                candidate(27, 7),
                candidate(28, 7),
                candidate(64, 7),
                candidate(67, 7),
                candidate(70, 7),
                candidate(73, 7),
                candidate(76, 7),
            ),
            focusDigits = setOf(7),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 0),
                HouseRef(type = HouseType.ROW, index = 3),
                HouseRef(type = HouseType.ROW, index = 7),
                HouseRef(type = HouseType.ROW, index = 8),
                HouseRef(type = HouseType.COLUMN, index = 0),
                HouseRef(type = HouseType.COLUMN, index = 1),
                HouseRef(type = HouseType.COLUMN, index = 4),
                HouseRef(type = HouseType.COLUMN, index = 7),
                HouseRef(type = HouseType.BOX, index = 2),
            ),
            baseHouses = listOf(
                HouseRef(type = HouseType.ROW, index = 0),
                HouseRef(type = HouseType.ROW, index = 3),
                HouseRef(type = HouseType.ROW, index = 7),
                HouseRef(type = HouseType.ROW, index = 8),
            ),
            coverHouses = listOf(
                HouseRef(type = HouseType.COLUMN, index = 0),
                HouseRef(type = HouseType.COLUMN, index = 1),
                HouseRef(type = HouseType.COLUMN, index = 4),
                HouseRef(type = HouseType.COLUMN, index = 7),
            ),
            finCandidates = setOf(candidate(6, 7)),
        ),
    ),
)

// https://sudoku.coach/en/learn/finned-jellyfish
private fun finnedJellyfishExample2() = advancedExample(
    givens = "800005090009076280402000700034061050090700036080300001900000540040508000520047300",
    solution = "873215694159476283462983715734861952291754836685329471917632548346598127528147369",
    candidates = "002p2s0a07000x000c050h0009000000000s001d00ap3o78000x0k1u0000ao0000ao001u03000h00420a3s00002q001c007m7e7c1u00002p690z070600005c11002s0075000x1u8y00004g810000000xao",
    step = SolveStep(
        technique = TechniqueId.FINNED_JELLYFISH,
        eliminations = listOf(CandidateElimination(candidate = candidate(1, 1))),
        evidence = StepEvidence(
            causeCells = setOf(
                cell(9),
                cell(36),
                cell(63),
                cell(38),
                cell(56),
                cell(4),
                cell(58),
                cell(67),
                cell(6),
                cell(69),
            ),
            causeCandidates = setOf(
                candidate(9, 1),
                candidate(36, 1),
                candidate(63, 1),
                candidate(38, 1),
                candidate(56, 1),
                candidate(4, 1),
                candidate(58, 1),
                candidate(67, 1),
                candidate(6, 1),
                candidate(69, 1),
            ),
            focusDigits = setOf(1),
            houses = listOf(
                HouseRef(type = HouseType.COLUMN, index = 0),
                HouseRef(type = HouseType.COLUMN, index = 2),
                HouseRef(type = HouseType.COLUMN, index = 4),
                HouseRef(type = HouseType.COLUMN, index = 6),
                HouseRef(type = HouseType.ROW, index = 0),
                HouseRef(type = HouseType.ROW, index = 4),
                HouseRef(type = HouseType.ROW, index = 6),
                HouseRef(type = HouseType.ROW, index = 7),
                HouseRef(type = HouseType.BOX, index = 0),
            ),
            baseHouses = listOf(
                HouseRef(type = HouseType.COLUMN, index = 0),
                HouseRef(type = HouseType.COLUMN, index = 2),
                HouseRef(type = HouseType.COLUMN, index = 4),
                HouseRef(type = HouseType.COLUMN, index = 6),
            ),
            coverHouses = listOf(
                HouseRef(type = HouseType.ROW, index = 0),
                HouseRef(type = HouseType.ROW, index = 4),
                HouseRef(type = HouseType.ROW, index = 6),
                HouseRef(type = HouseType.ROW, index = 7),
            ),
            finCandidates = setOf(candidate(9, 1)),
        ),
    ),
)

// https://sudoku.coach/en/learn/bug-plus-one
private fun bugPlusOneExample1() = advancedExample(
    givens = "900217548200586397785943261009758432000129675572634189050391724090472856427865913",
    solution = "936217548214586397785943261169758432843129675572634189658391724391472856427865913",
    candidates = "0010100000000000000009090000000000000000000000000000000x0x000000000000003o0c3w0000000000000000000000000000004g004g000000000000050005000000000000000000000000000000",
    step = SolveStep(
        technique = TechniqueId.BUG_PLUS_ONE,
        placements = listOf(Placement(cell = cell(38), digit = 3)),
        evidence = StepEvidence(
            causeCells = setOf(
                cell(1),
                cell(2),
                cell(10),
                cell(11),
                cell(27),
                cell(28),
                cell(36),
                cell(37),
                cell(38),
                cell(54),
                cell(56),
                cell(63),
                cell(65),
            ),
            causeCandidates = setOf(
                candidate(1, 3),
                candidate(1, 6),
                candidate(2, 3),
                candidate(2, 6),
                candidate(10, 1),
                candidate(10, 4),
                candidate(11, 1),
                candidate(11, 4),
                candidate(27, 1),
                candidate(27, 6),
                candidate(28, 1),
                candidate(28, 6),
                candidate(36, 3),
                candidate(36, 8),
                candidate(37, 3),
                candidate(37, 4),
                candidate(38, 3),
                candidate(38, 4),
                candidate(38, 8),
                candidate(54, 6),
                candidate(54, 8),
                candidate(56, 6),
                candidate(56, 8),
                candidate(63, 1),
                candidate(63, 3),
                candidate(65, 1),
                candidate(65, 3),
            ),
            focusDigits = setOf(3),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 4),
                HouseRef(type = HouseType.COLUMN, index = 2),
                HouseRef(type = HouseType.BOX, index = 3),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/bug-plus-one
private fun bugPlusOneExample2() = advancedExample(
    givens = "010236000038795102000184300791653284284917500300428719879341625500872901100569070",
    solution = "917236458438795162625184397791653284284917536356428719879341625563872941142569873",
    candidates = "7c00280000003s7k5c140000000000001400800i2a000000007k2o000000000000000000000000000000001010001c1c000000000000000000000000000000001410000000000c00000a060000003s003o",
    step = SolveStep(
        technique = TechniqueId.BUG_PLUS_ONE,
        placements = listOf(Placement(cell = cell(20), digit = 5)),
        evidence = StepEvidence(
            causeCells = setOf(
                cell(0),
                cell(2),
                cell(6),
                cell(7),
                cell(8),
                cell(9),
                cell(16),
                cell(18),
                cell(19),
                cell(20),
                cell(25),
                cell(26),
                cell(43),
                cell(44),
                cell(46),
                cell(47),
                cell(64),
                cell(65),
                cell(70),
                cell(73),
                cell(74),
                cell(78),
                cell(80),
            ),
            causeCandidates = setOf(
                candidate(0, 4),
                candidate(0, 9),
                candidate(2, 5),
                candidate(2, 7),
                candidate(6, 4),
                candidate(6, 8),
                candidate(7, 5),
                candidate(7, 9),
                candidate(8, 7),
                candidate(8, 8),
                candidate(9, 4),
                candidate(9, 6),
                candidate(16, 4),
                candidate(16, 6),
                candidate(18, 6),
                candidate(18, 9),
                candidate(19, 2),
                candidate(19, 5),
                candidate(20, 2),
                candidate(20, 5),
                candidate(20, 7),
                candidate(25, 5),
                candidate(25, 9),
                candidate(26, 6),
                candidate(26, 7),
                candidate(43, 3),
                candidate(43, 6),
                candidate(44, 3),
                candidate(44, 6),
                candidate(46, 5),
                candidate(46, 6),
                candidate(47, 5),
                candidate(47, 6),
                candidate(64, 4),
                candidate(64, 6),
                candidate(65, 3),
                candidate(65, 6),
                candidate(70, 3),
                candidate(70, 4),
                candidate(73, 2),
                candidate(73, 4),
                candidate(74, 2),
                candidate(74, 3),
                candidate(78, 4),
                candidate(78, 8),
                candidate(80, 3),
                candidate(80, 8),
            ),
            focusDigits = setOf(5),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 2),
                HouseRef(type = HouseType.COLUMN, index = 2),
                HouseRef(type = HouseType.BOX, index = 0),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/locked-candidate
private fun xChainExample1() = advancedExample(
    givens = "000756381567030924318294576895403000003080400000900835032010040450372008781649253",
    solution = "249756381567138924318294576895463712123587469674921835932815647456372198781649253",
    candidates = "760a7c0000000000000000003l003l000000000000000000000000000000000y002p0x1u0z1u000h002900818y0z2214000y1t0000008000004000402o008w0000800000000x8100000000000000000000",
    step = SolveStep(
        technique = TechniqueId.X_CHAIN,
        eliminations = listOf(CandidateElimination(candidate = candidate(33, 6))),
        evidence = StepEvidence(
            causeCells = setOf(cell(31), cell(49), cell(47), cell(65), cell(54), cell(60)),
            causeCandidates = setOf(
                candidate(31, 6),
                candidate(49, 6),
                candidate(47, 6),
                candidate(65, 6),
                candidate(54, 6),
                candidate(60, 6),
            ),
            focusDigits = setOf(6),
            houses = listOf(
                HouseRef(type = HouseType.COLUMN, index = 4),
                HouseRef(type = HouseType.BOX, index = 4),
                HouseRef(type = HouseType.ROW, index = 5),
                HouseRef(type = HouseType.COLUMN, index = 2),
                HouseRef(type = HouseType.BOX, index = 6),
                HouseRef(type = HouseType.ROW, index = 6),
            ),
            links = listOf(
                InferenceLink(from = candidate(31, 6), to = candidate(49, 6), type = InferenceLinkType.STRONG),
                InferenceLink(from = candidate(49, 6), to = candidate(47, 6), type = InferenceLinkType.WEAK),
                InferenceLink(from = candidate(47, 6), to = candidate(65, 6), type = InferenceLinkType.STRONG),
                InferenceLink(from = candidate(65, 6), to = candidate(54, 6), type = InferenceLinkType.WEAK),
                InferenceLink(from = candidate(54, 6), to = candidate(60, 6), type = InferenceLinkType.STRONG),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/x-wing
private fun xChainExample2() = advancedExample(
    givens = "600009000013027960794860200006083759907050000005970000000000000371690800060040100",
    solution = "682319574513427968794865213146283759927154386835976421458731692371692845269548137",
    candidates = "00423m0s05000o5l614000000o000000004800000000000h00050l0b0a00030000000000003q00030014183z4v3n3q00000014183z4v4a4aaq2c05431g923a00000000000i000a0q4200aq2c004200922e",
    step = SolveStep(
        technique = TechniqueId.X_CHAIN,
        eliminations = listOf(CandidateElimination(candidate = candidate(3, 5))),
        evidence = StepEvidence(
            causeCells = setOf(cell(1), cell(55), cell(60), cell(6), cell(26), cell(23)),
            causeCandidates = setOf(
                candidate(1, 5),
                candidate(55, 5),
                candidate(60, 5),
                candidate(6, 5),
                candidate(26, 5),
                candidate(23, 5),
            ),
            focusDigits = setOf(5),
            houses = listOf(
                HouseRef(type = HouseType.COLUMN, index = 1),
                HouseRef(type = HouseType.ROW, index = 6),
                HouseRef(type = HouseType.COLUMN, index = 6),
                HouseRef(type = HouseType.BOX, index = 2),
                HouseRef(type = HouseType.ROW, index = 2),
            ),
            links = listOf(
                InferenceLink(from = candidate(1, 5), to = candidate(55, 5), type = InferenceLinkType.STRONG),
                InferenceLink(from = candidate(55, 5), to = candidate(60, 5), type = InferenceLinkType.WEAK),
                InferenceLink(from = candidate(60, 5), to = candidate(6, 5), type = InferenceLinkType.STRONG),
                InferenceLink(from = candidate(6, 5), to = candidate(26, 5), type = InferenceLinkType.WEAK),
                InferenceLink(from = candidate(26, 5), to = candidate(23, 5), type = InferenceLinkType.STRONG),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/x-wing
private fun xChainLoopExample1() = advancedExample(
    givens = "074160009096005100021040000103004060907600401642010503430706018710400000260091300",
    solution = "374168259896275134521349876183954762957632481642817593435726918719483625268591347",
    candidates = "0k000000003m3m0k003o00003q5e00005q22440000as008w685w1c004000b65u00ci001u00400000463q003m00000000ao008w00cg0000007k000i007600000000ao003q3q827m1c000040400000002020",
    step = SolveStep(
        technique = TechniqueId.X_CHAIN_LOOP,
        eliminations = listOf(
            CandidateElimination(candidate = candidate(16, 2)),
            CandidateElimination(candidate = candidate(31, 2)),
            CandidateElimination(candidate = candidate(33, 2)),
            CandidateElimination(candidate = candidate(13, 2)),
        ),
        evidence = StepEvidence(
            causeCells = setOf(cell(5), cell(6), cell(17), cell(35), cell(30), cell(12)),
            causeCandidates = setOf(
                candidate(5, 2),
                candidate(6, 2),
                candidate(17, 2),
                candidate(35, 2),
                candidate(30, 2),
                candidate(12, 2),
            ),
            focusDigits = setOf(2),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 0),
                HouseRef(type = HouseType.BOX, index = 2),
                HouseRef(type = HouseType.COLUMN, index = 8),
                HouseRef(type = HouseType.ROW, index = 3),
                HouseRef(type = HouseType.COLUMN, index = 3),
            ),
            links = listOf(
                InferenceLink(from = candidate(5, 2), to = candidate(6, 2), type = InferenceLinkType.STRONG),
                InferenceLink(from = candidate(6, 2), to = candidate(17, 2), type = InferenceLinkType.WEAK),
                InferenceLink(from = candidate(17, 2), to = candidate(35, 2), type = InferenceLinkType.STRONG),
                InferenceLink(from = candidate(35, 2), to = candidate(30, 2), type = InferenceLinkType.WEAK),
                InferenceLink(from = candidate(30, 2), to = candidate(12, 2), type = InferenceLinkType.STRONG),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/jellyfish
private fun xChainLoopExample2() = advancedExample(
    givens = "040195026261304000500620413050041209418269537000053041034500002075932004900410300",
    solution = "843195726261374958597628413356741289418269537729853641634587192175932864982416375",
    candidates = "3o001w0000005c0000000000005c00cg7k4000ao8w00005c0000002s00105c0000004g000000000000000000002o76765c00004g00004h0000005c68758w003l00000000004h4g00003m0y000068002840",
    step = SolveStep(
        technique = TechniqueId.X_CHAIN_LOOP,
        eliminations = listOf(
            CandidateElimination(candidate = candidate(15, 8)),
            CandidateElimination(candidate = candidate(77, 8)),
        ),
        evidence = StepEvidence(
            causeCells = setOf(cell(0), cell(6), cell(17), cell(80), cell(73), cell(19)),
            causeCandidates = setOf(
                candidate(0, 8),
                candidate(6, 8),
                candidate(17, 8),
                candidate(80, 8),
                candidate(73, 8),
                candidate(19, 8),
            ),
            focusDigits = setOf(8),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 0),
                HouseRef(type = HouseType.BOX, index = 2),
                HouseRef(type = HouseType.COLUMN, index = 8),
                HouseRef(type = HouseType.ROW, index = 8),
                HouseRef(type = HouseType.COLUMN, index = 1),
            ),
            links = listOf(
                InferenceLink(from = candidate(0, 8), to = candidate(6, 8), type = InferenceLinkType.STRONG),
                InferenceLink(from = candidate(6, 8), to = candidate(17, 8), type = InferenceLinkType.WEAK),
                InferenceLink(from = candidate(17, 8), to = candidate(80, 8), type = InferenceLinkType.STRONG),
                InferenceLink(from = candidate(80, 8), to = candidate(73, 8), type = InferenceLinkType.WEAK),
                InferenceLink(from = candidate(73, 8), to = candidate(19, 8), type = InferenceLinkType.STRONG),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/locked-candidate
private fun xChainOneEndpointExample1() = advancedExample(
    givens = "400701008100052374570483019604807000750309080009005007005008746047506820000074005",
    solution = "463791258198652374572483619624817593751369482839245167315928746947536821286174935",
    candidates = "0086120080001e1c0000bk4g80000000000000000y0000000y00000007000003007k7k070000030014001400033q3r000z17001510007a0700777b00000000780000007900000005au4n4n770000757800",
    step = SolveStep(
        technique = TechniqueId.X_CHAIN_ONE_ENDPOINT,
        placements = listOf(Placement(cell = cell(38), digit = 1)),
        evidence = StepEvidence(
            causeCells = setOf(cell(38), cell(44), cell(51), cell(78), cell(74)),
            causeCandidates = setOf(
                candidate(38, 1),
                candidate(44, 1),
                candidate(51, 1),
                candidate(78, 1),
                candidate(74, 1),
            ),
            focusDigits = setOf(1),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 4),
                HouseRef(type = HouseType.BOX, index = 5),
                HouseRef(type = HouseType.COLUMN, index = 6),
                HouseRef(type = HouseType.ROW, index = 8),
                HouseRef(type = HouseType.COLUMN, index = 2),
            ),
            links = listOf(
                InferenceLink(from = candidate(38, 1), to = candidate(44, 1), type = InferenceLinkType.STRONG),
                InferenceLink(from = candidate(44, 1), to = candidate(51, 1), type = InferenceLinkType.WEAK),
                InferenceLink(from = candidate(51, 1), to = candidate(78, 1), type = InferenceLinkType.STRONG),
                InferenceLink(from = candidate(78, 1), to = candidate(74, 1), type = InferenceLinkType.WEAK),
                InferenceLink(from = candidate(74, 1), to = candidate(38, 1), type = InferenceLinkType.STRONG),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/swordfish
private fun xChainOneEndpointExample2() = advancedExample(
    givens = "428165739019804652560020481040006200200400006006012040635201890192008360874693125",
    solution = "428165739319874652567329481941536278253487916786912543635241897192758364874693125",
    candidates = "0000000000000000001w0000001w0000000000001w90008w00000090001x9g5w00001t3o00401x005g8w7k1t009040009000007k003o000000002000000020000000282g00000020000000000000000000",
    step = SolveStep(
        technique = TechniqueId.X_CHAIN_ONE_ENDPOINT,
        placements = listOf(Placement(cell = cell(9), digit = 3)),
        evidence = StepEvidence(
            causeCells = setOf(cell(9), cell(13), cell(40), cell(38), cell(20)),
            causeCandidates = setOf(
                candidate(9, 3),
                candidate(13, 3),
                candidate(40, 3),
                candidate(38, 3),
                candidate(20, 3),
            ),
            focusDigits = setOf(3),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 1),
                HouseRef(type = HouseType.COLUMN, index = 4),
                HouseRef(type = HouseType.ROW, index = 4),
                HouseRef(type = HouseType.COLUMN, index = 2),
                HouseRef(type = HouseType.BOX, index = 0),
            ),
            links = listOf(
                InferenceLink(from = candidate(9, 3), to = candidate(13, 3), type = InferenceLinkType.STRONG),
                InferenceLink(from = candidate(13, 3), to = candidate(40, 3), type = InferenceLinkType.WEAK),
                InferenceLink(from = candidate(40, 3), to = candidate(38, 3), type = InferenceLinkType.STRONG),
                InferenceLink(from = candidate(38, 3), to = candidate(20, 3), type = InferenceLinkType.WEAK),
                InferenceLink(from = candidate(20, 3), to = candidate(9, 3), type = InferenceLinkType.STRONG),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/x-chain
private fun groupedXChainExample1() = advancedExample(
    givens = "005901034319004200408205179607000040094000001501642790970853410840000300153426987",
    solution = "725981634319764258468235179637519842294378561581642793972853416846197325153426987",
    candidates = "1u0y000068004g00000000001s4g00001c4w000w00001000000000003q000l75ao40003q0200002c1w5c4w0y00003o0000000000003o00000y00000000000y00000y1t758w001e1e000000000000000000",
    step = SolveStep(
        technique = TechniqueId.GROUPED_X_CHAIN,
        placements = listOf(Placement(cell = cell(22), digit = 3)),
        eliminations = listOf(CandidateElimination(candidate = candidate(40, 3))),
        evidence = StepEvidence(
            causeCells = setOf(cell(22), cell(40), cell(30), cell(39)),
            causeCandidates = setOf(candidate(22, 3), candidate(40, 3), candidate(30, 3), candidate(39, 3)),
            focusDigits = setOf(3),
            groupedLinks = listOf(
                GroupedInferenceLink(
                    from = setOf(candidate(22, 3)),
                    to = setOf(candidate(40, 3)),
                    type = InferenceLinkType.STRONG,
                ),
                GroupedInferenceLink(
                    from = setOf(candidate(40, 3)),
                    to = setOf(candidate(30, 3)),
                    type = InferenceLinkType.WEAK,
                ),
                GroupedInferenceLink(
                    from = setOf(candidate(30, 3)),
                    to = setOf(candidate(39, 3)),
                    type = InferenceLinkType.STRONG,
                ),
                GroupedInferenceLink(
                    from = setOf(candidate(39, 3)),
                    to = setOf(candidate(40, 3)),
                    type = InferenceLinkType.WEAK,
                ),
                GroupedInferenceLink(
                    from = setOf(candidate(40, 3)),
                    to = setOf(candidate(30, 3), candidate(39, 3)),
                    type = InferenceLinkType.STRONG,
                ),
                GroupedInferenceLink(
                    from = setOf(candidate(30, 3), candidate(39, 3)),
                    to = setOf(candidate(40, 3)),
                    type = InferenceLinkType.WEAK,
                ),
                GroupedInferenceLink(
                    from = setOf(candidate(40, 3)),
                    to = setOf(candidate(22, 3)),
                    type = InferenceLinkType.STRONG,
                ),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/x-chain
private fun groupedXChainExample2() = advancedExample(
    givens = "000040060010096020008015090000530916106409835593681742000168274400053089000904050",
    solution = "759842361314796528628315497842537916176429835593681742935168274467253189281974653",
    candidates = "922c9e5i001u0l005h1w000o5g00000o005g2u32001y00000c001w5e5m2200001u000000001u00001u00000000000000000000000000780k7k000000000000002o1v1u00000x00006a6a1v001u00110005",
    step = SolveStep(
        technique = TechniqueId.GROUPED_X_CHAIN,
        eliminations = listOf(CandidateElimination(candidate = candidate(2, 2))),
        evidence = StepEvidence(
            causeCells = setOf(cell(5), cell(3), cell(21), cell(66), cell(65)),
            causeCandidates = setOf(
                candidate(5, 2),
                candidate(3, 2),
                candidate(21, 2),
                candidate(66, 2),
                candidate(65, 2),
            ),
            focusDigits = setOf(2),
            groupedLinks = listOf(
                GroupedInferenceLink(
                    from = setOf(candidate(5, 2)),
                    to = setOf(candidate(3, 2), candidate(21, 2)),
                    type = InferenceLinkType.STRONG,
                ),
                GroupedInferenceLink(
                    from = setOf(candidate(3, 2), candidate(21, 2)),
                    to = setOf(candidate(66, 2)),
                    type = InferenceLinkType.WEAK,
                ),
                GroupedInferenceLink(
                    from = setOf(candidate(66, 2)),
                    to = setOf(candidate(65, 2)),
                    type = InferenceLinkType.STRONG,
                ),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/wxyz-wing
private fun wxyzWingExample1() = advancedExample(
    givens = "000094000049207000100003090007830500500062087000745000700028341300400002000376859",
    solution = "653194278849257613172683495967831524534962187218745936796528341385419762421376859",
    candidates = "4i6u521d00002r2r4w4g00000041000x1150006q4y1c40002y00548a0z00000075000z1400050d7500007d0000bm4j4j000000831310008g1c7k000000000000b440000h752o2o000a030b000000000000",
    step = SolveStep(
        technique = TechniqueId.WXYZ_WING,
        eliminations = listOf(
            CandidateElimination(candidate = candidate(24, 6)),
            CandidateElimination(candidate = candidate(26, 6)),
        ),
        evidence = StepEvidence(
            causeCells = setOf(cell(13), cell(21), cell(22), cell(15)),
            causeCandidates = setOf(
                candidate(13, 1),
                candidate(13, 5),
                candidate(13, 8),
                candidate(21, 5),
                candidate(21, 6),
                candidate(22, 5),
                candidate(22, 8),
                candidate(15, 1),
                candidate(15, 6),
            ),
            focusDigits = setOf(1, 5, 8, 6),
        ),
    ),
)

// https://sudoku.coach/en/learn/wxyz-wing
private fun wxyzWingExample2() = advancedExample(
    givens = "890000006006879103300006980009051062500690000600080591004038605065047008138265749",
    solution = "897513426246879153351426987489751362513692874672384591724938615965147238138265749",
    candidates = "00001v0s030c0a2a000a0q00000000000i00002b1v0o0300000020203s002400003w0000003v0700000e3w1w2000221y24000e0000008y1u00750000000300760000750000060700000000000000000000",
    step = SolveStep(
        technique = TechniqueId.WXYZ_WING,
        eliminations = listOf(CandidateElimination(candidate = candidate(7, 7))),
        evidence = StepEvidence(
            causeCells = setOf(cell(6), cell(2), cell(4), cell(26)),
            causeCandidates = setOf(
                candidate(6, 2),
                candidate(6, 4),
                candidate(2, 1),
                candidate(2, 2),
                candidate(2, 7),
                candidate(4, 1),
                candidate(4, 2),
                candidate(26, 4),
                candidate(26, 7),
            ),
            focusDigits = setOf(2, 4, 1, 7),
        ),
    ),
)

// https://sudoku.coach/en/learn/wxyz-wing
private fun fiveYWingExample1() = advancedExample(
    givens = "000094000049207000100003090007830500500062087000745000700028341300400002000376859",
    solution = "653194278849257613172683495967831524534962187218745936796528341385419762421376859",
    candidates = "4i6u521d00002r2r4w4g00000041000x1150006q4y1c40002y00548a0z00000075000z1400050d7500007d0000bm4j4j000000831310008g1c7k000000000000b440000h752o2o000a030b000000000000",
    step = SolveStep(
        technique = TechniqueId.FIVE_Y_WING,
        eliminations = listOf(
            CandidateElimination(candidate = candidate(24, 6)),
            CandidateElimination(candidate = candidate(26, 6)),
        ),
        evidence = StepEvidence(
            causeCells = setOf(cell(13), cell(15), cell(16), cell(21), cell(22)),
            causeCandidates = setOf(
                candidate(13, 1),
                candidate(13, 5),
                candidate(13, 8),
                candidate(15, 1),
                candidate(15, 6),
                candidate(16, 1),
                candidate(16, 3),
                candidate(16, 6),
                candidate(21, 5),
                candidate(21, 6),
                candidate(22, 5),
                candidate(22, 8),
            ),
            focusDigits = setOf(1, 3, 5, 6, 8),
            houses = listOf(HouseRef(type = HouseType.ROW, index = 1), HouseRef(type = HouseType.BOX, index = 1)),
        ),
    ),
)

// https://sudoku.coach/en/learn/wxyz-wing
private fun fiveYWingExample2() = advancedExample(
    givens = "043160000017004306562039104756213849009478600184695030608900400090046000470380960",
    solution = "943167258817524396562839174756213849329478615184695732638951427291746583475382961",
    candidates = "ao000000001u2acy5uao0000400i0000aq000000005c0000005c00000000000000000000060600000000000h0h0000000000001u001u000600000i1v000j2f06000h2800002a5v5z00000h00000300000j",
    step = SolveStep(
        technique = TechniqueId.FIVE_Y_WING,
        eliminations = listOf(CandidateElimination(candidate = candidate(71, 5))),
        evidence = StepEvidence(
            causeCells = setOf(cell(69), cell(70), cell(66), cell(61), cell(80)),
            causeCandidates = setOf(
                candidate(69, 2),
                candidate(69, 5),
                candidate(69, 7),
                candidate(70, 1),
                candidate(70, 2),
                candidate(70, 5),
                candidate(70, 7),
                candidate(70, 8),
                candidate(66, 5),
                candidate(66, 7),
                candidate(61, 1),
                candidate(61, 2),
                candidate(61, 5),
                candidate(80, 1),
                candidate(80, 2),
                candidate(80, 5),
            ),
            focusDigits = setOf(1, 2, 5, 7, 8),
            houses = listOf(HouseRef(type = HouseType.ROW, index = 7), HouseRef(type = HouseType.BOX, index = 8)),
        ),
    ),
)

// https://sudoku.coach/en/learn/wxyz-wing
private fun sixYWingExample1() = advancedExample(
    givens = "000094000049207000100003090007830500500062087000745000700028341300400002000376859",
    solution = "653194278849257613172683495967831524534962187218745936796528341385419762421376859",
    candidates = "4i6u521d00002r2r4w4g00000041000x1150006q4y1c40002y00548a0z00000075000z1400050d7500007d0000bm4j4j000000831310008g1c7k000000000000b440000h752o2o000a030b000000000000",
    step = SolveStep(
        technique = TechniqueId.SIX_Y_WING,
        eliminations = listOf(CandidateElimination(candidate = candidate(38, 3))),
        evidence = StepEvidence(
            causeCells = setOf(cell(47), cell(2), cell(20), cell(56), cell(65), cell(37)),
            causeCandidates = setOf(
                candidate(47, 1),
                candidate(47, 2),
                candidate(47, 6),
                candidate(47, 8),
                candidate(2, 2),
                candidate(2, 3),
                candidate(2, 5),
                candidate(2, 6),
                candidate(2, 8),
                candidate(20, 2),
                candidate(20, 5),
                candidate(20, 6),
                candidate(20, 8),
                candidate(56, 5),
                candidate(56, 6),
                candidate(65, 5),
                candidate(65, 8),
                candidate(37, 1),
                candidate(37, 3),
            ),
            focusDigits = setOf(1, 2, 3, 5, 6, 8),
            houses = listOf(HouseRef(type = HouseType.COLUMN, index = 2), HouseRef(type = HouseType.BOX, index = 3)),
        ),
    ),
)

// https://sudoku.coach/en/learn/wxyz-wing
private fun sixYWingExample2() = advancedExample(
    givens = "203941000000060201108572300340000000010034000902610000020006480030000006000400902",
    solution = "253941768794368251168572349346789125815234697972615834527196483439827516681453972",
    candidates = "0034000000006o345s2g9c2g3o003o009c0000800000000000887c0000345eb6cw6p375s6o00345e00006oaacw005s0000005s5s0c0c28009d1x7k0000002c60009l5faqcw292900685s2p00405w002d00",
    step = SolveStep(
        technique = TechniqueId.SIX_Y_WING,
        eliminations = listOf(CandidateElimination(candidate = candidate(68, 5))),
        evidence = StepEvidence(
            causeCells = setOf(cell(66), cell(69), cell(70), cell(58), cell(67), cell(76)),
            causeCandidates = setOf(
                candidate(66, 1),
                candidate(66, 2),
                candidate(66, 7),
                candidate(66, 8),
                candidate(69, 1),
                candidate(69, 5),
                candidate(69, 7),
                candidate(70, 1),
                candidate(70, 5),
                candidate(70, 7),
                candidate(58, 5),
                candidate(58, 9),
                candidate(67, 2),
                candidate(67, 8),
                candidate(67, 9),
                candidate(76, 5),
                candidate(76, 8),
            ),
            focusDigits = setOf(1, 2, 5, 7, 8, 9),
            houses = listOf(HouseRef(type = HouseType.ROW, index = 7), HouseRef(type = HouseType.BOX, index = 7)),
        ),
    ),
)

// https://sudoku.coach/en/learn/wxyz-wing
private fun sevenYWingExample1() = advancedExample(
    givens = "200040587305807104748005309032408795007030410004000830000900600520080901009001200",
    solution = "291643587365897124748125369632418795857239416914576832173952648526384971489761253",
    candidates = "00810x100084000000008000008200000y000000000z0z00000y000x0000000x00000000bk4w001e008200000y810h002bab8200000y3t5d05002a0e002g3o0000102s00180020004o6800383400002g3o",
    step = SolveStep(
        technique = TechniqueId.SEVEN_Y_WING,
        eliminations = listOf(CandidateElimination(candidate = candidate(39, 6))),
        evidence = StepEvidence(
            causeCells = setOf(cell(48), cell(3), cell(66), cell(75), cell(31), cell(41), cell(50)),
            causeCandidates = setOf(
                candidate(48, 1),
                candidate(48, 2),
                candidate(48, 5),
                candidate(48, 7),
                candidate(3, 3),
                candidate(3, 6),
                candidate(66, 3),
                candidate(66, 6),
                candidate(66, 7),
                candidate(75, 3),
                candidate(75, 5),
                candidate(75, 6),
                candidate(75, 7),
                candidate(31, 1),
                candidate(31, 6),
                candidate(41, 2),
                candidate(41, 6),
                candidate(41, 9),
                candidate(50, 2),
                candidate(50, 6),
                candidate(50, 9),
            ),
            focusDigits = setOf(1, 2, 3, 5, 6, 7, 9),
            houses = listOf(HouseRef(type = HouseType.COLUMN, index = 3), HouseRef(type = HouseType.BOX, index = 4)),
        ),
    ),
)

// https://sudoku.coach/en/learn/xy-chain
private fun xyChainExample1() = advancedExample(
    givens = "700896100081732000000154870000249361039501207102307000000600904010978625090400700",
    solution = "743896152581732496926154873857249361439561287162387549278615934314978625695423718",
    candidates = "000a0s000000000k068g00000000000o7s80840y1000000000008640285s000000000000140000004g00003s00001400004g000obcao42285s00030k003p000c000c0000000000004y004w00030k003p3o",
    step = SolveStep(
        technique = TechniqueId.XY_CHAIN,
        eliminations = listOf(CandidateElimination(candidate = candidate(2, 4))),
        evidence = StepEvidence(
            causeCells = setOf(cell(1), cell(19), cell(20), cell(65)),
            causeCandidates = setOf(
                candidate(1, 2),
                candidate(1, 4),
                candidate(19, 2),
                candidate(19, 6),
                candidate(20, 3),
                candidate(20, 6),
                candidate(65, 3),
                candidate(65, 4),
            ),
            focusDigits = setOf(2, 4, 6, 3),
            houses = listOf(
                HouseRef(type = HouseType.COLUMN, index = 1),
                HouseRef(type = HouseType.BOX, index = 0),
                HouseRef(type = HouseType.ROW, index = 2),
                HouseRef(type = HouseType.COLUMN, index = 2),
            ),
            links = listOf(
                InferenceLink(from = candidate(1, 4), to = candidate(1, 2), type = InferenceLinkType.DUAL),
                InferenceLink(from = candidate(1, 2), to = candidate(19, 2), type = InferenceLinkType.WEAK),
                InferenceLink(from = candidate(19, 2), to = candidate(19, 6), type = InferenceLinkType.DUAL),
                InferenceLink(from = candidate(19, 6), to = candidate(20, 6), type = InferenceLinkType.WEAK),
                InferenceLink(from = candidate(20, 6), to = candidate(20, 3), type = InferenceLinkType.DUAL),
                InferenceLink(from = candidate(20, 3), to = candidate(65, 3), type = InferenceLinkType.WEAK),
                InferenceLink(from = candidate(65, 3), to = candidate(65, 4), type = InferenceLinkType.DUAL),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/xy-chain
private fun xyChainExample2() = advancedExample(
    givens = "504007090080900500900050600379010825415782369000395400650000900042509006193000054",
    solution = "534267198786941532921853647379416825415782369268395471657124983842539716193678254",
    candidates = "0012004j100003003r1u002p000e1900251z00061t3v003x005o5j0000001400140000000000000000000000003m0y4g000000001t1t00005c0b260d003o3q5c0000001w001t3p000000004i2q4g1u0000",
    step = SolveStep(
        technique = TechniqueId.XY_CHAIN,
        eliminations = listOf(CandidateElimination(candidate = candidate(1, 6))),
        evidence = StepEvidence(
            causeCells = setOf(cell(4), cell(67), cell(63), cell(45), cell(46)),
            causeCandidates = setOf(
                candidate(4, 3),
                candidate(4, 6),
                candidate(67, 3),
                candidate(67, 7),
                candidate(63, 7),
                candidate(63, 8),
                candidate(45, 2),
                candidate(45, 8),
                candidate(46, 2),
                candidate(46, 6),
            ),
            focusDigits = setOf(3, 6, 7, 8, 2),
            houses = listOf(
                HouseRef(type = HouseType.COLUMN, index = 4),
                HouseRef(type = HouseType.ROW, index = 7),
                HouseRef(type = HouseType.COLUMN, index = 0),
                HouseRef(type = HouseType.ROW, index = 5),
                HouseRef(type = HouseType.BOX, index = 3),
            ),
            links = listOf(
                InferenceLink(from = candidate(4, 6), to = candidate(4, 3), type = InferenceLinkType.DUAL),
                InferenceLink(from = candidate(4, 3), to = candidate(67, 3), type = InferenceLinkType.WEAK),
                InferenceLink(from = candidate(67, 3), to = candidate(67, 7), type = InferenceLinkType.DUAL),
                InferenceLink(from = candidate(67, 7), to = candidate(63, 7), type = InferenceLinkType.WEAK),
                InferenceLink(from = candidate(63, 7), to = candidate(63, 8), type = InferenceLinkType.DUAL),
                InferenceLink(from = candidate(63, 8), to = candidate(45, 8), type = InferenceLinkType.WEAK),
                InferenceLink(from = candidate(45, 8), to = candidate(45, 2), type = InferenceLinkType.DUAL),
                InferenceLink(from = candidate(45, 2), to = candidate(46, 2), type = InferenceLinkType.WEAK),
                InferenceLink(from = candidate(46, 2), to = candidate(46, 6), type = InferenceLinkType.DUAL),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/xy-chain
private fun xyChainLoopExample1() = advancedExample(
    givens = "081200004029000650005030000048912000203040100917563842030000000090600700106320480",
    solution = "781256934329174658465839217648912375253748196917563842832497561594681723176325489",
    candidates = "2s0000007k8g781w002400005l5c5l00005h2w2o005l0080761vch1c00000000000k2s38001c005c005c00808g0000000000000000005s000a5lcwd57m838h40000a00400900070500280000009c00007k",
    step = SolveStep(
        technique = TechniqueId.XY_CHAIN_LOOP,
        eliminations = listOf(CandidateElimination(candidate = candidate(58, 5))),
        evidence = StepEvidence(
            causeCells = setOf(cell(4), cell(23), cell(19), cell(73), cell(37), cell(27), cell(63), cell(67)),
            causeCandidates = setOf(
                candidate(4, 5),
                candidate(4, 9),
                candidate(23, 6),
                candidate(23, 9),
                candidate(19, 6),
                candidate(19, 7),
                candidate(73, 5),
                candidate(73, 7),
                candidate(37, 5),
                candidate(37, 6),
                candidate(27, 5),
                candidate(27, 6),
                candidate(63, 5),
                candidate(63, 8),
                candidate(67, 5),
                candidate(67, 8),
            ),
            focusDigits = setOf(5, 9, 6, 7, 8),
            houses = listOf(
                HouseRef(type = HouseType.BOX, index = 1),
                HouseRef(type = HouseType.ROW, index = 2),
                HouseRef(type = HouseType.COLUMN, index = 1),
                HouseRef(type = HouseType.BOX, index = 3),
                HouseRef(type = HouseType.COLUMN, index = 0),
                HouseRef(type = HouseType.ROW, index = 7),
            ),
            links = listOf(
                InferenceLink(from = candidate(4, 5), to = candidate(4, 9), type = InferenceLinkType.DUAL),
                InferenceLink(from = candidate(4, 9), to = candidate(23, 9), type = InferenceLinkType.WEAK),
                InferenceLink(from = candidate(23, 9), to = candidate(23, 6), type = InferenceLinkType.DUAL),
                InferenceLink(from = candidate(23, 6), to = candidate(19, 6), type = InferenceLinkType.WEAK),
                InferenceLink(from = candidate(19, 6), to = candidate(19, 7), type = InferenceLinkType.DUAL),
                InferenceLink(from = candidate(19, 7), to = candidate(73, 7), type = InferenceLinkType.WEAK),
                InferenceLink(from = candidate(73, 7), to = candidate(73, 5), type = InferenceLinkType.DUAL),
                InferenceLink(from = candidate(73, 5), to = candidate(37, 5), type = InferenceLinkType.WEAK),
                InferenceLink(from = candidate(37, 5), to = candidate(37, 6), type = InferenceLinkType.DUAL),
                InferenceLink(from = candidate(37, 6), to = candidate(27, 6), type = InferenceLinkType.WEAK),
                InferenceLink(from = candidate(27, 6), to = candidate(27, 5), type = InferenceLinkType.DUAL),
                InferenceLink(from = candidate(27, 5), to = candidate(63, 5), type = InferenceLinkType.WEAK),
                InferenceLink(from = candidate(63, 5), to = candidate(63, 8), type = InferenceLinkType.DUAL),
                InferenceLink(from = candidate(63, 8), to = candidate(67, 8), type = InferenceLinkType.WEAK),
                InferenceLink(from = candidate(67, 8), to = candidate(67, 5), type = InferenceLinkType.DUAL),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/3d-medusa
private fun threeDMedusaExample1() = advancedExample(
    givens = "009050030830200060040031200003004905492000000658090040000300010380010000900000403",
    solution = "729456138831279564546831297173624985492583671658197342267345819384912756915768423",
    candidates = "1v2r006g00685d005l00002900208w290095280034dc0000009c5c1t1t004g4i00003m000000000h5c0k115c0x0000001t001y1x001v2a2q3c002yaa6800dc00003cag0036349e2q002r356o6a6q002a00",
    step = SolveStep(
        technique = TechniqueId.THREE_D_MEDUSA,
        eliminations = listOf(CandidateElimination(candidate = candidate(51, 7))),
        evidence = StepEvidence(
            causeCells = setOf(cell(39), cell(48), cell(41), cell(42), cell(50), cell(51)),
            causeCandidates = setOf(
                candidate(39, 1),
                candidate(48, 1),
                candidate(39, 5),
                candidate(48, 7),
                candidate(41, 5),
                candidate(41, 3),
                candidate(42, 3),
                candidate(50, 3),
                candidate(51, 3),
            ),
            focusDigits = setOf(1, 5, 7, 3),
            houses = listOf(
                HouseRef(type = HouseType.COLUMN, index = 3),
                HouseRef(type = HouseType.BOX, index = 4),
                HouseRef(type = HouseType.ROW, index = 4),
                HouseRef(type = HouseType.ROW, index = 5),
                HouseRef(type = HouseType.COLUMN, index = 5),
                HouseRef(type = HouseType.COLUMN, index = 6),
                HouseRef(type = HouseType.BOX, index = 5),
            ),
            links = listOf(
                InferenceLink(
                    from = candidate(39, 1),
                    to = candidate(48, 1),
                    type = InferenceLinkType.DUAL,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(39, 1),
                    to = candidate(39, 5),
                    type = InferenceLinkType.DUAL,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(48, 1),
                    to = candidate(48, 7),
                    type = InferenceLinkType.DUAL,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(39, 5),
                    to = candidate(41, 5),
                    type = InferenceLinkType.DUAL,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(41, 5),
                    to = candidate(41, 3),
                    type = InferenceLinkType.DUAL,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(41, 3),
                    to = candidate(42, 3),
                    type = InferenceLinkType.DUAL,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(41, 3),
                    to = candidate(50, 3),
                    type = InferenceLinkType.DUAL,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(42, 3),
                    to = candidate(51, 3),
                    type = InferenceLinkType.DUAL,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(50, 3),
                    to = candidate(51, 3),
                    type = InferenceLinkType.DUAL,
                    branchId = 0,
                ),
            ),
            inferenceGraph = InferenceGraph(
                nodes = listOf(
                    InferenceNode(
                        id = 0,
                        candidate = candidate(39, 1),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = true,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 1,
                        candidate = candidate(48, 1),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 2,
                        candidate = candidate(39, 5),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 3,
                        candidate = candidate(48, 7),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 4,
                        candidate = candidate(41, 5),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 5,
                        candidate = candidate(41, 3),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 6,
                        candidate = candidate(42, 3),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 7,
                        candidate = candidate(50, 3),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 8,
                        candidate = candidate(51, 3),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                ),
                edges = listOf(
                    InferenceEdge(fromNodeId = 0, toNodeId = 1, type = InferenceLinkType.DUAL),
                    InferenceEdge(fromNodeId = 0, toNodeId = 2, type = InferenceLinkType.DUAL),
                    InferenceEdge(fromNodeId = 1, toNodeId = 3, type = InferenceLinkType.DUAL),
                    InferenceEdge(fromNodeId = 2, toNodeId = 4, type = InferenceLinkType.DUAL),
                    InferenceEdge(fromNodeId = 4, toNodeId = 5, type = InferenceLinkType.DUAL),
                    InferenceEdge(fromNodeId = 5, toNodeId = 6, type = InferenceLinkType.DUAL),
                    InferenceEdge(fromNodeId = 5, toNodeId = 7, type = InferenceLinkType.DUAL),
                    InferenceEdge(fromNodeId = 6, toNodeId = 8, type = InferenceLinkType.DUAL),
                    InferenceEdge(fromNodeId = 7, toNodeId = 8, type = InferenceLinkType.DUAL),
                ),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/3d-medusa
private fun threeDMedusaExample2() = advancedExample(
    givens = "008020000000180005070300800306570010040001700007030560904000000703040980200700000",
    solution = "698425173432187695175396824326578419549261738817934562964813257753642981281759346",
    candidates = "1k7o008800ag1998a5147a760000a01a9a001l007n008g8o007e8a0076000000ay0a00ay40007mbm8000007aau3l7700ay00ay0000ay004x004i1d52132e2v001d000y001e00000z004x0h008hc4190s19",
    step = SolveStep(
        technique = TechniqueId.THREE_D_MEDUSA,
        placements = listOf(
            Placement(cell = cell(18), digit = 1),
            Placement(cell = cell(36), digit = 5),
            Placement(cell = cell(45), digit = 8),
            Placement(cell = cell(46), digit = 1),
            Placement(cell = cell(74), digit = 1),
        ),
        eliminations = listOf(
            CandidateElimination(candidate = candidate(20, 1)),
            CandidateElimination(candidate = candidate(45, 1)),
            CandidateElimination(candidate = candidate(74, 5)),
            CandidateElimination(candidate = candidate(36, 8)),
            CandidateElimination(candidate = candidate(38, 5)),
        ),
        evidence = StepEvidence(
            causeCells = setOf(cell(18), cell(20), cell(45), cell(74), cell(46), cell(36), cell(38)),
            causeCandidates = setOf(
                candidate(18, 1),
                candidate(20, 1),
                candidate(45, 1),
                candidate(74, 1),
                candidate(46, 1),
                candidate(45, 8),
                candidate(74, 5),
                candidate(36, 8),
                candidate(36, 5),
                candidate(38, 5),
            ),
            focusDigits = setOf(1, 8, 5),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 2),
                HouseRef(type = HouseType.ROW, index = 5),
                HouseRef(type = HouseType.COLUMN, index = 0),
                HouseRef(type = HouseType.COLUMN, index = 2),
                HouseRef(type = HouseType.BOX, index = 0),
                HouseRef(type = HouseType.BOX, index = 3),
                HouseRef(type = HouseType.ROW, index = 4),
            ),
            links = listOf(
                InferenceLink(
                    from = candidate(18, 1),
                    to = candidate(20, 1),
                    type = InferenceLinkType.DUAL,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(18, 1),
                    to = candidate(45, 1),
                    type = InferenceLinkType.DUAL,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(20, 1),
                    to = candidate(74, 1),
                    type = InferenceLinkType.DUAL,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(45, 1),
                    to = candidate(46, 1),
                    type = InferenceLinkType.DUAL,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(45, 1),
                    to = candidate(45, 8),
                    type = InferenceLinkType.DUAL,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(74, 1),
                    to = candidate(74, 5),
                    type = InferenceLinkType.DUAL,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(45, 8),
                    to = candidate(36, 8),
                    type = InferenceLinkType.DUAL,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(36, 8),
                    to = candidate(36, 5),
                    type = InferenceLinkType.DUAL,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(36, 5),
                    to = candidate(38, 5),
                    type = InferenceLinkType.DUAL,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(74, 5),
                    to = candidate(38, 5),
                    type = InferenceLinkType.WEAK,
                    branchId = 0,
                ),
            ),
            inferenceGraph = InferenceGraph(
                nodes = listOf(
                    InferenceNode(
                        id = 0,
                        candidate = candidate(18, 1),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = true,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 1,
                        candidate = candidate(20, 1),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 2,
                        candidate = candidate(45, 1),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 3,
                        candidate = candidate(74, 1),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 4,
                        candidate = candidate(46, 1),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 5,
                        candidate = candidate(45, 8),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 6,
                        candidate = candidate(74, 5),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 7,
                        candidate = candidate(36, 8),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 8,
                        candidate = candidate(36, 5),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 9,
                        candidate = candidate(38, 5),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 10,
                        candidate = candidate(38, 5),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = true,
                    ),
                ),
                edges = listOf(
                    InferenceEdge(fromNodeId = 0, toNodeId = 1, type = InferenceLinkType.DUAL),
                    InferenceEdge(fromNodeId = 0, toNodeId = 2, type = InferenceLinkType.DUAL),
                    InferenceEdge(fromNodeId = 1, toNodeId = 3, type = InferenceLinkType.DUAL),
                    InferenceEdge(fromNodeId = 2, toNodeId = 4, type = InferenceLinkType.DUAL),
                    InferenceEdge(fromNodeId = 2, toNodeId = 5, type = InferenceLinkType.DUAL),
                    InferenceEdge(fromNodeId = 3, toNodeId = 6, type = InferenceLinkType.DUAL),
                    InferenceEdge(fromNodeId = 5, toNodeId = 7, type = InferenceLinkType.DUAL),
                    InferenceEdge(fromNodeId = 7, toNodeId = 8, type = InferenceLinkType.DUAL),
                    InferenceEdge(fromNodeId = 8, toNodeId = 9, type = InferenceLinkType.DUAL),
                    InferenceEdge(fromNodeId = 6, toNodeId = 10, type = InferenceLinkType.WEAK),
                ),
                contradiction = InferenceContradiction(
                    type = InferenceContradictionType.OPPOSITE_TRUTHS,
                    candidate = candidate(38, 5),
                ),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/aic
private fun aicExample1() = advancedExample(
    givens = "140070658080001002002800000091000240000060000004005000006000001000900300700003020",
    solution = "143279658987651432562834917691387245375462189824195763436728591258916374719543826",
    candidates = "0000780600760000008k009g1o7w009490007o3800007w889591981c00001w3o5c00001c462e5w0b007ecxcl9g3q2u00037700chdh90bi0m002i4a5md4cg004a0j40000r6i00682g000hb41l4900bc008o",
    step = SolveStep(
        technique = TechniqueId.AIC,
        eliminations = listOf(CandidateElimination(candidate = candidate(19, 3))),
        evidence = StepEvidence(
            causeCells = setOf(cell(2), cell(74), cell(54), cell(55)),
            causeCandidates = setOf(
                candidate(2, 3),
                candidate(2, 9),
                candidate(74, 9),
                candidate(54, 9),
                candidate(54, 3),
                candidate(55, 3),
            ),
            focusDigits = setOf(3, 9),
            houses = listOf(
                HouseRef(type = HouseType.COLUMN, index = 2),
                HouseRef(type = HouseType.BOX, index = 6),
                HouseRef(type = HouseType.ROW, index = 6),
            ),
            links = listOf(
                InferenceLink(from = candidate(2, 3), to = candidate(2, 9), type = InferenceLinkType.STRONG),
                InferenceLink(from = candidate(2, 9), to = candidate(74, 9), type = InferenceLinkType.WEAK),
                InferenceLink(from = candidate(74, 9), to = candidate(54, 9), type = InferenceLinkType.STRONG),
                InferenceLink(from = candidate(54, 9), to = candidate(54, 3), type = InferenceLinkType.WEAK),
                InferenceLink(from = candidate(54, 3), to = candidate(55, 3), type = InferenceLinkType.STRONG),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/aic
private fun aicExample2() = advancedExample(
    givens = "000576003309241070070893120007310049000759001913460750095137000020685007730924010",
    solution = "281576493359241678476893125567312849842759361913468752695137284124685937738924516",
    candidates = "033s03000000awao00004w000000001c004w1k001400000000001k1e1c0000003m4i00004q143u00000012100000000000003m00003m4g00000000000a4g0a09000900000078780000004g0000004w001c",
    step = SolveStep(
        technique = TechniqueId.AIC,
        eliminations = listOf(CandidateElimination(candidate = candidate(20, 4))),
        evidence = StepEvidence(
            causeCells = setOf(cell(1), cell(7), cell(70), cell(43), cell(61), cell(54), cell(74), cell(20)),
            causeCandidates = setOf(
                candidate(1, 4),
                candidate(1, 8),
                candidate(7, 8),
                candidate(7, 9),
                candidate(70, 9),
                candidate(70, 3),
                candidate(43, 3),
                candidate(43, 6),
                candidate(61, 6),
                candidate(54, 6),
                candidate(74, 6),
                candidate(20, 6),
            ),
            focusDigits = setOf(4, 8, 9, 3, 6),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 0),
                HouseRef(type = HouseType.COLUMN, index = 7),
                HouseRef(type = HouseType.ROW, index = 6),
                HouseRef(type = HouseType.BOX, index = 6),
                HouseRef(type = HouseType.COLUMN, index = 2),
            ),
            links = listOf(
                InferenceLink(from = candidate(1, 4), to = candidate(1, 8), type = InferenceLinkType.STRONG),
                InferenceLink(from = candidate(1, 8), to = candidate(7, 8), type = InferenceLinkType.WEAK),
                InferenceLink(from = candidate(7, 8), to = candidate(7, 9), type = InferenceLinkType.STRONG),
                InferenceLink(from = candidate(7, 9), to = candidate(70, 9), type = InferenceLinkType.WEAK),
                InferenceLink(from = candidate(70, 9), to = candidate(70, 3), type = InferenceLinkType.STRONG),
                InferenceLink(from = candidate(70, 3), to = candidate(43, 3), type = InferenceLinkType.WEAK),
                InferenceLink(from = candidate(43, 3), to = candidate(43, 6), type = InferenceLinkType.STRONG),
                InferenceLink(from = candidate(43, 6), to = candidate(61, 6), type = InferenceLinkType.WEAK),
                InferenceLink(from = candidate(61, 6), to = candidate(54, 6), type = InferenceLinkType.STRONG),
                InferenceLink(from = candidate(54, 6), to = candidate(74, 6), type = InferenceLinkType.WEAK),
                InferenceLink(from = candidate(74, 6), to = candidate(20, 6), type = InferenceLinkType.STRONG),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/forcing-chain-types
private fun nishioForcingChainExample1() = advancedExample(
    givens = "943060000600000920207190003570009400094500002360040059035970280006000397729003500",
    solution = "943265871618734925257198643571329468894516732362847159435971286186452397729683514",
    candidates = "0000000200425d1s4100413k24442g000048000g000000484g140000003n4m070000103l3l000000054h2o2s0000003n5c005f3l00000900000000150000153t3l000a0j0r0000000000004o3l00000914",
    step = SolveStep(
        technique = TechniqueId.NISHIO_FORCING_CHAIN,
        eliminations = listOf(CandidateElimination(candidate = candidate(24, 8))),
        evidence = StepEvidence(
            causeCells = setOf(cell(24), cell(25), cell(79)),
            causeCandidates = setOf(
                candidate(24, 8),
                candidate(24, 6),
                candidate(25, 6),
                candidate(25, 4),
                candidate(79, 4),
                candidate(79, 1),
            ),
            focusDigits = setOf(8, 6, 4, 1),
            houses = listOf(HouseRef(type = HouseType.ROW, index = 2), HouseRef(type = HouseType.COLUMN, index = 7)),
            links = listOf(
                InferenceLink(
                    from = candidate(24, 8),
                    to = candidate(24, 6),
                    type = InferenceLinkType.WEAK,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(24, 6),
                    to = candidate(25, 6),
                    type = InferenceLinkType.STRONG,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(25, 6),
                    to = candidate(25, 4),
                    type = InferenceLinkType.WEAK,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(25, 4),
                    to = candidate(79, 4),
                    type = InferenceLinkType.STRONG,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(79, 4),
                    to = candidate(79, 1),
                    type = InferenceLinkType.WEAK,
                    branchId = 0,
                ),
            ),
            inferenceGraph = InferenceGraph(
                nodes = listOf(
                    InferenceNode(
                        id = 0,
                        candidate = candidate(24, 8),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = true,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 1,
                        candidate = candidate(24, 6),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 2,
                        candidate = candidate(25, 6),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 3,
                        candidate = candidate(25, 4),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 4,
                        candidate = candidate(79, 4),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 5,
                        candidate = candidate(79, 1),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = true,
                    ),
                ),
                edges = listOf(
                    InferenceEdge(fromNodeId = 0, toNodeId = 1, type = InferenceLinkType.WEAK),
                    InferenceEdge(
                        fromNodeId = 1,
                        toNodeId = 2,
                        type = InferenceLinkType.STRONG,
                        house = HouseRef(type = HouseType.ROW, index = 2),
                    ),
                    InferenceEdge(fromNodeId = 2, toNodeId = 3, type = InferenceLinkType.WEAK),
                    InferenceEdge(
                        fromNodeId = 3,
                        toNodeId = 4,
                        type = InferenceLinkType.STRONG,
                        house = HouseRef(type = HouseType.COLUMN, index = 7),
                    ),
                    InferenceEdge(fromNodeId = 4, toNodeId = 5, type = InferenceLinkType.WEAK),
                ),
                premise = InferencePremise(type = InferencePremiseType.NISHIO, candidates = listOf(candidate(24, 8))),
                contradiction = InferenceContradiction(
                    type = InferenceContradictionType.EMPTY_HOUSE,
                    house = HouseRef(type = HouseType.COLUMN, index = 7),
                ),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/forcing-chain-types
private fun nishioForcingChainExample2() = advancedExample(
    givens = "374028105912500008685040200800010052157209000206005701560000000401000506720056010",
    solution = "374928165912563478685741239843617952157289643296435781568194327431872596729356814",
    candidates = "000000800000008000000000002s1w0c300000000091001x009090007g78300024840000000000004k004s3w0c007g003w3o0000as000000as5pck27b0cu980078005gck1y00cm000000as3w0000b0007g",
    step = SolveStep(
        technique = TechniqueId.NISHIO_FORCING_CHAIN,
        eliminations = listOf(CandidateElimination(candidate = candidate(3, 6))),
        evidence = StepEvidence(
            causeCells = setOf(cell(3), cell(30), cell(7), cell(33), cell(52)),
            causeCandidates = setOf(
                candidate(3, 6),
                candidate(3, 9),
                candidate(30, 6),
                candidate(7, 9),
                candidate(33, 6),
                candidate(52, 9),
                candidate(33, 9),
            ),
            focusDigits = setOf(6, 9),
            houses = listOf(
                HouseRef(type = HouseType.COLUMN, index = 3),
                HouseRef(type = HouseType.ROW, index = 0),
                HouseRef(type = HouseType.ROW, index = 3),
                HouseRef(type = HouseType.COLUMN, index = 7),
                HouseRef(type = HouseType.BOX, index = 5),
            ),
            links = listOf(
                InferenceLink(from = candidate(3, 6), to = candidate(3, 9), type = InferenceLinkType.WEAK, branchId = 0),
                InferenceLink(
                    from = candidate(3, 6),
                    to = candidate(30, 6),
                    type = InferenceLinkType.WEAK,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(3, 9),
                    to = candidate(7, 9),
                    type = InferenceLinkType.STRONG,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(30, 6),
                    to = candidate(33, 6),
                    type = InferenceLinkType.STRONG,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(7, 9),
                    to = candidate(52, 9),
                    type = InferenceLinkType.WEAK,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(33, 6),
                    to = candidate(33, 9),
                    type = InferenceLinkType.WEAK,
                    branchId = 0,
                ),
            ),
            inferenceGraph = InferenceGraph(
                nodes = listOf(
                    InferenceNode(
                        id = 0,
                        candidate = candidate(3, 6),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = true,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 1,
                        candidate = candidate(3, 9),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 2,
                        candidate = candidate(30, 6),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 3,
                        candidate = candidate(7, 9),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 4,
                        candidate = candidate(33, 6),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 5,
                        candidate = candidate(52, 9),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = true,
                    ),
                    InferenceNode(
                        id = 6,
                        candidate = candidate(33, 9),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = true,
                    ),
                ),
                edges = listOf(
                    InferenceEdge(fromNodeId = 0, toNodeId = 1, type = InferenceLinkType.WEAK),
                    InferenceEdge(
                        fromNodeId = 0,
                        toNodeId = 2,
                        type = InferenceLinkType.WEAK,
                        house = HouseRef(type = HouseType.COLUMN, index = 3),
                    ),
                    InferenceEdge(
                        fromNodeId = 1,
                        toNodeId = 3,
                        type = InferenceLinkType.STRONG,
                        house = HouseRef(type = HouseType.ROW, index = 0),
                    ),
                    InferenceEdge(
                        fromNodeId = 2,
                        toNodeId = 4,
                        type = InferenceLinkType.STRONG,
                        house = HouseRef(type = HouseType.ROW, index = 3),
                    ),
                    InferenceEdge(
                        fromNodeId = 3,
                        toNodeId = 5,
                        type = InferenceLinkType.WEAK,
                        house = HouseRef(type = HouseType.COLUMN, index = 7),
                    ),
                    InferenceEdge(fromNodeId = 4, toNodeId = 6, type = InferenceLinkType.WEAK),
                ),
                premise = InferencePremise(type = InferencePremiseType.NISHIO, candidates = listOf(candidate(3, 6))),
                contradiction = InferenceContradiction(
                    type = InferenceContradictionType.EMPTY_HOUSE,
                    house = HouseRef(type = HouseType.BOX, index = 5),
                ),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/forcing-chain-types
private fun cellForcingChainExample1() = advancedExample(
    givens = "000840009580006021060005000800050230001480006005070010010900005000500102250018000",
    solution = "173842569589736421462195873897651234321489756645273918716924385938567142254318697",
    candidates = "1t1w1y00001v1c1c000000941w78002000009500960376005o5k5o0094a00x007500002090920000007a9c9c00187i000y007aaw003s30006k0012226c6g009898do00102000dk000000a41w0000a4a024",
    step = SolveStep(
        technique = TechniqueId.CELL_FORCING_CHAIN,
        eliminations = listOf(CandidateElimination(candidate = candidate(37, 3))),
        evidence = StepEvidence(
            causeCells = setOf(cell(1), cell(37), cell(2), cell(5), cell(41)),
            causeCandidates = setOf(
                candidate(1, 3),
                candidate(37, 3),
                candidate(1, 7),
                candidate(2, 3),
                candidate(2, 2),
                candidate(5, 2),
                candidate(41, 2),
                candidate(37, 2),
            ),
            focusDigits = setOf(3, 7, 2),
            houses = listOf(
                HouseRef(type = HouseType.COLUMN, index = 1),
                HouseRef(type = HouseType.ROW, index = 0),
                HouseRef(type = HouseType.COLUMN, index = 5),
                HouseRef(type = HouseType.ROW, index = 4),
            ),
            links = listOf(
                InferenceLink(
                    from = candidate(1, 3),
                    to = candidate(37, 3),
                    type = InferenceLinkType.WEAK,
                    branchId = 0,
                ),
                InferenceLink(from = candidate(1, 7), to = candidate(1, 3), type = InferenceLinkType.WEAK, branchId = 1),
                InferenceLink(
                    from = candidate(1, 3),
                    to = candidate(2, 3),
                    type = InferenceLinkType.STRONG,
                    branchId = 1,
                ),
                InferenceLink(from = candidate(2, 3), to = candidate(2, 2), type = InferenceLinkType.WEAK, branchId = 1),
                InferenceLink(
                    from = candidate(2, 2),
                    to = candidate(5, 2),
                    type = InferenceLinkType.STRONG,
                    branchId = 1,
                ),
                InferenceLink(
                    from = candidate(5, 2),
                    to = candidate(41, 2),
                    type = InferenceLinkType.WEAK,
                    branchId = 1,
                ),
                InferenceLink(
                    from = candidate(41, 2),
                    to = candidate(37, 2),
                    type = InferenceLinkType.STRONG,
                    branchId = 1,
                ),
                InferenceLink(
                    from = candidate(37, 2),
                    to = candidate(37, 3),
                    type = InferenceLinkType.WEAK,
                    branchId = 1,
                ),
            ),
            inferenceGraph = InferenceGraph(
                nodes = listOf(
                    InferenceNode(
                        id = 0,
                        candidate = candidate(1, 3),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = true,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 1,
                        candidate = candidate(37, 3),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = true,
                    ),
                    InferenceNode(
                        id = 2,
                        candidate = candidate(1, 7),
                        truth = InferenceTruth.TRUE,
                        branchId = 1,
                        isAssumption = true,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 3,
                        candidate = candidate(1, 3),
                        truth = InferenceTruth.FALSE,
                        branchId = 1,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 4,
                        candidate = candidate(2, 3),
                        truth = InferenceTruth.TRUE,
                        branchId = 1,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 5,
                        candidate = candidate(2, 2),
                        truth = InferenceTruth.FALSE,
                        branchId = 1,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 6,
                        candidate = candidate(5, 2),
                        truth = InferenceTruth.TRUE,
                        branchId = 1,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 7,
                        candidate = candidate(41, 2),
                        truth = InferenceTruth.FALSE,
                        branchId = 1,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 8,
                        candidate = candidate(37, 2),
                        truth = InferenceTruth.TRUE,
                        branchId = 1,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 9,
                        candidate = candidate(37, 3),
                        truth = InferenceTruth.FALSE,
                        branchId = 1,
                        isAssumption = false,
                        isConclusion = true,
                    ),
                ),
                edges = listOf(
                    InferenceEdge(
                        fromNodeId = 0,
                        toNodeId = 1,
                        type = InferenceLinkType.WEAK,
                        house = HouseRef(type = HouseType.COLUMN, index = 1),
                    ),
                    InferenceEdge(fromNodeId = 2, toNodeId = 3, type = InferenceLinkType.WEAK),
                    InferenceEdge(
                        fromNodeId = 3,
                        toNodeId = 4,
                        type = InferenceLinkType.STRONG,
                        house = HouseRef(type = HouseType.ROW, index = 0),
                    ),
                    InferenceEdge(fromNodeId = 4, toNodeId = 5, type = InferenceLinkType.WEAK),
                    InferenceEdge(
                        fromNodeId = 5,
                        toNodeId = 6,
                        type = InferenceLinkType.STRONG,
                        house = HouseRef(type = HouseType.ROW, index = 0),
                    ),
                    InferenceEdge(
                        fromNodeId = 6,
                        toNodeId = 7,
                        type = InferenceLinkType.WEAK,
                        house = HouseRef(type = HouseType.COLUMN, index = 5),
                    ),
                    InferenceEdge(
                        fromNodeId = 7,
                        toNodeId = 8,
                        type = InferenceLinkType.STRONG,
                        house = HouseRef(type = HouseType.ROW, index = 4),
                    ),
                    InferenceEdge(fromNodeId = 8, toNodeId = 9, type = InferenceLinkType.WEAK),
                ),
                premise = InferencePremise(
                    type = InferencePremiseType.CELL,
                    candidates = listOf(candidate(1, 3), candidate(1, 7)),
                ),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/forcing-chain-types
private fun cellForcingNetExample1() = advancedExample(
    givens = "597461030621538974483297156102000000000302581058916420870100005000000710015009040",
    solution = "597461238621538974483297156142785369769342581358916427874123695936854712215679843",
    candidates = "0000000000003m003m000000000000000000000000000000000000000c005c600o10808w8w14880020000000001w000000000000001w000088000a0c1280007a18884g4a0s0000aq060000685e004m003q",
    step = SolveStep(
        technique = TechniqueId.CELL_FORCING_NET,
        eliminations = listOf(CandidateElimination(candidate = candidate(80, 2))),
        evidence = StepEvidence(
            causeCells = setOf(cell(6), cell(8), cell(78), cell(71), cell(75), cell(30), cell(35), cell(80)),
            causeCandidates = setOf(
                candidate(6, 2),
                candidate(6, 8),
                candidate(8, 8),
                candidate(78, 8),
                candidate(71, 8),
                candidate(78, 6),
                candidate(75, 6),
                candidate(75, 7),
                candidate(30, 7),
                candidate(35, 7),
                candidate(35, 9),
                candidate(71, 9),
                candidate(71, 2),
                candidate(80, 2),
                candidate(8, 2),
            ),
            focusDigits = setOf(2, 8, 6, 7, 9),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 0),
                HouseRef(type = HouseType.COLUMN, index = 6),
                HouseRef(type = HouseType.COLUMN, index = 8),
                HouseRef(type = HouseType.ROW, index = 8),
                HouseRef(type = HouseType.COLUMN, index = 3),
                HouseRef(type = HouseType.ROW, index = 3),
            ),
            links = listOf(
                InferenceLink(from = candidate(6, 2), to = candidate(6, 8), type = InferenceLinkType.WEAK, branchId = 0),
                InferenceLink(
                    from = candidate(6, 8),
                    to = candidate(8, 8),
                    type = InferenceLinkType.STRONG,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(6, 8),
                    to = candidate(78, 8),
                    type = InferenceLinkType.STRONG,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(8, 8),
                    to = candidate(71, 8),
                    type = InferenceLinkType.WEAK,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(78, 8),
                    to = candidate(78, 6),
                    type = InferenceLinkType.WEAK,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(78, 6),
                    to = candidate(75, 6),
                    type = InferenceLinkType.STRONG,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(75, 6),
                    to = candidate(75, 7),
                    type = InferenceLinkType.WEAK,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(75, 7),
                    to = candidate(30, 7),
                    type = InferenceLinkType.STRONG,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(30, 7),
                    to = candidate(35, 7),
                    type = InferenceLinkType.WEAK,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(35, 7),
                    to = candidate(35, 9),
                    type = InferenceLinkType.STRONG,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(35, 9),
                    to = candidate(71, 9),
                    type = InferenceLinkType.WEAK,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(71, 8),
                    to = candidate(71, 2),
                    type = InferenceLinkType.STRONG,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(71, 9),
                    to = candidate(71, 2),
                    type = InferenceLinkType.STRONG,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(71, 2),
                    to = candidate(80, 2),
                    type = InferenceLinkType.WEAK,
                    branchId = 0,
                ),
                InferenceLink(from = candidate(6, 8), to = candidate(6, 2), type = InferenceLinkType.WEAK, branchId = 1),
                InferenceLink(
                    from = candidate(6, 2),
                    to = candidate(8, 2),
                    type = InferenceLinkType.STRONG,
                    branchId = 1,
                ),
                InferenceLink(
                    from = candidate(8, 2),
                    to = candidate(80, 2),
                    type = InferenceLinkType.WEAK,
                    branchId = 1,
                ),
            ),
            inferenceGraph = InferenceGraph(
                nodes = listOf(
                    InferenceNode(
                        id = 0,
                        candidate = candidate(6, 2),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = true,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 1,
                        candidate = candidate(6, 8),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 2,
                        candidate = candidate(8, 8),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 3,
                        candidate = candidate(78, 8),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 4,
                        candidate = candidate(71, 8),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 5,
                        candidate = candidate(78, 6),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 6,
                        candidate = candidate(75, 6),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 7,
                        candidate = candidate(75, 7),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 8,
                        candidate = candidate(30, 7),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 9,
                        candidate = candidate(35, 7),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 10,
                        candidate = candidate(35, 9),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 11,
                        candidate = candidate(71, 9),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 12,
                        candidate = candidate(71, 2),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 13,
                        candidate = candidate(80, 2),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = true,
                    ),
                    InferenceNode(
                        id = 14,
                        candidate = candidate(6, 8),
                        truth = InferenceTruth.TRUE,
                        branchId = 1,
                        isAssumption = true,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 15,
                        candidate = candidate(6, 2),
                        truth = InferenceTruth.FALSE,
                        branchId = 1,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 16,
                        candidate = candidate(8, 2),
                        truth = InferenceTruth.TRUE,
                        branchId = 1,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 17,
                        candidate = candidate(80, 2),
                        truth = InferenceTruth.FALSE,
                        branchId = 1,
                        isAssumption = false,
                        isConclusion = true,
                    ),
                ),
                edges = listOf(
                    InferenceEdge(fromNodeId = 0, toNodeId = 1, type = InferenceLinkType.WEAK),
                    InferenceEdge(
                        fromNodeId = 1,
                        toNodeId = 2,
                        type = InferenceLinkType.STRONG,
                        house = HouseRef(type = HouseType.ROW, index = 0),
                    ),
                    InferenceEdge(
                        fromNodeId = 1,
                        toNodeId = 3,
                        type = InferenceLinkType.STRONG,
                        house = HouseRef(type = HouseType.COLUMN, index = 6),
                    ),
                    InferenceEdge(
                        fromNodeId = 2,
                        toNodeId = 4,
                        type = InferenceLinkType.WEAK,
                        house = HouseRef(type = HouseType.COLUMN, index = 8),
                    ),
                    InferenceEdge(fromNodeId = 3, toNodeId = 5, type = InferenceLinkType.WEAK),
                    InferenceEdge(
                        fromNodeId = 5,
                        toNodeId = 6,
                        type = InferenceLinkType.STRONG,
                        house = HouseRef(type = HouseType.ROW, index = 8),
                    ),
                    InferenceEdge(fromNodeId = 6, toNodeId = 7, type = InferenceLinkType.WEAK),
                    InferenceEdge(
                        fromNodeId = 7,
                        toNodeId = 8,
                        type = InferenceLinkType.STRONG,
                        house = HouseRef(type = HouseType.COLUMN, index = 3),
                    ),
                    InferenceEdge(
                        fromNodeId = 8,
                        toNodeId = 9,
                        type = InferenceLinkType.WEAK,
                        house = HouseRef(type = HouseType.ROW, index = 3),
                    ),
                    InferenceEdge(fromNodeId = 9, toNodeId = 10, type = InferenceLinkType.STRONG),
                    InferenceEdge(
                        fromNodeId = 10,
                        toNodeId = 11,
                        type = InferenceLinkType.WEAK,
                        house = HouseRef(type = HouseType.COLUMN, index = 8),
                    ),
                    InferenceEdge(fromNodeId = 4, toNodeId = 12, type = InferenceLinkType.STRONG),
                    InferenceEdge(fromNodeId = 11, toNodeId = 12, type = InferenceLinkType.STRONG),
                    InferenceEdge(
                        fromNodeId = 12,
                        toNodeId = 13,
                        type = InferenceLinkType.WEAK,
                        house = HouseRef(type = HouseType.COLUMN, index = 8),
                    ),
                    InferenceEdge(fromNodeId = 14, toNodeId = 15, type = InferenceLinkType.WEAK),
                    InferenceEdge(
                        fromNodeId = 15,
                        toNodeId = 16,
                        type = InferenceLinkType.STRONG,
                        house = HouseRef(type = HouseType.ROW, index = 0),
                    ),
                    InferenceEdge(
                        fromNodeId = 16,
                        toNodeId = 17,
                        type = InferenceLinkType.WEAK,
                        house = HouseRef(type = HouseType.COLUMN, index = 8),
                    ),
                ),
                premise = InferencePremise(
                    type = InferencePremiseType.CELL,
                    candidates = listOf(candidate(6, 2), candidate(6, 8)),
                ),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/forcing-chain-types
private fun cellForcingNetExample2() = advancedExample(
    givens = "943060000600000920207190003570009400094500002360040059035970280006000397729003500",
    solution = "943265871618734925257198643571329468894516732362847159435971286186452397729683514",
    candidates = "0000003m00425d1t4100413l24442g0000480040000000484g140000003n4m070000103l3l000000054h2o2s0000003n5c005f3l00000900000000150000153t3l000a0j0r0000000000004o3l00000914",
    step = SolveStep(
        technique = TechniqueId.CELL_FORCING_NET,
        eliminations = listOf(CandidateElimination(candidate = candidate(14, 5))),
        evidence = StepEvidence(
            causeCells = setOf(
                cell(48),
                cell(12),
                cell(14),
                cell(3),
                cell(51),
                cell(75),
                cell(35),
                cell(76),
                cell(66),
                cell(17),
                cell(79),
                cell(80),
            ),
            causeCandidates = setOf(
                candidate(48, 7),
                candidate(12, 7),
                candidate(14, 7),
                candidate(14, 5),
                candidate(48, 8),
                candidate(3, 8),
                candidate(51, 8),
                candidate(75, 8),
                candidate(3, 2),
                candidate(35, 8),
                candidate(76, 8),
                candidate(66, 2),
                candidate(17, 8),
                candidate(76, 1),
                candidate(66, 4),
                candidate(79, 1),
                candidate(75, 4),
                candidate(79, 4),
                candidate(80, 4),
                candidate(17, 4),
                candidate(17, 5),
            ),
            focusDigits = setOf(7, 5, 8, 2, 1, 4),
            houses = listOf(
                HouseRef(type = HouseType.COLUMN, index = 3),
                HouseRef(type = HouseType.ROW, index = 1),
                HouseRef(type = HouseType.ROW, index = 5),
                HouseRef(type = HouseType.BOX, index = 5),
                HouseRef(type = HouseType.ROW, index = 8),
                HouseRef(type = HouseType.COLUMN, index = 8),
            ),
            links = listOf(
                InferenceLink(
                    from = candidate(48, 7),
                    to = candidate(12, 7),
                    type = InferenceLinkType.WEAK,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(12, 7),
                    to = candidate(14, 7),
                    type = InferenceLinkType.STRONG,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(14, 7),
                    to = candidate(14, 5),
                    type = InferenceLinkType.WEAK,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(48, 8),
                    to = candidate(3, 8),
                    type = InferenceLinkType.WEAK,
                    branchId = 1,
                ),
                InferenceLink(
                    from = candidate(48, 8),
                    to = candidate(51, 8),
                    type = InferenceLinkType.WEAK,
                    branchId = 1,
                ),
                InferenceLink(
                    from = candidate(48, 8),
                    to = candidate(75, 8),
                    type = InferenceLinkType.WEAK,
                    branchId = 1,
                ),
                InferenceLink(
                    from = candidate(3, 8),
                    to = candidate(3, 2),
                    type = InferenceLinkType.STRONG,
                    branchId = 1,
                ),
                InferenceLink(
                    from = candidate(51, 8),
                    to = candidate(35, 8),
                    type = InferenceLinkType.STRONG,
                    branchId = 1,
                ),
                InferenceLink(
                    from = candidate(75, 8),
                    to = candidate(76, 8),
                    type = InferenceLinkType.STRONG,
                    branchId = 1,
                ),
                InferenceLink(
                    from = candidate(3, 2),
                    to = candidate(66, 2),
                    type = InferenceLinkType.WEAK,
                    branchId = 1,
                ),
                InferenceLink(
                    from = candidate(35, 8),
                    to = candidate(17, 8),
                    type = InferenceLinkType.WEAK,
                    branchId = 1,
                ),
                InferenceLink(
                    from = candidate(76, 8),
                    to = candidate(76, 1),
                    type = InferenceLinkType.WEAK,
                    branchId = 1,
                ),
                InferenceLink(
                    from = candidate(66, 2),
                    to = candidate(66, 4),
                    type = InferenceLinkType.STRONG,
                    branchId = 1,
                ),
                InferenceLink(
                    from = candidate(76, 1),
                    to = candidate(79, 1),
                    type = InferenceLinkType.STRONG,
                    branchId = 1,
                ),
                InferenceLink(
                    from = candidate(66, 4),
                    to = candidate(75, 4),
                    type = InferenceLinkType.WEAK,
                    branchId = 1,
                ),
                InferenceLink(
                    from = candidate(79, 1),
                    to = candidate(79, 4),
                    type = InferenceLinkType.WEAK,
                    branchId = 1,
                ),
                InferenceLink(
                    from = candidate(75, 4),
                    to = candidate(80, 4),
                    type = InferenceLinkType.STRONG,
                    branchId = 1,
                ),
                InferenceLink(
                    from = candidate(79, 4),
                    to = candidate(80, 4),
                    type = InferenceLinkType.STRONG,
                    branchId = 1,
                ),
                InferenceLink(
                    from = candidate(80, 4),
                    to = candidate(17, 4),
                    type = InferenceLinkType.WEAK,
                    branchId = 1,
                ),
                InferenceLink(
                    from = candidate(17, 4),
                    to = candidate(17, 5),
                    type = InferenceLinkType.STRONG,
                    branchId = 1,
                ),
                InferenceLink(
                    from = candidate(17, 8),
                    to = candidate(17, 5),
                    type = InferenceLinkType.STRONG,
                    branchId = 1,
                ),
                InferenceLink(
                    from = candidate(17, 5),
                    to = candidate(14, 5),
                    type = InferenceLinkType.WEAK,
                    branchId = 1,
                ),
            ),
            inferenceGraph = InferenceGraph(
                nodes = listOf(
                    InferenceNode(
                        id = 0,
                        candidate = candidate(48, 7),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = true,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 1,
                        candidate = candidate(12, 7),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 2,
                        candidate = candidate(14, 7),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 3,
                        candidate = candidate(14, 5),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = true,
                    ),
                    InferenceNode(
                        id = 4,
                        candidate = candidate(48, 8),
                        truth = InferenceTruth.TRUE,
                        branchId = 1,
                        isAssumption = true,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 5,
                        candidate = candidate(3, 8),
                        truth = InferenceTruth.FALSE,
                        branchId = 1,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 6,
                        candidate = candidate(51, 8),
                        truth = InferenceTruth.FALSE,
                        branchId = 1,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 7,
                        candidate = candidate(75, 8),
                        truth = InferenceTruth.FALSE,
                        branchId = 1,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 8,
                        candidate = candidate(3, 2),
                        truth = InferenceTruth.TRUE,
                        branchId = 1,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 9,
                        candidate = candidate(35, 8),
                        truth = InferenceTruth.TRUE,
                        branchId = 1,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 10,
                        candidate = candidate(76, 8),
                        truth = InferenceTruth.TRUE,
                        branchId = 1,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 11,
                        candidate = candidate(66, 2),
                        truth = InferenceTruth.FALSE,
                        branchId = 1,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 12,
                        candidate = candidate(17, 8),
                        truth = InferenceTruth.FALSE,
                        branchId = 1,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 13,
                        candidate = candidate(76, 1),
                        truth = InferenceTruth.FALSE,
                        branchId = 1,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 14,
                        candidate = candidate(66, 4),
                        truth = InferenceTruth.TRUE,
                        branchId = 1,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 15,
                        candidate = candidate(79, 1),
                        truth = InferenceTruth.TRUE,
                        branchId = 1,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 16,
                        candidate = candidate(75, 4),
                        truth = InferenceTruth.FALSE,
                        branchId = 1,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 17,
                        candidate = candidate(79, 4),
                        truth = InferenceTruth.FALSE,
                        branchId = 1,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 18,
                        candidate = candidate(80, 4),
                        truth = InferenceTruth.TRUE,
                        branchId = 1,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 19,
                        candidate = candidate(17, 4),
                        truth = InferenceTruth.FALSE,
                        branchId = 1,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 20,
                        candidate = candidate(17, 5),
                        truth = InferenceTruth.TRUE,
                        branchId = 1,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 21,
                        candidate = candidate(14, 5),
                        truth = InferenceTruth.FALSE,
                        branchId = 1,
                        isAssumption = false,
                        isConclusion = true,
                    ),
                ),
                edges = listOf(
                    InferenceEdge(
                        fromNodeId = 0,
                        toNodeId = 1,
                        type = InferenceLinkType.WEAK,
                        house = HouseRef(type = HouseType.COLUMN, index = 3),
                    ),
                    InferenceEdge(
                        fromNodeId = 1,
                        toNodeId = 2,
                        type = InferenceLinkType.STRONG,
                        house = HouseRef(type = HouseType.ROW, index = 1),
                    ),
                    InferenceEdge(fromNodeId = 2, toNodeId = 3, type = InferenceLinkType.WEAK),
                    InferenceEdge(
                        fromNodeId = 4,
                        toNodeId = 5,
                        type = InferenceLinkType.WEAK,
                        house = HouseRef(type = HouseType.COLUMN, index = 3),
                    ),
                    InferenceEdge(
                        fromNodeId = 4,
                        toNodeId = 6,
                        type = InferenceLinkType.WEAK,
                        house = HouseRef(type = HouseType.ROW, index = 5),
                    ),
                    InferenceEdge(
                        fromNodeId = 4,
                        toNodeId = 7,
                        type = InferenceLinkType.WEAK,
                        house = HouseRef(type = HouseType.COLUMN, index = 3),
                    ),
                    InferenceEdge(fromNodeId = 5, toNodeId = 8, type = InferenceLinkType.STRONG),
                    InferenceEdge(
                        fromNodeId = 6,
                        toNodeId = 9,
                        type = InferenceLinkType.STRONG,
                        house = HouseRef(type = HouseType.BOX, index = 5),
                    ),
                    InferenceEdge(
                        fromNodeId = 7,
                        toNodeId = 10,
                        type = InferenceLinkType.STRONG,
                        house = HouseRef(type = HouseType.ROW, index = 8),
                    ),
                    InferenceEdge(
                        fromNodeId = 8,
                        toNodeId = 11,
                        type = InferenceLinkType.WEAK,
                        house = HouseRef(type = HouseType.COLUMN, index = 3),
                    ),
                    InferenceEdge(
                        fromNodeId = 9,
                        toNodeId = 12,
                        type = InferenceLinkType.WEAK,
                        house = HouseRef(type = HouseType.COLUMN, index = 8),
                    ),
                    InferenceEdge(fromNodeId = 10, toNodeId = 13, type = InferenceLinkType.WEAK),
                    InferenceEdge(fromNodeId = 11, toNodeId = 14, type = InferenceLinkType.STRONG),
                    InferenceEdge(
                        fromNodeId = 13,
                        toNodeId = 15,
                        type = InferenceLinkType.STRONG,
                        house = HouseRef(type = HouseType.ROW, index = 8),
                    ),
                    InferenceEdge(
                        fromNodeId = 14,
                        toNodeId = 16,
                        type = InferenceLinkType.WEAK,
                        house = HouseRef(type = HouseType.COLUMN, index = 3),
                    ),
                    InferenceEdge(fromNodeId = 15, toNodeId = 17, type = InferenceLinkType.WEAK),
                    InferenceEdge(
                        fromNodeId = 16,
                        toNodeId = 18,
                        type = InferenceLinkType.STRONG,
                        house = HouseRef(type = HouseType.ROW, index = 8),
                    ),
                    InferenceEdge(
                        fromNodeId = 17,
                        toNodeId = 18,
                        type = InferenceLinkType.STRONG,
                        house = HouseRef(type = HouseType.ROW, index = 8),
                    ),
                    InferenceEdge(
                        fromNodeId = 18,
                        toNodeId = 19,
                        type = InferenceLinkType.WEAK,
                        house = HouseRef(type = HouseType.COLUMN, index = 8),
                    ),
                    InferenceEdge(fromNodeId = 19, toNodeId = 20, type = InferenceLinkType.STRONG),
                    InferenceEdge(fromNodeId = 12, toNodeId = 20, type = InferenceLinkType.STRONG),
                    InferenceEdge(
                        fromNodeId = 20,
                        toNodeId = 21,
                        type = InferenceLinkType.WEAK,
                        house = HouseRef(type = HouseType.ROW, index = 1),
                    ),
                ),
                premise = InferencePremise(
                    type = InferencePremiseType.CELL,
                    candidates = listOf(candidate(48, 7), candidate(48, 8)),
                ),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/forcing-chain-types
private fun regionForcingChainExample1() = advancedExample(
    givens = "000840009580006021060005000800050230001480006005070010010900005000500102250018000",
    solution = "173842569589736421462195873897651234321489756645273918716924385938567142254318697",
    candidates = "1x1w0600001v1c1c000000941w78002000009500960376005o5k5o0094a00x007500002090920000007a9c9c00187i000y007aaw003s30006k0012226c6g009090dg00102000dk000000a41w0000a4a024",
    step = SolveStep(
        technique = TechniqueId.REGION_FORCING_CHAIN,
        eliminations = listOf(CandidateElimination(candidate = candidate(29, 4))),
        evidence = StepEvidence(
            causeCells = setOf(cell(28), cell(29), cell(46)),
            causeCandidates = setOf(candidate(28, 4), candidate(29, 4), candidate(46, 4)),
            focusDigits = setOf(4),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 3),
                HouseRef(type = HouseType.BOX, index = 3),
                HouseRef(type = HouseType.COLUMN, index = 1),
            ),
            links = listOf(
                InferenceLink(
                    from = candidate(28, 4),
                    to = candidate(29, 4),
                    type = InferenceLinkType.WEAK,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(46, 4),
                    to = candidate(29, 4),
                    type = InferenceLinkType.WEAK,
                    branchId = 1,
                ),
            ),
            inferenceGraph = InferenceGraph(
                nodes = listOf(
                    InferenceNode(
                        id = 0,
                        candidate = candidate(28, 4),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = true,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 1,
                        candidate = candidate(29, 4),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = true,
                    ),
                    InferenceNode(
                        id = 2,
                        candidate = candidate(46, 4),
                        truth = InferenceTruth.TRUE,
                        branchId = 1,
                        isAssumption = true,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 3,
                        candidate = candidate(29, 4),
                        truth = InferenceTruth.FALSE,
                        branchId = 1,
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
                        fromNodeId = 2,
                        toNodeId = 3,
                        type = InferenceLinkType.WEAK,
                        house = HouseRef(type = HouseType.BOX, index = 3),
                    ),
                ),
                premise = InferencePremise(
                    type = InferencePremiseType.REGION,
                    candidates = listOf(candidate(28, 4), candidate(46, 4)),
                    house = HouseRef(type = HouseType.COLUMN, index = 1),
                ),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/forcing-chain-types
private fun regionForcingChainExample2() = advancedExample(
    givens = "900800000040000389008009000070508000005040100009001056000200000000086527862175934",
    solution = "916837245547612389328459761271568493685943172439721856753294618194386527862175934",
    candidates = "000n2t001j262y210j37002p2o0j1u0000002f0n000c1j002y2x0j1b00150080000a7c06123q0080001y008w3m0e3q001w06005m00002k7o2400780c4g0x3l0d790d7g0000000000000000000000000000",
    step = SolveStep(
        technique = TechniqueId.REGION_FORCING_CHAIN,
        eliminations = listOf(CandidateElimination(candidate = candidate(46, 2))),
        evidence = StepEvidence(
            causeCells = setOf(cell(37), cell(44), cell(41), cell(49), cell(46)),
            causeCandidates = setOf(
                candidate(37, 8),
                candidate(44, 8),
                candidate(44, 2),
                candidate(41, 2),
                candidate(49, 2),
                candidate(46, 2),
                candidate(46, 8),
            ),
            focusDigits = setOf(8, 2),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 4),
                HouseRef(type = HouseType.BOX, index = 4),
                HouseRef(type = HouseType.ROW, index = 5),
                HouseRef(type = HouseType.COLUMN, index = 1),
            ),
            links = listOf(
                InferenceLink(
                    from = candidate(37, 8),
                    to = candidate(44, 8),
                    type = InferenceLinkType.WEAK,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(44, 8),
                    to = candidate(44, 2),
                    type = InferenceLinkType.STRONG,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(44, 2),
                    to = candidate(41, 2),
                    type = InferenceLinkType.WEAK,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(41, 2),
                    to = candidate(49, 2),
                    type = InferenceLinkType.STRONG,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(49, 2),
                    to = candidate(46, 2),
                    type = InferenceLinkType.WEAK,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(44, 8),
                    to = candidate(37, 8),
                    type = InferenceLinkType.WEAK,
                    branchId = 1,
                ),
                InferenceLink(
                    from = candidate(37, 8),
                    to = candidate(46, 8),
                    type = InferenceLinkType.STRONG,
                    branchId = 1,
                ),
                InferenceLink(
                    from = candidate(46, 8),
                    to = candidate(46, 2),
                    type = InferenceLinkType.WEAK,
                    branchId = 1,
                ),
            ),
            inferenceGraph = InferenceGraph(
                nodes = listOf(
                    InferenceNode(
                        id = 0,
                        candidate = candidate(37, 8),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = true,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 1,
                        candidate = candidate(44, 8),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 2,
                        candidate = candidate(44, 2),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 3,
                        candidate = candidate(41, 2),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 4,
                        candidate = candidate(49, 2),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 5,
                        candidate = candidate(46, 2),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = true,
                    ),
                    InferenceNode(
                        id = 6,
                        candidate = candidate(44, 8),
                        truth = InferenceTruth.TRUE,
                        branchId = 1,
                        isAssumption = true,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 7,
                        candidate = candidate(37, 8),
                        truth = InferenceTruth.FALSE,
                        branchId = 1,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 8,
                        candidate = candidate(46, 8),
                        truth = InferenceTruth.TRUE,
                        branchId = 1,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 9,
                        candidate = candidate(46, 2),
                        truth = InferenceTruth.FALSE,
                        branchId = 1,
                        isAssumption = false,
                        isConclusion = true,
                    ),
                ),
                edges = listOf(
                    InferenceEdge(
                        fromNodeId = 0,
                        toNodeId = 1,
                        type = InferenceLinkType.WEAK,
                        house = HouseRef(type = HouseType.ROW, index = 4),
                    ),
                    InferenceEdge(fromNodeId = 1, toNodeId = 2, type = InferenceLinkType.STRONG),
                    InferenceEdge(
                        fromNodeId = 2,
                        toNodeId = 3,
                        type = InferenceLinkType.WEAK,
                        house = HouseRef(type = HouseType.ROW, index = 4),
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
                        house = HouseRef(type = HouseType.ROW, index = 5),
                    ),
                    InferenceEdge(
                        fromNodeId = 6,
                        toNodeId = 7,
                        type = InferenceLinkType.WEAK,
                        house = HouseRef(type = HouseType.ROW, index = 4),
                    ),
                    InferenceEdge(
                        fromNodeId = 7,
                        toNodeId = 8,
                        type = InferenceLinkType.STRONG,
                        house = HouseRef(type = HouseType.COLUMN, index = 1),
                    ),
                    InferenceEdge(fromNodeId = 8, toNodeId = 9, type = InferenceLinkType.WEAK),
                ),
                premise = InferencePremise(
                    type = InferencePremiseType.REGION,
                    candidates = listOf(candidate(37, 8), candidate(44, 8)),
                    house = HouseRef(type = HouseType.ROW, index = 4),
                ),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/forcing-chain-types
private fun regionForcingNetExample1() = advancedExample(
    givens = "943060000600000920207190003570009400094500002360040059035970280006000397729003500",
    solution = "943265871618734925257198643571329468894516732362847159435971286186452397729683514",
    candidates = "0000003m00425d1t0h00413l24442g0000480040000000484g140000003n4m070000103l3l000000054h2o2s0000003n5c005f3l00000900000000150000153t3l000a0j0r0000000000004o3l00000914",
    step = SolveStep(
        technique = TechniqueId.REGION_FORCING_NET,
        eliminations = listOf(CandidateElimination(candidate = candidate(14, 5))),
        evidence = StepEvidence(
            causeCells = setOf(cell(12), cell(30), cell(34), cell(25), cell(23), cell(14)),
            causeCandidates = setOf(
                candidate(12, 7),
                candidate(12, 3),
                candidate(12, 4),
                candidate(30, 3),
                candidate(30, 6),
                candidate(34, 6),
                candidate(25, 6),
                candidate(25, 4),
                candidate(23, 4),
                candidate(14, 4),
                candidate(14, 5),
                candidate(14, 7),
            ),
            focusDigits = setOf(7, 3, 4, 6, 5),
            houses = listOf(
                HouseRef(type = HouseType.COLUMN, index = 3),
                HouseRef(type = HouseType.ROW, index = 3),
                HouseRef(type = HouseType.COLUMN, index = 7),
                HouseRef(type = HouseType.ROW, index = 2),
                HouseRef(type = HouseType.BOX, index = 1),
                HouseRef(type = HouseType.ROW, index = 1),
            ),
            links = listOf(
                InferenceLink(
                    from = candidate(12, 7),
                    to = candidate(12, 3),
                    type = InferenceLinkType.WEAK,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(12, 7),
                    to = candidate(12, 4),
                    type = InferenceLinkType.WEAK,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(12, 3),
                    to = candidate(30, 3),
                    type = InferenceLinkType.STRONG,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(30, 3),
                    to = candidate(30, 6),
                    type = InferenceLinkType.WEAK,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(30, 6),
                    to = candidate(34, 6),
                    type = InferenceLinkType.STRONG,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(34, 6),
                    to = candidate(25, 6),
                    type = InferenceLinkType.WEAK,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(25, 6),
                    to = candidate(25, 4),
                    type = InferenceLinkType.STRONG,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(25, 4),
                    to = candidate(23, 4),
                    type = InferenceLinkType.WEAK,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(12, 4),
                    to = candidate(14, 4),
                    type = InferenceLinkType.STRONG,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(23, 4),
                    to = candidate(14, 4),
                    type = InferenceLinkType.STRONG,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(14, 4),
                    to = candidate(14, 5),
                    type = InferenceLinkType.WEAK,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(14, 7),
                    to = candidate(14, 5),
                    type = InferenceLinkType.WEAK,
                    branchId = 1,
                ),
            ),
            inferenceGraph = InferenceGraph(
                nodes = listOf(
                    InferenceNode(
                        id = 0,
                        candidate = candidate(12, 7),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = true,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 1,
                        candidate = candidate(12, 3),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 2,
                        candidate = candidate(12, 4),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 3,
                        candidate = candidate(30, 3),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 4,
                        candidate = candidate(30, 6),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 5,
                        candidate = candidate(34, 6),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 6,
                        candidate = candidate(25, 6),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 7,
                        candidate = candidate(25, 4),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 8,
                        candidate = candidate(23, 4),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 9,
                        candidate = candidate(14, 4),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 10,
                        candidate = candidate(14, 5),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = true,
                    ),
                    InferenceNode(
                        id = 11,
                        candidate = candidate(14, 7),
                        truth = InferenceTruth.TRUE,
                        branchId = 1,
                        isAssumption = true,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 12,
                        candidate = candidate(14, 5),
                        truth = InferenceTruth.FALSE,
                        branchId = 1,
                        isAssumption = false,
                        isConclusion = true,
                    ),
                ),
                edges = listOf(
                    InferenceEdge(fromNodeId = 0, toNodeId = 1, type = InferenceLinkType.WEAK),
                    InferenceEdge(fromNodeId = 0, toNodeId = 2, type = InferenceLinkType.WEAK),
                    InferenceEdge(
                        fromNodeId = 1,
                        toNodeId = 3,
                        type = InferenceLinkType.STRONG,
                        house = HouseRef(type = HouseType.COLUMN, index = 3),
                    ),
                    InferenceEdge(fromNodeId = 3, toNodeId = 4, type = InferenceLinkType.WEAK),
                    InferenceEdge(
                        fromNodeId = 4,
                        toNodeId = 5,
                        type = InferenceLinkType.STRONG,
                        house = HouseRef(type = HouseType.ROW, index = 3),
                    ),
                    InferenceEdge(
                        fromNodeId = 5,
                        toNodeId = 6,
                        type = InferenceLinkType.WEAK,
                        house = HouseRef(type = HouseType.COLUMN, index = 7),
                    ),
                    InferenceEdge(fromNodeId = 6, toNodeId = 7, type = InferenceLinkType.STRONG),
                    InferenceEdge(
                        fromNodeId = 7,
                        toNodeId = 8,
                        type = InferenceLinkType.WEAK,
                        house = HouseRef(type = HouseType.ROW, index = 2),
                    ),
                    InferenceEdge(
                        fromNodeId = 2,
                        toNodeId = 9,
                        type = InferenceLinkType.STRONG,
                        house = HouseRef(type = HouseType.BOX, index = 1),
                    ),
                    InferenceEdge(
                        fromNodeId = 8,
                        toNodeId = 9,
                        type = InferenceLinkType.STRONG,
                        house = HouseRef(type = HouseType.BOX, index = 1),
                    ),
                    InferenceEdge(fromNodeId = 9, toNodeId = 10, type = InferenceLinkType.WEAK),
                    InferenceEdge(fromNodeId = 11, toNodeId = 12, type = InferenceLinkType.WEAK),
                ),
                premise = InferencePremise(
                    type = InferencePremiseType.REGION,
                    candidates = listOf(candidate(12, 7), candidate(14, 7)),
                    house = HouseRef(type = HouseType.ROW, index = 1),
                ),
            ),
        ),
    ),
)

// https://sudoku.coach/en/learn/forcing-chain-types
private fun regionForcingNetExample2() = advancedExample(
    givens = "597461030621538974483297156102000000000302581058916420870100005000000710015009040",
    solution = "597461238621538974483297156142785369769342581358916427874123695936854712215679843",
    candidates = "0000000000003m003m000000000000000000000000000000000000000c005c600o10808w8w14880020000000001w000000000000001w000088000a0c1280007a18884g4a0s0000aq060000685e004m003q",
    step = SolveStep(
        technique = TechniqueId.REGION_FORCING_NET,
        eliminations = listOf(CandidateElimination(candidate = candidate(80, 2))),
        evidence = StepEvidence(
            causeCells = setOf(cell(6), cell(8), cell(78), cell(71), cell(75), cell(30), cell(35), cell(80)),
            causeCandidates = setOf(
                candidate(6, 2),
                candidate(6, 8),
                candidate(8, 8),
                candidate(78, 8),
                candidate(71, 8),
                candidate(78, 6),
                candidate(75, 6),
                candidate(75, 7),
                candidate(30, 7),
                candidate(35, 7),
                candidate(35, 9),
                candidate(71, 9),
                candidate(71, 2),
                candidate(80, 2),
                candidate(8, 2),
            ),
            focusDigits = setOf(2, 8, 6, 7, 9),
            houses = listOf(
                HouseRef(type = HouseType.ROW, index = 0),
                HouseRef(type = HouseType.COLUMN, index = 6),
                HouseRef(type = HouseType.COLUMN, index = 8),
                HouseRef(type = HouseType.ROW, index = 8),
                HouseRef(type = HouseType.COLUMN, index = 3),
                HouseRef(type = HouseType.ROW, index = 3),
            ),
            links = listOf(
                InferenceLink(from = candidate(6, 2), to = candidate(6, 8), type = InferenceLinkType.WEAK, branchId = 0),
                InferenceLink(
                    from = candidate(6, 8),
                    to = candidate(8, 8),
                    type = InferenceLinkType.STRONG,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(6, 8),
                    to = candidate(78, 8),
                    type = InferenceLinkType.STRONG,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(8, 8),
                    to = candidate(71, 8),
                    type = InferenceLinkType.WEAK,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(78, 8),
                    to = candidate(78, 6),
                    type = InferenceLinkType.WEAK,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(78, 6),
                    to = candidate(75, 6),
                    type = InferenceLinkType.STRONG,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(75, 6),
                    to = candidate(75, 7),
                    type = InferenceLinkType.WEAK,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(75, 7),
                    to = candidate(30, 7),
                    type = InferenceLinkType.STRONG,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(30, 7),
                    to = candidate(35, 7),
                    type = InferenceLinkType.WEAK,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(35, 7),
                    to = candidate(35, 9),
                    type = InferenceLinkType.STRONG,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(35, 9),
                    to = candidate(71, 9),
                    type = InferenceLinkType.WEAK,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(71, 8),
                    to = candidate(71, 2),
                    type = InferenceLinkType.STRONG,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(71, 9),
                    to = candidate(71, 2),
                    type = InferenceLinkType.STRONG,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(71, 2),
                    to = candidate(80, 2),
                    type = InferenceLinkType.WEAK,
                    branchId = 0,
                ),
                InferenceLink(
                    from = candidate(8, 2),
                    to = candidate(80, 2),
                    type = InferenceLinkType.WEAK,
                    branchId = 1,
                ),
            ),
            inferenceGraph = InferenceGraph(
                nodes = listOf(
                    InferenceNode(
                        id = 0,
                        candidate = candidate(6, 2),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = true,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 1,
                        candidate = candidate(6, 8),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 2,
                        candidate = candidate(8, 8),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 3,
                        candidate = candidate(78, 8),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 4,
                        candidate = candidate(71, 8),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 5,
                        candidate = candidate(78, 6),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 6,
                        candidate = candidate(75, 6),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 7,
                        candidate = candidate(75, 7),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 8,
                        candidate = candidate(30, 7),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 9,
                        candidate = candidate(35, 7),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 10,
                        candidate = candidate(35, 9),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 11,
                        candidate = candidate(71, 9),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 12,
                        candidate = candidate(71, 2),
                        truth = InferenceTruth.TRUE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 13,
                        candidate = candidate(80, 2),
                        truth = InferenceTruth.FALSE,
                        branchId = 0,
                        isAssumption = false,
                        isConclusion = true,
                    ),
                    InferenceNode(
                        id = 14,
                        candidate = candidate(8, 2),
                        truth = InferenceTruth.TRUE,
                        branchId = 1,
                        isAssumption = true,
                        isConclusion = false,
                    ),
                    InferenceNode(
                        id = 15,
                        candidate = candidate(80, 2),
                        truth = InferenceTruth.FALSE,
                        branchId = 1,
                        isAssumption = false,
                        isConclusion = true,
                    ),
                ),
                edges = listOf(
                    InferenceEdge(fromNodeId = 0, toNodeId = 1, type = InferenceLinkType.WEAK),
                    InferenceEdge(
                        fromNodeId = 1,
                        toNodeId = 2,
                        type = InferenceLinkType.STRONG,
                        house = HouseRef(type = HouseType.ROW, index = 0),
                    ),
                    InferenceEdge(
                        fromNodeId = 1,
                        toNodeId = 3,
                        type = InferenceLinkType.STRONG,
                        house = HouseRef(type = HouseType.COLUMN, index = 6),
                    ),
                    InferenceEdge(
                        fromNodeId = 2,
                        toNodeId = 4,
                        type = InferenceLinkType.WEAK,
                        house = HouseRef(type = HouseType.COLUMN, index = 8),
                    ),
                    InferenceEdge(fromNodeId = 3, toNodeId = 5, type = InferenceLinkType.WEAK),
                    InferenceEdge(
                        fromNodeId = 5,
                        toNodeId = 6,
                        type = InferenceLinkType.STRONG,
                        house = HouseRef(type = HouseType.ROW, index = 8),
                    ),
                    InferenceEdge(fromNodeId = 6, toNodeId = 7, type = InferenceLinkType.WEAK),
                    InferenceEdge(
                        fromNodeId = 7,
                        toNodeId = 8,
                        type = InferenceLinkType.STRONG,
                        house = HouseRef(type = HouseType.COLUMN, index = 3),
                    ),
                    InferenceEdge(
                        fromNodeId = 8,
                        toNodeId = 9,
                        type = InferenceLinkType.WEAK,
                        house = HouseRef(type = HouseType.ROW, index = 3),
                    ),
                    InferenceEdge(fromNodeId = 9, toNodeId = 10, type = InferenceLinkType.STRONG),
                    InferenceEdge(
                        fromNodeId = 10,
                        toNodeId = 11,
                        type = InferenceLinkType.WEAK,
                        house = HouseRef(type = HouseType.COLUMN, index = 8),
                    ),
                    InferenceEdge(fromNodeId = 4, toNodeId = 12, type = InferenceLinkType.STRONG),
                    InferenceEdge(fromNodeId = 11, toNodeId = 12, type = InferenceLinkType.STRONG),
                    InferenceEdge(
                        fromNodeId = 12,
                        toNodeId = 13,
                        type = InferenceLinkType.WEAK,
                        house = HouseRef(type = HouseType.COLUMN, index = 8),
                    ),
                    InferenceEdge(
                        fromNodeId = 14,
                        toNodeId = 15,
                        type = InferenceLinkType.WEAK,
                        house = HouseRef(type = HouseType.COLUMN, index = 8),
                    ),
                ),
                premise = InferencePremise(
                    type = InferencePremiseType.REGION,
                    candidates = listOf(candidate(6, 2), candidate(8, 2)),
                    house = HouseRef(type = HouseType.ROW, index = 0),
                ),
            ),
        ),
    ),
)

// Cell indices in the frozen proofs are zero-based, in row-major order.
private fun cell(index: Int) = CellRef.fromIndex(index)
private fun candidate(index: Int, digit: Int) = CandidateRef(cell(index), digit)
private fun advancedExample(givens: String, solution: String, candidates: String, step: SolveStep): TutorialExample = TutorialExample(
    technique = step.technique, givens = givens, solution = solution,
    house = step.evidence.houses.firstOrNull() ?: HouseRef(HouseType.ROW, step.evidence.causeCells.first().row),
    patternCells = step.evidence.causeCells, digits = step.evidence.focusDigits,
    candidateMasks = candidates, recordedDeduction = step,
)
