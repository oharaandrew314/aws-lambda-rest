import com.beust.kobalt.*
import com.beust.kobalt.plugin.packaging.assemble
import com.beust.kobalt.plugin.publish.bintray

val awsLambdaRest = project {
    name = "aws-lambda-rest"
    group = "io.andrewohara"
    artifactId = name
    version = "0.3.1"
    directory = name

    dependencies {
        compile(
                "org.jetbrains.kotlin:kotlin-stdlib:[1.2.0,)",
                "com.amazonaws:aws-lambda-java-core:[1.2.0,)",
                "com.amazonaws:aws-lambda-java-events:[2.0.1,)",
                "com.beust:klaxon:[2.1.2,)"
        )
    }

    dependenciesTest {
        compile("junit:junit:4.12")
    }

    assemble {
        mavenJars {}
    }

    bintray {
        publish = true

    }
}

val petsExample = project {
    name = "example-pets"
    directory = name
    version  = "0.0.1"

    dependsOn(awsLambdaRest)

    assemble {
        jar {
            fatJar = true
        }
    }
}