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

/**
 * The output variables. <br>
 * @author Julb.
 */
enum OutputVars {
    /**
     * The version.
     */
    VERSION("version"),

    /**
     * The Git tag.
     */
    GIT_TAG("git_tag"),

    /**
     * The Git tag with major if applicable.
     */
    GIT_TAG_MAJOR("git_tag_major"),

    /**
     * The Git tag with minor if applicable.
     */
    GIT_TAG_MINOR("git_tag_minor"),

    /**
     * The Git tag with patch if applicable.
     */
    GIT_TAG_PATCH("git_tag_patch"),

    /**
     * The Docker tag.
     */
    DOCKER_TAG("docker_tag"),

    /**
     * The Docker tag with major if applicable.
     */
    DOCKER_TAG_MAJOR("docker_tag_major"),

    /**
     * The Docker tag with minor if applicable.
     */
    DOCKER_TAG_MINOR("docker_tag_minor"),

    /**
     * The Docker tag with patch if applicable.
     */
    DOCKER_TAG_PATCH("docker_tag_patch"),

    /**
     * The major version.
     */
    VERSION_MAJOR("version_major"),

    /**
     * The minor version.
     */
    VERSION_MINOR("version_minor"),

    /**
     * The patch version.
     */
    VERSION_PATCH("version_patch"),

    /**
     * The suffix version.
     */
    VERSION_SUFFIX("version_suffix"),

    /**
     * The build version.
     */
    VERSION_BUILD("version_build"),

    /**
     * The next major version.
     */
    NEXT_MAJOR_VERSION("next_major_version"),

    /**
     * The next minor version.
     */
    NEXT_MINOR_VERSION("next_minor_version"),

    /**
     * The next minor version.
     */
    NEXT_PATCH_VERSION("next_patch_version"),

    /**
     * The next major version.
     */
    NEXT_MAJOR_SNAPSHOT_VERSION("next_major_snapshot_version"),

    /**
     * The next minor version.
     */
    NEXT_MINOR_SNAPSHOT_VERSION("next_minor_snapshot_version"),

    /**
     * The next minor version.
     */
    NEXT_PATCH_SNAPSHOT_VERSION("next_patch_snapshot_version"),

    /**
     * The trigger branch.
     */
    TRIGGER_BRANCH("trigger_branch"),

    /**
     * The trigger branch ref.
     */
    TRIGGER_BRANCH_REF("trigger_branch_ref"),

    /**
     * The run branch.
     */
    RUN_BRANCH("run_branch"),

    /**
     * The run branch ref.
     */
    RUN_BRANCH_REF("run_branch_ref"),

    /**
     * The target branch.
     */
    TARGET_BRANCH("target_branch"),

    /**
     * The target branch ref.
     */
    TARGET_BRANCH_REF("target_branch_ref");

    /**
     * The variable name.
     */
    private String key;

    /**
     * Default constructor.
     * @param key the key name.
     */
    OutputVars(String key) {
        this.key = key;
    }

    /**
     * Getter for property key.
     * @return Value of property key.
     */
    public String key() {
        return key;
    }
}
