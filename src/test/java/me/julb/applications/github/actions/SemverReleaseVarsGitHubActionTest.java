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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTag;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.LocalPagedIterable;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import me.julb.sdk.github.actions.kit.GitHubActionsKit;

/**
 * Test class for {@link SemverReleaseVarsGitHubAction} class. <br>
 * @author Julb.
 */
@ExtendWith(MockitoExtension.class)
class SemverReleaseVarsGitHubActionTest {

    /**
     * The class under test.
     */
    private SemverReleaseVarsGitHubAction githubAction = null;

    /**
     * A mock for GitHub action kit.
     */
    @Mock
    private GitHubActionsKit ghActionsKitMock;

    /**
     * A mock for GitHub API.
     */
    @Mock
    private GitHub ghApiMock;

    /**
     * A mock for GitHub repository.
     */
    @Mock
    private GHRepository ghRepositoryMock;

    /**
     * @throws java.lang.Exception
     */
    @BeforeEach
    void setUp()
        throws Exception {
        githubAction = new SemverReleaseVarsGitHubAction();
        githubAction.setGhActionsKit(ghActionsKitMock);
        githubAction.setGhApi(ghApiMock);
        githubAction.setGhRepository(ghRepositoryMock);
    }

    /**
     * Test method.
     */
    @Test
    void whenGetInputPackageVersion_thenReturnValue()
        throws Exception {
        when(this.ghActionsKitMock.getInput("package_version")).thenReturn(Optional.of("1.0.0"));

        assertThat(this.githubAction.getInputPackageVersion()).isPresent().contains("1.0.0");

        verify(this.ghActionsKitMock).getInput("package_version");
    }

    /**
     * Test method.
     */
    @Test
    void whenGetInputPackageVersionWithPrefix_thenReturnValue()
        throws Exception {
        when(this.ghActionsKitMock.getInput("package_version")).thenReturn(Optional.of("v1.0.0"));

        assertThat(this.githubAction.getInputPackageVersion()).isPresent().contains("1.0.0");

        verify(this.ghActionsKitMock).getInput("package_version");
    }

    /**
     * Test method.
     */
    @Test
    void whenGetInputPackageVersionNotProvided_thenReturnEmpty() {
        when(this.ghActionsKitMock.getInput("package_version")).thenReturn(Optional.empty());

        assertThat(this.githubAction.getInputPackageVersion()).isEmpty();

        verify(this.ghActionsKitMock).getInput("package_version");
    }

    /**
     * Test method.
     */
    @Test
    void whenGetReleaseBranchNameIsBranch_thenReturnValue()
        throws Exception {
        when(this.ghActionsKitMock.isGitHubRefTypeBranch()).thenReturn(true);
        when(this.ghActionsKitMock.getGitHubRefName()).thenReturn("branch-name");

        assertThat(this.githubAction.getReleaseBranchName()).isEqualTo("branch-name");

        verify(this.ghActionsKitMock).isGitHubRefTypeBranch();
        verify(this.ghActionsKitMock).getGitHubRefName();
    }

    /**
     * Test method.
     */
    @Test
    void whenGetReleaseBranchNameIsTag_thenThrowIllegalArgumentException()
        throws Exception {
        when(this.ghActionsKitMock.isGitHubRefTypeBranch()).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> this.githubAction.getReleaseBranchName());

