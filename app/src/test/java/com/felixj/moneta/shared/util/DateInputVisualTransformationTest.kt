package com.felixj.moneta.shared.util

import androidx.compose.ui.text.AnnotatedString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class DateInputVisualTransformationTest {

    @Test
    fun filter_emptyText_returnsMaskWithZeroOffsetAndCachesInstance() {
        val transformation = DateInputVisualTransformation()
        val first = transformation.filter(AnnotatedString(""))
        val second = transformation.filter(AnnotatedString(""))

        // Efficient zero-allocation check: returns exact cached TransformedText
        assertSame(first, second)
        assertEquals("__/__/____", first.text.text)
        assertEquals(0, first.offsetMapping.originalToTransformed(0))
        for (i in 0..10) {
            assertEquals(0, first.offsetMapping.transformedToOriginal(i))
        }
    }

    @Test
    fun filter_partialInputs_formatsWithProgressiveMask() {
        val transformation = DateInputVisualTransformation()

        assertEquals("0_/__/____", transformation.filter(AnnotatedString("0")).text.text)
        assertEquals("01/__/____", transformation.filter(AnnotatedString("01")).text.text)
        assertEquals("01/0_/____", transformation.filter(AnnotatedString("010")).text.text)
        assertEquals("01/02/____", transformation.filter(AnnotatedString("0102")).text.text)
        assertEquals("01/02/2___", transformation.filter(AnnotatedString("01022")).text.text)
        assertEquals("01/02/20__", transformation.filter(AnnotatedString("010220")).text.text)
        assertEquals("01/02/202_", transformation.filter(AnnotatedString("0102202")).text.text)
        assertEquals("01/02/2026", transformation.filter(AnnotatedString("01022026")).text.text)
    }

    @Test
    fun filter_customSeparatorAndMaskChar_formatsProperly() {
        val transformation = DateInputVisualTransformation(separator = '-', maskChar = '.')

        assertEquals("..-..-....", transformation.filter(AnnotatedString("")).text.text)
        assertEquals("0.-..-....", transformation.filter(AnnotatedString("0")).text.text)
        assertEquals("01-..-....", transformation.filter(AnnotatedString("01")).text.text)
        assertEquals("01-02-2026", transformation.filter(AnnotatedString("01022026")).text.text)
    }

    @Test
    fun filter_inputLongerThan8Digits_truncatesFormattedStringTo10Chars() {
        val transformation = DateInputVisualTransformation()
        val result = transformation.filter(AnnotatedString("01022026999"))

        assertEquals("01/02/2026", result.text.text)
        assertEquals(10, result.offsetMapping.originalToTransformed(11))
        // transformedToOriginal at end of transformed text maps to 8 (end of formatted date)
        assertEquals(8, result.offsetMapping.transformedToOriginal(10))
    }

    @Test
    fun offsetMapping_twoDigits_advancesPastSeparator() {
        // Raw: "01" (len 2)
        // Transformed: "01/__/____" (len 10)
        val transformation = DateInputVisualTransformation()
        val result = transformation.filter(AnnotatedString("01"))
        val mapping = result.offsetMapping

        // originalToTransformed
        assertEquals(0, mapping.originalToTransformed(0)) // before '0'
        assertEquals(1, mapping.originalToTransformed(1)) // after '0'
        assertEquals(3, mapping.originalToTransformed(2)) // after '1' jumps past '/'

        // transformedToOriginal
        assertEquals(0, mapping.transformedToOriginal(0)) // at '0'
        assertEquals(1, mapping.transformedToOriginal(1)) // at '1'
        assertEquals(2, mapping.transformedToOriginal(2)) // at '/'
        assertEquals(2, mapping.transformedToOriginal(3)) // after '/'
        // Tapping anywhere in unfilled mask area snaps to raw length (2)
        for (i in 4..10) {
            assertEquals(2, mapping.transformedToOriginal(i))
        }
    }

    @Test
    fun offsetMapping_fourDigits_advancesPastSecondSeparator() {
        // Raw: "0102" (len 4)
        // Transformed: "01/02/____" (len 10)
        val transformation = DateInputVisualTransformation()
        val result = transformation.filter(AnnotatedString("0102"))
        val mapping = result.offsetMapping

        assertEquals(0, mapping.originalToTransformed(0))
        assertEquals(1, mapping.originalToTransformed(1))
        assertEquals(3, mapping.originalToTransformed(2))
        assertEquals(4, mapping.originalToTransformed(3))
        assertEquals(6, mapping.originalToTransformed(4)) // after '2' jumps past second '/'

        assertEquals(0, mapping.transformedToOriginal(0))
        assertEquals(1, mapping.transformedToOriginal(1))
        assertEquals(2, mapping.transformedToOriginal(2))
        assertEquals(2, mapping.transformedToOriginal(3))
        assertEquals(3, mapping.transformedToOriginal(4))
        assertEquals(4, mapping.transformedToOriginal(5))
        assertEquals(4, mapping.transformedToOriginal(6))
        for (i in 7..10) {
            assertEquals(4, mapping.transformedToOriginal(i))
        }
    }

    @Test
    fun offsetMapping_fullDate_mapsAllOffsetsAccurately() {
        // Raw: "01022026" (len 8)
        // Transformed: "01/02/2026" (len 10)
        val transformation = DateInputVisualTransformation()
        val result = transformation.filter(AnnotatedString("01022026"))
        val mapping = result.offsetMapping

        val expectedOriginalToTransformed = listOf(0, 1, 3, 4, 6, 7, 8, 9, 10)
        for (offset in 0..8) {
            assertEquals("originalToTransformed($offset)", expectedOriginalToTransformed[offset], mapping.originalToTransformed(offset))
        }

        val expectedTransformedToOriginal = listOf(0, 1, 2, 2, 3, 4, 4, 5, 6, 7, 8)
        for (offset in 0..10) {
            assertEquals("transformedToOriginal($offset)", expectedTransformedToOriginal[offset], mapping.transformedToOriginal(offset))
        }
    }

    @Test
    fun offsetMapping_cursorTypingProgression_jumpsCorrectly() {
        // Tests the exact visual cursor position as each digit is typed
        val transformation = DateInputVisualTransformation()
        val digits = "01022026"
        val expectedTransformedCursor = listOf(
            0,  // "" -> cursor at 0 (|__/__/____)
            1,  // "0" -> cursor at 1 (0|_/__/____)
            3,  // "01" -> cursor at 3 (01/|__/____) [jumped past '/']
            4,  // "010" -> cursor at 4 (01/0|_/____)
            6,  // "0102" -> cursor at 6 (01/02/|____) [jumped past '/']
            7,  // "01022" -> cursor at 7 (01/02/2|___)
            8,  // "010220" -> cursor at 8 (01/02/20|__)
            9,  // "0102202" -> cursor at 9 (01/02/202|_)
            10  // "01022026" -> cursor at 10 (01/02/2026|)
        )

        for (len in 0..8) {
            val input = digits.take(len)
            val result = transformation.filter(AnnotatedString(input))
            // When typing at the end of input, raw cursor is at len
            val visualCursor = result.offsetMapping.originalToTransformed(len)
            assertEquals("Typing '$input' (length $len)", expectedTransformedCursor[len], visualCursor)
        }
    }

    @Test
    fun offsetMapping_tapOnUnfilledArea_snapsToFirstEmptySlot() {
        // Tests that tapping anywhere in the unfilled placeholder area
        // snaps cursor to the next empty input position, preventing orphaned cursors
        val transformation = DateInputVisualTransformation()
        val digits = "01022026"

        for (len in 0..8) {
            val input = digits.take(len)
            val result = transformation.filter(AnnotatedString(input))
            val mapping = result.offsetMapping
            val expectedNextSlot = mapping.originalToTransformed(len)

            // Any tap beyond filled content should map back to raw length 'len'
            // and thus render at expectedNextSlot
            for (transformedOffset in expectedNextSlot..10) {
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
            "0",
            "01",
            "010",
            "0102",
            "01022",
            "010220",
            "0102202",
            "01022026",
            "010220269",
            "010220269999"
        )
        val transformation = DateInputVisualTransformation()

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

                // Round-trip check for offsets within valid input range (0..min(originalLen, 8))
                if (offset <= 8) {
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
        val first = DateInputVisualTransformation('/', '_')
        val second = DateInputVisualTransformation('/', '_')
        val third = DateInputVisualTransformation('-', '_')

        assertEquals(first, second)
        assertEquals(first.hashCode(), second.hashCode())
        assertNotEquals(first, third)
    }
}
