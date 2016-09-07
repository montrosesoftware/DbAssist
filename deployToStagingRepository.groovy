def callMaven(path, goals) {
    def proc = "cmd /c mvn -f ${path}/pom.xml ${goals}".execute()
    proc.waitForProcessOutput(System.out, System.err)
    def exitCode = proc.exitValue()
    if (exitCode != 0)
        throw new RuntimeException("Build failed")
}

def callDeployRelease(path){
    callMaven(path, "clean deploy -P release")
}

callDeployRelease("DbAssist-hbm-3.3.2")
callDeployRelease("DbAssist-hbm-3.6.10")
callDeployRelease("DbAssist-4.2.21")
callDeployRelease("DbAssist-4.3.11")
callDeployRelease("DbAssist-5.0.10")
callDeployRelease("DbAssist-5.1.1")
callDeployRelease("DbAssist-5.2.2")

callDeployRelease("DbAssist-jpa-commons")
