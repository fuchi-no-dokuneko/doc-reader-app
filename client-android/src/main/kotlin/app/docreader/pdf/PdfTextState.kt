package app.docreader.pdf

data class PdfTextState(var transform: PdfMatrix = PdfMatrix(), var matrix: PdfMatrix = PdfMatrix(),
    var line: PdfMatrix = PdfMatrix(), var font: String = "", var size: Float = 12f,
    var leading: Float = 0f, var scale: Float = 1f, var rise: Float = 0f,
    var characterSpace: Float = 0f, var wordSpace: Float = 0f) {
    fun move(x: Float, y: Float) { line = PdfMatrix(e=x,f=y).then(line); matrix = line }
    fun advance(x: Float) { matrix = PdfMatrix(e=x).then(matrix) }
}
object PdfTextOperators {
    fun apply(op: String, args: List<PdfValue>, state: PdfTextState, show: (PdfString) -> Unit) {
        fun n(i: Int) = args.getOrNull(i).number().toFloat()
        fun nextLine() = state.move(0f,-state.leading)
        when (op) {
            "BT" -> { state.matrix = PdfMatrix(); state.line = PdfMatrix() }
            "Tf" -> { state.font = (args.firstOrNull() as? PdfName)?.name.orEmpty(); state.size = n(1) }
            "Tm" -> { state.matrix = PdfMatrix.of(args); state.line = state.matrix }
            "Td" -> state.move(n(0),n(1))
            "TD" -> { state.leading = -n(1); state.move(n(0),n(1)) }
            "T*" -> nextLine()
            "TL" -> state.leading = n(0)
            "Tz" -> state.scale = n(0)/100
            "Ts" -> state.rise = n(0)
            "Tc" -> state.characterSpace = n(0)
            "Tw" -> state.wordSpace = n(0)
            "Tj" -> (args.lastOrNull() as? PdfString)?.let(show)
            "TJ" -> args.firstOrNull().array().forEach {
                if (it is PdfString) show(it) else state.advance(-it.number().toFloat()*state.size*state.scale/1000)
            }
            "'" -> { nextLine(); (args.lastOrNull() as? PdfString)?.let(show) }
            "\"" -> { state.wordSpace = n(0); state.characterSpace = n(1); nextLine(); (args.lastOrNull() as? PdfString)?.let(show) }
        }
    }
}
