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

package ci.xlj.tools.jobarchiver.config

import ci.xlj.libs.utils.StringUtils

/**
 * @author kfzx-xulj
 *
 */
class ConfigLoader {

	private static Properties props = new Properties()
	private static JA_HOME

	static void load() {
		JA_HOME = System.getenv('JA_HOME')
		if (!StringUtils.isValid(JA_HOME)) {
			println 'Please SET system variable JA_HOME and retry.'
			System.exit(-1)
		}

		def config=new File("$JA_HOME/${Globals.CONFIG_FILE}")
		if(config.exists()){
			props.load(new FileReader(config))
		}else{
			println "Config file '${config}' does not exist. Please check and retry."
			System.exit(-2)
		}


		Globals.JA_HOME = JA_HOME
		Globals.URL = props.getProperty("SERVER_URL")
		Globals.USERNAME = props.getProperty("USERNAME")
		Globals.PASSWORD = props.getProperty("PASSWORD")
		Globals.ARCHIVE_DEST = props.getProperty("ARCHIVE_DEST")
		Globals.JOBS_DIR = props.getProperty("JOBS_DIR")

		if (!StringUtils.isValid(Globals.URL)
		|| !StringUtils.isValid(Globals.ARCHIVE_DEST)
		|| !StringUtils.isValid(Globals.JOBS_DIR)) {
			println 'SERVER URL or ARCHIVE_DEST or JOBS_DIR cannot be empty.'
			System.exit(-3)
		}
	}

}
