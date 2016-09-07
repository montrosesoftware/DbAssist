new GroovyShell().parse(new File('callMaven.groovy')).with {

    def goals = "clean install"

    //Montrose Software's HBM libs
    callMaven("DbAssist-hbm-3.3.2", goals)
    callMaven("DbAssist-hbm-3.6.10", goals)

    //Montrose Software's JPA & HBM libs
    callMaven("DbAssist-4.2.21", goals)
    callMaven("DbAssist-4.3.11", goals)
    callMaven("DbAssist-5.0.10", goals)
    callMaven("DbAssist-5.1.1", goals)
    callMaven("DbAssist-5.2.2", goals)

    //Montrose Software's JPA query building libs
    callMaven("DbAssist-jpa-commons", goals)

    //Montrose Software's testing framework
    callMaven("DbAssist-test-commons", goals)
    callMaven("MavenTestAll", goals)
    callMaven("DbAssist-jpa-commons", goals)

    //Run tests against all of the fix libs
    callMaven("DbAssist-jpa-tester", "MavenTestAll:testAll")
    callMaven("DbAssist-hbm-tester", "MavenTestAll:testAll")
}


