/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License") you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//      Contributors:      Xu Lijia

package ci.xlj.tools.jobarchiver

import org.apache.log4j.Logger

import ci.xlj.libs.jenkinsvisitor.JenkinsVisitor
import ci.xlj.libs.procinvoker.ProcInvoker
import ci.xlj.libs.utils.DateUtils
import ci.xlj.libs.utils.OSUtils
import ci.xlj.tools.jobarchiver.config.ConfigLoader
import ci.xlj.tools.jobarchiver.config.Globals

/**
 * @author kfzx-xulj
 *
 */
class JobArchiver {

	private Logger logger=Logger.getLogger(JobArchiver)

	private showInfo() {
		println '''Job Archiver v1.0.0 of 14 Jul. 2014, by Mr. Xu Lijia.
Send bug reports via email icbcsdcpmoxulj@outlook.com
This tool is used to archive jobs on Jenkins Server.\n'''
	}

	private showUsage() {
		println '''Usage:
java -jar ct-jobarchiver.jar [optitons]
\texp\tGenerate job list
\tarc\tArchive jobs in jobs-to-delete.txt'''
	}

	private JobArchiver(args){
		showInfo()
		logger.info "Starting at ${DateUtils.toString2(new Date())}..."

		checkPrerequisites()

		if(args){
			if(args[0]=='exp'){
				init()
				generateJobList()
			}else if(args[0]=='arc'){
				init()
				loadJobList()
				perform()
			}else{
				println 'Invalid parameters. See usage for details.'
				showUsage()
			}

			println '\nProcess completed.'
			logger.info "Finishing at ${DateUtils.toString2(new Date())}.\n"
		}else{
			showUsage()
		}
	}

	static main(args) {
		new JobArchiver(args)
	}

	private checkPrerequisites() {
		ConfigLoader.load()

		if(OSUtils.isWindows()){
			def executable= new File("${Globals.JA_HOME}/${Globals.WIN_EXECUTABLE}")

			if(!executable.exists()){
				println 'Program 7z.exe is missing. Please reinstall. \nProgram exited.'
				System.exit(-4)
			}
		}

		def arcDest=new File(Globals.ARCHIVE_DEST)

		if(!arcDest.exists()){
			println "Directory ${Globals.ARCHIVE_DEST} does not exist. Created automatically."
			if(!arcDest.mkdir()){
				println "Failed to create directory ${Globals.ARCHIVE_DEST}. Program now exit."
				System.exit(-5)
			}
		}
	}

	private JenkinsVisitor v

	private init(){
		v=new JenkinsVisitor(Globals.URL)

		try{
			def res=v.login(Globals.USERNAME, Globals.PASSWORD)

			if(!res){
				println 'Invalid username or password. Please check and retry.'
				System.exit(-6)
			}
		}catch(Exception e){
			println 'Connection to the given url failed. Please check and retry.'
			System.exit(-7)
		}
	}

	private generateJobList(){
		def file=new File("${Globals.JA_HOME}/${Globals.JOB_LIST}")
		def fileWriter=new FileWriter(file)
		def delimeter=OSUtils.getOSLineSeparator()

		println 'Generating job list...'
		v.getJobNameList().each {
			fileWriter.write("${it}${delimeter}")
		}

		fileWriter.close()

		println "Saved to file '${file.getAbsolutePath()}'"
	}

	private jobNames=[]

	private loadJobList(){
		def file=new File("${Globals.JA_HOME}/${Globals.JOB_LIST}")
		file.eachLine { jobNames<<it }
	}

	private p
	private doArchive(jobName){
		def date=DateUtils.toString(new Date())
		def cmd

		if(OSUtils.isWindows()){
			cmd="${Globals.JA_HOME}/${Globals.WIN_EXECUTABLE} a ${Globals.ARCHIVE_DEST}/$jobName-${date}.zip ${Globals.JOBS_DIR}/$jobName"
			logger.info 'Win process created.'
		}else{
			cmd="zip -r ${Globals.ARCHIVE_DEST}/$jobName-${date}.zip ${Globals.JOBS_DIR}/$jobName"
			logger.info "Linux process created."
		}

		p=new ProcInvoker(cmd)
		p.invoke()
	}

	private doDelete(jobName){
		def retCode=v.delete(jobName)
		if(retCode==302){
			def m="$jobName deleted."
			println m
			logger.info m
		}else{
			def m="Error in deleting $jobName. Please delete it manually."
			println m
			logger.error m
		}
	}

	private perform(){
		jobNames.each {
			println "Archiving $it..."
			if(doArchive(it)){
				println "Error in archiving $it. Deletion is ignored. See log for details"
				logger.error "Error in archiving $it. Deletion is ignored. Reason:"
				logger.error p.getErrorMessage()
			}else{
				def m="Job $it archived successfully."
				logger.info m
				println m
				println "Deleting $it..."
				doDelete(it)
			}
		}
	}

}
