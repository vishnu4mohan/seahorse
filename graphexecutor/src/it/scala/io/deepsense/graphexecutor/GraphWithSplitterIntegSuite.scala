/**
 * Copyright (c) 2015, CodiLime Inc.
 */

package io.deepsense.graphexecutor

import io.deepsense.deeplang.doperations.{DataFrameSplitter, LoadDataFrame, SaveDataFrame}
import io.deepsense.graph.Edge

class GraphWithSplitterIntegSuite extends GraphExecutionIntegSuite {

  def experimentName = "(LoadDF, Split, 2xSaveDF)"

  import io.deepsense.deeplang.doperations.LoadDataFrame._
  val loadOp = new LoadDataFrame
  loadOp.parameters.getStringParameter(idParam).value =
    Some(SimpleGraphExecutionIntegSuiteEntities.entityUuid)

  val splitOp = new DataFrameSplitter
  splitOp.parameters.getNumericParameter(splitOp.splitRatioParam).value = Some(0.2)
  splitOp.parameters.getNumericParameter(splitOp.seedParam).value = Some(1)

  import io.deepsense.deeplang.doperations.SaveDataFrame._
  val saveOpLeft = new SaveDataFrame
  saveOpLeft.parameters.getStringParameter(nameParam).value = Some("left name")
  saveOpLeft.parameters.getStringParameter(descriptionParam).value = Some("left description")

  val saveOpRight = new SaveDataFrame
  saveOpRight.parameters.getStringParameter(nameParam).value = Some("right name")
  saveOpRight.parameters.getStringParameter(descriptionParam).value = Some("right description")

  val nodes = Seq(node(loadOp), node(splitOp), node(saveOpLeft), node(saveOpRight))

  val edges = Seq(
    Edge(nodes(0), 0, nodes(1), 0),
    Edge(nodes(1), 0, nodes(2), 0),
    Edge(nodes(1), 1, nodes(3), 0)
  )
}
