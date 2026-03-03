package edu.uic.banking

import munit.ScalaCheckSuite
import org.scalacheck.Gen
import org.scalacheck.Prop.*

/** ============================================================================
  * PROPERTY-BASED TESTS — InterestCalculator, FeeCalculator, TransactionProcessor
  * ============================================================================
  *
  * WHAT IS PROPERTY-BASED TESTING?
  * --------------------------------
  * Instead of testing a function with one hand-crafted input, a property test
  * states a universal law and lets ScalaCheck generate hundreds of random inputs
  * to try to falsify it.  If any input violates the property, ScalaCheck
  * "shrinks" it to the smallest failing case and reports it.
  *
  * WHEN TO USE IT
  * --------------
  * - Mathematical invariants (interest is non-negative, fee is bounded)
  * - Ordering relationships (compound >= simple, Checking fee >= Premium fee)
  * - Round-trip / inverse laws (encode then decode returns the original)
  * - Conservation laws (transfer preserves total balance across accounts)
  *
  * TOOLS NEEDED
  * ------------
  *   munit.ScalaCheckSuite   // replaces FunSuite; provides property() macro
  *   org.scalacheck.Prop.forAll
  *   org.scalacheck.Gen      // custom generators
  *   org.scalacheck.Arbitrary // implicit generators for built-in types
  *
  * SCALAMOCK NOTE
  * --------------
  * The TransactionProcessor properties below also require ScalaMock, because
  * the processor has side-effecting dependencies.  Mix in MockFactory and follow
  * the same pattern shown in TransactionProcessorSuite.
  *
  * CUSTOM GENERATORS (implement these before writing the properties below)
  * -----------------------------------------------------------------------
  * ScalaCheck can generate Int, Double, String, etc. automatically, but
  * BigDecimal and domain types need custom Gen values.
  *
  *   /** Generates a positive BigDecimal with up to 2 decimal places. */
  *   val positiveBigDecimal: Gen[BigDecimal] =
  *     Gen.choose(1, 1_000_000).map(cents => BigDecimal(cents) / 100)
  *
  *   /** Generates a non-negative BigDecimal (includes zero). */
  *   val nonNegativeBigDecimal: Gen[BigDecimal] =
  *     Gen.choose(0, 1_000_000).map(cents => BigDecimal(cents) / 100)
  *
  *   /** Generates a rate between 0% and 30% inclusive. */
  *   val ratePct: Gen[BigDecimal] =
  *     Gen.choose(0, 3000).map(bp => BigDecimal(bp) / 100)
  *
  *   /** Generates a valid number of years (1–30). */
  *   val years: Gen[Int] = Gen.choose(1, 30)
  *
  *   /** Generates a valid compounding frequency (1, 2, 4, 12, 52, 365). */
  *   val compoundsPerYear: Gen[Int] =
  *     Gen.oneOf(1, 2, 4, 12, 52, 365)
  *
  *   /** Generates an arbitrary AccountType. */
  *   val accountType: Gen[AccountType] =
  *     Gen.oneOf(AccountType.Checking, AccountType.Savings, AccountType.Premium)
  *
  *   /** Generates a valid Account with a random balance. */
  *   val account: Gen[Account] = for
  *     id      <- Gen.uuid.map(_.toString)
  *     owner   <- Gen.alphaStr.suchThat(_.nonEmpty)
  *     balance <- nonNegativeBigDecimal
  *     atype   <- accountType
  *   yield Account(id, owner, balance, atype)
  * ============================================================================
  */
class BankingPropertySuite extends ScalaCheckSuite {

  // ==========================================================================
  // InterestCalculator properties
  // ==========================================================================

  // PROPERTY: simple interest is always non-negative for valid inputs
  // Law:      For all principal >= 0, rate >= 0, years >= 1:
  //             simpleInterest(principal, rate, years) >= 0
  // Generator: positiveBigDecimal for principal, ratePct for rate, years gen
  // Why it matters: A negative interest result would indicate a sign error
  //                 in the formula.

  val positiveBigDecimal: Gen[BigDecimal] =
    Gen.choose[BigDecimal](0, 1_000_000).map(bd => bd / 100)
  val ratePct: Gen[BigDecimal] =
    Gen.choose[BigDecimal](0, 10000).map(bd => bd / 100)
  val years: Gen[Int] = Gen.choose(0, 30)

  property("simpleInterest: result is non-negative for valid inputs") {
    forAll(positiveBigDecimal, ratePct, years) {
      (positiveBigDecimal, ratePct, years) =>
        println(
          s"Positive big decimal: ${positiveBigDecimal}\nratePct: ${ratePct}\nyears: ${years}"
        )
        assert(InterestCalculator.simpleInterest(
          positiveBigDecimal,
          ratePct,
          years
        ) >= 1)
    }
  }

