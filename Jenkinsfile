 pipeline {
	options {
		timeout(time: 90, unit: 'MINUTES')
		buildDiscarder(logRotator(numToKeepStr:'5'))
		disableConcurrentBuilds(abortPrevious: true)
	}
  agent {
    kubernetes {
      label 'linuxtools-buildtest-pod'
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
  tools {
		maven 'apache-maven-latest'
		jdk 'temurin-jdk17-latest'
	}
  environment {
        MAVEN_OPTS = "-Xmx2G"
  }
	stages {
		stage('Prepare-environment') {
			steps {
				container('container') {
					sh 'mutter --replace --sm-disable &'
				}
			}
		}
		stage('Initialize PGP') {
			steps {
				container('container') {
				withCredentials([file(credentialsId: 'secret-subkeys.asc', variable: 'KEYRING')]) {
					sh 'gpg --batch --import "${KEYRING}"'
					sh 'for fpr in $(gpg --list-keys --with-colons  | awk -F: \'/fpr:/ {print $10}\' | sort -u); do echo -e "5\ny\n" |  gpg --batch --command-fd 0 --expert --edit-key ${fpr} trust; done'
				}}
			}
		}
		stage('Build') {
			steps {
				container('container') {
				withCredentials([string(credentialsId: 'gpg-passphrase', variable: 'KEYRING_PASSPHRASE')]) {
					wrap([$class: 'Xvnc', useXauthority: true]) {
						sh 'mvn clean verify -e -Pbuild-server -Dmaven.test.failure.ignore=true -ntp -Ddash.fail=true -Dgpg.passphrase="${KEYRING_PASSPHRASE}"'
					}
				}}
			}
			post {
				always {
					junit '**/*.test*/target/surefire-reports/*.xml'
					archiveArtifacts artifacts: '**/*.log,**/*.html,**/screenshots/*.png,**/target/repository/*'
				}
			}
		}
		stage('Deploy') {
			when {
				branch 'master'
			}
            steps {
                sshagent ( ['projects-storage.eclipse.org-bot-ssh']) {
                    sh 'ssh genie.linuxtools@projects-storage.eclipse.org rm -rf /home/data/httpd/download.eclipse.org/linuxtools/updates-nightly/*'
                    sh 'ssh genie.linuxtools@projects-storage.eclipse.org rm -rf /home/data/httpd/download.eclipse.org/linuxtools/updates-docker-nightly/*'
                    sh 'scp -r releng/org.eclipse.linuxtools.releng-site/target/repository/* genie.linuxtools@projects-storage.eclipse.org:/home/data/httpd/download.eclipse.org/linuxtools/updates-nightly/'
                    sh 'scp -r releng/org.eclipse.linuxtools.docker-site/target/repository/* genie.linuxtools@projects-storage.eclipse.org:/home/data/httpd/download.eclipse.org/linuxtools/updates-docker-nightly/'
                }
            }
        }
	}
}