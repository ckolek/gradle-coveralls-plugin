# gradle-coveralls-plugin
#### Gradle plugin to upload coverage to [Coveralls](http://coveralls.io)

[![Build Status](https://travis-ci.org/ckolek/gradle-coveralls-plugin.svg?branch=master)](https://travis-ci.org/ckolek/gradle-coveralls-plugin)
[![Coverage Status](https://coveralls.io/repos/github/ckolek/gradle-coveralls-plugin/badge.svg?branch=master)](https://coveralls.io/github/ckolek/gradle-coveralls-plugin?branch=master)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/me.kolek.gradle/gradle-coveralls-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/me.kolek.gradle/gradle-coveralls-plugin)

## Usage

1. Make plugin available to Gradle build script
    ```groovy
    buildscript {
        repositories {
            mavenCentral()
        }
        
        dependencies {
            classpath 'me.kolek.gradle:gradle-coveralls-plugin:1.0.0'
        }
    }
    ```
    
2. Apply plugin to Gradle project
    ```groovy
    apply plugin: 'me.kolek.coveralls'
    ```

3. Configure Coveralls plugin extension
    ```groovy
    coveralls {
        // use repo token from COVERALLS_REPO_TOKEN environment variable
        repoToken System.env.COVERALLS_REPO_TOKEN
        
        // upload JaCoCo coverage data
        jacoco()
        
        services {
            // use Codeship build environment values
            codeship()
            
            // use Travis CI build environment values
            travis()
            
            // use custom/local build environment values
            custom {
                // ...
            }
        }
    }
    ```

4. Run tests, generate and upload code coverage
    ```
    gradle test jacocoTestReport uploadCodeCoverage
    ```
