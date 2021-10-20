def call(Map deployparams) {
pipeline {
    agent any

    parameters {
        choice choices: ['Deployment', 'Rollback'], description: 'Make a choice', name: 'CLICK'
        string(name: 'IP', description: 'Please tell me your IP?')
        string(name: 'GIT', description: 'Please enter your git url')
        string(name: 'branch_name', description: 'Please enter your branch name')
    }
        

    stages {
        stage("Git checkout") {
            steps {
                echo "checkout the code from bitbucket"
                git branch: "${params.branch_name}", url: "${params.GIT}" , poll: true
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
                echo "Hello ${params.IP}"
                sh "echo $WORKSPACE"
                sh '''
                #!/bin/bash
                ssh tomcat@$IP << EOF
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

