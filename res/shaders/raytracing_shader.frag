#version 150 core

uniform sampler2D u_NormalMap;

uniform vec3 u_EyePosition;
uniform vec3 u_LightPosition;
uniform vec3 u_LookDirection;
uniform vec3 u_RightDirection;
uniform vec3 u_AvatarPosition;
uniform vec2 u_ScreenDimension;

in vec4 pass_Position;
in vec2 pass_TexCoord;

out vec4 out_Color;

const int NEW_RAY_COUNT = 2;
const int BOUNCE_COUNT = 2;
const float EPSILON = 0.001;
const float INFINITY = 10000.0;
const float PI_HALF = 1.57079632679;

struct IntersectionData {
    float dist;
    vec3 nor;
    vec4 col;
};

struct NewRaysData {
    vec4 col;
	vec3 origRay;
	vec3 rayDir;
	//vec3[BOUNCE_COUNT] rayDir_arr;
};

vec4 calculateIntersectionWithSphere(vec3 center, float radius, vec3 origRay, vec3 rayDir) {
	float dist;
	vec3 normal;
	vec3 distToCenter = center - origRay;
	float projdRayDir = dot(distToCenter, rayDir);
	vec3 rayDirComp = rayDir * projdRayDir;
	vec3 ray_perp_comp = distToCenter - rayDirComp;
	float distFromRay = length(ray_perp_comp);
	if (projdRayDir < 0.0) {
	    distFromRay = 1000000000.0;
	}
	if (distFromRay < radius) {    // The ray intersects the sphere.
	    float rayInsideSphereHalf = sqrt(radius*radius - distFromRay*distFromRay);

		dist = length(rayDirComp) - rayInsideSphereHalf;
		vec3 intersectionPoint = origRay + dist*rayDir;
		vec3 radVec = normalize(intersectionPoint - center);
		float azim = acos(dot(radVec, vec3(1.0, 0.0, 0.0)));
		float polar = acos(dot(radVec, vec3(0.0, 1.0, 0.0)));
		vec2 texCoord = vec2(azim, polar);
		vec3 tanspaceNormal = normalize((2.0 * texture(u_NormalMap, texCoord).rgb) - 1.0);	// Sampler call is costly
		//normal = tanspaceNormal.x * tangent + tanspaceNormal.y * bitangent + tanspaceNormal.z * radVec;
		normal = normalize(intersectionPoint - center);
	} else {
	    dist = -INFINITY;
		normal = vec3(0.0, 0.0, 0.0);
	}
	return vec4(normal, dist);
}

vec4 calculateIntersectionWithPlane(vec3 planePoint, vec3 planeNormal, vec3 origRay, vec3 rayDir) {
	float dist;
	vec3 normal;
    float distToPlane = dot((planePoint - origRay), planeNormal) / dot(rayDir, planeNormal);
	if( distToPlane > 0.0) {    // The ray intersects the plane in the forward direction.
		dist = distToPlane;
		vec3 intersectionPoint = origRay + distToPlane*rayDir;
		vec2 texCoord = vec2(mod(intersectionPoint.x, 1.0), mod(intersectionPoint.z, 1.0));
		vec3 tanspaceNormal = normalize( (2.0 * texture(u_NormalMap, texCoord).rgb) - 1.0 );
		vec3 tangent = vec3(1.0, 0.0, 0.0);
		vec3 bitangent = cross(planeNormal, tangent);
		if (dot(rayDir, planeNormal) < 0.0) {
		    //normal = planeNormal;
			normal = tanspaceNormal.x * tangent + tanspaceNormal.y * bitangent + tanspaceNormal.z * planeNormal;
		} else {
		    //normal = -planeNormal;
			normal = -tanspaceNormal.x * tangent - tanspaceNormal.y * bitangent - tanspaceNormal.z * planeNormal;
		}
	} else {
		dist = -INFINITY;
		normal = vec3(0.0, 0.0, 0.0);
	}
	return vec4(normal, dist);
}

