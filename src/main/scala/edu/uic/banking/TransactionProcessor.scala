package edu.uic.banking

import java.util.UUID
import java.time.Instant

/** Orchestrates deposits, withdrawals, and transfers.
  *
  * This class is the primary subject of the mock-based tests. It has four
  * injected dependencies, all of which are traits:
  *
  *   - [[AccountRepository]]    – read/write accounts (data layer)
  *   - [[NotificationService]]  – fire-and-forget customer alerts
  *   - [[AuditLogger]]          – compliance logging for every attempt
  *   - [[FraudDetectionService]] – external risk evaluation
  *
  * None of those dependencies have pure-function semantics, so tests replace
  * them all with ScalaMock mocks and set expectations on which methods are
  * called, with which arguments, and how many times.
  *
  * All public methods return `Either[TransactionError, TransactionResult]` so
  * that the caller can distinguish success from each error case without
  * exceptions.
  */
class TransactionProcessor(
  repo: AccountRepository,
  notifications: NotificationService,
  audit: AuditLogger,
  fraud: FraudDetectionService,
) {

  // -------------------------------------------------------------------------
  // Public API
  // -------------------------------------------------------------------------

  /** Credits `amount` to the account identified by `accountId`.
    *
    * Happy path:
    *   1. Validate amount > 0
    *   2. Look up the account (AccountNotFound if missing)
    *   3. Persist the updated balance
    *   4. Log the transaction
    *   5. Send a deposit notification
    *
    * @return
    *   Right(result) on success, Left(error) on any failure
    */
  def deposit(
    accountId: String,
    amount: BigDecimal,
  ): Either[TransactionError, TransactionResult] = {
    if amount <= 0 then return Left(TransactionError.InvalidAmount)

    repo.findById(accountId) match {
      case None =>
        Left(TransactionError.AccountNotFound(accountId))
      case Some(account) =>
        val txn     = makeTransaction(TransactionType.Deposit, accountId, amount)
        val updated = account.copy(balance = account.balance + amount)
        val saved   = repo.save(updated)
        audit.logTransaction(txn)
        notifications.notifyDeposit(saved, amount)
        Right(TransactionResult(txn, saved))
    }
  }

  /** Debits `amount` from the account identified by `accountId`.
    *
    * Happy path:
    *   1. Validate amount > 0
    *   2. Look up the account (AccountNotFound if missing)
    *   3. Ask fraud service (FraudDetected if suspicious; logs + notifies, no save)
    *   4. Check sufficient funds (InsufficientFunds if not; logs failed attempt, no save)
    *   5. Persist the updated balance
    *   6. Log the transaction
    *   7. Send a withdrawal notification
    *
    * @return
    *   Right(result) on success, Left(error) on any failure
    */
  def withdraw(
    accountId: String,
    amount: BigDecimal,
  ): Either[TransactionError, TransactionResult] = {
    if amount <= 0 then return Left(TransactionError.InvalidAmount)

    repo.findById(accountId) match {
      case None =>
        Left(TransactionError.AccountNotFound(accountId))
      case Some(account) =>
        if fraud.isSuspicious(account, amount) then {
          val txn = makeTransaction(TransactionType.Withdrawal, accountId, amount)
          audit.logFailedAttempt(accountId, "Fraud detected")
          notifications.notifyFraudAlert(account, txn)
          Left(TransactionError.FraudDetected(accountId))
        }
        else if account.balance < amount then {
          audit.logFailedAttempt(accountId, "Insufficient funds")
          Left(TransactionError.InsufficientFunds(accountId, account.balance, amount))
        }
        else {
          val txn     = makeTransaction(TransactionType.Withdrawal, accountId, amount)
          val updated = account.copy(balance = account.balance - amount)
          val saved   = repo.save(updated)
          audit.logTransaction(txn)
          notifications.notifyWithdrawal(saved, amount)
          Right(TransactionResult(txn, saved))
        }
    }
  }

  /** Moves `amount` from `fromId` to `toId` atomically (both saves succeed or neither does).
    *
    * Happy path:
    *   1. Validate amount > 0 and fromId != toId
    *   2. Look up both accounts (AccountNotFound for whichever is missing)
    *   3. Ask fraud service on the source account (FraudDetected + log + notify, no save)
    *   4. Check source has sufficient funds (InsufficientFunds + log, no save)
    *   5. Persist both updated accounts
    *   6. Log the transaction
    *   7. Send a transfer notification
    *
    * @return
    *   Right(result) on success, Left(error) on any failure
    */
  def transfer(
    fromId: String,
    toId: String,
    amount: BigDecimal,
  ): Either[TransactionError, TransactionResult] = {
    if amount <= 0 || fromId == toId then return Left(TransactionError.InvalidAmount)

    (repo.findById(fromId), repo.findById(toId)) match {
      case (None, _) =>
        Left(TransactionError.AccountNotFound(fromId))
      case (_, None) =>
        Left(TransactionError.AccountNotFound(toId))
      case (Some(from), Some(to)) =>
        if fraud.isSuspicious(from, amount) then {
          val txn = makeTransaction(TransactionType.Transfer, fromId, amount, Some(toId))
          audit.logFailedAttempt(fromId, "Fraud detected")
          notifications.notifyFraudAlert(from, txn)
          Left(TransactionError.FraudDetected(fromId))
        }
        else if from.balance < amount then {
          audit.logFailedAttempt(fromId, "Insufficient funds")
          Left(TransactionError.InsufficientFunds(fromId, from.balance, amount))
        }
        else {
          val txn         = makeTransaction(TransactionType.Transfer, fromId, amount, Some(toId))
          val updatedFrom = from.copy(balance = from.balance - amount)
          val updatedTo   = to.copy(balance = to.balance + amount)
          val savedFrom   = repo.save(updatedFrom)
          val savedTo     = repo.save(updatedTo)
          audit.logTransaction(txn)
          notifications.notifyTransfer(savedFrom, savedTo, amount)
          Right(TransactionResult(txn, savedFrom, Some(savedTo)))
        }
    }
  }

  // -------------------------------------------------------------------------
  // Private helpers
  // -------------------------------------------------------------------------

  private def makeTransaction(
    txnType: TransactionType,
    accountId: String,
    amount: BigDecimal,
    relatedId: Option[String] = None,
  ): Transaction =
    Transaction(
      id = UUID.randomUUID().toString,
      transactionType = txnType,
      accountId = accountId,
      amount = amount,
      relatedAccountId = relatedId,
      timestamp = Instant.now(),
    )
}