  // PROPERTY: simple interest is zero when principal is zero
  // Law:      For all rate >= 0, years >= 1:
  //             simpleInterest(0, rate, years) == 0
  // Generator: ratePct, years
  property("simpleInterest: zero principal always produces zero interest") {
    forAll(ratePct, years) { (rp, years) =>
      assertEquals(
        InterestCalculator.simpleInterest(BigDecimal(0), rp, years),
        BigDecimal(0)
      )
    }
  }

  // PROPERTY: simple interest is zero when rate is zero
  // Law:      For all principal >= 0, years >= 1:
  //             simpleInterest(principal, 0, years) == 0
  // Generator: nonNegativeBigDecimal, years
  property("simpleInterest: zero rate always produces zero interest") {
    ???
  }

  // PROPERTY: simple interest is linear in principal
  // Law:      For all principal >= 0, rate >= 0, years >= 1, k > 0:
  //             simpleInterest(k * principal, rate, years) ==
  //             k * simpleInterest(principal, rate, years)
  // Generator: nonNegativeBigDecimal for principal, ratePct, years,
  //            Gen.choose(2, 10) for k
  property("simpleInterest: scales linearly with principal") {
    ???
  }

  // PROPERTY: compound interest is always non-negative for valid inputs
  // Law:      For all principal >= 0, rate >= 0, years >= 1, n >= 1:
  //             compoundInterest(principal, rate, years, n) >= 0
  property("compoundInterest: result is non-negative for valid inputs") {
    ???
  }

  // PROPERTY: compound interest >= simple interest (rate > 0, years > 1)
  // Law:      For all principal > 0, rate > 0, years > 1, n >= 1:
  //             compoundInterest(principal, rate, years, n) >=
  //             simpleInterest(principal, rate, years)
  // Why it matters: The defining advantage of compound interest is that it
  //                 re-invests earned interest.  This must always hold.
  // Note:     Use years > 1 (for years == 1 and n == 1 they are equal to
  //           within floating-point tolerance).
  property("compoundInterest: always >= simpleInterest for matching params") {
    ???
  }

  // PROPERTY: more frequent compounding produces more (or equal) interest
  // Law:      For all principal > 0, rate > 0, years >= 1,
  //             n1 < n2 => compoundInterest(..., n1) <= compoundInterest(..., n2)
  // Generator: positiveBigDecimal, ratePct (> 0), years,
  //            Gen.choose(1, 364) for n1, then n2 = n1 + 1 (or pick from ordered list)
  property(
    "compoundInterest: higher compounding frequency yields >= interest"
  ) {
    ???
  }

  // PROPERTY: effectiveAnnualRate >= nominalRate for any compoundsPerYear >= 1
  // Law:      For all rate >= 0, n >= 1:
  //             effectiveAnnualRate(rate, n) >= rate
  // Generator: ratePct, compoundsPerYear
  property("effectiveAnnualRate: EAR is >= nominal rate for any compounding") {
    ???
  }

  // PROPERTY: effectiveAnnualRate with n=1 equals the nominal rate
  // Law:      For all rate >= 0:
  //             effectiveAnnualRate(rate, 1) ≈ rate   (within floating-point tol)
  // Generator: ratePct
  property("effectiveAnnualRate: annual compounding leaves rate unchanged") {
    ???
  }

  // ==========================================================================
  // FeeCalculator properties
  // ==========================================================================

  // PROPERTY: transactionFee is always >= MinFee
  // Law:      For all amount > 0, accountType:
  //             transactionFee(amount, accountType) >= FeeCalculator.MinFee
  // Generator: positiveBigDecimal, accountType
  property("transactionFee: result is always >= MinFee") {
    ???
  }

  // PROPERTY: transactionFee is always <= MaxFee
  // Law:      For all amount > 0, accountType:
  //             transactionFee(amount, accountType) <= FeeCalculator.MaxFee
  // Generator: positiveBigDecimal, accountType
  property("transactionFee: result is always <= MaxFee") {
    ???
  }

  // PROPERTY: Checking fee >= Savings fee >= Premium fee for same amount
  // Law:      For all amount > 0:
  //             transactionFee(amount, Checking) >= transactionFee(amount, Savings)
  //             transactionFee(amount, Savings)  >= transactionFee(amount, Premium)
  // Why it matters: Rate ordering is part of the product spec; if the rates in
  //                 FeeCalculator were accidentally reordered this property catches it.
  property("transactionFee: Checking >= Savings >= Premium for any amount") {
    ???
  }

  // PROPERTY: overdraftPenalty is zero for non-positive amounts
  // Law:      For all amount <= 0:
  //             overdraftPenalty(amount) == 0
  // Generator: Gen.choose(-10000, 0).map(BigDecimal(_))
  property("overdraftPenalty: non-positive overdraft yields zero penalty") {
    ???
  }

  // PROPERTY: overdraftPenalty is strictly positive for positive amounts
  // Law:      For all amount > 0:
  //             overdraftPenalty(amount) > 0
  property("overdraftPenalty: positive overdraft yields positive penalty") {
    ???
  }

