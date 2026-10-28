plugins {
    id ("java-library")
    id ("cern.devops.cbng.dependency")
    id("cern.devops.cbng.publication")
    id("cern.devops.cbng.project")
}

configurations.configureEach {
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation ("cern.japc:japc:PRO")
    implementation ("cern.japc:japc-ext-cmwrda3:PRO")
    implementation ("cern.japc:japc-value:PRO")
    implementation ("cern.japc:japc-ext-remote:PRO")
    implementation ("cern.japc:japc-json:PRO")
    implementation ("com.fasterxml.jackson.core:jackson-databind")
    implementation ("cern.cmw:cmw-data:PRO")
    implementation("org.apache.commons:commons-lang3:3.18.0")
    implementation("jakarta.servlet:jakarta.servlet-api:5.0.0")
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation("cern.rade.services:common:refactor-SNAPSHOT")

}


//    implementation(files("/Users/ncass/workspace/common/build/libs/common-test.jar"))
//    implementation(files("C:\\Users\\ncass\\workspace\\common\\build\\libs\\common-test.jar"))

tasks.named<Jar>("jar") {
    val jarName = project.findProperty("jarName")
    if(jarName!=null){
        archiveFileName.set(jarName as String)
    }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21

    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))

    }
    withJavadocJar()
    withSourcesJar()
}
