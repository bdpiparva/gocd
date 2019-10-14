/*
 * Copyright 2019 ThoughtWorks, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import _ from "lodash";
import Stream from "mithril/stream";
import {ValidatableMixin} from "models/mixins/new_validatable_mixin";

export interface SiteUrlsJSON {
  site_url?: string;
  secure_site_url?: string;
}

export class SiteUrls extends ValidatableMixin {
  readonly siteUrl: Stream<string | undefined>;
  readonly secureSiteUrl: Stream<string | undefined>;

  constructor(siteUrl?: string, secureSiteUrl?: string) {
    super();
    this.siteUrl       = Stream(siteUrl);
    this.secureSiteUrl = Stream(secureSiteUrl);
    this.validateUrlPattern("siteUrl");
    this.validateUrlPattern("secureSiteUrl");
  }

  static fromJSON(data: SiteUrlsJSON) {
    return new SiteUrls(data.site_url, data.secure_site_url);
  }
}

export interface PurgeSettingsJSON {
  purge_start_disk_space?: number;
  purge_upto_disk_space?: number;
}

export interface PipelineManagementJSON {
  artifacts_dir: string;
  purge_settings?: PurgeSettingsJSON;
}

export class PurgeSettings extends ValidatableMixin {
  readonly purgeStartDiskSpace: Stream<number | undefined>;
  readonly purgeUptoDiskSpace: Stream<number | undefined>;
  readonly cleanupArtifact: Stream<boolean>;

  constructor(purgeStartDiskSpace?: number, purgeUptoDiskSpace?: number) {
    super();
    this.purgeStartDiskSpace = Stream(purgeStartDiskSpace);
    this.purgeUptoDiskSpace  = Stream(purgeUptoDiskSpace);
    this.cleanupArtifact     = Stream(this.isCleanupEnabled());
    this.validatePresenceOf("purgeStartDiskSpace",
                            {condition: () => this.cleanupArtifact(), message: "purge start disk space is empty"});
    this.validatePresenceOf("purgeUptoDiskSpace",
                            {condition: () => this.cleanupArtifact(), message: "purge upto disk space is empty"});
  }

  static fromJSON(data: PurgeSettingsJSON) {
    return new PurgeSettings(data.purge_start_disk_space, data.purge_upto_disk_space);
  }

  isCleanupEnabled(): boolean {
    return !!(this.purgeStartDiskSpace() || this.purgeUptoDiskSpace());
  }

  toJSON() {
    const purgeSettingsJSON: PurgeSettingsJSON = {};

    if (this.purgeStartDiskSpace()) {
      purgeSettingsJSON.purge_start_disk_space = this.purgeStartDiskSpace();
    }
    if (this.purgeUptoDiskSpace()) {
      purgeSettingsJSON.purge_upto_disk_space = this.purgeUptoDiskSpace();
    }
    return purgeSettingsJSON;
  }
}

export interface ArtifactConfigJSON {
  artifacts_dir: string;
  purge_settings?: PurgeSettingsJSON;
}

export class ArtifactConfig extends ValidatableMixin {
  readonly artifactsDir: Stream<string>;
  readonly purgeSettings: Stream<PurgeSettings>;

  constructor(artifactsDir: string, purgeStartdiskSpace?: number, purgeUptoDiskSpace?: number) {
    super();
    this.artifactsDir  = Stream(artifactsDir);
    this.purgeSettings = Stream(new PurgeSettings(purgeStartdiskSpace, purgeUptoDiskSpace));

    this.validatePresenceOf("artifactsDir", {message: "Please provide artifacts directory location"});
    this.validateAssociated("purgeSettings");
  }

  static fromJSON(data: ArtifactConfigJSON) {
    const artifactConfig = new ArtifactConfig(data.artifacts_dir);
    if (data.purge_settings) {
      artifactConfig.purgeSettings(PurgeSettings.fromJSON(data.purge_settings));
    }

    return artifactConfig;
  }

  isArtifactsCleanupEnabled(): boolean {
    return this.purgeSettings().isCleanupEnabled();
  }

  cleanupArtifact(): boolean {
    return this.purgeSettings().cleanupArtifact();
  }

  toJSON() {
    const artifactConfigJSON: ArtifactConfigJSON = {
      artifacts_dir: this.artifactsDir(),
    };

    if (!_.isEmpty(this.purgeSettings().toJSON())) {
      artifactConfigJSON.purge_settings = this.purgeSettings().toJSON();
    }
    return artifactConfigJSON;
  }
}

interface DefaultJobTimeoutJSON {
  default_job_timeout: string;
}

export class DefaultJobTimeout extends ValidatableMixin {
  readonly defaultJobTimeout: Stream<number>;
  readonly neverTimeout: Stream<boolean>;

  constructor(defaultJobTimeout: number) {
    super();
    this.defaultJobTimeout = Stream(defaultJobTimeout);
    this.neverTimeout      = Stream(defaultJobTimeout === 0);

    this.validatePresenceOf("defaultJobTimeout", {condition: this.neverTimeout});
  }

  static fromJSON(data: DefaultJobTimeoutJSON) {
    return new DefaultJobTimeout(parseInt(data.default_job_timeout, 10));
  }

  toJSON() {
    return {
      default_job_timeout: this.defaultJobTimeout
    };
  }

  isValid(): boolean {
    const valid = super.isValid();

    if (!this.neverTimeout() && this.defaultJobTimeout() === 0) {
      this.errors().add("defaultJobTimeout", " Timeout cannot be a negative number as it represents number of minutes");
      return false;
    }

    return valid;
  }
}
