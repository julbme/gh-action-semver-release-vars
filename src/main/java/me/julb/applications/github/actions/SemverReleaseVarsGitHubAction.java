/**
 * MIT License
 *
 * Copyright (c) 2017-2022 Julb
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package me.julb.applications.github.actions;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.concurrent.CompletionException;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTag;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import com.vdurmont.semver4j.Semver;
import com.vdurmont.semver4j.SemverException;

import me.julb.sdk.github.actions.kit.GitHubActionsKit;
import me.julb.sdk.github.actions.spi.GitHubActionProvider;

import lombok.AccessLevel;
import lombok.NonNull;
import lombok.Setter;

/**
 * The action to compute SemVer release vars. <br>
 * @author Julb.
 */
public class SemverReleaseVarsGitHubAction implements GitHubActionProvider {

    /**
     * The SNAPSHOT_SUFFIX attribute.
     */
    private static final String SNAPSHOT_SUFFIX = "SNAPSHOT";

    /**
     * The pattern to match first "v" character.
     */
    private static final Pattern STARTS_WITH_V_PATTERN = Pattern.compile("^v");

    /**
     * The pattern to match for trigger release branch.
     */
    private static final Pattern TRIGGER_RELEASE_BRANCH_PATTERN =
            Pattern.compile("^releases/trigger(-v?(?<version>[0-9]+[.][0-9]+[.][0-9]+[\\w.+\\-]*))?$");

    /**
     * The pattern to match for maintenance branch.
     */
    private static final Pattern MAINTENANCE_BRANCH_PATTERN =
            Pattern.compile("^maintenances/(?<major>[0-9]+)[.]((?<minor>[0-9]+)[.])?x$");

    /**
     * The GitHub action kit.
     */
    @Setter(AccessLevel.PACKAGE)
    private GitHubActionsKit ghActionsKit = GitHubActionsKit.INSTANCE;

    /**
     * The GitHub API.
     */
    @Setter(AccessLevel.PACKAGE)
    private GitHub ghApi;

