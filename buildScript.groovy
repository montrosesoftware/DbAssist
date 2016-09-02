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
callCleanInstall("DbAssist-hbm-3.3.2.GA")
callCleanInstall("DbAssist-hbm-3.6.10")
callCleanInstall("DbAssist-hbm-4.2.21")
callCleanInstall("DbAssist-hbm-4.3.11")
callCleanInstall("DbAssist-hbm-5.0.10")
callCleanInstall("DbAssist-hbm-5.1.1")
callCleanInstall("DbAssist-hbm-5.2.2")

//Montrose Software's JPA libs
callCleanInstall("DbAssist-jpa-4.2.21")
callCleanInstall("DbAssist-jpa-4.3.11")
callCleanInstall("DbAssist-jpa-5.0.10")
callCleanInstall("DbAssist-jpa-5.1.1")
callCleanInstall("DbAssist-jpa-5.2.2")

//Montrose Software's JPA query building libs
callCleanInstall("DbAssist-jpa-commons")

//Montrose Software's testing framework
callCleanInstall("DbAssist-Commons")
callCleanInstall("MavenBuildAllPlugin")
callCleanInstall("DbAssist-jpa-commons")

callMaven("DbAssist-jpa-testcases", "MavenBuildAllPlugin:buildAll")
callMaven("DbAssist-hbm-tester", "MavenBuildAllPlugin:buildAll")



