package com.github.jelmerk.knn.scalalike

import java.io.{File, OutputStream}
import java.nio.file.Path

import scala.collection.JavaConverters._
import com.github.jelmerk.knn.{Index => JIndex, ProgressListener => JProgressListener}

/**
  * Adapts the interface of a scala Index to that of the java index.
  *
  * @param delegate java class this adapter class delegates to
  * @tparam TId Type of the external identifier of an item
  * @tparam TVector Type of the vector to perform distance calculation on
  * @tparam TItem Type of items stored in the index
  * @tparam TDistance Type of distance between items (expect any numeric type: float, double, int, ..)
  */
@SerialVersionUID(1L)
class ScalaIndexAdapter[TId, TVector, TItem <: Item[TId, TVector], TDistance](val delegate: JIndex[TId, TVector, TItem, TDistance])
  extends Index[TId, TVector, TItem, TDistance] {

  /**
    * @inheritdoc
    */
  override def add(item: TItem): Boolean = delegate.add(item)

  /**
    * @inheritdoc
    */
  override def addAll(items: Iterable[TItem],
                      numThreads: Int = Runtime.getRuntime.availableProcessors,
                      listener: ProgressListener = (_, _) => (),
                      progressUpdateInterval: Int = JIndex.DEFAULT_PROGRESS_UPDATE_INTERVAL): Unit = {
    delegate
      .addAll(items.asJavaCollection, numThreads, new ScalaProgressListenerAdapter(listener), progressUpdateInterval)
  }

  /**
    * @inheritdoc
    */
  override def remove(id: TId, version: Long): Boolean = delegate.remove(id, version)

  /**
    * @inheritdoc
    */
  override def size: Int = delegate.size

  /**
    * @inheritdoc
    */
  override def apply(id: TId): TItem = get(id).getOrElse(throw new NoSuchElementException)

  /**
    * @inheritdoc
    */
  override def get(id: TId): Option[TItem] = Option(delegate.get(id).orElse(null.asInstanceOf[TItem]))

  /**
    * @inheritdoc
    */
  override def iterator: Iterator[TItem] = delegate.items().asScala.iterator

  /**
    * @inheritdoc
    */
  override def findNearest(vector: TVector, k: Int): Seq[SearchResult[TItem, TDistance]] =
    delegate.findNearest(vector, k).asScala

  /**
    * @inheritdoc
    */
  override def findNeighbors(id: TId, k: Int): Seq[SearchResult[TItem, TDistance]] =
    delegate.findNeighbors(id, k).asScala

  /**
    * @inheritdoc
    */
  override def save(out: OutputStream): Unit = delegate.save(out)

  /**
    * @inheritdoc
    */
  override def save(file: File): Unit = delegate.save(file)

  /**
    * @inheritdoc
    */
  override def save(path: Path): Unit = delegate.save(path)

}

/**
  * Adapts the interface of a scala progress listener to that of a java progress listener
  *
  * @param delegate the scala progress listener to delegate to
  */
class ScalaProgressListenerAdapter(val delegate: ProgressListener) extends JProgressListener {

  /**
    * @inheritdoc
    */
  override def updateProgress(workDone: Int, max: Int): Unit = delegate(workDone, max)
}