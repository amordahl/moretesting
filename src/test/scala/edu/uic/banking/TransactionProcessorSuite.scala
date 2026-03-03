package edu.uic.banking

import munit.FunSuite
import org.scalamock.stubs.*

/** ============================================================================
  * MOCK-BASED TESTS — TransactionProcessor
  * ============================================================================
  *
  * WHAT TO TEST
  * ------------
  * TransactionProcessor depends on four external services.  Tests here replace
  * every dependency with a ScalaMock mock and assert:
  *   1. The return value (Right/Left + specific error) is correct.
  *   2. The correct side-effecting methods were called (or NOT called).
  *   3. The correct arguments were passed to those methods.
  *   4. Each method was called the expected number of times (once, never, etc.).
  *
  * WHY MOCKS
  * ---------
  * Using real implementations would couple tests to a database, email server,
  * ML model, etc.  Mocks let each test exercise exactly one code path in
  * TransactionProcessor without any infrastructure.
  *
  * TOOLS NEEDED
  * ------------
  *   import org.scalamock.scalatest.MockFactory   // provides mock[T]
  *   munit.FunSuite                               // provides test() / assert*
  *
  * SCALAMOCK QUICK REFERENCE
  * -------------------------
  *   val mockRepo = mock[AccountRepository]
  *
  *   // Stub a return value (called any number of times):
  *   (mockRepo.findById _).stubs("acc-1").returning(Some(account))
  *
  *   // Expect exactly one call with specific arguments:
  *   (mockRepo.save _).expects(updatedAccount).returning(updatedAccount).once()
  *
  *   // Expect a method is NEVER called:
  *   (mockRepo.save _).expects(*).never()
  *
  *   // Expect a Unit method is called once (no return value to set):
  *   (mockAudit.logTransaction _).expects(*).once()
  *
  * SHARED FIXTURE (add a beforeEach or helper method)
  * ---------------------------------------------------
  * All tests need mocks + a processor instance.  Consider extracting:
  *
  *   def fixtures() =
  *     val repo   = mock[AccountRepository]
  *     val notify = mock[NotificationService]
  *     val audit  = mock[AuditLogger]
  *     val fraud  = mock[FraudDetectionService]
  *     val proc   = TransactionProcessor(repo, notify, audit, fraud)
  *     (repo, notify, audit, fraud, proc)
  *
  * SAMPLE ACCOUNTS (reuse across tests)
  * -------------------------------------
  *   val alice = Account("acc-alice", "Alice", BigDecimal("1000.00"), AccountType.Checking)
  *   val bob   = Account("acc-bob",   "Bob",   BigDecimal("500.00"),  AccountType.Savings)
  * ============================================================================
  */
class TransactionProcessorSuite extends FunSuite, Stubs {

  // ==========================================================================
  // deposit
  // ==========================================================================

  // TEST: successful deposit returns Right and updates balance
  // Purpose: Happy path — all dependencies cooperate and the balance increases.
  // Setup:
  //   - mockRepo.findById("acc-alice") returns Some(alice)
  /* - mockRepo.save expects an account with balance = 1000 + 200 = 1200,
   * returns it */
  //   - mockAudit.logTransaction expects exactly one call with any Transaction
  /* - mockNotify.notifyDeposit expects exactly one call with the saved account
   * and 200 */
  /* - mockFraud is NOT called during a deposit (verify with .never() or just
   * don't stub) */
  // Assert:
  //   - result is Right(TransactionResult)
  //   - result.updatedAccount.balance == BigDecimal("1200.00")
  //   - result.transaction.transactionType == TransactionType.Deposit
  //   - result.transaction.amount == BigDecimal("200.00")
  test("deposit: successful deposit returns Right and updates balance") {
    val mockRepo   = stub[AccountRepository]
    val mockAudit  = stub[AuditLogger]
    val mockNotify = stub[NotificationService]
    val mockFraud  = stub[FraudDetectionService]

    val accountName = "acc-alice"
    val account =
      Account(accountName, "Alice", BigDecimal(1000), AccountType.Checking)
    mockRepo.findById.returns(s =>
      s match {
        case `accountName` => Some(account)
        case _             => None
      }
    )
    mockAudit.logTransaction.returnsWith(())
    mockNotify.notifyDeposit.returnsWith(())

    val augAccount = account.copy(balance = BigDecimal(300))
    mockRepo.save.returns {
      case `augAccount` => augAccount
      case _            => ???
    }
    val transactionProcessor =
      TransactionProcessor(mockRepo, mockNotify, mockAudit, mockFraud)

    // Act
    val acc = transactionProcessor.deposit(accountName, BigDecimal(200))

    // Assert
    assert(acc.isRight)
  }

