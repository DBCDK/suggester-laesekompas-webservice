def dockerRepository = 'https://docker-de.artifacts.dbccloud.dk'

if (env.BRANCH_NAME == 'master') {

    properties([
            pipelineTriggers([
                    triggers: [
                            [
                                    $class          : 'jenkins.triggers.ReverseBuildTrigger',
                                    upstreamProjects: "Docker-payara6-bump-trigger", threshold: hudson.model.Result.SUCCESS
                            ]
                    ]
            ]
            )
    ])
}
pipeline {
    agent { label "devel10" }
    tools {
        maven "Maven 3"
        jdk "jdk11"
    }
    environment {
        MAVEN_OPTS = "-XX:+TieredCompilation -XX:TieredStopAtLevel=1"
    }
    triggers {
        pollSCM("H/3 * * * *")
    }
    options {
        buildDiscarder(logRotator(artifactDaysToKeepStr: "", artifactNumToKeepStr: "", daysToKeepStr: "30", numToKeepStr: "30"))
        timestamps()
    }
    stages {
        stage("Build") {
            steps {
                // Fail Early..
                script {
                    if (! env.BRANCH_NAME) {
                        currentBuild.rawBuild.result = Result.ABORTED
                        throw new hudson.AbortException('Job Started from non-MultiBranch Build')
                    } else {
                        println(" Building BRANCH_NAME == ${BRANCH_NAME}")
                    }
                    def status = sh returnStatus: true, script:  """
                        rm -rf \$WORKSPACE/.repo
                        mvn -B -Dmaven.repo.local=\$WORKSPACE/.repo dependency:resolve dependency:resolve-plugins >/dev/null
                        mvn -B -Dmaven.repo.local=\$WORKSPACE/.repo clean
                        mvn -B -Dmaven.repo.local=\$WORKSPACE/.repo --fail-at-end org.jacoco:jacoco-maven-plugin:prepare-agent install -Dsurefire.useFile=false
                    """

                    // We want code-coverage and pmd/findbugs even if unittests fails
                    status += sh returnStatus: true, script:  """
                        mvn -B -Dmaven.repo.local=\$WORKSPACE/.repo pmd:pmd pmd:cpd spotbugs:spotbugs javadoc:aggregate -Dspotbugs.excludeFilterFile=src/test/spotbugs/spotbugs-exclude.xml
                    """

                    junit testResults: '**/target/*-reports/TEST-*.xml'

                    def java = scanForIssues tool: [$class: 'Java']
                    def javadoc = scanForIssues tool: [$class: 'JavaDoc']
                    publishIssues issues:[java, javadoc], unstableTotalAll:1

                    def pmd = scanForIssues tool: [$class: 'Pmd'], pattern: '**/target/pmd.xml'
                    publishIssues issues:[pmd], unstableTotalAll:1

                    def cpd = scanForIssues tool: [$class: 'Cpd'], pattern: '**/target/cpd.xml'
                    publishIssues issues:[cpd]

                    def spotbugs = scanForIssues tool: [$class: 'SpotBugs'], pattern: '**/target/spotbugsXml.xml'
                    publishIssues issues:[spotbugs], unstableTotalAll:1

                    step([$class: 'JacocoPublisher',
                          execPattern: 'target/*.exec,**/target/*.exec',
                          classPattern: 'target/classes,**/target/classes',
                          sourcePattern: 'src/main/java,**/src/main/java',
                          exclusionPattern: 'src/test*,**/src/test*,**/*?Request.*,**/*?Response.*,**/*?Request$*,**/*?Response$*,**/*?DTO.*,**/*?DTO$*'
                    ])

                    warnings consoleParsers: [
                            [parserName: "Java Compiler (javac)"],
                            [parserName: "JavaDoc Tool"]],
                            unstableTotalAll: "0",
                            failedTotalAll: "0"

                    if ( status != 0 ) {
                        currentBuild.result = Result.FAILURE
                    }
                }
            }
        }
        
        stage('Docker') {
            steps {
                script {
                    def allDockerFiles = findFiles glob: '**/Dockerfile'
                    def dockerFiles = allDockerFiles.findAll { f -> f.path.endsWith("src/main/docker/Dockerfile") }
                    def version = readMavenPom().version

                    for (def f : dockerFiles) {
                        def dirName = f.path.take(f.path.length() - 27)
                        echo "We are looking to run in: $dirName"

                        dir(dirName) {
                            modulePom = readMavenPom file: 'pom.xml'
                            def projectArtifactId = modulePom.getArtifactId()
                            if( !projectArtifactId ) {
                                throw new hudson.AbortException("Unable to find module ArtifactId in ${dirName}/pom.xml remember to add a <ArtifactId> element")
                            }

                            def imageName = "${projectArtifactId}-${version}".toLowerCase()
                            if (! env.CHANGE_BRANCH) {
                                imageLabel = env.BRANCH_NAME
                            } else {
                                imageLabel = env.CHANGE_BRANCH
                            }
                            if ( ! (imageLabel ==~ /master|trunk/) ) {
                                println("Using branch_name ${imageLabel}")
                                imageLabel = imageLabel.split(/\//)[-1]
                                imageLabel = imageLabel.toLowerCase()
                            } else {
                                println(" Using Master branch ${BRANCH_NAME}")
                                imageLabel = env.BUILD_NUMBER
                            }

                            println("In ${dirName} build ${projectArtifactId} as ${imageName}:$imageLabel")
                            def app = docker.build("$imageName:${imageLabel}".toLowerCase(), '--pull --no-cache -f target/docker/Dockerfile .')

                            if (currentBuild.resultIsBetterOrEqualTo('SUCCESS')) {
                                docker.withRegistry(dockerRepository, 'docker') {
                                    app.push()
                                    if (env.BRANCH_NAME ==~ /master|trunk/) {
                                        println("app.push \"latest\"")
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }
    }
    post {
        failure {
            script {
                if ("${env.BRANCH_NAME}" == 'master') {
                    emailext(
                            recipientProviders: [developers(), culprits()],
                            to: "de-team@dbc.dk",
                            subject: "[Jenkins] ${env.JOB_NAME} #${env.BUILD_NUMBER} failed",
                            mimeType: 'text/html; charset=UTF-8',
                            body: "<p>The master build failed. Log attached. </p><p><a href=\"${env.BUILD_URL}\">Build information</a>.</p>",
                            attachLog: true,
                    )
                    slackSend(channel: 'de-notifications',
                            color: 'warning',
                            message: "${env.JOB_NAME} #${env.BUILD_NUMBER} failed and needs attention: ${env.BUILD_URL}",
                            tokenCredentialId: 'slack-global-integration-token')

                } else {
                    // this is some other branch, only send to developer
                    emailext(
                            recipientProviders: [developers()],
                            subject: "[Jenkins] ${env.BUILD_TAG} failed and needs your attention",
                            mimeType: 'text/html; charset=UTF-8',
                            body: "<p>${env.BUILD_TAG} failed and needs your attention. </p><p><a href=\"${env.BUILD_URL}\">Build information</a>.</p>",
                            attachLog: false,
                    )
                }
            }
        }
        success {
            step([$class: 'JavadocArchiver', javadocDir: 'target/site/apidocs', keepAll: false])
        }
    }
}
