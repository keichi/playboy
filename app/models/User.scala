package models

import play.api.db.slick.Config.driver.simple._
import scala.slick.lifted.Tag

import org.joda.time.DateTime

import play.api.libs.json.Json
import play.ball._

object Role extends Enum with RoleLike {
  type Role = Value
  val Administrator = Value("管理者")
  val NormalUser = Value("一般ユーザ")
}

import Role.Role

case class User(
  id: Option[Long],
  @label("名前")
  name: String,
  @label("ユーザ名")
  username: String,
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
  createdBy: Option[Long] = None,
  updatedBy: Option[Long] = None,
  createdAt: DateTime = new DateTime,
  updatedAt: DateTime = new DateTime
) extends UserLike

object User {
  implicit val format = Json.format[User]
}

// case classをmappingするTable[A]を定義する
class Users(tag: Tag) extends Table[User](tag, "user") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")
  def username = column[String]("username", O.NotNull)
  def mail = column[String]("mail")
  def role = column[Role]("role")
  def zip = column[String]("zip")
  def address = column[String]("address")
  def password = column[String]("password", O.NotNull)
  def hashed = column[Boolean]("hashed")
  def createdBy = column[Option[Long]]("created_by")
  def updatedBy = column[Option[Long]]("updated_by")
  def createdAt = column[DateTime]("created_at")
  def updatedAt = column[DateTime]("updated_at")
  def * = (id.?, name, username, mail, role, zip, address, password, hashed, createdBy, updatedBy, createdAt, updatedAt) <> ((User.apply _).tupled, User.unapply _)
}

object Users extends DAO[User, Users] with UserDAOLike {
  val query = TableQuery[Users]

  def findByUsernameAndPassword(username: String, password: String)(implicit s: Session) = {
    val user = query.filter(u => u.username === username).firstOption

    user.flatMap(u => {
      if (u.hashed && u.password == hashPassword(password)
        || !u.hashed && u.password == password) {
        Some(u)
      } else {
        None
      }
    })
  }

  private def hashPassword(password: String) = {
    val md = java.security.MessageDigest.getInstance("SHA-1")
    md.update(password.getBytes)
    play.api.Play.current.configuration.getString("application.secret")
    .foreach(salt => md.update(salt.getBytes))

    md.digest.foldLeft("")((s, b) => s + "%02x".format(if(b < 0) b + 256 else b))
  }

  def changePassword(username: String, oldPassword: String, newPassword: String)(implicit s: Session) = {
    query.filter(u => u.username === username && u.password === oldPassword)
    .map(r => (r.password, r.hashed)).update((hashPassword(newPassword), true))
  }
}
