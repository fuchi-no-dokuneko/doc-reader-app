package app.docreader

fun <T> androidXml(action: () -> T): T {
    val key="javax.xml.parsers.DocumentBuilderFactory"
    val previous=System.getProperty(key)
    System.setProperty(key,"org.apache.harmony.xml.parsers.DocumentBuilderFactoryImpl")
    try { return action() }
    finally { if (previous==null) System.clearProperty(key) else System.setProperty(key,previous) }
}