    /**
     * The GitHub repository.
     */
    @Setter(AccessLevel.PACKAGE)
    private GHRepository ghRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute() {
        try {
            // Get inputs
            var packageVersion = getInputPackageVersion();
            var releaseBranchName = getReleaseBranchName();
            var runBranchName = getRunReleaseBranchName();

            // Trace parameters
            ghActionsKit.debug(String.format("parameters: [package_version: %s]", packageVersion));

            // Get release version
            var releaseVersion = getReleaseVersion(packageVersion, releaseBranchName);
            var semverReleaseVersion = getSemverVersion(releaseVersion);

            // Read GitHub repository.
            connectApi();

            // Retrieve repository
            ghRepository = ghApi.getRepository(ghActionsKit.getGitHubRepository());

            // Get repository tags.
            var tagsByVersion = getValidSemverTags();

            // Ensure a tag with this version does not exist.
            if (tagsByVersion.containsKey(releaseVersion.toLowerCase(Locale.ROOT))) {
                throw new IllegalArgumentException(
                        String.format("a tag for version %s already exists in the repository.", releaseVersion));
            }

            // verify if this releases are the latest version
            boolean isLatestMajorVersion = isLatestMajorVersion(releaseVersion, tagsByVersion.keySet());
            boolean isLatestMajorMinorVersion = isLatestMajorMinorVersion(releaseVersion, tagsByVersion.keySet());
            boolean isLatestMajorMinorPatchVersion =
                    isLatestMajorMinorPatchVersion(releaseVersion, tagsByVersion.keySet());

            // Parse version
            var valueVersion = semverReleaseVersion.getValue();
            var majorVersion = String.valueOf(semverReleaseVersion.getMajor());
            var majorAndMinorVersion =
                    StringUtils.join(semverReleaseVersion.getMajor(), ".", semverReleaseVersion.getMinor());
            var majorAndMinorAndPatchVersion = StringUtils.join(
                    semverReleaseVersion.getMajor(),
                    ".",
                    semverReleaseVersion.getMinor(),
                    ".",
                    semverReleaseVersion.getPatch());
            var minorVersion = String.valueOf(semverReleaseVersion.getMinor());
            var patchVersion = String.valueOf(semverReleaseVersion.getPatch());
            String suffixVersion = null;
            if (semverReleaseVersion.getSuffixTokens().length > 0) {
                suffixVersion = StringUtils.join(semverReleaseVersion.getSuffixTokens(), ".");
            }
            var buildVersion = semverReleaseVersion.getBuild();

            // Get target branch
            String targetBranch = getMaintenanceBranchName(releaseVersion)
                    .orElse(Optional.ofNullable(ghRepository.getDefaultBranch()).orElseThrow());

            // Set output variables.
            // -- release version
            this.ghActionsKit.setOutput(OutputVars.VERSION.key(), valueVersion);
            this.ghActionsKit.setOutput(OutputVars.VERSION_MAJOR.key(), majorVersion);
            this.ghActionsKit.setOutput(OutputVars.VERSION_MINOR.key(), minorVersion);
            this.ghActionsKit.setOutput(OutputVars.VERSION_PATCH.key(), patchVersion);
            this.ghActionsKit.setOptionalOutput(OutputVars.VERSION_SUFFIX.key(), Optional.ofNullable(suffixVersion));
            this.ghActionsKit.setOptionalOutput(OutputVars.VERSION_BUILD.key(), Optional.ofNullable(buildVersion));

            this.ghActionsKit.setOutput(OutputVars.GIT_TAG.key(), gitTag(valueVersion));
            this.ghActionsKit.setOptionalOutput(
                    OutputVars.GIT_TAG_MAJOR.key(),
                    Optional.of(isLatestMajorVersion)
                            .filter(Boolean.TRUE::equals)
                            .map(v -> gitTag(majorVersion)));
            this.ghActionsKit.setOptionalOutput(
                    OutputVars.GIT_TAG_MINOR.key(),
                    Optional.of(isLatestMajorMinorVersion)
                            .filter(Boolean.TRUE::equals)
                            .map(v -> gitTag(majorAndMinorVersion)));
            this.ghActionsKit.setOptionalOutput(
                    OutputVars.GIT_TAG_PATCH.key(),
                    Optional.of(isLatestMajorMinorPatchVersion)
                            .filter(Boolean.TRUE::equals)
                            .map(v -> gitTag(majorAndMinorAndPatchVersion)));

            this.ghActionsKit.setOutput(OutputVars.DOCKER_TAG.key(), valueVersion);
            this.ghActionsKit.setOptionalOutput(
                    OutputVars.DOCKER_TAG_MAJOR.key(),
                    Optional.of(isLatestMajorVersion)
                            .filter(Boolean.TRUE::equals)
                            .map(v -> majorVersion));
            this.ghActionsKit.setOptionalOutput(
                    OutputVars.DOCKER_TAG_MINOR.key(),
                    Optional.of(isLatestMajorMinorVersion)
                            .filter(Boolean.TRUE::equals)
                            .map(v -> majorAndMinorVersion));
            this.ghActionsKit.setOptionalOutput(
                    OutputVars.DOCKER_TAG_PATCH.key(),
                    Optional.of(isLatestMajorMinorPatchVersion)
                            .filter(Boolean.TRUE::equals)
                            .map(v -> majorAndMinorAndPatchVersion));

            // -- next version
            this.ghActionsKit.setOutput(
                    OutputVars.NEXT_MAJOR_VERSION.key(),
                    semverReleaseVersion.nextMajor().getValue());
            this.ghActionsKit.setOutput(
                    OutputVars.NEXT_MINOR_VERSION.key(),
                    semverReleaseVersion.nextMinor().getValue());
            this.ghActionsKit.setOutput(
                    OutputVars.NEXT_PATCH_VERSION.key(),
                    semverReleaseVersion.nextPatch().getValue());
            this.ghActionsKit.setOutput(
                    OutputVars.NEXT_MAJOR_SNAPSHOT_VERSION.key(),
                    semverReleaseVersion.nextMajor().withSuffix(SNAPSHOT_SUFFIX).getValue());
            this.ghActionsKit.setOutput(
                    OutputVars.NEXT_MINOR_SNAPSHOT_VERSION.key(),
                    semverReleaseVersion.nextMinor().withSuffix(SNAPSHOT_SUFFIX).getValue());
            this.ghActionsKit.setOutput(
                    OutputVars.NEXT_PATCH_SNAPSHOT_VERSION.key(),
                    semverReleaseVersion.nextPatch().withSuffix(SNAPSHOT_SUFFIX).getValue());

            // -- branch
            this.ghActionsKit.setOutput(OutputVars.TRIGGER_BRANCH.key(), releaseBranchName);
            this.ghActionsKit.setOutput(OutputVars.TRIGGER_BRANCH_REF.key(), branchRef(releaseBranchName));
            this.ghActionsKit.setOutput(OutputVars.RUN_BRANCH.key(), runBranchName);
            this.ghActionsKit.setOutput(OutputVars.RUN_BRANCH_REF.key(), branchRef(runBranchName));
            this.ghActionsKit.setOutput(OutputVars.TARGET_BRANCH.key(), targetBranch);
            this.ghActionsKit.setOutput(OutputVars.TARGET_BRANCH_REF.key(), branchRef(targetBranch));
        } catch (Exception e) {
            throw new CompletionException(e);
        }
    }

