# IBM Application Security on Cloud Jenkins Integrations
---
The files located in this folder are end to end automation using the [IBM Application Security on Cloud service](http://ibm.biz/ASoCLinkFromGitRepo) to run **SAST (Static Application Security Testing)** against source code orchestrated with [Jenkins](https://jenkins.io/).

Below are [Jenkins Pipeline](https://jenkins.io/doc/book/pipeline/) templates using the [Scripted Pipeline Syntax](https://jenkins.io/doc/book/pipeline/#scripted-pipeline-fundamentals) and guides to help you implement this control before entering continuous integration.

## Supported Tools & Services
* [GitHub](https://github.com/)
* [GitHub Enterprise](https://enterprise.github.com/home)
* [Jenkins](https://jenkins.io/)
* [JFrog Artifactory](https://jfrog.com/artifactory/)
* [IBM Application Security on Cloud](http://ibm.biz/ASoCLinkFromGitRepo)

## Supported Jenkins Executor Operating System
* Any Linux distribution

## Supported Pipeline Scenarios
* [Scan a single GitHub Repository & Setup Pull Request Status Checks](#scan-single-github-repository)
* [Scan all repositories in a GitHub Organization](#scan-all-repositories-in-a-github-organization)

## Basic Pre-Reqs
---
### Get Setup on IBM Application Security on Cloud

* Vist [IBM Application Security on Cloud](http://ibm.biz/ASoCLinkFromGitRepo) to setup your account.
* Once you have an account, from the **My Applications** tab, click **Create App** and give the app a unique name.
* Take note of the URL (ie. `https://appscan.ibmcloud.com/AsoCUI/serviceui/main/myapps/oneapp/ASOC_APPLICATION_ID` where **ASOC_APPLICATION_ID** will be the **ID** we use later for the environment property `ASOC_APPLICATION_ID` in your Jenkinsfile.

###  Jenkins Plugins Required
> **NOTE:**  [Install the Jenkins plugins](https://jenkins.io/doc/book/managing/plugins/#installing-a-plugin) below on your Jenkins master before proceeding.

* [**IBM Application Security on Cloud**](https://wiki.jenkins.io/display/JENKINS/IBM+Application+Security+On+Cloud+Plugin) - This plugin allows you to integrate Jenkins with IBM Application Security on Cloud

* [**Credentials Binding**](https://plugins.jenkins.io/credentials-binding) - This plugin allows credentials to be bound to environment variables for use in build steps, this is particularly useful for passing username and passwords into builds.

* [**SSH Agent**](https://plugins.jenkins.io/ssh-agent) - This plugin allows you to provide SSH credentials to builds via a ssh-agent in Jenkins.

* [**Slack Notification**](https://plugins.jenkins.io/slack) - The Slack plugin allows for configuring a default Slack channel, or a Slack channel may be defined on a per build task basis.  [See directions on Slack Plugin's GitHub home page](https://github.com/jenkinsci/slack-plugin).

* [**Blue Ocean**](https://plugins.jenkins.io/blueocean) - Blue Ocean is a new project that rethinks the user experience of Jenkins. Designed from the ground up for Jenkins Pipeline and compatible with Freestyle jobs, Blue Ocean reduces clutter and increases clarity for every member of your team through the following key features:
  * **Sophisticated visualizations of CD pipelines**, allowing for fast and intuitive comprehension of software pipeline status.
  * **Pipeline editor** that makes automating CD pipelines approachable by guiding the user through an intuitive and visual process to create a pipeline.
  * **Personalization of the Jenkins UI to suit the role-based needs of each member of the DevOps team**.
  * **Pinpoint precision when intervention is needed and/or issues arise**. The Blue Ocean UI shows where in the pipeline attention is needed, facilitating exception handling and increasing productivity.
  * **Native integration for branch and pull requests enables maximum developer productivity when collaborating on code with others in GitHub**.

* [**Pipeline**](https://plugins.jenkins.io/workflow-aggregator) - Jenkins Pipeline (or simply "Pipeline" with a capital "P") is a suite of plugins which supports implementing and integrating continuous delivery pipelines into Jenkins.

* [**Docker**](https://plugins.jenkins.io/docker-plugin) - Docker plugin allows user to use a docker host to dynamically provision build agents, run a single build, then tear-down the agent. **Stable Releases:** [1.1.3](https://updates.jenkins.io/download/plugins/docker-plugin/1.1.3/docker-plugin.hpi) or higher
> **NOTE:** This plugin is critical to pulling docker images from Artifactory dynamically into the Swarm Cloud or your Docker Cloud.  Staying at the above stable releases is typically a good idea.  Upgrading this plugin should come with caution.

* [**Github**](https://plugins.jenkins.io/github) - This plugin integrates Jenkins with Github projects.The plugin currently has three major functionalities:
  * Create hyperlinks between your Jenkins projects and GitHub
  * Trigger a job when you push to the repository by groking HTTP POSTs from
  * post-receive hook and optionally auto-managing the hook setup.
  * Report build status result back to github as Commit Status (documented on SO)
  * Base features for other plugins

* [**GitHub Branch Source**](https://plugins.jenkins.io/github-branch-source) - This plugin provides branch sources for GitHub in Multibranch projects and organization folders from GitHub.

* [**Artifactory**](https://www.jfrog.com/confluence/display/RTF/Jenkins+Artifactory+Plug-in) - The Jenkins Artifactory Plugin brings Artifactory's Build Integration support to Jenkins. This integration allows your build jobs to deploy artifacts automatically to Artifactory and have them linked to the build job that created them.

### Add IBM ASoC Credentials to Jenkins Master
* Visit `https://yourjenkins/credentials/store/system/domain/_/`
* Click **Add Credentials**.
* In the **Kind** drop-down list, select **IBM Application Security on Cloud Credentials**.
* Enter your [IBM ASoC generated API Key ID](https://www.ibm.com/support/knowledgecenter/SSYJJF_1.0.0/ApplicationSecurityonCloud/appseccloud_generate_api_key_cm.html) into the **ID** field.
* Enter your [API Key Secret](https://www.ibm.com/support/knowledgecenter/SSYJJF_1.0.0/ApplicationSecurityonCloud/appseccloud_generate_api_key_cm.html) into the **Secret** field.

### Add GitHub API Key as Credentials to Jenkins Master
* Visit `https://yourjenkins/credentials/store/system/domain/_/`
* Click **Add Credentials**.
* In the **Kind** drop-down list, select **Username with password**.
* Enter your **GitHub ID** into the **Username** field.
* Enter your [GitHub Personal Access API Token Credential](https://help.github.com/articles/creating-a-personal-access-token-for-the-command-line/) into the **Password** field.
* Give your **ID** field something meaningful as a variable name to reference in the Jenkinsfile later.  ( ie. `github.access.token`)

### Add Artifactory API Key as Credentials to Jenkins Master
> **NOTE:** This isn't necessary if you don't have Artifactory and can be toggled by a variable in the Jenkinsfile itself for use.

* Visit `https://yourjenkins/credentials/store/system/domain/_/`
* Click **Add Credentials**.
* In the **Kind** drop-down list, select **Username with password**.
* Enter your **Artifactory Username** into the **Username** field.
* Enter your [Artifactory API Token Credential](https://www.jfrog.com/confluence/display/RTF/Updating+Your+Profile#UpdatingYourProfile-APIKey) into the **Password** field.
* Give your **ID** field something meaningful as a variable name to reference in the Jenkinsfile later.  ( ie. `artifactory.access.token`)

### Configure Jenkins Master with Global GitHub Settings
* Visit your Jenkins master's global configuration page at `https://yourjenkins/configure`
* Find the **GitHub** section
* Choose **Add GitHub Server**
* Name your GitHub or GitHub Enterprise Server
* Set your API URL.
* Select **Add Credentials** > Type: **Secret Text** > Enter your GitHub Personal API Access Token in the **Secret** field
* Set your **ID** to `github.secret`
* Select the checkbox for **Manage Hooks**
* **Click** `TEST CONNECTION`

### Configure Jenkins Master with Global Aritfactory Settings
> **NOTE:** This isn't necessary if you don't have Artifactory and can be toggled by a variable in the Jenkinsfile itself for use.

* Visit your Jenkins master's global configuration page at `https://yourjenkins/configure`
* Find the **Artifactory** section
* Configure **Artifactory** server
* **Click** `ADD ARTIFACTORY SERVER`
* **Use the Credential Plugin**: `true` (checkbox checked)
* **Server ID**: `yourartifactory`
* **URL**: `https://yourartifactory/artifactory`
* **Default Deployer Credentials**: **Add a Jenkins UserName Password Credential** if you haven't done so already or else select the Artifactory Credentials from the previous section.
* **Click** `TEST CONNECTION`


## Scan Single GitHub Repository
> **NOTE:**  For most adopters this will be the common scenario used and the recommended approach to drive a review of the repository's code vulnerability reports prior to a Pull Request merger.

* In your Jenkins master [define a multi-branch pipeline in SCM](https://jenkins.io/doc/tutorials/build-a-multibranch-pipeline-project/)
* Add [Jenkinsfile_SingleGitHubRepo_Scan](Jenkinsfile_SingleGitHubRepo_Scan) to your repository that you configured in your pipeline on your Jenkins master in the previous step.
* Complete the Environment Variable Properties in the `Jenkinsfile` we provided
* Add Jenkins GitHub Pull Request Webhook at your Repository Settings tab (`https://github.ibm.com/<your organization>/<your repository>/settings/hooks`).  
* Select **Add webhook**.
  * **Payload url**: `https://myjenkins/github-webhook/`
  * Content type: **application/x-www-form-urlencoded**
  * Let me select individual events: **Push**, **Pull Request**
  * Select **Active**
  * Select **Update webhook**


* On your GitHub Repository [enforce Pull Request Reviews](https://help.github.com/articles/enabling-required-reviews-for-pull-requests/) and [enforce Status Checks](https://help.github.com/articles/enabling-required-status-checks/)
* After the first build has run you can come back on the repository settings and select the status `ci/asoc-sast/build-status` as a gate to your Pull Request.
* Now you are ready to code while IBM Application Security on Cloud checks your Pull Requests!


## Scan all repositories in a GitHub Organization
> **NOTE:** The value of this type of scan is to help a team baseline the Security status of their current code base.  We do not recommend using this type of scan routinely as this is to help a Security team or Focal get an understanding of an entire code base quickly.  Ideally you will move to the **Scan Single GitHub Repository** approach above immediately for all repositories.

**If you are NOT using a [Legacy Docker Swarm cluster](https://www.ibm.com/cloud/garage/experience/code/bradley_herrin_jenkins_for_building_bluemix_services) or Kubernetes for scalable executors on demand we highly advise you don't attempt this as it will add a long queue wait to the Jenkins master which if not properly tuned may over power your JVM settings on the master node.** :smile:

* In your Jenkins master [define a pipeline in SCM](https://jenkins.io/doc/book/pipeline/getting-started/#defining-a-pipeline-in-scm)
* Add [Jenkinsfile_GitHub_Org_Scan](Jenkinsfile_GitHub_Org_Scan) to the repository that you configured in your pipeline on your Jenkins master in the previous step.
* Complete the Environment Variable Properties in the `Jenkinsfile` we provided
