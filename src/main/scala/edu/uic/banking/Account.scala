package edu.uic.banking

/** The type of bank account, which governs fee rates and maintenance thresholds. */
enum AccountType {
  case Checking, Savings, Premium
}

/** An individual bank account.
  *
  * @param id
  *   Unique account identifier (UUID string)
  * @param owner
  *   Name of the account holder
  * @param balance
  *   Current balance; must be non-negative in a valid system state
  * @param accountType
  *   Governs fee schedules and maintenance fee waivers
  */
case class Account(
  id: String,
  owner: String,
  balance: BigDecimal,
  accountType: AccountType,
)
