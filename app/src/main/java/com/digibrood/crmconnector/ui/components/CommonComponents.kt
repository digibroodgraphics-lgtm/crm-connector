package com.digibrood.crmconnector.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.digibrood.crmconnector.R
import com.digibrood.crmconnector.ui.theme.StatusAmber
import com.digibrood.crmconnector.ui.theme.StatusGreen
import com.digibrood.crmconnector.ui.theme.StatusRed

/**
 * Branding logo. [model] can be a remote https URL (String) or a local drawable
 * resource id (Int). Falls back to the bundled logo when loading fails.
 */
@Composable
fun BrandingLogo(
    model: Any,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = model,
        contentDescription = "Company logo",
        contentScale = ContentScale.Fit,
        error = androidx.compose.ui.res.painterResource(id = R.drawable.ic_logo_fallback),
        placeholder = androidx.compose.ui.res.painterResource(id = R.drawable.ic_logo_fallback),
        fallback = androidx.compose.ui.res.painterResource(id = R.drawable.ic_logo_fallback),
        modifier = modifier.clip(RoundedCornerShape(12.dp))
    )
}

/** A small coloured dot representing connection / approval state. */
@Composable
fun StatusDot(
    color: Color,
    modifier: Modifier = Modifier,
    size: Int = 12
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape(percent = 50))
    ) {
        Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(size.dp)
        )
    }
}

object StatusColors {
    val active = StatusGreen
    val pending = StatusAmber
    val stopped = StatusRed
}
