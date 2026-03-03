package edu.uic.banking

import munit.FunSuite

/** ============================================================================
  * UNIT TESTS — InterestCalculator
  * ============================================================================
  *
  * WHAT TO TEST
  * ------------
  * InterestCalculator contains three pure, deterministic functions with no
  * dependencies. Unit tests here should follow the Arrange-Act-Assert pattern:
  * construct known inputs, call the function, and assert an exact output.
  *
  * TOOLS NEEDED
  * ------------
  * munit.FunSuite only. No mocking library required because there are no
  * dependencies to substitute.
  *
  * BOUNDARY VALUES TO EXERCISE
  * ---------------------------
  * - Zero principal          → interest should always be 0
  * - Zero rate               → interest should always be 0
  * - 1 year                  → simplest non-trivial case
  * - Multiple years          → check linear scaling for simple interest
  * - compoundsPerYear = 1    → compound interest degenerates to annual compounding
  * - compoundsPerYear = 12   → monthly compounding, common real-world case
  * - compoundsPerYear = 365  → daily compounding, maximum common frequency
  *
  * PRECISION NOTE
  * --------------
  * BigDecimal arithmetic is exact, but the Math.pow call inside compoundInterest
  * and effectiveAnnualRate uses Double, introducing floating-point error.
  * Use assertEqualsDouble(result.toDouble, expected, delta = 0.0001) instead of
  * assertEquals when comparing those results.
  * ============================================================================
  */
// https://github.com/amordahl/moretesting
class InterestCalculatorSuite extends FunSuite {

  // --------------------------------------------------------------------------
  // simpleInterest
  // --------------------------------------------------------------------------

  // TEST: zero principal yields zero interest
  // Purpose:  Multiplying by principal = 0 must short-circuit to 0 regardless
  //           of rate or years.
  // Setup:    principal = 0, annualRatePct = 5, years = 3
  // Assert:   result == BigDecimal(0)
  test("simpleInterest: zero principal yields zero interest") {
    // Arrange, Act, Assert
    // Arrange
    val principal     = BigDecimal(0)
    val annualRatePct = BigDecimal(5)
    val years         = 3

    // Act
    val result =
      InterestCalculator.simpleInterest(principal, annualRatePct, years)

    // Assert
    assertEquals(result, BigDecimal(0))
  }

  // TEST: zero rate yields zero interest
  /* Purpose: A 0% rate means no money is earned regardless of principal or
   * time. */
  // Setup:    principal = 1000, annualRatePct = 0, years = 5
  // Assert:   result == BigDecimal(0)
  test("simpleInterest: zero rate yields zero interest") {
    // Arrange
    val principal     = BigDecimal(1000)
    val annualRatePct = BigDecimal(0)
    val years         = 5

    // Act
    val result =
      InterestCalculator.simpleInterest(principal, annualRatePct, years)

    // Assert
    assertEquals(result, BigDecimal(0))
  }

  // TEST: known values produce expected interest
  // Purpose:  Spot-check the formula P × r × t with hand-computed reference.
  // Setup:    principal = 1000, annualRatePct = 5, years = 3  →  expected = 150
  // Assert:   result == BigDecimal(150)
  test("simpleInterest: known values produce expected interest".fail) { ??? }

  // TEST: interest scales linearly with years
  // Purpose:  Simple interest is a linear function of time; doubling years
  //           must double the interest.
  // Setup: Compute for years = 1 and years = 2 with identical principal/rate.
  // Assert:   interest(years=2) == interest(years=1) * 2
  test("simpleInterest: interest scales linearly with years".fail) { ??? }

  // TEST: interest scales linearly with principal
  // Purpose:  Doubling the principal must double the interest.
  /* Setup: Compute for principal = 500 and principal = 1000 with same
   * rate/years. */
  // Assert:   interest(principal=1000) == interest(principal=500) * 2
  test("simpleInterest: interest scales linearly with principal".fail) { ??? }