        verify(this.ghActionsKitMock).isGitHubRefTypeBranch();
    }

    /**
     * Test method.
     */
    @Test
    void whenGetRunReleaseBranchName_thenReturnValue()
        throws Exception {
        when(this.ghActionsKitMock.getGitHubRunId()).thenReturn("123456");

        assertThat(this.githubAction.getRunReleaseBranchName()).isEqualTo("releases/run-123456");

        verify(this.ghActionsKitMock).getGitHubRunId();
    }

    /**
     * Test method.
     */
    @Test
    void whenGetReleaseVersionBranchNameWithoutVersion_thenReturnProvidedVersion()
        throws Exception {
        assertThat(this.githubAction.getReleaseVersion(Optional.of("1.0.0"), "releases/trigger")).isEqualTo("1.0.0");
    }

    /**
     * Test method.
     */
    @Test
    void whenGetReleaseVersionBranchNameWithVersion_thenReturnBranchVersion()
        throws Exception {
        assertThat(this.githubAction.getReleaseVersion(Optional.of("1.0.0"), "releases/trigger-1.2.3")).isEqualTo("1.2.3");
        assertThat(this.githubAction.getReleaseVersion(Optional.of("1.0.0"), "releases/trigger-v1.2.3")).isEqualTo("1.2.3");
        assertThat(this.githubAction.getReleaseVersion(Optional.of("1.0.0"), "releases/trigger-1.0.3-rc1+abcde1234")).isEqualTo("1.0.3-rc1+abcde1234");
    }

    /**
     * Test method.
     */
    @Test
    void whenGetReleaseVersionBranchNameWithoutVersionAndNoProvidedVersion_thenThrowNoSuchElementException()
        throws Exception {
        var emptyOpt = Optional.<String> empty();
        assertThrows(NoSuchElementException.class, () -> this.githubAction.getReleaseVersion(emptyOpt, "releases/trigger"));
    }

    /**
     * Test method.
     */
    @Test
    void whenGetReleaseVersionBranchNameIsInvalid_thenThrowIllegalArgumentException()
        throws Exception {
        var emptyOpt = Optional.<String> empty();
        assertThrows(IllegalArgumentException.class, () -> this.githubAction.getReleaseVersion(emptyOpt, "branch-name"));
    }

    /**
     * Test method.
     */
    @Test
    void whenGetReleaseVersionNull_thenThrowNullPointerException()
        throws Exception {
        var emptyOpt = Optional.<String> empty();
        assertThrows(NullPointerException.class, () -> this.githubAction.getReleaseVersion(null, "branch-name"));
        assertThrows(NullPointerException.class, () -> this.githubAction.getReleaseVersion(emptyOpt, null));
    }

    /**
     * Test method.
     */
    @Test
    void whenGetSemverVersion_thenReturnValidVersion()
        throws Exception {
        var semver = this.githubAction.getSemverVersion("1.0.3-rc1+abcde1234");
        assertThat(semver.getMajor()).isEqualTo(1);
        assertThat(semver.getMinor()).isZero();
        assertThat(semver.getPatch()).isEqualTo(3);
        assertThat(semver.getSuffixTokens()).contains("rc1");
        assertThat(semver.getBuild()).contains("abcde1234");
    }

    /**
     * Test method.
     */
    @Test
    void whenGetSemverVersionInvalid_thenThrowIllegalArgumentException()
        throws Exception {
        assertThrows(IllegalArgumentException.class, () -> this.githubAction.getSemverVersion("abcd"));
    }

    /**
     * Test method.
     */
    @Test
    void whenGetSemverVersionNull_thenThrowNullPointerException()
        throws Exception {
        assertThrows(NullPointerException.class, () -> this.githubAction.getSemverVersion(null));
    }

    /**
     * Test method.
     */
    @Test
    @SuppressWarnings("java:S5961")
    void whenExecuteWithoutMaintenanceBranchAndLatest_thenReturnValidValues()
        throws Exception {
        var spy = spy(this.githubAction);

        var ghTag = Mockito.mock(GHTag.class);

        when(this.ghActionsKitMock.getGitHubRepository()).thenReturn("octocat/Hello-World");
        doReturn(Optional.of("1.1.0-rc.1+abcdef")).when(spy).getInputPackageVersion();
        doReturn("releases/trigger").when(spy).getReleaseBranchName();
        doReturn("releases/run-123456").when(spy).getRunReleaseBranchName();

        doNothing().when(spy).connectApi();

        when(this.ghApiMock.getRepository("octocat/Hello-World")).thenReturn(ghRepositoryMock);
        doReturn(Map.of("1.0.0", ghTag)).when(spy).getTags();

        doReturn(Optional.empty()).when(spy).getMaintenanceBranchName("1.1.0-rc.1+abcdef");
        when(this.ghRepositoryMock.getDefaultBranch()).thenReturn("main");

        doReturn(true).when(spy).isLatestMajorVersion("1.1.0-rc.1+abcdef", Set.of("1.0.0"));
        doReturn(true).when(spy).isLatestMajorMinorVersion("1.1.0-rc.1+abcdef", Set.of("1.0.0"));
        doReturn(true).when(spy).isLatestMajorMinorPatchVersion("1.1.0-rc.1+abcdef", Set.of("1.0.0"));

        spy.execute();

        verify(this.ghActionsKitMock).getGitHubRepository();

        verify(spy).getInputPackageVersion();
        verify(spy).getReleaseBranchName();
        verify(spy).getRunReleaseBranchName();
        verify(spy).getMaintenanceBranchName("1.1.0-rc.1+abcdef");
        verify(spy).connectApi();
        verify(spy).getTags();
        verify(spy).isLatestMajorVersion("1.1.0-rc.1+abcdef", Set.of("1.0.0"));
        verify(spy).isLatestMajorMinorVersion("1.1.0-rc.1+abcdef", Set.of("1.0.0"));
        verify(spy).isLatestMajorMinorPatchVersion("1.1.0-rc.1+abcdef", Set.of("1.0.0"));
        verify(this.ghApiMock).getRepository("octocat/Hello-World");
        verify(this.ghRepositoryMock).getDefaultBranch();

        verify(this.ghActionsKitMock).setOutput(OutputVars.VERSION.key(), "1.1.0-rc.1+abcdef");

        verify(this.ghActionsKitMock).setOutput(OutputVars.GIT_TAG.key(), "v1.1.0-rc.1+abcdef");
        verify(this.ghActionsKitMock).setOptionalOutput(OutputVars.GIT_TAG_MAJOR.key(), Optional.of("v1"));
        verify(this.ghActionsKitMock).setOptionalOutput(OutputVars.GIT_TAG_MINOR.key(), Optional.of("v1.1"));
        verify(this.ghActionsKitMock).setOptionalOutput(OutputVars.GIT_TAG_PATCH.key(), Optional.of("v1.1.0"));

        verify(this.ghActionsKitMock).setOutput(OutputVars.DOCKER_TAG.key(), "1.1.0-rc.1+abcdef");
        verify(this.ghActionsKitMock).setOptionalOutput(OutputVars.DOCKER_TAG_MAJOR.key(), Optional.of("1"));
        verify(this.ghActionsKitMock).setOptionalOutput(OutputVars.DOCKER_TAG_MINOR.key(), Optional.of("1.1"));
        verify(this.ghActionsKitMock).setOptionalOutput(OutputVars.DOCKER_TAG_PATCH.key(), Optional.of("1.1.0"));

        verify(this.ghActionsKitMock).setOutput(OutputVars.VERSION_MAJOR.key(), "1");
        verify(this.ghActionsKitMock).setOutput(OutputVars.VERSION_MINOR.key(), "1");
        verify(this.ghActionsKitMock).setOutput(OutputVars.VERSION_PATCH.key(), "0");
        verify(this.ghActionsKitMock).setOptionalOutput(OutputVars.VERSION_SUFFIX.key(), Optional.of("rc.1"));
        verify(this.ghActionsKitMock).setOptionalOutput(OutputVars.VERSION_BUILD.key(), Optional.of("abcdef"));

        verify(this.ghActionsKitMock).setOutput(OutputVars.NEXT_MAJOR_VERSION.key(), "2.0.0");
        verify(this.ghActionsKitMock).setOutput(OutputVars.NEXT_MINOR_VERSION.key(), "1.2.0");
        verify(this.ghActionsKitMock).setOutput(OutputVars.NEXT_PATCH_VERSION.key(), "1.1.1");
        verify(this.ghActionsKitMock).setOutput(OutputVars.NEXT_MAJOR_SNAPSHOT_VERSION.key(), "2.0.0-SNAPSHOT");
        verify(this.ghActionsKitMock).setOutput(OutputVars.NEXT_MINOR_SNAPSHOT_VERSION.key(), "1.2.0-SNAPSHOT");
        verify(this.ghActionsKitMock).setOutput(OutputVars.NEXT_PATCH_SNAPSHOT_VERSION.key(), "1.1.1-SNAPSHOT");

        verify(this.ghActionsKitMock).setOutput(OutputVars.TRIGGER_BRANCH.key(), "releases/trigger");
        verify(this.ghActionsKitMock).setOutput(OutputVars.TRIGGER_BRANCH_REF.key(), "refs/heads/releases/trigger");
        verify(this.ghActionsKitMock).setOutput(OutputVars.RUN_BRANCH.key(), "releases/run-123456");
        verify(this.ghActionsKitMock).setOutput(OutputVars.RUN_BRANCH_REF.key(), "refs/heads/releases/run-123456");
        verify(this.ghActionsKitMock).setOutput(OutputVars.TARGET_BRANCH.key(), "main");
        verify(this.ghActionsKitMock).setOutput(OutputVars.TARGET_BRANCH_REF.key(), "refs/heads/main");
    }

    /**
     * Test method.
     */
    @Test
    @SuppressWarnings("java:S5961")
    void whenExecuteWithoutMaintenanceBranchAndNotLatest_thenReturnValidValues()
        throws Exception {
        var spy = spy(this.githubAction);

        var ghTag = Mockito.mock(GHTag.class);

        when(this.ghActionsKitMock.getGitHubRepository()).thenReturn("octocat/Hello-World");
        doReturn(Optional.of("1.1.0-rc.1+abcdef")).when(spy).getInputPackageVersion();
        doReturn("releases/trigger").when(spy).getReleaseBranchName();
        doReturn("releases/run-123456").when(spy).getRunReleaseBranchName();

        doNothing().when(spy).connectApi();

        when(this.ghApiMock.getRepository("octocat/Hello-World")).thenReturn(ghRepositoryMock);
        doReturn(Map.of("1.1.1", ghTag)).when(spy).getTags();

        doReturn(Optional.empty()).when(spy).getMaintenanceBranchName("1.1.0-rc.1+abcdef");
        when(this.ghRepositoryMock.getDefaultBranch()).thenReturn("main");

        doReturn(false).when(spy).isLatestMajorVersion("1.1.0-rc.1+abcdef", Set.of("1.1.1"));
        doReturn(false).when(spy).isLatestMajorMinorVersion("1.1.0-rc.1+abcdef", Set.of("1.1.1"));
        doReturn(false).when(spy).isLatestMajorMinorPatchVersion("1.1.0-rc.1+abcdef", Set.of("1.1.1"));

        spy.execute();

        verify(this.ghActionsKitMock).getGitHubRepository();

        verify(spy).getInputPackageVersion();
        verify(spy).getReleaseBranchName();
        verify(spy).getRunReleaseBranchName();
        verify(spy).getMaintenanceBranchName("1.1.0-rc.1+abcdef");
        verify(spy).connectApi();
        verify(spy).getTags();
        verify(spy).isLatestMajorVersion("1.1.0-rc.1+abcdef", Set.of("1.1.1"));
        verify(spy).isLatestMajorMinorVersion("1.1.0-rc.1+abcdef", Set.of("1.1.1"));
        verify(spy).isLatestMajorMinorPatchVersion("1.1.0-rc.1+abcdef", Set.of("1.1.1"));
        verify(this.ghApiMock).getRepository("octocat/Hello-World");
        verify(this.ghRepositoryMock).getDefaultBranch();

        verify(this.ghActionsKitMock).setOutput(OutputVars.VERSION.key(), "1.1.0-rc.1+abcdef");

        verify(this.ghActionsKitMock).setOutput(OutputVars.GIT_TAG.key(), "v1.1.0-rc.1+abcdef");
        verify(this.ghActionsKitMock).setOptionalOutput(OutputVars.GIT_TAG_MAJOR.key(), Optional.empty());
        verify(this.ghActionsKitMock).setOptionalOutput(OutputVars.GIT_TAG_MINOR.key(), Optional.empty());
        verify(this.ghActionsKitMock).setOptionalOutput(OutputVars.GIT_TAG_PATCH.key(), Optional.empty());

        verify(this.ghActionsKitMock).setOutput(OutputVars.DOCKER_TAG.key(), "1.1.0-rc.1+abcdef");
        verify(this.ghActionsKitMock).setOptionalOutput(OutputVars.DOCKER_TAG_MAJOR.key(), Optional.empty());
        verify(this.ghActionsKitMock).setOptionalOutput(OutputVars.DOCKER_TAG_MINOR.key(), Optional.empty());
        verify(this.ghActionsKitMock).setOptionalOutput(OutputVars.DOCKER_TAG_PATCH.key(), Optional.empty());

        verify(this.ghActionsKitMock).setOutput(OutputVars.VERSION_MAJOR.key(), "1");
        verify(this.ghActionsKitMock).setOutput(OutputVars.VERSION_MINOR.key(), "1");
        verify(this.ghActionsKitMock).setOutput(OutputVars.VERSION_PATCH.key(), "0");
        verify(this.ghActionsKitMock).setOptionalOutput(OutputVars.VERSION_SUFFIX.key(), Optional.of("rc.1"));
        verify(this.ghActionsKitMock).setOptionalOutput(OutputVars.VERSION_BUILD.key(), Optional.of("abcdef"));

        verify(this.ghActionsKitMock).setOutput(OutputVars.NEXT_MAJOR_VERSION.key(), "2.0.0");
        verify(this.ghActionsKitMock).setOutput(OutputVars.NEXT_MINOR_VERSION.key(), "1.2.0");
        verify(this.ghActionsKitMock).setOutput(OutputVars.NEXT_PATCH_VERSION.key(), "1.1.1");
        verify(this.ghActionsKitMock).setOutput(OutputVars.NEXT_MAJOR_SNAPSHOT_VERSION.key(), "2.0.0-SNAPSHOT");
        verify(this.ghActionsKitMock).setOutput(OutputVars.NEXT_MINOR_SNAPSHOT_VERSION.key(), "1.2.0-SNAPSHOT");
        verify(this.ghActionsKitMock).setOutput(OutputVars.NEXT_PATCH_SNAPSHOT_VERSION.key(), "1.1.1-SNAPSHOT");

        verify(this.ghActionsKitMock).setOutput(OutputVars.TRIGGER_BRANCH.key(), "releases/trigger");
        verify(this.ghActionsKitMock).setOutput(OutputVars.TRIGGER_BRANCH_REF.key(), "refs/heads/releases/trigger");
        verify(this.ghActionsKitMock).setOutput(OutputVars.RUN_BRANCH.key(), "releases/run-123456");
        verify(this.ghActionsKitMock).setOutput(OutputVars.RUN_BRANCH_REF.key(), "refs/heads/releases/run-123456");
        verify(this.ghActionsKitMock).setOutput(OutputVars.TARGET_BRANCH.key(), "main");
        verify(this.ghActionsKitMock).setOutput(OutputVars.TARGET_BRANCH_REF.key(), "refs/heads/main");
    }

    /**
     * Test method.
     */
    @Test
    @SuppressWarnings("java:S5961")
    void whenExecuteWithMaintenanceBranchAndLatest_thenReturnValidValues()
        throws Exception {
        var spy = spy(this.githubAction);

        var ghTag = Mockito.mock(GHTag.class);

        when(this.ghActionsKitMock.getGitHubRepository()).thenReturn("octocat/Hello-World");
        doReturn(Optional.of("1.1.0")).when(spy).getInputPackageVersion();
        doReturn("releases/trigger").when(spy).getReleaseBranchName();
        doReturn("releases/run-123456").when(spy).getRunReleaseBranchName();

        doNothing().when(spy).connectApi();

        when(this.ghApiMock.getRepository("octocat/Hello-World")).thenReturn(ghRepositoryMock);
        doReturn(Map.of("1.0.0", ghTag)).when(spy).getTags();

        doReturn(Optional.of("maintenances/1.x")).when(spy).getMaintenanceBranchName("1.1.0");
        when(this.ghRepositoryMock.getDefaultBranch()).thenReturn("main");

        doReturn(true).when(spy).isLatestMajorVersion("1.1.0", Set.of("1.0.0"));
        doReturn(true).when(spy).isLatestMajorMinorVersion("1.1.0", Set.of("1.0.0"));
        doReturn(true).when(spy).isLatestMajorMinorPatchVersion("1.1.0", Set.of("1.0.0"));

        spy.execute();

        verify(this.ghActionsKitMock).getGitHubRepository();

        verify(spy).getInputPackageVersion();
        verify(spy).getReleaseBranchName();
        verify(spy).getRunReleaseBranchName();
        verify(spy).getMaintenanceBranchName("1.1.0");
        verify(spy).connectApi();
        verify(spy).getTags();
        verify(spy).isLatestMajorVersion("1.1.0", Set.of("1.0.0"));
        verify(spy).isLatestMajorMinorVersion("1.1.0", Set.of("1.0.0"));
        verify(spy).isLatestMajorMinorPatchVersion("1.1.0", Set.of("1.0.0"));
        verify(this.ghApiMock).getRepository("octocat/Hello-World");
        verify(this.ghRepositoryMock).getDefaultBranch();

        verify(this.ghActionsKitMock).setOutput(OutputVars.VERSION.key(), "1.1.0");

        verify(this.ghActionsKitMock).setOutput(OutputVars.GIT_TAG.key(), "v1.1.0");
        verify(this.ghActionsKitMock).setOptionalOutput(OutputVars.GIT_TAG_MAJOR.key(), Optional.of("v1"));
        verify(this.ghActionsKitMock).setOptionalOutput(OutputVars.GIT_TAG_MINOR.key(), Optional.of("v1.1"));
        verify(this.ghActionsKitMock).setOptionalOutput(OutputVars.GIT_TAG_PATCH.key(), Optional.of("v1.1.0"));

        verify(this.ghActionsKitMock).setOutput(OutputVars.DOCKER_TAG.key(), "1.1.0");
        verify(this.ghActionsKitMock).setOptionalOutput(OutputVars.DOCKER_TAG_MAJOR.key(), Optional.of("1"));
        verify(this.ghActionsKitMock).setOptionalOutput(OutputVars.DOCKER_TAG_MINOR.key(), Optional.of("1.1"));
        verify(this.ghActionsKitMock).setOptionalOutput(OutputVars.DOCKER_TAG_PATCH.key(), Optional.of("1.1.0"));

        verify(this.ghActionsKitMock).setOutput(OutputVars.VERSION_MAJOR.key(), "1");
        verify(this.ghActionsKitMock).setOutput(OutputVars.VERSION_MINOR.key(), "1");
        verify(this.ghActionsKitMock).setOutput(OutputVars.VERSION_PATCH.key(), "0");
        verify(this.ghActionsKitMock).setOptionalOutput(OutputVars.VERSION_SUFFIX.key(), Optional.empty());
        verify(this.ghActionsKitMock).setOptionalOutput(OutputVars.VERSION_BUILD.key(), Optional.empty());

        verify(this.ghActionsKitMock).setOutput(OutputVars.NEXT_MAJOR_VERSION.key(), "2.0.0");
        verify(this.ghActionsKitMock).setOutput(OutputVars.NEXT_MINOR_VERSION.key(), "1.2.0");
        verify(this.ghActionsKitMock).setOutput(OutputVars.NEXT_PATCH_VERSION.key(), "1.1.1");
        verify(this.ghActionsKitMock).setOutput(OutputVars.NEXT_MAJOR_SNAPSHOT_VERSION.key(), "2.0.0-SNAPSHOT");
        verify(this.ghActionsKitMock).setOutput(OutputVars.NEXT_MINOR_SNAPSHOT_VERSION.key(), "1.2.0-SNAPSHOT");
        verify(this.ghActionsKitMock).setOutput(OutputVars.NEXT_PATCH_SNAPSHOT_VERSION.key(), "1.1.1-SNAPSHOT");

        verify(this.ghActionsKitMock).setOutput(OutputVars.TRIGGER_BRANCH.key(), "releases/trigger");
        verify(this.ghActionsKitMock).setOutput(OutputVars.TRIGGER_BRANCH_REF.key(), "refs/heads/releases/trigger");
        verify(this.ghActionsKitMock).setOutput(OutputVars.RUN_BRANCH.key(), "releases/run-123456");
        verify(this.ghActionsKitMock).setOutput(OutputVars.RUN_BRANCH_REF.key(), "refs/heads/releases/run-123456");
        verify(this.ghActionsKitMock).setOutput(OutputVars.TARGET_BRANCH.key(), "maintenances/1.x");
        verify(this.ghActionsKitMock).setOutput(OutputVars.TARGET_BRANCH_REF.key(), "refs/heads/maintenances/1.x");
    }

    /**
     * Test method.
     */
    @Test
    void whenExecuteTagWithVersionAlreadyExist_thenThrowIllegalArgumentException()
        throws Exception {
        var spy = spy(this.githubAction);

        var ghTag = Mockito.mock(GHTag.class);

        when(this.ghActionsKitMock.getGitHubRepository()).thenReturn("octocat/Hello-World");
        doReturn(Optional.of("1.0.0")).when(spy).getInputPackageVersion();
        doReturn("releases/trigger").when(spy).getReleaseBranchName();

        doNothing().when(spy).connectApi();

        when(this.ghApiMock.getRepository("octocat/Hello-World")).thenReturn(ghRepositoryMock);
        doReturn(Map.of("1.0.0", ghTag)).when(spy).getTags();

        assertThrows(CompletionException.class, () -> spy.execute());

        verify(this.ghActionsKitMock).getGitHubRepository();

        verify(spy).getInputPackageVersion();
        verify(spy).getReleaseBranchName();
        verify(spy).connectApi();
        verify(spy).getTags();
        verify(this.ghApiMock).getRepository("octocat/Hello-World");
        verify(this.ghActionsKitMock, never()).setOutput(anyString(), any());
    }

    /**
     * Test method.
     */
    @Test
    void whenConnectApi_thenVerifyOK()
        throws Exception {
        when(ghActionsKitMock.getRequiredEnv("GITHUB_TOKEN")).thenReturn("token");
        when(ghActionsKitMock.getGitHubApiUrl()).thenReturn("https://api.github.com");

        this.githubAction.connectApi();

        verify(ghActionsKitMock).getRequiredEnv("GITHUB_TOKEN");
        verify(ghActionsKitMock).getGitHubApiUrl();
        verify(ghActionsKitMock, times(2)).debug(Mockito.anyString());
        verify(ghApiMock).checkApiUrlValidity();
    }

    /**
     * Test method.
     */
    @Test
    void whenIsLatestMajorVersion_thenReturnValidValue()
        throws Exception {
        assertThat(this.githubAction.isLatestMajorVersion("1.3.0", List.of())).isTrue();
        assertThat(this.githubAction.isLatestMajorVersion("1.3.0", List.of("1.0.0", "1.2.0", "2.0.0"))).isTrue();
        assertThat(this.githubAction.isLatestMajorVersion("1.3.0", List.of("1.0.0", "1.4.0", "2.0.0"))).isFalse();
    }

    /**
     * Test method.
     */
    @Test
    void whenIsLatestMajorVersionNull_thenThrowNullPointerException()
        throws Exception {
        var emptyList = List.<String> of();
        assertThrows(NullPointerException.class, () -> this.githubAction.isLatestMajorVersion(null, emptyList));
        assertThrows(NullPointerException.class, () -> this.githubAction.isLatestMajorVersion("1.0.0", null));
    }

    /**
     * Test method.
     */
    @Test
    void whenIsLatestMajorMinorVersion_thenReturnValidValue()
        throws Exception {
        assertThat(this.githubAction.isLatestMajorMinorVersion("1.3.3", List.of())).isTrue();
        assertThat(this.githubAction.isLatestMajorMinorVersion("1.3.3", List.of("1.0.0", "1.2.0", "1.3.0", "1.4.0", "2.0.0"))).isTrue();
        assertThat(this.githubAction.isLatestMajorMinorVersion("1.3.3", List.of("1.0.0", "1.2.0", "1.3.0", "1.3.4", "1.4.0", "2.0.0"))).isFalse();
    }

    /**
     * Test method.
     */
    @Test
    void whenIsLatestMajorMinorVersionNull_thenThrowNullPointerException()
        throws Exception {
        var emptyList = List.<String> of();
        assertThrows(NullPointerException.class, () -> this.githubAction.isLatestMajorMinorVersion(null, emptyList));
        assertThrows(NullPointerException.class, () -> this.githubAction.isLatestMajorMinorVersion("1.0.0", null));
    }

    /**
     * Test method.
     */
    @Test
    void whenIsLatestMajorMinorPatchVersion_thenReturnValidValue()
        throws Exception {
        assertThat(this.githubAction.isLatestMajorMinorPatchVersion("1.3.3-rc.1", List.of())).isTrue();
        assertThat(this.githubAction.isLatestMajorMinorPatchVersion("1.3.3-rc.1", List.of("1.0.0", "1.2.0", "1.3.3-rc.0", "1.4.0", "2.0.0"))).isTrue();
        assertThat(this.githubAction.isLatestMajorMinorPatchVersion("1.3.3-rc.1", List.of("1.0.0", "1.2.0", "1.3.0", "1.3.3-rc.2", "1.3.4", "1.4.0", "2.0.0"))).isFalse();
    }

    /**
     * Test method.
     */
    @Test
    void whenIsLatestMajorMinorPatchVersionNull_thenThrowNullPointerException()
        throws Exception {
        var emptyList = List.<String> of();
        assertThrows(NullPointerException.class, () -> this.githubAction.isLatestMajorMinorPatchVersion(null, emptyList));
        assertThrows(NullPointerException.class, () -> this.githubAction.isLatestMajorMinorPatchVersion("1.0.0", null));
    }

    /**
     * Test method.
     */
    @Test
    void whenGetMaintenanceBranchNameMaintenanceBranchMajor_thenReturnPresent()
        throws Exception {
        when(this.ghRepositoryMock.getBranches()).thenReturn(Map.of("main", mock(GHBranch.class), "maintenances/1.x", mock(GHBranch.class)));

        assertThat(this.githubAction.getMaintenanceBranchName("1.0.0")).isPresent().contains("maintenances/1.x");

        verify(this.ghRepositoryMock).getBranches();
    }

    /**
     * Test method.
     */
    @Test
    void whenGetMaintenanceBranchNameMaintenanceBranchMajorMinor_thenReturnPresent()
        throws Exception {
        when(this.ghRepositoryMock.getBranches()).thenReturn(Map.of("main", mock(GHBranch.class), "maintenances/1.0.x", mock(GHBranch.class)));

        assertThat(this.githubAction.getMaintenanceBranchName("1.0.0")).isPresent().contains("maintenances/1.0.x");

        verify(this.ghRepositoryMock).getBranches();
    }

    /**
     * Test method.
     */
    @Test
    void whenGetMaintenanceBranchNameNoMaintenanceBranch_thenReturnEmpty()
        throws Exception {
        when(this.ghRepositoryMock.getBranches()).thenReturn(Map.of("main", mock(GHBranch.class), "maintenances/2.0.x", mock(GHBranch.class), "maintenances/1.1.x", mock(GHBranch.class)));

        assertThat(this.githubAction.getMaintenanceBranchName("1.0.0")).isEmpty();

        verify(this.ghRepositoryMock).getBranches();
    }

    /**
     * Test method.
     */
    @Test
    void whenGetMaintenanceBranchNameNull_thenThrowNullPointerException()
        throws Exception {
        assertThrows(NullPointerException.class, () -> this.githubAction.getMaintenanceBranchName(null));
    }

    /**
     * Test method.
     */
    @Test
    void whenGetTags_thenReturnTags()
        throws Exception {
        var ghTag1 = Mockito.mock(GHTag.class);
        when(ghTag1.getName()).thenReturn("v1.0.0");

        var ghTag2 = Mockito.mock(GHTag.class);
        when(ghTag2.getName()).thenReturn("2.0.0");

        when(ghRepositoryMock.listTags()).thenReturn(new LocalPagedIterable<>(List.of(ghTag1, ghTag2)));

        assertThat(this.githubAction.getTags()).isEqualTo(Map.of("1.0.0", ghTag1, "2.0.0", ghTag2));

        verify(ghRepositoryMock).listTags();
        verify(ghTag1).getName();
        verify(ghTag2).getName();
    }

    /**
     * Test method.
     */
    @Test
    void whenGitTagPresent_thenReturnTag() {
        assertThat(this.githubAction.gitTag("1")).isEqualTo("v1");
    }

    /**
     * Test method.
     */
    @Test
    void whenGitTagNull_thenThrowNullPointerException()
        throws Exception {
        assertThrows(NullPointerException.class, () -> this.githubAction.gitTag(null));
    }

    /**
     * Test method.
     */
    @Test
    void whenBranchRefNamePresent_thenReturnRefValue() {
        assertThat(this.githubAction.branchRef("branch-name")).isEqualTo("refs/heads/branch-name");
    }

    /**
     * Test method.
     */
    @Test
    void whenBranchRefNameNull_thenThrowNullPointerException()
        throws Exception {
        assertThrows(NullPointerException.class, () -> this.githubAction.branchRef(null));
    }

}