  // PROPERTY: overdraftPenalty is monotonically increasing
  // Law:      For all 0 < a <= b:
  //             overdraftPenalty(a) <= overdraftPenalty(b)
  /* Generator: Two positive BigDecimals; use Gen.zip or suchThat to ensure a <=
   * b */
  property("overdraftPenalty: larger overdraft incurs larger penalty") {
    ???
  }

  // PROPERTY: monthlyMaintenanceFee for Premium is always zero
  // Law:      For all balance >= 0:
  //             monthlyMaintenanceFee(AccountType.Premium, balance) == 0
  // Generator: nonNegativeBigDecimal
  property("monthlyMaintenanceFee: Premium account is always free") {
    ???
  }

  // PROPERTY: monthly fee is never negative for any account type or balance
  // Law:      For all accountType, balance >= 0:
  //             monthlyMaintenanceFee(accountType, balance) >= 0
  // Generator: accountType, nonNegativeBigDecimal
  val accountType: Gen[AccountType] =
    Gen.frequency(
      (1, AccountType.Checking),
      (1, AccountType.Savings),
      (3, AccountType.Premium)
    )
  property("monthlyMaintenanceFee: result is always non-negative") {
    ???
  }

  // ==========================================================================
  // TransactionProcessor invariant properties
  // ==========================================================================
  //
  // These properties use a real (in-memory) AccountRepository implementation
  // and mock the side-effecting services (notifications, audit, fraud).
  // Implement a simple InMemoryAccountRepository for use in these tests:
  //
  //   class InMemoryAccountRepository extends AccountRepository:
  //     private var store = Map.empty[String, Account]
  //     def findById(id: String): Option[Account]   = store.get(id)
  /* def save(account: Account): Account = { store = store + (account.id ->
   * account); account } */
  //     def exists(id: String): Boolean             = store.contains(id)
  //
  // Then stub all side-effecting mocks to do nothing:
  //   (mockNotify.notifyDeposit _).stubs(*, *).returning(())
  //   (mockAudit.logTransaction _).stubs(*).returning(())
  //   etc.

  // PROPERTY: deposit increases balance by exactly the deposited amount
  // Law:      For all account, amount > 0:
  //             deposit(account.id, amount) returns Right(_)  AND
  //             result.updatedAccount.balance == account.balance + amount
  // Generator: account (nonNegative balance), positiveBigDecimal for amount
  /* Mock setup: fraud.isSuspicious stubbed to return false (unused for
   * deposits) */
  property("deposit: balance after deposit equals initial balance + amount") {
    ???
  }

  // PROPERTY: withdrawal decreases balance by exactly the withdrawn amount
  // Law:      For all account (balance > 0), amount in (0, balance]:
  //             withdraw(account.id, amount) returns Right(_)  AND
  //             result.updatedAccount.balance == account.balance - amount
  /* Generator: account, then amount = Gen.choose(1,
   * balance.toInt).map(BigDecimal(_)) */
  // Mock setup: fraud.isSuspicious stubbed to return false
  property(
    "withdraw: balance after withdrawal equals initial balance - amount"
  ) {
    ???
  }

  // PROPERTY: transfer is balance-conserving (zero-sum across both accounts)
  /* Law: For all accountA, accountB (distinct ids), amount in (0,
   * accountA.balance]: */
  //             transfer(accountA.id, accountB.id, amount) returns Right(_)  AND
  //             result.updatedAccount.balance + result.relatedAccount.get.balance
  //               == accountA.balance + accountB.balance
  // Generator: Two accounts with distinct ids; amount bounded by source balance
  // Mock setup: fraud.isSuspicious stubbed to return false
  // Why it matters: This invariant cannot be checked with a unit test because
  //                 it must hold for ALL valid (amount, balance) pairs, not just
  //                 one example.
  property("transfer: combined balance of both accounts is unchanged") {
    ???
  }

  // PROPERTY: a failed withdrawal never changes the account balance
  // Law:      For all account (balance >= 0), amount > account.balance:
  //             withdraw(account.id, amount) returns Left(InsufficientFunds)  AND
  //             re-reading the account from the repo shows the original balance
  // Generator: account, then amount = account.balance + positiveBigDecimal
  // Mock setup: fraud.isSuspicious stubbed to return false
  property("withdraw: failed withdrawal leaves balance unchanged") {
    ???
  }

  // PROPERTY: a fraud-rejected withdrawal never changes the account balance
  // Law:      For all account, amount > 0:
  //             when fraud.isSuspicious returns true,
  //             withdraw(account.id, amount) returns Left(FraudDetected)  AND
  //             account balance in the repo is unchanged
  // Mock setup: fraud.isSuspicious stubbed to return true
  property("withdraw: fraud-rejected withdrawal leaves balance unchanged") {
    ???
  }
}
