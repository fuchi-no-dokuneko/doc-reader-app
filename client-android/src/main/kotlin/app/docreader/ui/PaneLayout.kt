package app.docreader.ui

import android.view.View
import app.docreader.domain.LayoutSpec

object PaneLayout {
    fun apply(pane: ReaderPane, stage: View, model: ReaderModel, id: String) {
        if (stage.width<80 || stage.height<120) return
        val metrics=pane.resources.displayMetrics
        val scale=android.util.TypedValue.applyDimension(android.util.TypedValue.COMPLEX_UNIT_SP,1f,metrics)
        model.loading.layout(id,LayoutSpec(stage.width,stage.height,metrics.density,scale,model.state.value.settings))
    }
}
