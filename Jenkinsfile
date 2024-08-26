 pipeline {
	options {
		timeout(time: 90, unit: 'MINUTES')
		buildDiscarder(logRotator(numToKeepStr:'5'))
		disableConcurrentBuilds(abortPrevious: true)
	}
  agent {
    kubernetes {
      inheritFrom 'linuxtools-buildtest-pod'
      defaultContainer 'jnlp'
      yaml """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: container
    image: akurtakov/linuxtools-build-test-dependencies:latest
    imagePullPolicy: "Always"
    tty: true
    command: [ "cat" ]
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
  }
	stages {
		stage('Build') {
			steps {
				container('container') {
				withCredentials([file(credentialsId: 'secret-subkeys.asc', variable: 'KEYRING'),string(credentialsId: 'gpg-passphrase', variable: 'KEYRING_PASSPHRASE') ]) {
					wrap([$class: 'Xvnc', useXauthority: true]) {
						sh '''mvn -e -Psign -Dmaven.test.failure.ignore=true -ntp -Ddash.fail=true -Dgpg.passphrase="${KEYRING_PASSPHRASE}" -Dtycho.pgp.signer.bc.secretKeys="${KEYRING}" clean verify'''
					}
				}}
			}
			post {
				always {
					junit '**/*.test*/target/surefire-reports/*.xml'
					archiveArtifacts artifacts: '**/*.log,**/*.html,**/screenshots/*.png,**/target/repository/*'
					recordIssues publishAllIssues: true, tools: [mavenConsole(), java(), eclipse(), javaDoc()]
				}
			}
		}
		stage('Deploy') {
			when {
				branch 'stable-8.16'
			}
            steps {
                sshagent ( ['projects-storage.eclipse.org-bot-ssh']) {
                    sh 'ssh genie.linuxtools@projects-storage.eclipse.org rm -rf /home/data/httpd/download.eclipse.org/linuxtools/updates-nightly-8.16/*'
                    sh 'ssh genie.linuxtools@projects-storage.eclipse.org rm -rf /home/data/httpd/download.eclipse.org/linuxtools/updates-docker-nightly-5.16/*'
                    sh 'scp -r releng/org.eclipse.linuxtools.releng-site/target/repository/* genie.linuxtools@projects-storage.eclipse.org:/home/data/httpd/download.eclipse.org/linuxtools/updates-nightly-8.16/'
                    sh 'scp -r releng/org.eclipse.linuxtools.docker-site/target/repository/* genie.linuxtools@projects-storage.eclipse.org:/home/data/httpd/download.eclipse.org/linuxtools/updates-docker-nightly-5.16/'
                }
            }
        }
	}
}
