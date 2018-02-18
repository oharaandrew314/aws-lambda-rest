import com.beust.kobalt.*
import com.beust.kobalt.plugin.packaging.assemble
import com.beust.kobalt.plugin.publish.bintray

object Version {
    const val kotlin = "1.2.21"
    const val lambdaCore = "1.2.0"
    const val lambdaEvents = "2.0.1"
    const val klaxon = "2.1.6"
}

val awsLambdaRest = project {
    name = "aws-lambda-rest"
    group = "io.andrewohara"
    artifactId = name
    version = "0.0.1"
    directory = name

    dependencies {
        compile(
                "org.jetbrains.kotlin:kotlin-stdlib:${Version.kotlin}",
                "com.amazonaws:aws-lambda-java-core:${Version.lambdaCore}",
                "com.amazonaws:aws-lambda-java-events:${Version.lambdaEvents}",
                "com.beust:klaxon:${Version.klaxon}"
        )
    }

    dependenciesTest {
        compile("junit:junit:4.12")
    }

    assemble {
        mavenJars {
        }
    }

    bintray {
        publish = true
    }
}

val petsExample = project {
    name = "example-pets"
    group = "io.andrewohara"
    artifactId = name
    version = "0.0.1"
    directory = name

    dependsOn(awsLambdaRest)

    dependencies {
        compile(
                "org.jetbrains.kotlin:kotlin-stdlib:${Version.kotlin}",
                "com.amazonaws:aws-lambda-java-core:${Version.lambdaCore}",
                "com.amazonaws:aws-lambda-java-events:${Version.lambdaEvents}",
                "com.beust:klaxon:${Version.klaxon}"
        )
    }

    assemble {
        jar {
            fatJar = true
        }
    }
}