  // TEST: deposit on nonexistent account returns AccountNotFound
  // Purpose: When the repo returns None, the processor must not save or notify.
  // Setup:
  //   - mockRepo.findById("unknown") returns None
  //   - mockRepo.save expects NEVER (no save should happen)
  //   - mockAudit.logTransaction expects NEVER
  //   - mockNotify.notifyDeposit expects NEVER
  // Assert:
  //   - result == Left(TransactionError.AccountNotFound("unknown"))
  test("deposit: nonexistent account returns AccountNotFound".fail) { ??? }

  // TEST: deposit with zero amount returns InvalidAmount
  // Purpose:  Guard clause fires before any repo lookup.
  // Setup:
  //   - No mock expectations needed; no service should be called at all.
  // Assert:
  //   - result == Left(TransactionError.InvalidAmount)
  test(
    "deposit: zero amount returns InvalidAmount without touching repo".fail
  ) { ??? }

  // TEST: deposit with negative amount returns InvalidAmount
  // Purpose:  Negative deposits are just as invalid as zero.
  // Setup:    Same as zero-amount test above.
  // Assert:   result == Left(TransactionError.InvalidAmount)
  test("deposit: negative amount returns InvalidAmount".fail) { ??? }

  // ==========================================================================
  // withdraw
  // ==========================================================================

  // TEST: successful withdrawal returns Right and deducts balance
  // Purpose:  Happy path — enough funds, no fraud flag, everything succeeds.
  // Setup:
  //   - alice has balance 1000; withdraw 300
  //   - mockFraud.isSuspicious(alice, 300) returns false
  //   - mockRepo.findById returns Some(alice)
  //   - mockRepo.save expects account with balance 700, returns it
  //   - mockAudit.logTransaction expects once
  //   - mockNotify.notifyWithdrawal expects once with saved account and 300
  //   - mockNotify.notifyFraudAlert expects NEVER
  //   - mockAudit.logFailedAttempt expects NEVER
  // Assert:
  //   - result is Right(TransactionResult)
  //   - result.updatedAccount.balance == BigDecimal("700.00")
  test(
    "withdraw: successful withdrawal returns Right and deducts balance".fail
  ) { ??? }

  // TEST: withdrawal with insufficient funds returns InsufficientFunds
  // Purpose:  Balance check fires after fraud check; no save or notification.
  // Setup:
  //   - alice has balance 1000; attempt to withdraw 1500
  //   - mockFraud.isSuspicious returns false
  //   - mockRepo.findById returns Some(alice)
  //   - mockRepo.save expects NEVER
  //   - mockAudit.logFailedAttempt expects once (reason contains "funds")
  //   - mockNotify.notifyWithdrawal expects NEVER
  // Assert:
  /* - result == Left(TransactionError.InsufficientFunds("acc-alice", 1000,
   * 1500)) */
  test(
    "withdraw: insufficient funds returns InsufficientFunds and logs failure".fail
  ) { ??? }

  // TEST: withdrawal flagged as fraud returns FraudDetected
  // Purpose:  Fraud check fires before the balance check; save must not occur.
  // Setup:
  //   - mockFraud.isSuspicious(alice, 900) returns true
  //   - mockRepo.findById returns Some(alice)
  //   - mockRepo.save expects NEVER
  //   - mockAudit.logFailedAttempt expects once
  //   - mockNotify.notifyFraudAlert expects once
  //   - mockNotify.notifyWithdrawal expects NEVER
  // Assert:
  //   - result == Left(TransactionError.FraudDetected("acc-alice"))
  test(
    "withdraw: fraud flag returns FraudDetected and sends fraud alert".fail
  ) { ??? }

  // TEST: fraud check is skipped when account does not exist
  // Purpose:  AccountNotFound must be returned immediately; fraud service must
  //           never be consulted (it would need an Account argument we don't have).
  // Setup:
  //   - mockRepo.findById returns None
  //   - mockFraud.isSuspicious expects NEVER
  // Assert:
  //   - result == Left(TransactionError.AccountNotFound(...))
  test("withdraw: account not found without calling fraud service".fail) { ??? }

  // TEST: withdrawal with zero amount returns InvalidAmount
  // Purpose:  Guard clause fires before any repo or fraud lookup.
  // Setup:    No mock expectations on any service.
  // Assert:   result == Left(TransactionError.InvalidAmount)
  test(
    "withdraw: zero amount returns InvalidAmount without touching any service".fail
  ) { ??? }

  // ==========================================================================
  // transfer
  // ==========================================================================

