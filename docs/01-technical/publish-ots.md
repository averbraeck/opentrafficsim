# Publish OTS release on Maven

Development on OTS is continuous where improvements and new concepts are added frequently. Other projects are dependent on OTS and would benefit from the ongoing developments. Such dependencies are managed through Maven, a system by which projects are stated to be dependent on tools provide at Maven Central. This necessitates that new versions of OTS are published on Maven Central at some interval, depending partially on specific developments and requirements from other projects. This page describes the workflow of publishing a new version of OTS.

## Preparation
OTS can only be published if all unit test are successful and no errors occur during the build process. For a large part this can be checked before changes are made to online repositories. The following checks can be performed to make sure the unit tests and javadoc generation will run with no issues.

1. Richt-click on the main 'ots' project > Run As > Maven test.
2. Right-click on the main 'ots' project > Run As > Maven build..., then type 'javadoc:javadoc' under 'Goals:', and click 'Run'.

The progress of these processes will be provided in the Console. Any errors that are reported here shouls be solved before a new release is published.

## Publishing a new release
The following steps are undertaken to publish a new release.

1. On the main Github page, click 'Releases', then 'Draft new release'.
2. Provide the following information:
    - Create a new Tag under 'Choose a tag'. Increase the correct level in version number relative to the previous release. 
    - Release title: _OTS v1.7.5_.
    - In the main text field, describe the release. It is common practice to enumerate all the included issues since the previous release.
3. The click 'Publish release`.

After this a Github Action named _Maven Package to Central (v3)_, defined in `maven-publish.yml` is executed. this runs various tests and when successful, eventually publishes new jar-files of the various OTS project to Maven Central.

## Backtracking
Should anything go wrong during the publication of a release, the underlying issue needs to be solved before re-attempting to publish a new release. Up to the point something went wrong, it is important to remove what was successfully created by the procedure.

1. Remove release from Github.
2. If a fix changed the repository, remove tag from Github. If instead the error was in the procedure itself (e.g. Maven requires a new URL), then the same tag cna be used in the updated procedure.
