package com.felixj.moneta.shared.util

import androidx.compose.ui.text.AnnotatedString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class TimeInputVisualTransformationTest {

    @Test
    fun filter_emptyText_returnsMaskWithZeroOffsetAndCachesInstance() {
        val transformation = TimeInputVisualTransformation()
        val first = transformation.filter(AnnotatedString(""))
        val second = transformation.filter(AnnotatedString(""))

        assertSame(first, second)
        assertEquals("__:__", first.text.text)
        assertEquals(0, first.offsetMapping.originalToTransformed(0))
        for (i in 0..5) {
            assertEquals(0, first.offsetMapping.transformedToOriginal(i))
        }
    }

    @Test
    fun filter_partialInputs_formatsWithProgressiveMask() {
        val transformation = TimeInputVisualTransformation()

        assertEquals("1_:__", transformation.filter(AnnotatedString("1")).text.text)
        assertEquals("14:__", transformation.filter(AnnotatedString("14")).text.text)
        assertEquals("14:3_", transformation.filter(AnnotatedString("143")).text.text)
        assertEquals("14:30", transformation.filter(AnnotatedString("1430")).text.text)
    }

    @Test
    fun filter_customSeparatorAndMaskChar_formatsProperly() {
        val transformation = TimeInputVisualTransformation(separator = '.', maskChar = '-')

        assertEquals("--.--", transformation.filter(AnnotatedString("")).text.text)
        assertEquals("1-.--", transformation.filter(AnnotatedString("1")).text.text)
        assertEquals("14.--", transformation.filter(AnnotatedString("14")).text.text)
        assertEquals("14.30", transformation.filter(AnnotatedString("1430")).text.text)
    }

    @Test
    fun filter_inputLongerThan4Digits_truncatesFormattedStringTo5Chars() {
        val transformation = TimeInputVisualTransformation()
        val result = transformation.filter(AnnotatedString("143099"))

        assertEquals("14:30", result.text.text)
        assertEquals(5, result.offsetMapping.originalToTransformed(6))
        assertEquals(4, result.offsetMapping.transformedToOriginal(5))
    }

    @Test
    fun offsetMapping_twoDigits_advancesPastSeparator() {
        // Raw: "14" (len 2)
        // Transformed: "14:__" (len 5)
        val transformation = TimeInputVisualTransformation()
        val result = transformation.filter(AnnotatedString("14"))
        val mapping = result.offsetMapping

        // originalToTransformed
        assertEquals(0, mapping.originalToTransformed(0)) // before '1'
        assertEquals(1, mapping.originalToTransformed(1)) // after '1'
        assertEquals(3, mapping.originalToTransformed(2)) // after '4' jumps past ':'

        // transformedToOriginal
        assertEquals(0, mapping.transformedToOriginal(0)) // at '1'
        assertEquals(1, mapping.transformedToOriginal(1)) // at '4'
        assertEquals(2, mapping.transformedToOriginal(2)) // at ':'
        assertEquals(2, mapping.transformedToOriginal(3)) // after ':'
        for (i in 4..5) {
            assertEquals(2, mapping.transformedToOriginal(i))
        }
    }

    @Test
    fun offsetMapping_fullTime_mapsAllOffsetsAccurately() {
        // Raw: "1430" (len 4)
        // Transformed: "14:30" (len 5)
        val transformation = TimeInputVisualTransformation()
        val result = transformation.filter(AnnotatedString("1430"))
        val mapping = result.offsetMapping

        val expectedOriginalToTransformed = listOf(0, 1, 3, 4, 5)
        for (offset in 0..4) {
            assertEquals("originalToTransformed($offset)", expectedOriginalToTransformed[offset], mapping.originalToTransformed(offset))
        }

        val expectedTransformedToOriginal = listOf(0, 1, 2, 2, 3, 4)
        for (offset in 0..5) {
            assertEquals("transformedToOriginal($offset)", expectedTransformedToOriginal[offset], mapping.transformedToOriginal(offset))
        }
    }

    @Test
    fun offsetMapping_cursorTypingProgression_jumpsCorrectly() {
        val transformation = TimeInputVisualTransformation()
        val digits = "1430"
        val expectedTransformedCursor = listOf(
            0, // "" -> cursor at 0 (|__:__)
            1, // "1" -> cursor at 1 (1|_:__)
            3, // "14" -> cursor at 3 (14:|__) [jumped past ':']
            4, // "143" -> cursor at 4 (14:3|_)
            5  // "1430" -> cursor at 5 (14:30|)
        )

        for (len in 0..4) {
            val input = digits.take(len)
            val result = transformation.filter(AnnotatedString(input))
            val visualCursor = result.offsetMapping.originalToTransformed(len)
            assertEquals("Typing '$input' (length $len)", expectedTransformedCursor[len], visualCursor)
        }
    }

    @Test
    fun offsetMapping_tapOnUnfilledArea_snapsToFirstEmptySlot() {
        val transformation = TimeInputVisualTransformation()
        val digits = "1430"

        for (len in 0..4) {
            val input = digits.take(len)
            val result = transformation.filter(AnnotatedString(input))
            val mapping = result.offsetMapping
            val expectedNextSlot = mapping.originalToTransformed(len)

            for (transformedOffset in expectedNextSlot..5) {
                val rawOffset = mapping.transformedToOriginal(transformedOffset)
                assertEquals(
                    "Tapping at $transformedOffset for input '$input' should resolve to raw offset $len",
                    len,
                    rawOffset
                )
            }
        }
    }

    @Test
    fun offsetMapping_composeContract_allOffsetsWithinBoundsMonotonicAndRoundTrip() {
        val testInputs = listOf(
            "",
            "1",
            "14",
            "143",
            "1430",
            "14309",
            "1430999"
        )
        val transformation = TimeInputVisualTransformation()

        for (raw in testInputs) {
            val result = transformation.filter(AnnotatedString(raw))
            val mapping = result.offsetMapping
            val originalLen = raw.length
            val transformedLen = result.text.text.length

            // Verify originalToTransformed bounds and monotonicity
            var prevTransformed = -1
            for (offset in 0..originalLen) {
                val transformed = mapping.originalToTransformed(offset)
                assertTrue("offset $offset: $transformed in 0..$transformedLen", transformed in 0..transformedLen)
                assertTrue("monotonicity: $transformed >= $prevTransformed", transformed >= prevTransformed)
                prevTransformed = transformed

                // Round-trip check for offsets within valid input range (0..min(originalLen, 4))
                if (offset <= 4) {
                    val roundTrip = mapping.transformedToOriginal(transformed)
                    assertEquals("Round-trip for offset $offset in '$raw'", offset, roundTrip)
                }
            }

            // Verify transformedToOriginal bounds and monotonicity
            var prevOriginal = -1
            for (offset in 0..transformedLen) {
                val original = mapping.transformedToOriginal(offset)
                assertTrue("offset $offset: $original in 0..$originalLen", original in 0..originalLen)
                assertTrue("monotonicity: $original >= $prevOriginal", original >= prevOriginal)
                prevOriginal = original
            }
        }
    }

    @Test
    fun equalsAndHashCode_behaveCorrectly() {
        val first = TimeInputVisualTransformation(':', '_')
        val second = TimeInputVisualTransformation(':', '_')
        val third = TimeInputVisualTransformation('.', '_')

        assertEquals(first, second)
        assertEquals(first.hashCode(), second.hashCode())
        assertNotEquals(first, third)
    }
}
