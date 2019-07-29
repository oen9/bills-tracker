package oen.billstracker.model
import reactivemongo.bson.{BSONDocumentReader, BSONDocumentWriter, Macros}
import reactivemongo.bson.BSONObjectID
import io.scalaland.chimney.Transformer

object StorageData {
  val USERS_COLLECTION_NAME = "appUsers"

  case class DbUser(_id: Option[BSONObjectID] = None,
    name: String = "",
    password: String = "",
    token: String = "",
    billsGroups: IndexedSeq[DbBillGroup] = IndexedSeq())
  case class DbBillGroup(name: String, items: IndexedSeq[DbBillItem] = IndexedSeq(), id: Option[BSONObjectID] = None)
  case class DbBillItem(description: String, value: BigDecimal, id: Option[BSONObjectID] = None)

  implicit val bSONObjectIDToString: Transformer[BSONObjectID, String] = (id: BSONObjectID) => id.stringify
  implicit val stringToBSONObjectID: Transformer[String, BSONObjectID] = (id: String) => BSONObjectID.parse(id).fold(_ => BSONObjectID.generate(), identity)

  implicit def dbBillItemWriter: BSONDocumentWriter[DbBillItem] = Macros.writer[DbBillItem]
  implicit def dbBillItemReader: BSONDocumentReader[DbBillItem] = Macros.reader[DbBillItem]
  implicit def dbBillGroupWriter: BSONDocumentWriter[DbBillGroup] = Macros.writer[DbBillGroup]
  implicit def dbBillGroupReader: BSONDocumentReader[DbBillGroup] = Macros.reader[DbBillGroup]
  implicit def dbdbUserWriter: BSONDocumentWriter[DbUser] = Macros.writer[DbUser]
  implicit def dbUserReader: BSONDocumentReader[DbUser] = Macros.reader[DbUser]
}
