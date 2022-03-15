package com.fluxnetworks.plugin.common;

import com.fluxnetworks.java_api.FluxVersion;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class GlobalConstants {

	public static final Set<FluxVersion> SUPPORTED_WEBSITE_VERSIONS = EnumSet.of(
			FluxVersion.V2_0_0_PR_13
	);

	public static final Set<FluxVersion> DEPRECATED_WEBSITE_VERSIONS = Collections.emptySet();

}
