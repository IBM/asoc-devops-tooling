# IBM Application Security on Cloud Jenkins Integrations
---
The files located in this folder are end to end automation using the [Application Security on Cloud service](https://www.ibm.com/us-en/marketplace/application-security-on-cloud/resources) to run **SAST (Static Application Security Testing)** against source code orchestrated with [Jenkins](https://jenkins.io/).

**It is imperative that we know the the most about the security status of our code before leaving Source Control Management systems and shipping code further down the pipeline.**

Below are [Jenkins Pipeline](https://jenkins.io/doc/book/pipeline/) templates using the [Scripted Pipeline Syntax](https://jenkins.io/doc/book/pipeline/#scripted-pipeline-fundamentals) and guides to help you implement this control before entering continuous integration.

## Supported Tools & Services
* [GitHub](https://github.com/)
* [GitHub Enterprise](https://enterprise.github.com/home)
* [Jenkins](https://jenkins.io/)
* [JFrog Artifactory](https://jfrog.com/artifactory/)

## Supported Jenkins Executor Operating System
* Any Linux distribution

## Supported Pipeline Scenarios
* [Scan a single GitHub Repository & Setup Pull Request Status Checks](#scan-single-github-repository)
* [Scan all repositories in a GitHub Organization](#scan-all-repositories-in-a-github-organization)

## Basic Pre-Reqs
---
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
* Enter your [IBM ASoC generated API key details](https://www.ibm.com/support/knowledgecenter/SSYJJF_1.0.0/ApplicationSecurityonCloud/appseccloud_generate_api_key_cm.html).


## Scan Single GitHub Repository
> **NOTE:**  For most adopters this will be the common scenario used and the recommended approach to drive a review of the repository's code vulnerability reports prior to a Pull Request merger.



## Scan all repositories in a GitHub Organization
...
