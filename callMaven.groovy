def callMaven(path, goals) {
    def proc = "cmd /c mvn -f ${path}/pom.xml ${goals}".execute()
    proc.waitForProcessOutput(System.out, System.err)
    def exitCode = proc.exitValue()
    if (exitCode != 0)
        throw new RuntimeException("Maven call execution failed")
}