    // ------------------------------------------ Utility methods.

    /**
     * Gets the "package_version" input.
     * @return the "package_version" input.
     */
    Optional<String> getInputPackageVersion() {
        return ghActionsKit
                .getInput("package_version")
                .map(v -> STARTS_WITH_V_PATTERN.matcher(v).replaceFirst(""));
    }

    /**
     * Gets the release branch name.
     * @return the release branch name.
     */
    String getReleaseBranchName() {
        if (ghActionsKit.isGitHubRefTypeBranch()) {
            return ghActionsKit.getGitHubRefName();
        } else {
            throw new IllegalArgumentException("GITHUB_REF should be a branch.");
        }
    }

    /**
     * Gets the run release branch name.
     * @return the run release branch name.
     */
    String getRunReleaseBranchName() {
        return String.format("releases/run-%s", this.ghActionsKit.getGitHubRunId());
    }

    /**
     * Gets the release version.
     * @param packageVersion the package version if any.
     * @param releaseBranchName the release branch name.
     * @return the release version.
     */
    String getReleaseVersion(@NonNull Optional<String> packageVersion, @NonNull String releaseBranchName) {
        var matcher = TRIGGER_RELEASE_BRANCH_PATTERN.matcher(releaseBranchName);
        if (matcher.matches()) {
            return Optional.ofNullable(matcher.group("version")).orElse(packageVersion.orElseThrow());
        } else {
            throw new IllegalArgumentException("GITHUB_REF shoud match releases/trigger(-<version>)? format.");
        }
    }

