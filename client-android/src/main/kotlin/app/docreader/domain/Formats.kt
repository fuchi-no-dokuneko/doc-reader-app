package app.docreader.domain

object Formats {
    val code = ("py pyw pyi c h cpp cc cxx hpp hxx cs go java kt kts js jsx mjs cjs " +
        "ts tsx swift m mm rs rb php phtml lua pl pm sh bash zsh fish ps1 psm1 " +
        "r rmd scala sc dart jl ex exs erl hrl hs lhs clj cljs cljc edn fs fsx " +
        "vb vbs f f90 f95 for cob cbl asm s sql groovy gradle cmake make " +
        "html htm css scss sass less vue svelte astro xml xsd xsl svg tex sty " +
        "proto graphql gql wat v sv vhd vhdl sol zig nim d pas pp tcl awk " +
        "bat cmd dockerfile makefile ipy pde ino coffee litcoffee ahk elm ml mli " +
        "nix hcl tf tfvars bzl bazel cu cuh nu el lisp lsp scm ss rkt y l vba applescript").split(' ').toSet()
    val config = "config conf cfg ini toml properties env rc desktop service settings gitignore gitattributes editorconfig npmrc yarnrc lock".split(' ').toSet()
    fun detect(name: String, mime: String? = null): Kind {
        val lower = name.lowercase(java.util.Locale.ROOT)
        val ext = lower.substringAfterLast('.', lower)
        return when (ext) {
            "pdf" -> Kind.PDF; "epub" -> Kind.EPUB
            "ipynb", "ipybn" -> Kind.NOTEBOOK
            "md", "markdown", "mdown" -> Kind.MARKDOWN
            "json", "jsonl", "ndjson", "geojson", "jsonc" -> Kind.JSON
            "yaml", "yml" -> Kind.YAML
            "docx" -> Kind.DOCX; "xlsx", "xlsm" -> Kind.XLSX
            "pptx" -> Kind.PPTX; "doc" -> Kind.DOC
            "xls" -> Kind.XLS; "ppt" -> Kind.PPT
            "rtf" -> Kind.RTF; "csv", "tsv" -> Kind.CSV
            "html", "htm", "xhtml" -> Kind.HTML
            in config -> Kind.CONFIG
            in code -> Kind.CODE
            else -> when {
                lower.startsWith('.') -> Kind.CONFIG
                lower in setOf("dockerfile", "makefile", "gemfile", "rakefile") -> Kind.CODE
                mime == "application/pdf" -> Kind.PDF
                mime == "application/epub+zip" -> Kind.EPUB
                mime?.contains("json") == true -> Kind.JSON
                mime?.contains("yaml") == true -> Kind.YAML
                else -> Kind.TEXT
            }
        }
    }
    fun language(name: String) = name.substringAfterLast('.', name).lowercase(java.util.Locale.ROOT)
}
