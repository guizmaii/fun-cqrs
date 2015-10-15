package shop.app

import akka.actor.Actor
import akka.persistence.query.PersistenceQuery
import akka.persistence.query.journal.leveldb.scaladsl.LeveldbReadJournal
import akka.stream.scaladsl.Source
import io.strongtyped.funcqrs.akka.EventsSourceProvider
import io.strongtyped.funcqrs.{DomainEvent, Tag}

trait LevelDbProjectionSource extends EventsSourceProvider {
  this: Actor =>

  def tag: Tag

  def source(offset: Long): Source[DomainEvent, Unit] = {

    val readJournal =
      PersistenceQuery(context.system)
        .readJournalFor[LeveldbReadJournal](LeveldbReadJournal.Identifier)

    // will always read from start!!
    readJournal.eventsByTag(tag.value, offset).map { env =>
      // and this will blow up if something different than a DomainEvent comes in!!
      env.event match {
        case evt: DomainEvent => evt
        case unexpected       => sys.error(s"Oeps!! That's was totally unexpected $unexpected")
      }
    }
  }

}