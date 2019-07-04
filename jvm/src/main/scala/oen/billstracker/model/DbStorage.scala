package oen.billstracker.model
import oen.billstracker.shared.Dto._

object StorageData {
  case class DbUser(_id: String = "", name: String = "", password: String = "", token: String = "", billsGroups: IndexedSeq[BillGroup] = IndexedSeq())
}