// Returns the distance to and the normal at the closest intersection between the ray and the environment
IntersectionData calc_IntersectionData(vec3 origRay, vec3 rayDir) {
    vec3 sphere1Center = u_AvatarPosition;
    float sphere1Radius = 0.5;
    vec4 sphere1Color = vec4(0.7, 0.3, 0.3, 1.0);

	const int sphereCount = 2;
	vec3[4] sphereCenters;
	float[4] sphereRadiuses;
    vec4[4] sphereColors;
	for (int i = 0 ; i < sphereCount; i++) {
	    sphereCenters[i] = vec3(i * 8.0 - 10.0, 2.0, -10.0);
	    sphereRadiuses[i] = i * 1.0 + 2.0;
        sphereColors[i] = vec4(i * 0.3 + 0.4, -i*0.4 + 0.6, i * 0.1 + 0.5, 1.0);
	}

	const int planeCount = 1;
	vec3[4] planePoints;
	vec3[4] planeNormals;
    vec4[4] planeColors;
	for (int i = 0 ; i < planeCount; i++) {
	    planePoints[i] = vec3(-i * 10.0, 0.0, -i * 10.0);
	    planeNormals[i] = normalize(vec3(i * 0.2, 1.0, i * 0.2));
        planeColors[i] = vec4(i * 0.15 + 0.1, -i * 0.1 + 0.4, i * 0.1 + 0.6, 1.0);
	}

	// Values to find
	IntersectionData intersectionData;
	intersectionData.dist = -INFINITY;
	intersectionData.nor = vec3(0.0, 0.0, 0.0);
	intersectionData.col = vec4(0.0, 0.0, 0.0, 0.0);

	// Check intersection with spheres
	vec4 sphereIntersectionData;
	vec3 sphereNormal;
	float sphereDistance;
	for (int i = 0 ; i < sphereCount; i++) {
	    sphereIntersectionData = calculateIntersectionWithSphere(sphereCenters[i], sphereRadiuses[i], origRay, rayDir);
	    sphereNormal = sphereIntersectionData.xyz;
	    sphereDistance = sphereIntersectionData.w;
	    if( sphereDistance > 0.0 && sphereDistance < abs(intersectionData.dist) ) {
	        intersectionData.dist = sphereDistance;
		    intersectionData.nor = sphereNormal;
	        intersectionData.col = sphereColors[i];
	    }
	}

	// Check intersection with planes
	vec4 planeIntersectionData;
	vec3 planeNormal;
	float planeDist;
	for (int i = 0 ; i < planeCount; i++) {
	    planeIntersectionData = calculateIntersectionWithPlane(planePoints[i], planeNormals[i], origRay, rayDir);
		planeNormal = planeIntersectionData.xyz;
		planeDist = planeIntersectionData.w;
		if (planeDist > 0.0 && planeDist < abs(intersectionData.dist)) {
			intersectionData.dist = planeDist;
			intersectionData.nor = planeNormal;
			intersectionData.col = planeColors[i];
		}
	}

	// Check intersection with sphere 1
	vec4 sphere1IntersectionData = calculateIntersectionWithSphere(sphere1Center, sphere1Radius, origRay, rayDir);
	vec3 sphereNormal1 = sphere1IntersectionData.xyz;
	float sphereDistance1 = sphere1IntersectionData.w;
	if (sphereDistance1 > 0.0 && sphereDistance1 < abs(intersectionData.dist)) {
	    intersectionData.dist = sphereDistance1;
		intersectionData.nor = sphereNormal1;
	    intersectionData.col = sphere1Color;
	}

    return intersectionData;
}

vec4 calculateBackgroundColor(vec3 origRay, vec3 rayDir) {
    vec4 bg_col;
    const float minIntensity = 0.05;
    float intensity = max(0.0, dot(rayDir, normalize(u_LightPosition - origRay)));
    if (intensity < 0.98) {
        intensity = minIntensity;
    } else {
        //intensity = 1.0;
    }
    bg_col = vec4(intensity, intensity, intensity, 1.0);
    return bg_col;
}

