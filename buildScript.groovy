def callMaven(path, goals){
    def proc = "cmd /c mvn -f ${path}/pom.xml ${goals}".execute()
    proc.waitForProcessOutput(System.out, System.err)
    def exitCode = proc.exitValue()
    if (exitCode != 0)
        throw new RuntimeException("Build failed")
}

def callCleanInstall(path){
    callMaven(path, "clean install")
}

//Montrose Software's HBM libs
callCleanInstall("DbAssist-hbm-3.3.2")
callCleanInstall("DbAssist-hbm-3.6.10")

//Montrose Software's JPA & HBM libs
callCleanInstall("DbAssist-4.2.21")
callCleanInstall("DbAssist-4.3.11")
callCleanInstall("DbAssist-5.0.10")
callCleanInstall("DbAssist-5.1.1")
callCleanInstall("DbAssist-5.2.2")

//Montrose Software's JPA query building libs
callCleanInstall("DbAssist-jpa-commons")

//Montrose Software's testing framework
callCleanInstall("DbAssist-test-commons")
callCleanInstall("MavenTestAll")
callCleanInstall("DbAssist-jpa-commons")

//Run tests against all of the fix libs
callMaven("DbAssist-jpa-tester", "MavenTestAll:testAll")
callMaven("DbAssist-hbm-tester", "MavenTestAll:testAll")