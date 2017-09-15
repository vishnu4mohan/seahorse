/**
 * Copyright (c) 2016, CodiLime Inc.
 */

package io.deepsense.workflowmanager.storage.impl

import java.util.UUID

import scala.concurrent.{ExecutionContext, Future}

import com.google.inject.Inject
import com.google.inject.name.Named
import slick.driver.JdbcDriver
import slick.lifted.{Index, PrimaryKey, ProvenShape}
import spray.json._

import io.deepsense.commons.datetime.DateTimeConverter
import io.deepsense.graph.Node
import io.deepsense.graph.nodestate.NodeStatus
import io.deepsense.models.json.graph.NodeStatusJsonProtocol
import io.deepsense.models.json.workflow.EntitiesMapJsonProtocol
import io.deepsense.models.workflows.Workflow.Id
import io.deepsense.models.workflows.{EntitiesMap, NodeState}
import io.deepsense.workflowmanager.storage.WorkflowStateStorage

class WorkflowStateDaoImpl @Inject()(
    @Named("workflowmanager") db: JdbcDriver#API#Database,
    @Named("workflowmanager") driver: JdbcDriver)
    (implicit ec: ExecutionContext)
  extends WorkflowStateStorage
  with EntitiesMapJsonProtocol
  with NodeStatusJsonProtocol{

  import driver.api._

  override def get(workflowId: Id): Future[Map[Node.Id, NodeState]] = {
    db.run(workflowStates.filter(_.workflowId === workflowId.value).result)
      .map(_.map(toIdAndNodeState).toMap)
  }

  override def save(workflowId: Id, state: Map[Node.Id, NodeState]): Future[Unit] = {

    def update(nodeId: Node.Id, nodeState: NodeState) = {
      val previousState = db.run(workflowStates.filter(
          s => s.workflowId === workflowId.value && s.nodeId === nodeId.value).result)
      val previousReport = previousState.map {
        case Seq() => None
        case Seq((_, _, _, _, previousReports)) => previousReports
      }

      previousReport.map {
        case previousReports =>
          val reports = nodeState.reports.map(entitiesMapToCell)
          db.run(workflowStates.insertOrUpdate(
            (workflowId.value, nodeId.value, DateTimeConverter.now.getMillis,
              nodeStatusToCell(nodeState.nodeStatus), reports.orElse(previousReports))))
      }
    }

    val futures = state.map {
      case (nodeId, nodeState) => update(nodeId, nodeState)
    }

    Future.sequence(futures).map(_ => ())
  }

  private def toIdAndNodeState(
      row: (UUID, UUID, Long, String, Option[String])): (Node.Id, NodeState) = {
    val (_, nodeId, updateTime, state, reportsJson) = row
    val nodeStatus = state.parseJson.convertTo[NodeStatus]
    val reports = reportsJson map { _.parseJson.convertTo[EntitiesMap] }
    (Node.Id(nodeId), NodeState(nodeStatus, reports))
  }

  private def nodeStatusToCell(nodeStatus: NodeStatus): String =
    nodeStatus.toJson.compactPrint

  private def entitiesMapToCell(entitiesMap: EntitiesMap): String =
    entitiesMap.toJson.compactPrint

  val WorkflowId = "workflow_id"
  val NodeId = "node_id"
  val UpdateTime = "update_time"
  val State = "state"
  val Reports = "reports"

  private class WorkflowStates(tag: Tag)
    extends Table[(UUID, UUID, Long, String, Option[String])](tag, "WORKFLOW_STATES") {

    def workflowId: Rep[UUID] = column[UUID](WorkflowId)
    def nodeId: Rep[UUID] = column[UUID](NodeId)
    def updateTime: Rep[Long] = column[Long](UpdateTime)
    def state: Rep[String] = column[String](State)
    def reports: Rep[Option[String]] = column[Option[String]](Reports)

    def pk: PrimaryKey = primaryKey("pk_workflow_states", (workflowId, nodeId))
    def index: Index = index("idx_workflow_states_workflow_id", workflowId, unique = false)

    def * : ProvenShape[(UUID, UUID, Long, String, Option[String])] =
      (workflowId, nodeId, updateTime, state, reports)
  }

  private val workflowStates = TableQuery[WorkflowStates]

  private[impl] def create(): Future[Unit] = db.run(workflowStates.schema.create)
  private[impl] def drop(): Future[Unit] = db.run(workflowStates.schema.drop)
}
