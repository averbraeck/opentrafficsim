# Publish OTS release on Maven

Development on OTS is continuous where improvements and new concepts are added frequently. Other projects are dependent on OTS and would benefit from the ongoing developments. Such dependencies are managed through Maven, a system by which projects are stated to be dependent on tools provide at Maven Central. This necessitates that new versions of OTS are published on Maven Central at some interval, depending partially on specific developments and requirements from other projects. This page describes the workflow of publishing a new version of OTS.

## Preparation
OTS can only be published if all unit test are successful and no errors occur during the build process. For a large part this can be checked before changes are made to online repositories. The following checks can be performed to make sure the unit tests and javadoc generation will run with no issues.

Right-click on the main 'ots' project > Run As > Maven build..., then type 'site' under 'Goals:', and click 'Run'.

The progress of these processes will be provided in the Console. Any errors that are reported here should be solved before a new release is published.

## Publishing a new release
The following steps are undertaken to publish a new release.

1. Increase the version number in all pom files using a three-level version number _X.Y.Z_.
2. Commit the pom files to Github.
3. On the main Github page, click 'Releases', then 'Draft a new release'.
4. Provide the following information:
    - Create a new Tag under 'Choose a tag'. For the tag name use _vX.Y.Z_, where _X.Y.Z_ should match the version.
    - Release title: _OTS vX.Y.Z_, where _vX.Y.Z_ should match the tag.
    - In the main text field, describe the release. It is common practice to enumerate all the included issues since the previous release.
5. Then click 'Publish release`.

After this a Github Action named _Maven Package to Central (v3)_, defined in `maven-publish.yml` is executed. This runs various tests and when successful, eventually publishes new jar-files of the various OTS projects to Maven Central.

## Backtracking
Should anything go wrong during the publication of a release, the underlying issue needs to be solved before re-attempting to publish a new release. Up to the point something went wrong, it is important to remove what was successfully created by the procedure.

1. Copy the release description to a local file so you can re-use it later.
2. Remove release from Github.
3. If a fix changed the repository, remove tag from Github. If instead the error was in the procedure itself (e.g. Maven requires a new URL), then the same tag can be used in the updated procedure.
