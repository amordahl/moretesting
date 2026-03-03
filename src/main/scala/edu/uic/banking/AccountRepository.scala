package edu.uic.banking

/** Persistence layer for Account entities.
  *
  * This trait is the primary dependency to mock when testing TransactionProcessor.
  * In production it would be backed by a database; in tests it is replaced with a
  * ScalaMock stub so that no real I/O occurs.
  */
trait AccountRepository {
  /** Returns the account with the given id, or None if it does not exist. */
  def findById(id: String): Option[Account]

  /** Persists an account (insert or update) and returns the saved value. */
  def save(account: Account): Account

  /** Returns true if an account with the given id exists in the store. */
  def exists(id: String): Boolean
}
