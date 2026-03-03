package edu.uic.banking

/** Evaluates whether a proposed transaction looks suspicious.
  *
  * In production this might call an external ML service. In tests it is mocked
  * to deterministically return true or false so that the processor's fraud-
  * handling branch can be exercised in isolation without any real model.
  */
trait FraudDetectionService {
  /** Returns true if the amount is considered suspicious for the given account. */
  def isSuspicious(account: Account, amount: BigDecimal): Boolean
}
