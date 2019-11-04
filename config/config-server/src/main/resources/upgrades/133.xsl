<?xml version="1.0"?>
<!--
  ~ Copyright 2019 ThoughtWorks, Inc.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:template match="/cruise/@schemaVersion">
        <xsl:attribute name="schemaVersion">133</xsl:attribute>
    </xsl:template>
    <!-- Copy everything -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="//authConfig">
        <xsl:copy>
            <!-- Explicitly add allowOnlyKnownUsersToLogin to false when attribute does not exists on security tag -->
            <xsl:if test="not(//security/@allowOnlyKnownUsersToLogin)">
                <xsl:attribute name="allowOnlyKnownUsersToLogin">false</xsl:attribute>
            </xsl:if>
            <!-- Copy value of allowOnlyKnownUsersToLogin attribute when it exists security tag -->
            <xsl:copy-of select="//security/@allowOnlyKnownUsersToLogin"/>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>


    <!-- Remove allowOnlyKnownUsersToLogin attribute from security tag -->
    <xsl:template match="//security/@allowOnlyKnownUsersToLogin"/>
</xsl:stylesheet>
