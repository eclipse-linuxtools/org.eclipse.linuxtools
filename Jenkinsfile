 pipeline {
	options {
		timeout(time: 90, unit: 'MINUTES')
		buildDiscarder(logRotator(numToKeepStr:'5'))
	}
  agent {
    kubernetes {
      label 'linuxtools-buildtest-pod-' + env.BUILD_NUMBER
      defaultContainer 'jnlp'
      yaml """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: container
    image: akurtakov/linuxtools-build-test-dependencies:latest
    tty: true
    command: [ "uid_entrypoint", "cat" ]
    resources:
      requests:
        memory: "2Gi"
        cpu: "1"
      limits:
        memory: "2Gi"
        cpu: "1"
  - name: jnlp
    image: 'eclipsecbi/jenkins-jnlp-agent'
    volumeMounts:
    - name: volume-known-hosts
      mountPath: /home/jenkins/.ssh
    - name: settings-xml
      mountPath: /home/jenkins/.m2/settings.xml
      subPath: settings.xml
      readOnly: true
    - name: m2-repo
      mountPath: /home/jenkins/.m2/repository
  volumes:
  - name: volume-known-hosts
    configMap:
      name: known-hosts
  - name: settings-xml
    secret:
      secretName: m2-secret-dir
      items:
      - key: settings.xml
        path: settings.xml
  - name: m2-repo
    emptyDir: {}
"""
    }
  }
  environment {
        MAVEN_OPTS = "-Xmx2G"
        JAVA_HOME = "/usr/lib/jvm/java-11-openjdk"
  }
	stages {
		stage('Prepare-environment') {
			steps {
				container('container') {
					sh 'mutter --replace --sm-disable &'
				}
			}
		}
		stage('Build') {
			steps {
				container('container') {
					wrap([$class: 'Xvnc', useXauthority: true]) {
						sh 'mvn clean verify -Pbuild-server -Dmaven.test.failure.ignore=true -ntp -Ddash.fail=true'
					}
				}
			}
			post {
				always {
					junit '**/*.test*/target/surefire-reports/*.xml'
					archiveArtifacts artifacts: '**/*.log,**/*.html,**/screenshots/*.png'
				}
			}
		}
		stage('Deploy') {
			when {
				branch 'stable-8.6'
			}
            steps {
                sshagent ( ['projects-storage.eclipse.org-bot-ssh']) {
                    sh 'ssh genie.linuxtools@projects-storage.eclipse.org rm -rf /home/data/httpd/download.eclipse.org/linuxtools/updates-nightly-8.6/*'
                    sh 'ssh genie.linuxtools@projects-storage.eclipse.org rm -rf /home/data/httpd/download.eclipse.org/linuxtools/updates-docker-nightly-5.6/*'
                    sh 'scp -r releng/org.eclipse.linuxtools.releng-site/target/repository/* genie.linuxtools@projects-storage.eclipse.org:/home/data/httpd/download.eclipse.org/linuxtools/updates-nightly-8.6/'
                    sh 'scp -r releng/org.eclipse.linuxtools.docker-site/target/repository/* genie.linuxtools@projects-storage.eclipse.org:/home/data/httpd/download.eclipse.org/linuxtools/updates-docker-nightly-5.6/'
                }
            }
        }
	}
}
