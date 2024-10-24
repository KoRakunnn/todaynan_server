pipeline {
    agent any
    tools {
        gradle 'gradle'
    }
    environment {
            DOCKERHUB_CREDENTIALS = 'docker'  // DockerHub 자격 증명 ID
            DOCKER_IMAGE_NAME = 'kohyunchoel/todaynan_backend'
            GITHUB_CREDENTIALS = 'github'
            GITHUB_URL = 'https://github.com/KoRakunnn/todaynan_server'
            APPLICATION_PROPERTIES = 'application_properties'
            INSTANCE_SSH_CREDENTIALS_1 = 'instance1'
            INSTANCE_IP_ADDRESS_1 = 'instance1_ip'
            INSTANCE_IP_ADDRESS_2 = 'instance2_ip'
        }
    stages {
        stage('Git Clone') {
            steps {
                git branch: 'main', credentialsId: "$GITHUB_CREDENTIALS", url: "$GITHUB_URL"
            }
        }
        stage('Apply application properties') {
            steps {
                withCredentials([file(credentialsId: "$APPLICATION_PROPERTIES", variable: 'SECRET_FILE')]) {
                    sh 'cp $SECRET_FILE ./src/main/resources/application.properties'
                }
            }
        }

       stage('Build') {
           steps {
               sh "./gradlew clean build -x test"
           }
       }


        stage('Docker Build') {
            steps {
                 dir("/var/jenkins_home/workspace/todaynan") {
                     sh '''
                         echo 'Building Docker image...'
                         docker build -t $DOCKER_IMAGE_NAME:latest .
                     '''
                 }
            }
        }

        stage('Docker Hub Push') {
            steps {
                withCredentials([usernamePassword(credentialsId: "${DOCKERHUB_CREDENTIALS}", passwordVariable: 'DOCKER_PASSWORD', usernameVariable: 'DOCKER_USERNAME')]) {
                    sh '''
                        echo 'Logging into DockerHub...'
                        echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin
                        docker push $DOCKER_IMAGE_NAME:latest
                    '''
                }
            }
        }

        stage('Deploy to Instance') {
            steps {
                    script {
                        def instances = [
                            [ip: "${INSTANCE_IP_ADDRESS_1}", credentials: "${INSTANCE_SSH_CREDENTIALS_1}"],
                            [ip: "${INSTANCE_IP_ADDRESS_2}", credentials: "${INSTANCE_SSH_CREDENTIALS_1}"]
                        ]

                        instances.each { instance ->
                            withCredentials([string(credentialsId: instance.ip, variable: 'INSTANCE_IP_ADDRESS')]) {
                                sshagent(credentials: [instance.credentials]) {
                                    sh '''
                                        echo "Deploying Docker container on Oracle instance ${INSTANCE_IP_ADDRESS}..."
                                        ssh -o StrictHostKeyChecking=no ubuntu@${INSTANCE_IP_ADDRESS} "
                                        sudo docker pull $DOCKER_IMAGE_NAME:latest && \
                                        sudo docker stop spring || true && \
                                        sudo docker rm spring || true && \
                                        sudo docker run -d --name spring -p 8080:8080 $DOCKER_IMAGE_NAME:latest
                                        "
                                    '''
                                }
                            }
                        }
                    }
                }

        }



    }
}
