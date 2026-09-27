package app.docreader.render

object SyntaxWords {
    val words = ("abstract alignas and as assert async await auto begin bool boolean break byte case catch char class " +
        "const constexpr continue data def default defer del do double elif else end enum except export extends extern false final finally " +
        "float fn for foreach from fun function go goto if impl import in inline int interface internal is lambda let local long match " +
        "module namespace new nil none null object of or override package pass private protected protocol pub public raise readonly " +
        "record ref require return sealed self short sizeof static string struct super suspend switch synchronized template then this " +
        "throw throws trait true try type typedef typeof union unless unsigned use using val var virtual void volatile when where while with yield " +
        "select insert update delete create alter drop table into values join left right inner outer on group order by limit having distinct " +
        "set primary key foreign references not exists count sum asc desc union all").split(' ').toSet()
}
