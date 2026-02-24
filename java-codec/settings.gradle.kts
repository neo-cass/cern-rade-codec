pluginManagement {
    repositories {
        // the CBNG plugin repo
        maven {url = uri("https://artifactory.cern.ch/gradle") }
        maven {url = uri("https://artifactory.cern.ch/release")}

        mavenCentral()
    }
//    plugins {
//        id ("cern.devops.cbng.dependency") version ${cbngVersion}
//        id ("cern.devops.cbng.publication") version ${cbngVersion}
//    }
}

rootProject.name = "codec"
