package com.felixj.moneta.shared.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.felixj.moneta.R
import com.felixj.moneta.ui.theme.MonetaTheme


@Composable
fun SeeMoreButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        modifier = modifier,
        onClick = onClick
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(stringResource(R.string.see_more))

            Icon(
                painterResource(R.drawable.baseline_arrow_forward_24),
                "arrow_forward"
            )
        }
    }
}

@Preview
@Composable
private fun SeeMoreButtonPreview() {
    MonetaTheme {
        SeeMoreButton({})
    }
}