package com.anymind.btc.shared

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Remove trailing fractional zeros and avoid scientific notation.
 * Keeps it a numeric value (not a String) so Jackson emits numbers like 1000, 1001.1.
 */
fun BigDecimal.compact(): BigDecimal {
    val s = this.stripTrailingZeros()
    // If scale < 0 (e.g., 1E+3), bring it back to an integer with scale 0.
    return if (s.scale() < 0) s.setScale(0, RoundingMode.UNNECESSARY) else s
}
