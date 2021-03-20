package org.daviscale.client.components

import org.daviscale.client.CssSettings._

object GlobalStyles extends StyleSheet.Inline {
  import dsl._

  style(unsafeRoot("body")(
    paddingTop(70.px))
  )

  val bootstrapStyles = new BootstrapStyles
}
