[![Build](https://github.com/julbme/gh-action-semver-release-vars/actions/workflows/maven-build.yml/badge.svg)](https://github.com/julbme/gh-action-semver-release-vars/actions/workflows/maven-build.yml)
[![Lint Commit Messages](https://github.com/julbme/gh-action-semver-release-vars/actions/workflows/commitlint.yml/badge.svg)](https://github.com/julbme/gh-action-semver-release-vars/actions/workflows/commitlint.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=julbme_gh-action-semver-release-vars&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=julbme_gh-action-semver-release-vars)
![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/julbme/gh-action-semver-release-vars)

# GitHub Action to compute SemVer release vars

The GitHub Action for computing SemVer release vars.

As pre-requisite, it should be invoked from a release branch:
* `releases/trigger`
* `releases/trigger-<version-to-release>`

Maintenances branches can be defined among these patterns:
* `maintenances/<major>.x`
* `maintenances/<major>.<minor>.x`

## Usage

### Example Workflow file

```yaml
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Merge branch
        uses: julbme/gh-action-semver-release-vars@v1
        with:
          package_version: 1.0.0
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

### Inputs

|       Name        |  Type  | Default |                                                      Description                                                       |
|-------------------|--------|---------|------------------------------------------------------------------------------------------------------------------------|
| `package_version` | string | ` `     | The release version set in the package manager's file in the current branch. It is used as release version by default. |

### Outputs

|             Name              |  Type  |                                                                                                                     Description                                                                                                                      |
|-------------------------------|--------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `version`                     | string | The release version. Ex: `1.2.3-rc.1+abcdef`                                                                                                                                                                                                         |
| `version_major`               | string | The release version major value. Ex: `1.2.3-rc.1+abcdef` => `1`                                                                                                                                                                                      |
| `version_minor`               | string | The release version minor value. Ex: `1.2.3-rc.1+abcdef` => `2`                                                                                                                                                                                      |
| `version_patch`               | string | The release version patch value. Ex: `1.2.3-rc.1+abcdef` => `3`                                                                                                                                                                                      |
| `version_suffix`              | string | The release version suffix value. Ex: `1.2.3-rc.1+abcdef` => `rc.1`                                                                                                                                                                                  |
| `version_build`               | string | The release version build value. Ex: `1.2.3-rc.1+abcdef` => `abcdef`                                                                                                                                                                                 |
| `git_tag`                     | string | The git tag for that version. Ex: `1.2.3-rc.1+abcdef` => `v1.2.3-rc.1+abcdef`                                                                                                                                                                        |
| `git_tag_major`               | string | The git tag with major version only if the release version is the latest in major scope. Ex: `1.2.3-rc.1+abcdef` => `v1` or ``                                                                                                                       |
| `git_tag_minor`               | string | The git tag with major/minor version only if the release version is the latest in major/minor scope. Ex: `1.2.3-rc.1+abcdef` => `v1.2` or ``                                                                                                         |
| `git_tag_patch`               | string | The git tag with major/minor/patch version only if the release version is the latest in major/minor/patch scope. Ex: `1.2.3-rc.1+abcdef` => `v1.2.3` or ``                                                                                           |
| `docker_tag`                  | string | The Docker tag for that version. Ex: `1.2.3-rc.1+abcdef` => `1.2.3-rc.1+abcdef`                                                                                                                                                                      |
| `docker_tag_major`            | string | The Docker tag with major version only if the release version is the latest in major scope. Ex: `1.2.3-rc.1+abcdef` => `1` or ``                                                                                                                     |
| `docker_tag_minor`            | string | The Docker tag with major/minor version only if the release version is the latest in major/minor scope. Ex: `1.2.3-rc.1+abcdef` => `1.2` or ``                                                                                                       |
| `docker_tag_patch`            | string | The Docker tag with major/minor/patch version only if the release version is the latest in major/minor/patch scope. Ex: `1.2.3-rc.1+abcdef` => `1.2.3` or ``                                                                                         |
| `next_major_version`          | string | If major-incremented, the next version. Ex: `1.2.3-rc.1+abcdef` => `2.0.0`                                                                                                                                                                           |
| `next_minor_version`          | string | If minor-incremented, the next version. Ex: `1.2.3-rc.1+abcdef` => `1.3.0`                                                                                                                                                                           |
| `next_patch_version`          | string | If patch-incremented, the next version. Ex: `1.2.3-rc.1+abcdef` => `1.2.4`                                                                                                                                                                           |
| `next_major_snapshot_version` | string | If major-incremented, the next version with SNAPSHOT suffix. Ex: `1.2.3-rc.1+abcdef` => `2.0.0-SNAPSHOT`                                                                                                                                             |
| `next_minor_snapshot_version` | string | If minor-incremented, the next version with SNAPSHOT suffix. Ex: `1.2.3-rc.1+abcdef` => `1.3.0-SNAPSHOT`                                                                                                                                             |
| `next_patch_snapshot_version` | string | If patch-incremented, the next version with SNAPSHOT suffix. Ex: `1.2.3-rc.1+abcdef` => `1.2.4-SNAPSHOT`                                                                                                                                             |
| `trigger_branch`              | string | The branch which triggered the release. Ex: `releases/trigger`                                                                                                                                                                                       |
| `trigger_branch_ref`          | string | The branch ref which triggered the release. Ex: `refs/heads/releases/trigger`                                                                                                                                                                        |
| `run_branch`                  | string | The temporary branch in which release commits can be pushed before being merged in target branch. Ex: `releases/run-123456`                                                                                                                          |
| `run_branch_ref`              | string | The temporary branch ref in which release commits can be pushed before being merged in target branch. Ex: `refs/heads/releases/run-123456`                                                                                                           |
| `target_branch`               | string | The target branch in which release commits should be merged if successful. It will try to resolve the corresponding maintenance branch. If not found, it will use the project default branch. Ex: `main` or `maintenances/1.x`                       |
| `target_branch_ref`           | string | The target branch in which release commits should be merged if successful. It will try to resolve the corresponding maintenance branch. If not found, it will use the project default branch. Ex: `refs/heads/main` or `refs/heads/maintenances/1.x` |

## Contributing

This project is totally open source and contributors are welcome.
