package edu.uic.banking

/** Pure fee-calculation functions with no side effects.
  *
  * All functions are total and deterministic, making this object well-suited for:
  *   - Classic unit tests with boundary values (min fee floor, max fee ceiling)
  *   - Property-based tests asserting ordering invariants across account types
  */
object FeeCalculator {

  // Per-transaction fee rates by account type
  val CheckingFeeRate: BigDecimal = BigDecimal("0.0010") // 0.10%
  val SavingsFeeRate: BigDecimal  = BigDecimal("0.0005") // 0.05%
  val PremiumFeeRate: BigDecimal  = BigDecimal("0.0002") // 0.02%

  // Absolute floor and ceiling applied to every transaction fee
  val MinFee: BigDecimal = BigDecimal("0.25")
  val MaxFee: BigDecimal = BigDecimal("25.00")

  // Monthly maintenance fee thresholds and amounts
  val CheckingMaintenanceFee: BigDecimal    = BigDecimal("12.00")
  val CheckingMaintenanceWaiver: BigDecimal = BigDecimal("1500.00")
  val SavingsMaintenanceFee: BigDecimal     = BigDecimal("5.00")
  val SavingsMaintenanceWaiver: BigDecimal  = BigDecimal("300.00")

  /** Computes the per-transaction fee for a given amount and account type.
    *
    * The fee is the product of amount × rate, clamped to [MinFee, MaxFee].
    *
    * @param amount
    *   Transaction amount (must be > 0)
    * @param accountType
    *   Determines which rate applies
    * @return
    *   Fee in the range [MinFee, MaxFee]
    */
  def transactionFee(amount: BigDecimal, accountType: AccountType): BigDecimal = {
    val rate = accountType match {
      case AccountType.Checking => CheckingFeeRate
      case AccountType.Savings  => SavingsFeeRate
      case AccountType.Premium  => PremiumFeeRate
    }
    (amount * rate).max(MinFee).min(MaxFee)
  }

  /** Computes the penalty charged when an account goes into overdraft.
    *
    * Penalty = $35 flat + 5% of the overdraft amount.
    * Returns zero when overdraftAmount <= 0 (no overdraft).
    *
    * @param overdraftAmount
    *   How far below zero the account went (positive value represents deficit)
    * @return
    *   Total penalty owed
    */
  def overdraftPenalty(overdraftAmount: BigDecimal): BigDecimal =
    if overdraftAmount <= 0 then BigDecimal(0)
    else BigDecimal("35.00") + overdraftAmount * BigDecimal("0.05")

  /** Computes the monthly maintenance fee for an account.
    *
    * Checking: $12/month, waived if balance >= $1,500
    * Savings:  $5/month,  waived if balance >= $300
    * Premium:  always $0
    *
    * @param accountType
    *   The type of account
    * @param balance
    *   Current account balance (used to determine waiver eligibility)
    * @return
    *   Monthly fee owed (may be zero)
    */
  def monthlyMaintenanceFee(accountType: AccountType, balance: BigDecimal): BigDecimal =
    accountType match {
      case AccountType.Checking =>
        if balance >= CheckingMaintenanceWaiver then BigDecimal(0)
        else CheckingMaintenanceFee
      case AccountType.Savings =>
        if balance >= SavingsMaintenanceWaiver then BigDecimal(0)
        else SavingsMaintenanceFee
      case AccountType.Premium => BigDecimal(0)
    }
}
