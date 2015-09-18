package shop.api

import fun.cqrs.Repository
import play.api.libs.json.{Writes, JsValue, Json}
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext.Implicits.global

trait ViewController {
  this: Controller with RestRecovery =>

  type ViewRepo <: Repository

  val viewRepo: ViewRepo

  def toAggregateId(id: String): viewRepo.Identifier

  implicit def viewModelWrites: Writes[ViewRepo#Model]

  def get(id: String) = Action.async {
    val viewFuture = viewRepo.find(toAggregateId(id))
    viewFuture.map { view =>
      Ok(Json.toJson(view))
    }.recover(recoverRest)
  }

  def list = Action.async {
    viewRepo.fetchAll.map { views =>
      Ok(Json.toJson(views))
    }
  }

}