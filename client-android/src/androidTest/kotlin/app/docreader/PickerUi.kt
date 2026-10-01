package app.docreader

object PickerUi {
    fun open(name: String) {
        NativeUi.click("Open")
        NativeUi.waitFor("Picker did not show $name") {
            when {
                NativeUi.visible("Open from") -> {
                    NativeUi.tryClick("Doc Reader fixtures"); false
                }
                NativeUi.visible(name) -> true
                else -> { NativeUi.tryClick("Show roots"); false }
            }
        }
        NativeUi.click(name)
    }
}
