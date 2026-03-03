package edu.uic.banking

/** Records every transaction attempt for compliance and debugging.
  *
  * Side-effectful; in tests this should be mocked to verify that the processor
  * logs successful transactions exactly once and logs failed attempts with a
  * meaningful reason string, without writing to any real log sink.
  */
trait AuditLogger {
  /** Called after every successfully committed transaction. */
  def logTransaction(transaction: Transaction): Unit

  /** Called whenever an attempt is rejected before it reaches persistence. */
  def logFailedAttempt(accountId: String, reason: String): Unit
}
