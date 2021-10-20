def call(Map deployparams) {
pipeline {
    agent any

    parameters {
        string(name: 'NAME', description: 'Please tell me your IP?')
        string(name: 'GIT', description: 'Please enter your git url')
    }
        

    stages {
        stage("Git checkout") {
            steps {
                echo "checkout the code from bitbucket"
                checkout([$class: 'GitSCM', branches: [[name: '*/master']], extensions: [], userRemoteConfigs: [[url: 'https://github.com/vennavenkatesh/devops-sample.git']]])
            }
        }

        stage("Test-cases & Build") {
            steps {
                echo "executing the test cases"
                sh "cd $WORKSPACE && mvn clean install"
            }
        }

        stage("Backup") {
            steps {
                echo "Print the workspave path"
                echo "Hello ${params.NAME}"
                sh "echo $WORKSPACE"
                sh '''
                #!/bin/bash
                ssh tomcat@$NAME << EOF
                cd /opt/apache-tomcat-8.5.72/webapps
                mv *.war /opt/apache-tomcat-8.5.72/backup
                exit 0
                EOF'''
            }

        }

        stage("Deploy-tomcat-server") {
            steps {
                echo "Deploy into tomcat server"
                sh 'scp -r $WORKSPACE/target/*.war tomcat@${params.ip}:/opt/apache-tomcat-8.5.72/webapps'
                echo " Deployment has been completed successfully"
            }    
        }
    }
}
}

