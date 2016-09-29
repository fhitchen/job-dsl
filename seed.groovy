#!groovy

// job-dsl langauge
String team= 'fhitchen'
String gitUrl = "https://github.com/$team"
String cronSchedule = 'H/30 * * * *'
services = ['hello', 'goodbye']
branches = ['master', 'qa', 'staging', 'prod']

folder "$team"

services.each {
  service = it
  currentFolder = "$team/$service"
  folder currentFolder

  branches.each {
    branch = it

    pipelineJob ("$currentFolder/$branch") {
      description("do not hand edit, built by seed.groovy")
      // when...
      triggers {
          scm cronSchedule
      }
      // what...
      scm {
        git {
          remote {
            url "$gitUrl/$service"
            branch "$branch"
          }
        }
      }
      definition {
  	     cpsScm {
    	      scm {
    		        git {
      		          remote {
        		            url "$gitUrl/$service"
        		            branch "$branch"
      		          }
    	          }
            }
            // how
            scriptPath "Jenkinsfile_$branch"
        }
      }
    }
  }

  // a benchtest for each repo...
  multibranchPipelineJob ("$team/$service/benchtest") {
    description("Benchtest pipelines. Push commit/branch/tag to git push <branchname>:refs/heads/benchtest/<branchname>, then the Jenkinsfile pipeline will execute" )
    branchSources {
        git {
          remote "$gitUrl/$service"
          includes "benchtest/**"
        }
    }
    triggers {
      cron cronSchedule
    }
    orphanedItemStrategy {
        discardOldItems {
            numToKeep 20
        }
    }
  }
  // a release area for each repo...
  multibranchPipelineJob("$team/$service/releases") {
    description "Releease pipelines. Push commit/branch/tag to git push <branchname>:refs/heads/releases/<branchname>, then the Jenkinsfile pipeline will execute"
    branchSources {
        git {
          remote "$gitUrl/$service"
          includes "releases/**"
        }
    }
    triggers {
      cron cronSchedule
    }
  }
}

folder "Metro"

job('Metro/AOT') {
    scm {
        perforceP4('p4_credentials') {
            workspace {
                manual('ws_name', '//METRO/1910-Release/AOT/... //ws_name/AOT/...')
            }
            configure { node ->
                node / workspace / spec / clobber('true')
            }
        }
    }
}


String project= 'Sprint'
String SprintCronSchedule = 'H/60 * * * *'
branches = ['1625', '1630', '1710']
building_blocks = ['abp', 'api', 'apijobs', 'aprm', 'asmn',
                   'bims' 'BOOSTUI',
                   'crm',
                   'lel',
                   'omniChannel',
                   'PowerBuilder',
                   'tools_config',
                   'vad',
                   'wm']

folder project

folder "$project/Api"

pipelineJob ("$project/Api/1625/api-ant") {
  description("do not hand edit, built by seed.groovy")
  // when...
  triggers {
    scm SprintCronSchedule
  }
  // what..
  scm {
    perforceP4('p4_credentials') {
      workspace {
        manual('ws_name', '//SPRINT/fhitchen_1625-api/... //ws_name/...')
      }
      configure { node ->
        node / workspace / spec / clobber('true')
      }
    }
  }
  definition {
    cpsScm {
      scm {
        perforceP4('p4_credentials') {
          workspace {
            manual('ws_name', '//SPRINT/fhitchen_1625-api/... //ws_name/...')
          }
          configure { node ->
            node / workspace / spec / clobber('true')
          }
        }
      }
   
      // how
      scriptPath "api/Jenkinsfile"
    }
  }
}


branches.each {
  branch = it

  building_blocks.each {
    bb = it
    display_bb = bb.capitalize()
    
    currentFolder = "$project/$display_bb/$branch"
    folder currentFolder

    pipelineJob ("$project/$display_bb/$branch/$bb-gradle") {
      description("do not hand edit, built by seed.groovy")
      // when...
      triggers {
        scm SprintCronSchedule
      }
      // what..
      scm {
        perforceP4('p4_credentials') {
          workspace {
            manual('ws_name', "//SPRINT/fhitchen_$branch-$bb/... //ws_name/...")
          }
          configure { node ->
            node / workspace / spec / clobber('true')
          }
        }
      }
      definition {
        cpsScm {
          scm {
            perforceP4('p4_credentials') {
              workspace {
                manual('ws_name', "//SPRINT/fhitchen_$branch-$bb/... //ws_name/...")
              }
              configure { node ->
                node / workspace / spec / clobber('true')
              }
            }
          }	
          // how
          scriptPath "$bb/Jenkinsfile_gradle"
        }
      }
    }
  }
}
