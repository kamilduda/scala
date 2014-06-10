/* NSC -- new Scala compiler
 * Copyright 2002-2013 LAMP/EPFL
 * @author Martin Odersky
 */

package scala.tools.nsc
package reporters

import scala.reflect.internal.util._

/** Report information, warnings and errors.
 *
 * This describes the stable interface for issuing information, warnings and errors.
 * The only abstract method in this class must be info0.
 */
abstract class Reporter {
  protected def info0(pos: Position, msg: String, severity: Severity, force: Boolean): Unit

  /** Informational messages. If `!force`, they may be suppressed. */
  final def info(pos: Position, msg: String, force: Boolean): Unit = info0(pos, msg, INFO, force)

  /** For sending a message which should not be labeled as a warning/error,
   *  but also shouldn't require -verbose to be visible.
   */
  def echo(msg: String): Unit                   = info(NoPosition, msg, force = true)
  def echo(pos: Position, msg: String): Unit    = info(pos, msg, force = true)

  /** Warnings and errors. */
  def warning(pos: Position, msg: String): Unit = info0(pos, msg, WARNING, force = false)
  def error(pos: Position, msg: String): Unit   = info0(pos, msg, ERROR, force = false)

  def flush(): Unit = { }

  // overridden by sbt, IDE
  def reset(): Unit = {
    INFO.count        = 0
    WARNING.count     = 0
    ERROR.count       = 0
    cancelled         = false
  }

  object severity extends Enumeration
  class Severity(val id: Int) extends severity.Value {
    var count: Int = 0
  }
  val INFO    = new Severity(0) {
    override def toString: String = "INFO"
  }
  val WARNING = new Severity(1) {
    override def toString: String = "WARNING"
  }
  val ERROR   = new Severity(2) {
    override def toString: String = "ERROR"
  }

  // used by sbt (via unit.cancel) to cancel a compile (see hasErrors)
  var cancelled: Boolean = false

  // overridden by sbt
  def hasErrors: Boolean   = ERROR.count > 0 || cancelled

  // overridden by sbt
  def hasWarnings: Boolean = WARNING.count > 0

  // overridden by sbt, IDE -- should move out of this interface
  // it's unrelated to reporting (IDE receives comments from ScaladocAnalyzer)
  def comment(pos: Position, msg: String): Unit = {}
}