  // --------------------------------------------------------------------------
  // compoundInterest
  // --------------------------------------------------------------------------

  // TEST: zero principal yields zero compound interest
  // Purpose:  P = 0 must return 0 under any rate/compounding schedule.
  // Setup: principal = 0, annualRatePct = 6, years = 5, compoundsPerYear = 12
  // Assert: result == BigDecimal(0) (or within floating-point tolerance of 0)
  test("compoundInterest: zero principal yields zero compound interest".fail) {
    ???
  }

  // TEST: zero rate yields zero compound interest
  // Purpose:  (1 + 0/n)^(nt) = 1 for any n and t, so interest = P*1 - P = 0
  /* Setup: principal = 2000, annualRatePct = 0, years = 10, compoundsPerYear =
   * 12 */
  // Assert:   result ≈ 0.0 (within tolerance)
  test("compoundInterest: zero rate yields zero compound interest".fail) { ??? }

  // TEST: annual compounding matches known reference value
  // Purpose: With compoundsPerYear = 1, the formula simplifies to P(1+r)^t − P.
  //           Hand-verify: 1000 × (1.05)^2 − 1000 = 102.50
  // Setup: principal = 1000, annualRatePct = 5, years = 2, compoundsPerYear = 1
  // Assert:   result ≈ 102.50 (within tolerance 0.01)
  test(
    "compoundInterest: annual compounding matches known reference value".fail
  ) { ??? }

  // TEST: monthly compounding produces more interest than annual compounding
  /* Purpose: More frequent compounding always yields more interest (for rate >
   * 0). */
  // Setup:    Same principal/rate/years; compare compoundsPerYear = 1 vs 12.
  // Assert:   monthly result > annual result
  test("compoundInterest: monthly compounding yields more than annual".fail) {
    ???
  }

  // TEST: compound interest is always >= simple interest (rate > 0, years > 1)
  // Purpose:  Compounding re-invests earned interest; simple interest does not.
  //           So compound >= simple for all valid positive inputs when years > 1.
  // Setup:    Compute both for same principal = 1000, rate = 7, years = 5.
  // Assert:   compoundInterest(...) >= simpleInterest(...)
  test("compoundInterest: always >= simpleInterest for same parameters".fail) {
    ???
  }

  // --------------------------------------------------------------------------
  // effectiveAnnualRate
  // --------------------------------------------------------------------------

  // TEST: zero nominal rate produces zero EAR
  // Purpose:  (1 + 0/n)^n − 1 = 0 regardless of n.
  // Setup:    nominalRatePct = 0, compoundsPerYear = 12
  // Assert:   result ≈ 0.0
  test("effectiveAnnualRate: zero nominal rate produces zero EAR".fail) { ??? }

  // TEST: annual compounding leaves nominal rate unchanged
  // Purpose:  When compoundsPerYear = 1, EAR equals the nominal rate exactly.
  // Setup:    nominalRatePct = 6, compoundsPerYear = 1
  // Assert:   result ≈ 6.0
  test(
    "effectiveAnnualRate: annual compounding leaves nominal rate unchanged".fail
  ) { ??? }

  // TEST: EAR is always >= nominal rate for compoundsPerYear > 1
  // Purpose:  More frequent compounding always increases the effective rate.
  // Setup:    nominalRatePct = 6, compoundsPerYear = 12
  // Assert:   result > 6.0
  test(
    "effectiveAnnualRate: EAR exceeds nominal rate when compoundsPerYear > 1".fail
  ) { ??? }

  // TEST: EAR increases as compounding frequency increases
  // Purpose:  Daily compounding should produce a higher EAR than monthly.
  // Setup:    nominalRatePct = 5; compare compoundsPerYear = 12 vs 365.
  // Assert:   EAR(365) > EAR(12)
  test(
    "effectiveAnnualRate: higher compounding frequency produces higher EAR".fail
  ) { ??? }
}
