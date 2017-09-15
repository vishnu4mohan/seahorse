/**
 * Copyright 2015, deepsense.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.deepsense.deeplang.doperables

import org.apache.spark.mllib.linalg.{Vector => LinAlgVector}
import org.apache.spark.mllib.regression.RidgeRegressionModel

import io.deepsense.deeplang.DOperable
import io.deepsense.deeplang.doperables.machinelearning.LinearRegressionParameters
import io.deepsense.deeplang.doperables.machinelearning.ridgeregression.TrainedRidgeRegression

class TrainedRidgeRegressionSpec extends BaseTrainedLinearRegressionSpec {

  override def scorableName: String = "TrainedRidgeRegression"

  override def createRegression(
      params: LinearRegressionParameters,
      featureColumns: Seq[String],
      targetColumn: String,
      weights: LinAlgVector,
      intercept: Double): DOperable = {

    val model = new RidgeRegressionModel(weights, intercept)
    TrainedRidgeRegression(params, model, featureColumns, targetColumn, null)
  }

  override def scorable: Scorable = new TrainedRidgeRegression()
}
