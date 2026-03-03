package edu.uic.banking

import java.time.Instant

/** Distinguishes what kind of movement a transaction represents. */
enum TransactionType {
  case Deposit, Withdrawal, Transfer
}

/** An immutable record of a single financial operation.
  *
  * @param id
  *   Unique transaction identifier (UUID string)
  * @param transactionType
  *   The kind of movement (deposit, withdrawal, transfer)
  * @param accountId
  *   The primary account involved
  * @param amount
  *   The value moved; always positive
  * @param relatedAccountId
  *   The destination account for a Transfer; None otherwise
  * @param timestamp
  *   When the transaction was created
  */
case class Transaction(
  id: String,
  transactionType: TransactionType,
  accountId: String,
  amount: BigDecimal,
  relatedAccountId: Option[String] = None,
  timestamp: Instant = Instant.now(),
)

/** The outcome of a successfully processed transaction.
  *
  * @param transaction
  *   The committed transaction record
  * @param updatedAccount
  *   The primary account after the operation
  * @param relatedAccount
  *   The destination account after the operation, if this was a Transfer
  */
case class TransactionResult(
  transaction: Transaction,
  updatedAccount: Account,
  relatedAccount: Option[Account] = None,
)

/** All ways a transaction attempt can fail. */
sealed trait TransactionError

object TransactionError {
  case class AccountNotFound(id: String)     extends TransactionError
  case class InsufficientFunds(
    accountId: String,
    balance: BigDecimal,
    required: BigDecimal,
  )                                          extends TransactionError
  case object InvalidAmount                  extends TransactionError
  case class FraudDetected(accountId: String) extends TransactionError
}
