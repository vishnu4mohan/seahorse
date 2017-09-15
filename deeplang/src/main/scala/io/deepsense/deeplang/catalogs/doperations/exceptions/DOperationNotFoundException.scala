/**
 * Copyright (c) 2015, CodiLime, Inc.
 *
 * Owner: Witold Jedrzejewski
 */

package io.deepsense.deeplang.catalogs.doperations.exceptions

case class DOperationNotFoundException(operationName: String)
  extends DOperationsCatalogException(s"DOperation not found: ${operationName}")
