package com.felixj.moneta.shared.view

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.felixj.moneta.ui.theme.MonetaTheme

@Stable
class ResponsiveFontSizeGroup(initial: TextUnit) {
    var value by mutableStateOf(initial)
        private set

    fun coerce(candidate: TextUnit) {
        if (candidate < value) value = candidate
    }
}

@Composable
fun rememberResponsiveFontSizeGroup(baseFontSize: TextUnit) =
    remember(baseFontSize) { ResponsiveFontSizeGroup(baseFontSize) }

private fun stepsFor(base: TextUnit, min: TextUnit = 12.sp): List<TextUnit> {
    if (base == TextUnit.Unspecified) return listOf(16.sp, 14.sp, 12.sp)
    return generateSequence(base) { prev ->
        val next = prev.value - 2
        if (next >= min.value) next.sp else null
    }.toList()
}

private fun pickLargestFitting(
    measurer: TextMeasurer,
    text: String,
    baseStyle: TextStyle,
    maxWidthPx: Float
): TextUnit {
    val steps = stepsFor(baseStyle.fontSize)
    return steps.firstOrNull { size ->
        measurer.measure(text, baseStyle.copy(fontSize = size)).size.width <= maxWidthPx * 0.9f
    } ?: steps.last()
}

@Composable
fun ResponsiveAmountText(
    text: String,
    baseStyle: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    group: ResponsiveFontSizeGroup? = null,
) {
    BoxWithConstraints(modifier) {
        val textMeasurer = rememberTextMeasurer()
        val maxWidthPx = with(LocalDensity.current) { maxWidth.toPx() }

        val ownSize = remember(text, baseStyle, maxWidthPx) {
            pickLargestFitting(textMeasurer, text, baseStyle, maxWidthPx)
        }

        val fontSize = if (group == null) {
            ownSize
        } else {
            group.coerce(ownSize)
            group.value
        }

        Text(
            text = text,
            style = baseStyle.copy(fontSize = fontSize),
            color = color,
            maxLines = 1,
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun ResponsiveAmountTextPreview() {
    MonetaTheme {
        ResponsiveAmountText(
            text = "Rp 5.000.000",
            baseStyle = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}
