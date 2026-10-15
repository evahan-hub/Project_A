package io.artly.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import io.artly.ui.resources.Res
import io.artly.ui.resources.bricolage_grotesque
import io.artly.ui.resources.public_sans
import io.artly.ui.resources.public_sans_italic
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.FontResource

private val variableWeights = listOf(FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold, FontWeight.Bold, FontWeight.ExtraBold)

@Composable
private fun variableFonts(resource: FontResource, style: FontStyle = FontStyle.Normal) = variableWeights.map { weight ->
    Font(resource, weight, style, variationSettings = FontVariation.Settings(weight, style))
}

/** Headings, as in the design. Bricolage Grotesque by Ateliertriay, SIL Open Font License. */
@Composable
fun bricolage() = FontFamily(variableFonts(Res.font.bricolage_grotesque))

/** Text, as in the design. Public Sans by USWDS, SIL Open Font License. */
@Composable
fun publicSans() = FontFamily(
    variableFonts(Res.font.public_sans) + variableFonts(Res.font.public_sans_italic, FontStyle.Italic)
)

/**
 * Large headings use Bricolage Grotesque, everything else Public Sans, including list item titles. Composable, as the
 * fonts are resources, which are loaded in composition.
 */
@Composable
fun artlyTypography(): Typography {
    val base = Typography()
    val bricolage = bricolage()
    val publicSans = publicSans()
    return Typography(
        displayLarge = base.displayLarge.copy(fontFamily = bricolage, fontWeight = FontWeight.ExtraBold),
        displayMedium = base.displayMedium.copy(fontFamily = bricolage, fontWeight = FontWeight.ExtraBold),
        displaySmall = base.displaySmall.copy(fontFamily = bricolage, fontWeight = FontWeight.ExtraBold),
        headlineLarge = base.headlineLarge.copy(fontFamily = bricolage, fontWeight = FontWeight.ExtraBold),
        headlineMedium = base.headlineMedium.copy(fontFamily = bricolage, fontWeight = FontWeight.ExtraBold),
        headlineSmall = base.headlineSmall.copy(fontFamily = bricolage, fontWeight = FontWeight.Bold),
        titleLarge = base.titleLarge.copy(fontFamily = bricolage, fontWeight = FontWeight.Bold),
        titleMedium = base.titleMedium.copy(fontFamily = publicSans, fontWeight = FontWeight.SemiBold),
        titleSmall = base.titleSmall.copy(fontFamily = publicSans, fontWeight = FontWeight.SemiBold),
        bodyLarge = base.bodyLarge.copy(fontFamily = publicSans),
        bodyMedium = base.bodyMedium.copy(fontFamily = publicSans),
        bodySmall = base.bodySmall.copy(fontFamily = publicSans),
        labelLarge = base.labelLarge.copy(fontFamily = publicSans, fontWeight = FontWeight.SemiBold),
        labelMedium = base.labelMedium.copy(fontFamily = publicSans, fontWeight = FontWeight.SemiBold),
        labelSmall = base.labelSmall.copy(fontFamily = publicSans),
    )
}
