package com.montrosesoftware.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.shared.invoker.*;

import java.io.File;
import java.util.Arrays;
import java.util.Properties;

@Mojo( name = "buildAll")
public class BuildAllVersions extends AbstractMojo
{
    public void execute() throws MojoExecutionException
    {
        InvocationRequest request = new DefaultInvocationRequest();
        request.setPomFile( new File( "pom.xml" ) );
        request.setGoals( Arrays.asList( "clean", "test" ) );

        Properties properties = new Properties();
        properties.setProperty("montrosesoftware.version", "DbAssist-jpa-fix");
        request.setProperties(properties);

        Invoker invoker = new DefaultInvoker();
        try {
            invoker.execute( request );
        } catch (MavenInvocationException e) {
            getLog().info(e.getMessage());
        }
    }
}
