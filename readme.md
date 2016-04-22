### Pre-reqs
* [CICS Transaction Gateway V9.1](http://www.ibm.com/software/products/en/cics-ctg/)
* [Apache Commons Codec](http://commons.apache.org/proper/commons-codec/)
* [json.org Java library](http://www.json.org/java/index.html)

### Introduction

This Request Monitoring exit for the CICS Transaction Gateway is designed to store the monitoring information in a cloudant database as described in the blog post [Analysis of mobile workload using IBM Bluemix](https://developer.ibm.com/cics/2015/03/09/analysis-of-mobile-workload-using-ibm-bluemix).

### Usage

1. Add the `ctgcloudmonitor.jar` to the classpath of your Gateway daemon along with the commons codec and json jar files.
2. Add the following line to the Gateway daemon configuration file `requestexits=com.ibm.ctg.samples.CloudMonitor`
3. Start the CICS Transaction Gateway with the startup parameter `-j-Dcloudmonitor.dburl=<url of cloudant database>` and optionally `-j-Dcloudmonitor.logfile=<path to log file>`

### Notice
&copy; Copyright IBM Corporation 2015

### License
```
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