  // TEST: successful transfer credits destination and debits source
  // Purpose:  Happy path — both accounts exist, funds are available, no fraud.
  // Setup:
  //   - alice has 1000; bob has 500; transfer 400 from alice to bob
  //   - mockFraud.isSuspicious(alice, 400) returns false
  //   - mockRepo.findById("acc-alice") returns Some(alice)
  //   - mockRepo.findById("acc-bob")   returns Some(bob)
  //   - mockRepo.save expects account with id=alice and balance=600, returns it
  //   - mockRepo.save expects account with id=bob   and balance=900, returns it
  //   - mockAudit.logTransaction expects once
  //   - mockNotify.notifyTransfer expects once with savedAlice, savedBob, 400
  // Assert:
  //   - result is Right(TransactionResult)
  //   - result.updatedAccount.balance   == BigDecimal("600.00")  (alice)
  /* - result.relatedAccount is Some(_) with balance == BigDecimal("900.00")
   * (bob) */
  test(
    "transfer: successful transfer debits source and credits destination".fail
  ) { ??? }

  // TEST: transfer from nonexistent source returns AccountNotFound
  // Purpose:  The first repo lookup fails; no further action is taken.
  // Setup:
  //   - mockRepo.findById("unknown") returns None
  //   - mockRepo.save expects NEVER
  //   - mockFraud.isSuspicious expects NEVER
  //   - mockAudit.logFailedAttempt expects NEVER
  // Assert:
  //   - result == Left(TransactionError.AccountNotFound("unknown"))
  test("transfer: source account not found returns AccountNotFound".fail) {
    ???
  }

  // TEST: transfer to nonexistent destination returns AccountNotFound
  // Purpose:  Source exists but destination lookup returns None.
  // Setup:
  //   - mockRepo.findById("acc-alice") returns Some(alice)
  //   - mockRepo.findById("unknown")   returns None
  //   - mockRepo.save expects NEVER
  // Assert:
  //   - result == Left(TransactionError.AccountNotFound("unknown"))
  test("transfer: destination account not found returns AccountNotFound".fail) {
    ???
  }

  // TEST: transfer with insufficient funds in source returns InsufficientFunds
  // Purpose:  Balance check for the source fires after the fraud check.
  // Setup:
  //   - alice has 1000; attempt to transfer 2000
  //   - mockFraud.isSuspicious returns false
  //   - Both accounts found
  //   - mockRepo.save expects NEVER
  //   - mockAudit.logFailedAttempt expects once
  // Assert:
  /* - result == Left(TransactionError.InsufficientFunds("acc-alice", 1000,
   * 2000)) */
  test(
    "transfer: insufficient funds in source returns InsufficientFunds".fail
  ) { ??? }

  // TEST: transfer flagged as fraud halts before any save
  // Purpose:  Fraud on the source account stops the entire transfer.
  // Setup:
  //   - mockFraud.isSuspicious(alice, 800) returns true
  //   - mockRepo.save expects NEVER (neither account is modified)
  //   - mockNotify.notifyFraudAlert expects once
  //   - mockAudit.logFailedAttempt expects once
  // Assert:
  //   - result == Left(TransactionError.FraudDetected("acc-alice"))
  test(
    "transfer: fraud on source returns FraudDetected and does not save".fail
  ) { ??? }

  // TEST: transfer to self (fromId == toId) returns InvalidAmount
  /* Purpose: Self-transfers are rejected by the guard clause without any
   * lookup. */
  // Setup:    No mock expectations on any service.
  // Assert:   result == Left(TransactionError.InvalidAmount)
  test("transfer: self-transfer returns InvalidAmount".fail) { ??? }

  // TEST: transfer with zero amount returns InvalidAmount
  // Purpose:  Zero-amount guard clause fires before any repo call.
  // Setup:    No mock expectations on any service.
  // Assert:   result == Left(TransactionError.InvalidAmount)
  test("transfer: zero amount returns InvalidAmount".fail) { ??? }

  // ==========================================================================
  // Interaction / collaboration tests
  // ==========================================================================

  // TEST: audit logger is called once per successful deposit
  // Purpose:  Verify that logTransaction is called exactly once — not twice,
  //           not zero times — on the happy path.
  // Setup:    Same as the successful deposit test.
  //           Use .expects(*).once() on mockAudit.logTransaction.
  // Assert:   ScalaMock verifies the expectation automatically after the test.
  test("deposit: audit logger receives exactly one logTransaction call".fail) {
    ???
  }

  // TEST: notification service is never called when deposit finds no account
  /* Purpose: Negative interaction test — ensures no spurious notifications
   * fire. */
  // Setup:    mockRepo.findById returns None.
  //           mockNotify.notifyDeposit expects NEVER (use .expects(*).never()).
  // Assert:   ScalaMock verifies; result is also Left(AccountNotFound).
  test("deposit: no notification fired when account is not found".fail) { ??? }
}
