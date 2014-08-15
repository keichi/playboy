package controllers

import play.api._
import play.api.mvc._
import play.api.db.slick._

import models._
import views._

object Application extends Controller {
  def index = DBAction { implicit s =>

    Ok(views.html.index())
  }
}
