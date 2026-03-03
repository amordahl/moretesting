package edu.uic.banking

/** Pure interest-calculation functions with no side effects.
  *
  * Because every method here is a total, deterministic function of its inputs,
  * this object is ideal for demonstrating both:
  *   - Classic unit tests (specific inputs → specific expected outputs)
  *   - Property-based tests (algebraic laws that must hold for ALL valid inputs)
  */
object InterestCalculator {

  /** Computes the interest earned under the simple-interest formula.
    *
    * Formula: I = P × r × t
    *
    * @param principal
    *   Starting balance (must be >= 0)
    * @param annualRatePct
    *   Annual interest rate as a percentage, e.g. 5 for 5% (must be >= 0)
    * @param years
    *   Number of years (must be > 0)
    * @return
    *   Interest earned (never the final balance)
    */
  def simpleInterest(
    principal: BigDecimal,
    annualRatePct: BigDecimal,
    years: Int,
  ): BigDecimal =
    principal * annualRatePct / 100 * years

  /** Computes the interest earned under the compound-interest formula.
    *
    * Formula: I = P(1 + r/n)^(nt) − P
    *
    * @param principal
    *   Starting balance (must be >= 0)
    * @param annualRatePct
    *   Annual interest rate as a percentage (must be >= 0)
    * @param years
    *   Number of years (must be > 0)
    * @param compoundsPerYear
    *   How many times interest compounds each year (must be >= 1)
    * @return
    *   Interest earned
    */
  def compoundInterest(
    principal: BigDecimal,
    annualRatePct: BigDecimal,
    years: Int,
    compoundsPerYear: Int,
  ): BigDecimal = {
    val rate   = annualRatePct / 100
    val n      = compoundsPerYear
    val factor = Math.pow((1 + (rate / n).toDouble), years * n)
    principal * BigDecimal(factor) - principal
  }

  /** Converts a nominal annual rate to the effective annual rate (EAR).
    *
    * EAR = (1 + r/n)^n − 1, expressed as a percentage.
    *
    * Useful for comparing products with different compounding frequencies.
    *
    * @param nominalRatePct
    *   Nominal annual rate as a percentage (must be >= 0)
    * @param compoundsPerYear
    *   Number of compounding periods per year (must be >= 1)
    * @return
    *   Effective annual rate as a percentage
    */
  def effectiveAnnualRate(
    nominalRatePct: BigDecimal,
    compoundsPerYear: Int,
  ): BigDecimal = {
    val r = nominalRatePct / 100 / compoundsPerYear
    (Math.pow((1 + r.toDouble), compoundsPerYear) - 1) * 100
  }
}
