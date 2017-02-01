//Codedeploy Config
import static java.util.UUID.randomUUID 
import groovy.json.*


node {
    env.AWS_DEFAULT_REGION = 'ap-southeast-1'
	
	String stackName = 'cdc-fajar'

	deleteDir()
	stage('Checkout') {
		checkout scm	
	}
	
	
	stage("Sonar Analyze") {

	}
	//deploy
	stage('Deploy') {
		withCredentials([[$class          : 'UsernamePasswordMultiBinding',
                      credentialsId   : 'CdcAWS',
                      usernameVariable: 'AWS_ACCESS_KEY_ID',
                      passwordVariable: 'AWS_SECRET_ACCESS_KEY']]) {
	        
	        String applicationId = randomUUID()

	        //ZIP
	        sh("zip -r ${applicationId}.zip .")
	        
	        //Upload
	        sh("aws s3 cp ${applicationId}.zip s3://deployment-cdc/${applicationId}.zip")

	        //Create Deployment
	        def result = sh(returnStdout:true, script: "aws deploy create-deployment --application-name CDC-deploy --deployment-group-name ${stackName} --s3-location bucket=deployment-cdc,bundleType=zip,key=${applicationId}.zip")	        
	        def json = new JsonSlurper().parseText(result)
	        String deploymentId = json.deploymentId

	        //Wait until success
	        sh("aws deploy wait deployment-successful --deployment-id ${deploymentId}")
	    }
	}	
}
