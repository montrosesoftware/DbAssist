new GroovyShell().parse( new File( 'callMaven.groovy' ) ).with {

    def goals = "clean deploy -P release"

    callMaven("DbAssist-hbm-3.3.2",  goals)
    callMaven("DbAssist-hbm-3.6.10",  goals)
    callMaven("DbAssist-4.2.21",  goals)
    callMaven("DbAssist-4.3.11",  goals)
    callMaven("DbAssist-5.0.10",  goals)
    callMaven("DbAssist-5.1.1",  goals)
    callMaven("DbAssist-5.2.2",  goals)

    callMaven("DbAssist-jpa-commons",  goals)
}
