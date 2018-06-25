# Contributing to asoc-devops-tooling
---
This is an open source project, and we appreciate your help!  Before you get started take a short read to learn how to contribute to the efforts.

## Fork & Pull
We use the Fork and Pull development model in this project.   https://help.github.com/articles/about-collaborative-development-models/

## Code Review
We have designated reviewers for areas of the project using the [CODEOWNERS](CODEOWNERS) file.  When you are ready one of these owners will work with you through your Pull Request to integrate your changes.

## For Code Owners
Members of the project with write access to the project we ask that if you are creating a new folder in the root of the project to support new tooling or workflows please update the [CODEOWNERS](CODEOWNERS) file to assign ownership or transfer ownership.

## Project Structure
The project is organized around a basic folder structure where a new folder in the root represents a new tool integration or framework being supported for the [IBM Application Security on Cloud](https://www.ibm.com/us-en/marketplace/application-security-on-cloud/resources) service

Each root subfolder requires a README.md for organizing and providing detailed playbook guidance on how to setup, update, and deploy the integration.

The [README.md](README.md) in the root of the repository is updated with a link to the supported integrations subfolder.

```
README.md
CODEOWNERS
CONTRIBUTING.md
LICENSE
/jenkins/README.md
/travis/README.md
..
```
