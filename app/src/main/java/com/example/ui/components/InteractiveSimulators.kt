package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Composable
fun OhmsLawSimulator(modifier: Modifier = Modifier) {
    var voltage by remember { mutableFloatStateOf(12f) }
    var resistance by remember { mutableFloatStateOf(4f) }

    val current = if (resistance > 0f) voltage / resistance else 0f
    val power = voltage * current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ohms_law_simulator"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Bolt,
                        contentDescription = "Ohm's Law",
                        tint = AcademicGold,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Ohm's Law Circuit Simulator",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    color = AcademicGold.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "V = I × R",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = AcademicGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Circuit Visualizer (Bulb and Meter)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ScholarNavyDark)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Voltage indicator
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Voltage (V)", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    Text(
                        "%.1f V".format(voltage),
                        color = Color(0xFF60A5FA),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                // Bulb brightness visualizer
                val bulbAlpha = (current / 8f).coerceIn(0.2f, 1f)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEF08A).copy(alpha = bulbAlpha))
                        .border(2.dp, Color(0xFFEAB308), CircleShape)
                ) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = "Circuit Bulb",
                        tint = Color(0xFFCA8A04),
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Current indicator
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Current (I)", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    Text(
                        "%.2f A".format(current),
                        color = Color(0xFF34D399),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }

                // Power indicator
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Power (P)", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    Text(
                        "%.1f W".format(power),
                        color = Color(0xFFFBBF24),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Voltage Slider
            Text(
                "Adjust Voltage: ${voltage.roundToInt()} V",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
            Slider(
                value = voltage,
                onValueChange = { voltage = it },
                valueRange = 1f..36f,
                steps = 34,
                colors = SliderDefaults.colors(
                    thumbColor = ScholarNavy,
                    activeTrackColor = ScholarNavy
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("voltage_slider")
            )

            // Resistance Slider
            Text(
                "Adjust Resistance: ${resistance.roundToInt()} Ω",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
            Slider(
                value = resistance,
                onValueChange = { resistance = it },
                valueRange = 1f..20f,
                steps = 18,
                colors = SliderDefaults.colors(
                    thumbColor = ScienceTeal,
                    activeTrackColor = ScienceTeal
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("resistance_slider")
            )

            Text(
                "Observation: Doubling voltage doubles current at constant resistance (I ∝ V). Increasing resistance impedes current.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun QuadraticFormulaSolver(modifier: Modifier = Modifier) {
    var a by remember { mutableFloatStateOf(1f) }
    var b by remember { mutableFloatStateOf(-5f) }
    var c by remember { mutableFloatStateOf(6f) }

    val discriminant = (b * b) - (4 * a * c)
    val nature = when {
        discriminant > 0f -> "Two Distinct Real Roots"
        discriminant == 0f -> "Two Equal Real Roots (Repeated)"
        else -> "No Real Roots (Complex / Imaginary)"
    }

    val roots = if (discriminant >= 0f && a != 0f) {
        val r1 = (-b + sqrt(discriminant)) / (2 * a)
        val r2 = (-b - sqrt(discriminant)) / (2 * a)
        "x₁ = %.2f,  x₂ = %.2f".format(r1, r2)
    } else {
        "x = (-%.1f ± √%.1f i) / %.1f".format(b, abs(discriminant), 2 * a)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("quadratic_solver"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Calculate,
                        contentDescription = "Quadratic",
                        tint = ScholarNavy,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Quadratic Equation Solver",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    color = ScholarNavy.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "ax² + bx + c = 0",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = ScholarNavy
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Active Equation display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ScholarNavyDark)
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                val bSign = if (b >= 0) "+ %.0f".format(b) else "- %.0f".format(abs(b))
                val cSign = if (c >= 0) "+ %.0f".format(c) else "- %.0f".format(abs(c))
                Text(
                    "%.0fx² $bSign x $cSign = 0".format(a),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Discriminant and Roots summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = if (discriminant >= 0) SuccessGreen.copy(alpha = 0.15f) else AlertRed.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text("Discriminant D = b²-4ac", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "D = %.1f".format(discriminant),
                            fontWeight = FontWeight.Bold,
                            color = if (discriminant >= 0) SuccessGreen else AlertRed
                        )
                    }
                }
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1.5f)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(nature, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ScholarNavy)
                        Text(roots, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Coefficient sliders
            Text("Coefficient a: ${a.roundToInt()}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Slider(
                value = a,
                onValueChange = { if (it != 0f) a = it },
                valueRange = -5f..5f,
                steps = 9,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Coefficient b: ${b.roundToInt()}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Slider(
                value = b,
                onValueChange = { b = it },
                valueRange = -10f..10f,
                steps = 19,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Coefficient c: ${c.roundToInt()}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Slider(
                value = c,
                onValueChange = { c = it },
                valueRange = -10f..10f,
                steps = 19,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun LensRefractionSimulator(modifier: Modifier = Modifier) {
    var focalLength by remember { mutableFloatStateOf(20f) } // +20 cm for convex lens
    var objectDistance by remember { mutableFloatStateOf(-30f) } // -30 cm (u is always negative)

    // Lens formula: 1/f = 1/v - 1/u => 1/v = 1/f + 1/u
    val oneOverV = (1f / focalLength) + (1f / objectDistance)
    val imageDistance = if (oneOverV != 0f) 1f / oneOverV else 9999f
    val magnification = if (objectDistance != 0f) imageDistance / objectDistance else 0f

    val isReal = imageDistance > 0
    val nature = if (isReal) "Real & Inverted" else "Virtual & Erect"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("lens_simulator"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.RemoveRedEye,
                        contentDescription = "Optics",
                        tint = ScienceTeal,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Convex Lens Ray Optics Simulator",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    color = ScienceTeal.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "1/f = 1/v - 1/u",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = ScienceTeal
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Calculated Optics Data Panel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ScholarNavyDark)
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Object Distance (u)", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                    Text("%.0f cm".format(objectDistance), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Column {
                    Text("Image Distance (v)", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                    Text("%.1f cm".format(imageDistance), color = Color(0xFF60A5FA), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Column {
                    Text("Magnification (m)", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                    Text("%.2f x".format(magnification), color = Color(0xFFFBBF24), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
                Column {
                    Text("Nature", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                    Text(nature, color = if (isReal) Color(0xFF34D399) else Color(0xFFF472B6), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Object Distance u: ${objectDistance.roundToInt()} cm (placed in front)", fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Slider(
                value = objectDistance,
                onValueChange = { objectDistance = it },
                valueRange = -60f..-10f,
                steps = 49,
                modifier = Modifier.fillMaxWidth()
            )

            Text("Lens Focal Length f: ${focalLength.roundToInt()} cm", fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Slider(
                value = focalLength,
                onValueChange = { focalLength = it },
                valueRange = 10f..40f,
                steps = 29,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                "Board Rule: When object is at 2F (u = -40 cm, f = 20 cm), image is formed at 2F (v = +40 cm) with same size (m = -1).",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}