    /**
     * Gets the semver verison object from the given version.
     * @param version the version.
     * @return the semver object for that version.
     * @throws IllegalArgumentException if the version is not semver-valid.
     */
    Semver getSemverVersion(@NonNull String version) {
        try {
            return new Semver(version);
        } catch (SemverException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Connects to GitHub API.
     * @throws IOException if an error occurs.
     */
    void connectApi() throws IOException {
        ghActionsKit.debug("github api url connection: check.");

        // Get token
        var githubToken = ghActionsKit.getRequiredEnv("GITHUB_TOKEN");

        // @formatter:off
        ghApi = Optional.ofNullable(ghApi)
                .orElse(new GitHubBuilder()
                        .withEndpoint(ghActionsKit.getGitHubApiUrl())
                        .withOAuthToken(githubToken)
                        .build());
        ghApi.checkApiUrlValidity();
        ghActionsKit.debug("github api url connection: ok.");
        // @formatter:on
    }

    /**
     * Gets the maintenance branch name matching this release version.
     * @param releaseVersion the release version.
     * @return the maintenance branch name matching this release version, or {@link Optional#empty()} otherwise.
     * @throws IOException if an error occurs.
     */
    Optional<String> getMaintenanceBranchName(@NonNull String releaseVersion) throws IOException {
        // Get semver version
        var currentSemverVersion = new Semver(releaseVersion);

        // Parse branches.
        var branches = ghRepository.getBranches().keySet();
        for (String branch : branches) {
            var matcher = MAINTENANCE_BRANCH_PATTERN.matcher(branch);

            // @formatter:off
            if (matcher.matches()
                    && Integer.valueOf(matcher.group("major")).equals(currentSemverVersion.getMajor())
                    && (matcher.group("minor") == null
                            || Integer.valueOf(matcher.group("minor")).equals(currentSemverVersion.getMinor()))) {
                return Optional.of(branch);
            }
            // @formatter:on
        }

        return Optional.empty();
    }

    /**
     * Gets the repository tags.
     * @return the tags of the given repository.
     * @throws IOException if an error occurs.
     */
    Map<String, GHTag> getValidSemverTags() throws IOException {
        var tags = new HashMap<String, GHTag>();
        for (GHTag ghTag : ghRepository.listTags()) {
            var tagName = STARTS_WITH_V_PATTERN
                    .matcher(ghTag.getName().toLowerCase(Locale.ROOT))
                    .replaceFirst("");
            try {
                new Semver(tagName);
                tags.put(tagName, ghTag);
            } catch (SemverException e) {
                // NOOP
            }
        }
        return tags;
    }

    /**
     * Returns <code>true</code> if the version is the latest under major version scopes,
     *  <code>false</code> otherwise.
     * @param version the version.
     * @param taggedVersions the list of versions to check.
     * @return <code>true</code> if the version is the latest under major version scopes,
     *  <code>false</code> otherwise.
     */
    boolean isLatestMajorVersion(@NonNull String version, @NonNull Collection<String> taggedVersions) {
        // list that holds all versions sharing major.
        var semverVersions = new TreeSet<>();

        // add current version
        var currentSemverVersion = new Semver(version);
        semverVersions.add(currentSemverVersion);

        // add all other versions sharing the same major version
        for (String taggedVersion : taggedVersions) {
            var semverTaggedVersion = new Semver(taggedVersion);
            if (semverTaggedVersion.getMajor().equals(currentSemverVersion.getMajor())) {
                semverVersions.add(semverTaggedVersion);
            }
        }

        // last item is the latest one.
        return semverVersions.last().equals(currentSemverVersion);
    }

    /**
     * Returns <code>true</code> if the version is the latest under major.minor version scope,
     *  <code>false</code> otherwise.
     * @param version the version.
     * @param taggedVersions the list of versions to check.
     * @return <code>true</code> if the version is the latest under major.minor version scope,
     *  <code>false</code> otherwise.
     */
    boolean isLatestMajorMinorVersion(@NonNull String version, @NonNull Collection<String> taggedVersions) {
        // list that holds all versions sharing major/minor.
        var semverVersions = new TreeSet<>();

        // add current version
        var currentSemverVersion = new Semver(version);
        semverVersions.add(currentSemverVersion);

        // add all other versions sharing the same major/minor version
        for (String taggedVersion : taggedVersions) {
            var semverTaggedVersion = new Semver(taggedVersion);
            if (semverTaggedVersion.getMajor().equals(currentSemverVersion.getMajor())
                    && semverTaggedVersion.getMinor().equals(currentSemverVersion.getMinor())) {
                semverVersions.add(semverTaggedVersion);
            }
        }

        // last item is the latest one.
        return semverVersions.last().equals(currentSemverVersion);
    }

    /**
     * Returns <code>true</code> if the version is the latest under major.minor.patch version scope,
     *  <code>false</code> otherwise.
     * @param version the version.
     * @param taggedVersions the list of versions to check.
     * @return <code>true</code> if the version is the latest under major.minor.patch version scope,
     *  <code>false</code> otherwise.
     */
    boolean isLatestMajorMinorPatchVersion(@NonNull String version, @NonNull Collection<String> taggedVersions) {
        // list that holds all versions sharing major/minor.
        var semverVersions = new TreeSet<>();

        // add current version
        var currentSemverVersion = new Semver(version);
        semverVersions.add(currentSemverVersion);

        // add all other versions sharing the same major/minor version
        for (String taggedVersion : taggedVersions) {
            var semverTaggedVersion = new Semver(taggedVersion);
            if (semverTaggedVersion.getMajor().equals(currentSemverVersion.getMajor())
                    && semverTaggedVersion.getMinor().equals(currentSemverVersion.getMinor())
                    && semverTaggedVersion.getPatch().equals(currentSemverVersion.getPatch())) {
                semverVersions.add(semverTaggedVersion);
            }
        }

        // last item is the latest one.
        return semverVersions.last().equals(currentSemverVersion);
    }

    /**
     * Gets the git tag from the version.
     * @param version the version.
     * @return the git tag for the given version.
     */
    String gitTag(@NonNull String version) {
        return String.format("v%s", version);
    }

    /**
     * Gets the ref from a branch name.
     * @param branchName the branch name.
     * @return the ref for the given branch name.
     */
    String branchRef(@NonNull String branchName) {
        return String.format("refs/heads/%s", branchName);
    }
}
