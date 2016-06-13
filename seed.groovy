String project= 'boise_devops_2016'

String gitUrl = 'git@bitbucket.org:${project}'
String org = 'devteam'

services = ['hello', 'goodbye']
branches = ['master', 'qa', 'staging', 'prod']

services.each {
  service = it

  branches.each {
    branch = it

    String currentFolder = '$project/$service/$branch'

    folder currentFolder

    pipelineJob("$currentFolder/$branch") {

      triggers {
          scm('H/5 * * * *')
      }
      definition {

        cpsScm {
            scm {
                git('$gitUrl/$service')
            }
            scriptPath('Jenkinsfile-$branch.groovy')
        }
      }
    }
  }
}
~
