package com.felixj.moneta.shared.util

import androidx.compose.ui.text.AnnotatedString
import com.felixj.moneta.settings.model.Currency
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class CurrencyAmountInputVisualTransformationTest {

    @Test
    fun filter_emptyText_returnsEmptyWithIdentityMapping() {
        val transformation = CurrencyAmountInputVisualTransformation(Currency.USD)
        val result = transformation.filter(AnnotatedString(""))

        assertEquals("", result.text.text)
        assertEquals(0, result.offsetMapping.originalToTransformed(0))
        assertEquals(0, result.offsetMapping.transformedToOriginal(0))
    }

    @Test
    fun filter_usdCurrency_treatsLastTwoDigitsAsCents() {
        val transformation = CurrencyAmountInputVisualTransformation(Currency.USD)
        assertEquals("$ 100.00", transformation.filter(AnnotatedString("10000")).text.text)
        assertEquals("$ 20.05", transformation.filter(AnnotatedString("2005")).text.text)
        assertEquals("$ 12,345.67", transformation.filter(AnnotatedString("1234567")).text.text)
    }

    @Test
    fun filter_idrCurrency_formatsWithRpPrefixAndDots() {
        val transformation = CurrencyAmountInputVisualTransformation(Currency.IDR)
        val result = transformation.filter(AnnotatedString("10000"))

        assertEquals("Rp 10.000", result.text.text)
    }

    @Test
    fun filter_withoutPrefix_formatsNumbersWithoutLeadingSpace() {
        val transformation = CurrencyAmountInputVisualTransformation(
            prefix = "",
            locale = Locale.US
        )
        val result = transformation.filter(AnnotatedString("10000"))

        assertEquals("10,000", result.text.text)
    }

    @Test
    fun filter_usdCurrency_padsShortInputsWithLeadingZero() {
        val transformation = CurrencyAmountInputVisualTransformation(Currency.USD)

        assertEquals("$ 0.05", transformation.filter(AnnotatedString("5")).text.text)
        assertEquals("$ 0.50", transformation.filter(AnnotatedString("50")).text.text)
        assertEquals("$ 5.00", transformation.filter(AnnotatedString("500")).text.text)
    }

    @Test
    fun filter_largeNumber_formatsWithoutLongOverflow() {
        val transformation = CurrencyAmountInputVisualTransformation(
            prefix = "$",
            locale = Locale.US
        )
        // 25 digits (exceeds Long.MAX_VALUE which is 19 digits)
        val largeNumber = "1234567890123456789012345"
        val result = transformation.filter(AnnotatedString(largeNumber))

        assertEquals("$ 1,234,567,890,123,456,789,012,345", result.text.text)
    }

    @Test
    fun offsetMapping_usd_correctCursorPositions() {
        // Raw: "10000" (len 5) stored as cents
        // Transformed: "$ 100.00" (len 8)
        // Indices in transformed:
        // 0: '$', 1: ' ', 2: '1', 3: '0', 4: '0', 5: '.', 6: '0', 7: '0'
        val transformation = CurrencyAmountInputVisualTransformation(Currency.USD)
        val result = transformation.filter(AnnotatedString("10000"))
        val mapping = result.offsetMapping

        // originalToTransformed
        assertEquals(2, mapping.originalToTransformed(0)) // before '1' -> after "$ "
        assertEquals(3, mapping.originalToTransformed(1)) // after '1'
        assertEquals(4, mapping.originalToTransformed(2)) // after second '0'
        assertEquals(6, mapping.originalToTransformed(3)) // after third '0' and dot '.'
        assertEquals(7, mapping.originalToTransformed(4)) // after first cents '0'
        assertEquals(8, mapping.originalToTransformed(5)) // end

        // transformedToOriginal
        assertEquals(0, mapping.transformedToOriginal(0)) // at '$'
        assertEquals(0, mapping.transformedToOriginal(1)) // at ' '
        assertEquals(0, mapping.transformedToOriginal(2)) // before '1'
        assertEquals(1, mapping.transformedToOriginal(3)) // after '1'
        assertEquals(2, mapping.transformedToOriginal(4)) // after second '0'
        assertEquals(3, mapping.transformedToOriginal(5)) // at '.'
        assertEquals(3, mapping.transformedToOriginal(6)) // after '.' / before cents
        assertEquals(4, mapping.transformedToOriginal(7)) // after first cents '0'
        assertEquals(5, mapping.transformedToOriginal(8)) // end
    }

    @Test
    fun offsetMapping_usd_paddedShortInput_mapsTypedDigitOnly() {
        // Raw: "5" (len 1) -> Transformed: "$ 0.05" (len 6)
        // 0: '$', 1: ' ', 2: '0', 3: '.', 4: '0', 5: '5'
        val transformation = CurrencyAmountInputVisualTransformation(Currency.USD)
        val result = transformation.filter(AnnotatedString("5"))
        val mapping = result.offsetMapping

        assertEquals(5, mapping.originalToTransformed(0)) // the typed '5'
        assertEquals(6, mapping.originalToTransformed(1)) // end

        assertEquals(0, mapping.transformedToOriginal(0))
        assertEquals(0, mapping.transformedToOriginal(2)) // at padding '0'
        assertEquals(0, mapping.transformedToOriginal(3)) // at '.'
        assertEquals(0, mapping.transformedToOriginal(4)) // at padding '0'
        assertEquals(0, mapping.transformedToOriginal(5)) // before typed '5'
        assertEquals(1, mapping.transformedToOriginal(6)) // after typed '5' (end)
    }

    @Test
    fun offsetMapping_idr_correctCursorPositionsWithDotSeparator() {
        // Raw: "10000" (len 5)
        // Transformed: "Rp 10.000" (len 9)
        // Indices:
        // 0: 'R', 1: 'p', 2: ' ', 3: '1', 4: '0', 5: '.', 6: '0', 7: '0', 8: '0'
        val transformation = CurrencyAmountInputVisualTransformation(Currency.IDR)
        val result = transformation.filter(AnnotatedString("10000"))
        val mapping = result.offsetMapping

        assertEquals(3, mapping.originalToTransformed(0)) // after "Rp "
        assertEquals(4, mapping.originalToTransformed(1)) // after '1'
        assertEquals(6, mapping.originalToTransformed(2)) // after first '0' and dot '.'
        assertEquals(7, mapping.originalToTransformed(3))
        assertEquals(8, mapping.originalToTransformed(4))
        assertEquals(9, mapping.originalToTransformed(5)) // end

        assertEquals(0, mapping.transformedToOriginal(0))
        assertEquals(0, mapping.transformedToOriginal(1))
        assertEquals(0, mapping.transformedToOriginal(2))
        assertEquals(0, mapping.transformedToOriginal(3))
        assertEquals(1, mapping.transformedToOriginal(4))
        assertEquals(2, mapping.transformedToOriginal(5)) // at '.'
        assertEquals(2, mapping.transformedToOriginal(6)) // after '.'
        assertEquals(3, mapping.transformedToOriginal(7))
        assertEquals(4, mapping.transformedToOriginal(8))
        assertEquals(5, mapping.transformedToOriginal(9))
    }

    @Test
    fun offsetMapping_composeContract_allOffsetsWithinBoundsAndMonotonic() {
        val testInputs = listOf(
            "1",
            "12",
            "123",
            "1234",
            "12345",
            "123456",
            "1234567",
            "1000000",
            "9876543210123456789"
        )
        val transformations = listOf(
            CurrencyAmountInputVisualTransformation(Currency.USD),
            CurrencyAmountInputVisualTransformation(Currency.IDR),
            CurrencyAmountInputVisualTransformation(prefix = "", locale = Locale.US),
            CurrencyAmountInputVisualTransformation(prefix = "Rp ", locale = Locale.forLanguageTag("id-ID"))
        )

        for (transformation in transformations) {
            for (raw in testInputs) {
                val result = transformation.filter(AnnotatedString(raw))
                val mapping = result.offsetMapping
                val originalLen = raw.length
                val transformedLen = result.text.text.length

                // Test originalToTransformed bounds & monotonicity & roundtrip
                var prevTransformed = -1
                for (offset in 0..originalLen) {
                    val transformed = mapping.originalToTransformed(offset)
                    assertTrue("offset $offset: $transformed <= $transformedLen", transformed in 0..transformedLen)
                    assertTrue("monotonicity: $transformed >= $prevTransformed", transformed >= prevTransformed)
                    prevTransformed = transformed

                    // Round-trip check: transformedToOriginal(originalToTransformed(offset)) == offset
                    val roundTrip = mapping.transformedToOriginal(transformed)
                    assertEquals("Round-trip for offset $offset in '$raw'", offset, roundTrip)
                }

                // Test transformedToOriginal bounds & monotonicity
                var prevOriginal = -1
                for (offset in 0..transformedLen) {
                    val original = mapping.transformedToOriginal(offset)
                    assertTrue("offset $offset: $original in 0..$originalLen", original in 0..originalLen)
                    assertTrue("monotonicity: $original >= $prevOriginal", original >= prevOriginal)
                    prevOriginal = original
                }
            }
        }
    }
}
