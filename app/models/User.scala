package models

import play.api.db.slick.Config.driver.simple._
import scala.slick.lifted.Tag

import org.joda.time.DateTime
import com.github.tototoshi.slick.H2JodaSupport._

import play.api.libs.json._
import play.boy.dao._
import play.boy.annotation._
import play.boy.types._

object Role extends Enum {
  type Role = Value
  val Administrator = Value("管理者")
  val NormalUser = Value("一般ユーザ")
}

import Role.Role

case class User(
  id: Option[Long],
  @label("名前")
  name: String,
  @label("メールアドレス")
  mail: String,
  @label("権限")
  role: Role,
  @label("郵便番号")
  zip: String,
  @label("住所")
  address: String,
  @label("パスワード")
  password: String,
  hashed: Boolean,
  createdAt: DateTime = new DateTime,
  updatedAt: DateTime = new DateTime
)

object User {
  implicit val format = Json.format[User]
}

// case classをmappingするTable[A]を定義する
class Users(tag: Tag) extends Table[User](tag, "user") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name", O.NotNull)
  def mail = column[String]("mail")
  def role = column[Role]("role")
  def zip = column[String]("zip")
  def address = column[String]("address")
  def password = column[String]("password")
  def hashed = column[Boolean]("hashed")
  def createdAt = column[DateTime]("created_at")
  def updatedAt = column[DateTime]("updated_at")
  def * = (id.?, name, mail, role, zip, address, password, hashed, createdAt, updatedAt) <> ((User.apply _).tupled, User.unapply _)
}

object Users extends DAO[User, Users] {
  val query = TableQuery[Users]
}