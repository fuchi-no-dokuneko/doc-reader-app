package app.docreader.pdf

class PdfContent(private val file: PdfFile, private val page: PdfPageTransform) {
    val text = mutableListOf<PdfText>()
    fun read(stream: PdfStream, resources: PdfDict, initial: PdfTextState = PdfTextState(), depth: Int = 0) {
        readAll(listOf(stream),resources,initial,depth)
    }
    fun readAll(streams: List<PdfStream>, resources: PdfDict, initial: PdfTextState = PdfTextState(), depth: Int = 0) {
        if (depth > 32) return
        val fonts = mutableMapOf<String,PdfFont>()
        var state = initial.copy(); val stack = java.util.ArrayDeque<PdfTextState>()
        fun show(value: PdfString) {
            val font=fonts.getOrPut(state.font) { PdfFont(file,file.dict(file.dict(resources["Font"])[state.font])) }
            PdfTextDraw.show(value,font,state,page)?.let(text::add)
        }
        PdfStreamReader.read(file,streams) { lex ->
            val args = mutableListOf<PdfValue>()
            while (true) {
                val value = lex.value()
                if (value is PdfWord) {
                    val op = value.word; if (op.isEmpty()) break
                    when (op) {
                        "q" -> stack.push(state.copy())
                        "Q" -> if (stack.isNotEmpty()) state = stack.pop()
                        "cm" -> state.transform = PdfMatrix.of(args).then(state.transform)
                        "Do" -> {
                            val name = (args.firstOrNull() as? PdfName)?.name
                            val form = file.resolve(file.dict(resources["XObject"])[name.orEmpty()]) as? PdfStream
                            if (form?.dict?.name("Subtype") == "Form") read(form,
                                file.dict(form.dict["Resources"]).takeIf { it.values.isNotEmpty() } ?: resources,
                                state.copy(transform=PdfMatrix.of(form.dict["Matrix"].array()).then(state.transform)),depth+1)
                        }
                        "ID" -> { var prior = 0; while (true) { val c = lex.cursor.read(); if (c < 0 || prior == 69 && c == 73) break; prior = c } }
                        else -> PdfTextOperators.apply(op,args,state,::show)
                    }; args.clear()
                } else { args += value; require(args.size < 10000) }
            }
        }
    }
}
