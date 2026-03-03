package edu.uic.banking

/** Sends customer-facing alerts for account activity.
  *
  * Side-effectful; in tests this should be mocked so that no emails/SMS are
  * actually sent, and so that callers can verify the correct notification was
  * triggered (or was intentionally NOT triggered).
  */
trait NotificationService {
  def notifyDeposit(account: Account, amount: BigDecimal): Unit
  def notifyWithdrawal(account: Account, amount: BigDecimal): Unit
  def notifyTransfer(from: Account, to: Account, amount: BigDecimal): Unit
  def notifyFraudAlert(account: Account, transaction: Transaction): Unit
}
