package app.docreader.render

object SyntaxGrammar {
    private val hash="py pyw pyi r rb sh bash zsh fish ps1 psm1 yaml yml toml ini conf config cfg env properties pl pm jl makefile dockerfile nix tf hcl php".split(' ').toSet()
    private val dash=setOf("sql","lua","hs","lhs")
    private val markup=setOf("html","htm","xml","xhtml","svg","vue","svelte","xsd","xsl")
    private val noSlash=hash+markup+dash+setOf("json","jsonl","ndjson","ipynb","txt")-setOf("php","nix","tf","hcl")
    fun comment(text: String,start: Int,lang: String): Int? {
        fun starts(value: String)=text.startsWith(value,start)
        if (lang in markup && starts("<!--")) return until(text,start+4,"-->")
        if ((lang !in noSlash || lang=="sql") && starts("/*")) return until(text,start+2,"*/")
        if (lang=="lua" && starts("--[[")) return until(text,start+4,"]]")
        val line=(lang !in noSlash && starts("//")) || (lang in dash && starts("--")) ||
            ((lang in hash || lang.startsWith('.')) && starts("#"))
        return if (line) text.indexOf('\n',start).let { if (it<0) text.length else it } else null
    }
    fun quoted(text: String,start: Int): Int {
        val quote=text[start]
        val delimiter=quote.toString().repeat(if (text.startsWith(quote.toString().repeat(3),start)) 3 else 1)
        var i=start+delimiter.length
        while (i<text.length) {
            if (text.startsWith(delimiter,i)) return i+delimiter.length
            if (text[i]=='\\') i+=2
            else if (text[i]=='\n' && delimiter.length==1 && quote!='`') return i
            else i++
        }
        return text.length
    }
    private fun until(text: String,start: Int,end: String) = text.indexOf(end,start).let {
        if (it<0) text.length else it+end.length
    }
}
