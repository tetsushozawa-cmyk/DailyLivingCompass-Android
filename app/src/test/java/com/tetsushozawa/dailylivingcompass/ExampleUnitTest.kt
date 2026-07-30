package com.tetsushozawa.dailylivingcompass

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExerciseRecordStartStateTest {
    @Test
    fun selectedStartConditionIndexesAreConvertedToStoredLabels() {
        val values = startConditionStorageValues(
            SelfCheckState(
                pain = 5,
                fatigue = 4,
                sleep = 1,
                breathing = 2,
                comparison = 3
            )
        )

        assertEquals(
            linkedMapOf(
                "pain" to "動くのがつらい",
                "fatigue" to "かなり強い",
                "sleep" to "少し眠れた",
                "breathing" to "苦しさがある",
                "comparedWithYesterday" to "少し悪い"
            ),
            values
        )
    }

    @Test
    fun selectedMobilityIndexesAreConvertedToStoredLabels() {
        val values = mobilityStorageValues(
            SelfCheckState(
                getUp = 0,
                sit = 1,
                stand = 2,
                indoorWalk = 1,
                outdoor = 2
            )
        )

        assertEquals(
            linkedMapOf(
                "getUp" to "できる",
                "sit" to "少しならできる",
                "stand" to "今日は難しい",
                "indoorWalk" to "少しならできる",
                "outdoorWalk" to "今日は難しい"
            ),
            values
        )
    }
}

class MandalaTrajectoryTest {
    @Test
    fun mobilityColumnsUseHighestAvailableDailyAction() {
        assertEquals(1, mobilityMandalaColumn(state(upTo = 1)))
        assertEquals(2, mobilityMandalaColumn(state(upTo = 2)))
        assertEquals(3, mobilityMandalaColumn(state(upTo = 3)))
        assertEquals(4, mobilityMandalaColumn(state(upTo = 4)))
        assertEquals(5, mobilityMandalaColumn(state(upTo = 5)))
    }

    @Test
    fun allDifficultMobilityUsesFirstColumn() {
        assertEquals(1, mobilityMandalaColumn(state(upTo = 0)))
    }

    @Test
    fun fatigueStorageLabelsMapToFiveRowsInSelectionOrder() {
        assertEquals(1, fatigueMandalaRow("無し"))
        assertEquals(2, fatigueMandalaRow("軽い"))
        assertEquals(3, fatigueMandalaRow("少しある"))
        assertEquals(4, fatigueMandalaRow("強い"))
        assertEquals(5, fatigueMandalaRow("かなり強い"))
        assertEquals(5, fatigueMandalaRow("休息が必要"))
    }

    @Test
    fun legacyRecordsWithoutStartConditionOrMobilityAreExcluded() {
        val missingFatigue = state(upTo = 3).copy(fatigue = "")
        val missingMobility = state(upTo = 3).copy(indoorWalk = "")

        assertTrue(buildMandalaTrajectory(listOf(missingFatigue, missingMobility)).isEmpty())
    }

    @Test
    fun multipleRecordsInSameCellRemainSeparatePoints() {
        val repeated = state(upTo = 3, fatigue = "少しある")
        val trajectory = buildMandalaTrajectory(listOf(repeated, repeated, repeated))

        assertEquals(3, trajectory.size)
        assertEquals(listOf(3 to 3, 3 to 3, 3 to 3), trajectory.map { it.cell })
    }

    @Test
    fun trajectoryIsOldestFirstAndOnlyNewestPointIsMarkedLatest() {
        val newest = state(upTo = 5, fatigue = "無し")
        val middle = state(upTo = 3, fatigue = "少しある")
        val oldest = state(upTo = 1, fatigue = "強い")

        val trajectory = buildMandalaTrajectory(listOf(newest, middle, oldest))

        assertEquals(listOf(1 to 4, 3 to 3, 5 to 1), trajectory.map { it.cell })
        assertFalse(trajectory[0].isLatest)
        assertFalse(trajectory[1].isLatest)
        assertTrue(trajectory[2].isLatest)
    }

    private fun state(upTo: Int, fatigue: String = "軽い"): MandalaStartState {
        val values = List(5) { index ->
            if (index < upTo) "できる" else "今日は難しい"
        }
        return MandalaStartState(
            fatigue = fatigue,
            getUp = values[0],
            sit = values[1],
            stand = values[2],
            indoorWalk = values[3],
            outdoorWalk = values[4]
        )
    }
}

class ExerciseRecordProgramNameTest {
    @Test
    fun newProgramNamesKeepExistingExerciseNamesInDetails() {
        assertEquals(
            "深呼吸の練習",
            exerciseNameForRecord(BasicRecoveryProgramName, "レベル1")
        )
        assertEquals(
            "屋内歩行練習",
            exerciseNameForRecord(WalkingProgramName, "レベル4")
        )
    }

    @Test
    fun legacyProgramNameRemainsItsStoredExerciseName() {
        assertEquals(
            "起立練習",
            exerciseNameForRecord("起立練習", "レベル3")
        )
    }
}

class SocialActivityGuidanceTest {
    @Test
    fun latestThreeOutdoorLowFatigueRecordsShowGuidance() {
        val records = listOf(
            state(outdoor = true, fatigue = "無し"),
            state(outdoor = true, fatigue = "軽い"),
            state(outdoor = true, fatigue = "無し")
        )

        assertTrue(shouldShowSocialActivityGuidance(records))
    }

    @Test
    fun onlyTwoMatchingRecordsDoNotShowGuidance() {
        val records = listOf(
            state(outdoor = true, fatigue = "無し"),
            state(outdoor = true, fatigue = "軽い"),
            state(outdoor = false, fatigue = "無し")
        )

        assertFalse(shouldShowSocialActivityGuidance(records))
    }

    @Test
    fun fatigueRowThreeOrHigherInLatestThreeDoesNotShowGuidance() {
        val records = listOf(
            state(outdoor = true, fatigue = "無し"),
            state(outdoor = true, fatigue = "少しある"),
            state(outdoor = true, fatigue = "軽い")
        )

        assertFalse(shouldShowSocialActivityGuidance(records))
    }

    @Test
    fun indoorOnlyRecordsDoNotShowGuidance() {
        val records = List(3) { state(outdoor = false, fatigue = "無し") }

        assertFalse(shouldShowSocialActivityGuidance(records))
    }

    @Test
    fun legacyRecordsWithoutStartStateDoNotShowGuidance() {
        val legacy = MandalaStartState("", "", "", "", "", "")

        assertFalse(shouldShowSocialActivityGuidance(List(3) { legacy }))
    }

    @Test
    fun noRecordsDoNotShowGuidance() {
        assertFalse(shouldShowSocialActivityGuidance(emptyList()))
    }

    private fun state(outdoor: Boolean, fatigue: String): MandalaStartState {
        return MandalaStartState(
            fatigue = fatigue,
            getUp = "できる",
            sit = "できる",
            stand = "できる",
            indoorWalk = "できる",
            outdoorWalk = if (outdoor) "できる" else "今日は難しい"
        )
    }
}

class DeleteAllRecordsTest {
    @Test
    fun deleteAllButtonIsHiddenWhenThereAreNoRecords() {
        assertFalse(shouldShowDeleteAllRecords(0))
    }

    @Test
    fun deleteAllButtonIsShownWhenThereIsAtLeastOneRecord() {
        assertTrue(shouldShowDeleteAllRecords(1))
        assertTrue(shouldShowDeleteAllRecords(10))
    }
}