NewRaysData calculateSurfaceColor(vec3 origRay, vec3 rayDir) {
	NewRaysData newRayData;
	vec4 color;
	float illum = 1.0;
	IntersectionData rayIntersectionData = calc_IntersectionData(origRay, rayDir);
	float dist = rayIntersectionData.dist;
	if (dist > 0.0) { // Object hit
	    vec3 normal_surf = rayIntersectionData.nor;    // Get surface normal for reflecting the ray
        origRay += rayDir * dist;                // Move the ray to the hit point
        rayDir = reflect(rayDir, normal_surf);        // Reflect the ray
    	origRay += rayDir * EPSILON;             // Move the ray off the surface to avoid hitting the same point twice

		// Check if we are in shadow - cast a shadow ray
		IntersectionData shadowIntersectionData;
		vec3 light_dir = normalize(u_LightPosition - origRay);
		vec3 light_right = vec3(light_dir.y, light_dir.x, light_dir.z);
		vec3 light_up = cross(light_right, light_dir);
		const float shadowRayCount = 4.0;
		float dIllum = illum / shadowRayCount;
		const float lightDirPerturbation = 0.025;
		for (int j = 0; j < shadowRayCount; j++) {
		    shadowIntersectionData = calc_IntersectionData(origRay, light_dir
			        + ((j-5.0)/10.0)*lightDirPerturbation*light_up
					+ ((j-5.0)/10.0)*lightDirPerturbation*light_right);
			if (shadowIntersectionData.dist > 0.0) illum -= dIllum;	// If there is any intersection towards the light source
		}

		// Apply the reflected color
		//transmit *= rayIntersectionData.col;

        // Apply diffuse lighting
		color = rayIntersectionData.col;
    	vec3 lightVecN = normalize(u_LightPosition - origRay);
		float diffIntensity = max(dot(rayIntersectionData.nor, lightVecN), 0.0);
		color *= diffIntensity;

		// Apply specular lighting
		//vec3 eyeVecN = normalize(u_EyePosition - origRay);
		//vec3 halfway = normalize(lightVecN + eyeVecN);
		//float shininess = 50.0;
		//float spec = pow(max(dot(halfway, rayIntersectionData.nor), 0.0), shininess);
		//lightColor += spec * rayIntersectionData.col;

		//lightColor += transmit * background_color(origRay, rayDir);
	} else { // Background hit
        //lightColor += transmit * calculateBackgroundColor(origRay, rayDir);
        //break;	// Don't bounce off the background
    }

	newRayData.col = illum * color;
	newRayData.origRay = origRay;
	newRayData.rayDir = rayDir;
	/*for (int i = 0; i < NEW_RAY_COUNT; i++) {
	    newRayData.rayDir_arr[i] = rayDir;
	}*/

	return newRayData;
}


void main(void) {
    // Ray tracing
	vec2 resolution = u_ScreenDimension;
	vec2 pxCoord = (-1.0 + 2.0 * gl_FragCoord.xy / resolution.xy) * vec2(resolution.x / resolution.y, 1.0);
	vec3 origRay = u_EyePosition;
    vec3 eyeSpaceRayDir = normalize(vec3(pxCoord, -1.0));
	vec3 upDir = cross(u_RightDirection, u_LookDirection);
	vec3 rayDir =
	        eyeSpaceRayDir.x * u_RightDirection
			+ eyeSpaceRayDir.y * upDir
			-eyeSpaceRayDir.z * u_LookDirection;	// The direction of the ray in world space
    vec4 lightColor = vec4(1.0, 1.0, 1.0, 1.0);

	NewRaysData newRayData;
	for (int i = 0; i < BOUNCE_COUNT; i++) {
	    newRayData = calculateSurfaceColor(origRay, rayDir);
		lightColor *= newRayData.col;
		origRay = newRayData.origRay;
		rayDir = newRayData.rayDir;

	    // Applying gloss
		// Cast a ray. Find the nearest intersection. Get the surface color. Initialize new rays. Repeat for every new ray.
	    /*float currentOriginCount = max(pow(NEW_RAY_COUNT, i) - 1, 1);
	    float currRayCount = pow(NEW_RAY_COUNT, i);
	    //for (int j = 0; j < currentOriginCount; j++) {
		    for (int k = 0; k < currRayCount; k++) {
			    newRayData = calculateSurfaceColor(origRay_arr[0], rayDir_arr[0*NEW_RAY_COUNT + k], transmit);
		        lightColor += newRayData.col;
			    newOrigRays[k] = newRayData.origRay;
				for (int l = 0; l < NEW_RAY_COUNT; l++) {
				    newRayDirs[k*NEW_RAY_COUNT + l] = newRayData.rayDir_arr[l];
				}
			}
		//}*/
    }
    out_Color = lightColor;
}
