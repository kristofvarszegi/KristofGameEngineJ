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


const int NUM_NEW_RAYS = 2;
const int NUM_BOUNCES = 2;

const float EPSILON = 0.001;
const float INFINITY = 10000.0;
const float PI_HALF = 1.57079632679;


struct IntersectionData {
    float dist;
    vec3 nor;
    vec4 col;
};

struct New_rays_data {
    vec4 col;
	vec3 ray_orig;
	vec3 ray_dir;
	//vec3[NUM_BOUNCES] ray_dir_arr;
};




vec4 calc_intersection_sphere(vec3 center, float radius, vec3 ray_orig, vec3 ray_dir) {
    // Checking intersection with sphere
	float dist;
	vec3 normal;
	vec3 dist_to_center = center - ray_orig;
	float ray_dir_proj = dot(dist_to_center, ray_dir);
	vec3 ray_dir_comp = ray_dir * ray_dir_proj;
	vec3 ray_perp_comp = dist_to_center - ray_dir_comp;
	float dist_from_ray = length(ray_perp_comp);
	if(ray_dir_proj < 0.0) {
	    dist_from_ray = 1000000000.0;
	}
	if(dist_from_ray < radius) {    // The ray intersects the sphere.
	    float ray_inside_sphere_half = sqrt(radius*radius - dist_from_ray*dist_from_ray);
	    
		dist = length(ray_dir_comp) - ray_inside_sphere_half;
		vec3 intersect_point = ray_orig + dist*ray_dir;
		vec3 rad_vec = normalize(intersect_point - center);
		float azim = acos(dot(rad_vec, vec3(1.0, 0.0, 0.0)));
		float polar = acos(dot(rad_vec, vec3(0.0, 1.0, 0.0)));
		//vec2 tex_coord = vec2(mod(azim/PI_HALF, 1.0), mod(polar/PI_HALF, 1.0));		// Seems to be costly
		//vec2 tex_coord = vec2(azim/PI_HALF, polar/PI_HALF);		// Seems to be costly
		vec2 tex_coord = vec2(azim, polar);
		vec3 normal_tanspace = normalize( (2.0 * texture(u_NormalMap, tex_coord).rgb) - 1.0 );	// Sampler call is costly
		//vec3 tangent = normalize(cross(vec3(0.0, 1.0, 0.0), rad_vec));
		//vec3 bitangent = cross(rad_vec, tangent);
		
		//normal = normal_tanspace.x * tangent + normal_tanspace.y * bitangent + normal_tanspace.z * rad_vec;
		normal = normalize(intersect_point - center);
		
	} else {
	    dist = -INFINITY;
		normal = vec3(0.0, 0.0, 0.0);
	}
	
	return vec4(normal, dist);
	//return vec4(1.0, 1.0, 1.0, 1.0);

}


vec4 calc_intersection_plane(vec3 plane_point, vec3 plane_normal, vec3 ray_orig, vec3 ray_dir) {
    // Checking intersection with the plane.
	float dist;
	vec3 normal;
    float dist_to_plane = dot((plane_point - ray_orig), plane_normal) / dot(ray_dir, plane_normal);
	if( dist_to_plane > 0.0) {    // The ray intersects the plane in the forward direction.
		dist = dist_to_plane;

		vec3 intersect_point = ray_orig + dist_to_plane*ray_dir;
		vec2 tex_coord = vec2(mod(intersect_point.x, 1.0), mod(intersect_point.z, 1.0));
		vec3 normal_tanspace = normalize( (2.0 * texture(u_NormalMap, tex_coord).rgb) - 1.0 );
		vec3 tangent = vec3(1.0, 0.0, 0.0);
		vec3 bitangent = cross(plane_normal, tangent);
		if( dot(ray_dir, plane_normal) < 0.0) {
		    //normal = plane_normal;
			normal = normal_tanspace.x * tangent + normal_tanspace.y * bitangent + normal_tanspace.z * plane_normal;
		} else {
		    //normal = -plane_normal;
			normal = -normal_tanspace.x * tangent - normal_tanspace.y * bitangent - normal_tanspace.z * plane_normal;
		}
		
	} else {
		dist = -INFINITY;
		normal = vec3(0.0, 0.0, 0.0);
	}
	
	return vec4(normal, dist);
	
}


// Returns the distance to and the normal at the closest intersection between the ray and the environment.
//vec4 calc_IntersectionData(vec3 ray_orig, vec3 ray_dir) {
IntersectionData calc_IntersectionData(vec3 ray_orig, vec3 ray_dir) {
    
    //vec3 sphere1_center = vec3(3.0, 0.0, -10.0);
    vec3 sphere1_center = u_AvatarPosition;
    float sphere1_radius = 0.5;
    vec4 sphere1_color = vec4(0.7, 0.3, 0.3, 1.0);

	int n_spheres = 2;
	vec3[4] sphere_center_arr;
	float[4] sphere_radius_arr;
    vec4[4] sphere_color_arr;
	for(int i = 0 ; i < n_spheres; i++) {
	    sphere_center_arr[i] = vec3(i*8.0 - 10.0, 2.0, -10.0);
	    sphere_radius_arr[i] = i*1.0 + 2.0;
        sphere_color_arr[i] = vec4(i*0.3 + 0.4, -i*0.4 + 0.6, i*0.1 + 0.5, 1.0);
	}
    //vec3 sphere2_center = vec3(-3.0, 0.0, -20.0);
    //float sphere2_radius = 10.0;
    //vec4 sphere2_color = vec4(0.1, 0.8, 0.6, 1.0);
    //vec3 sphere3_center = vec3(10.0, 3.0, 0.0);
    //float sphere3_radius = 2.0;
    //vec4 sphere3_color = vec4(0.7, 0.15, 0.7, 1.0);

	int n_planes = 1;
	vec3[4] plane_point_arr;
	vec3[4] plane_normal_arr;
    vec4[4] plane_color_arr;
	for(int i = 0 ; i < n_planes; i++) {
	    plane_point_arr[i] = vec3(-i*10.0, 0.0, -i*10.0);
	    plane_normal_arr[i] = normalize(vec3(i*0.2, 1.0, i*0.2));
        plane_color_arr[i] = vec4(i*0.15 + 0.1, -i*0.1 + 0.4, i*0.1 + 0.6, 1.0);
	}
	//vec3 plane1_point = vec3(INFINITY, -2.0, INFINITY);
	//vec3 plane1_normal = vec3(0.0, 1.0, 0.0);
	//vec4 plane1_color = vec4(0.3, 0.4, 0.6, 1.0);

	
	// Values to find
	IntersectionData ints_dat;//(-1.0, vec3(0.0, 0.0, 0.0), vec4(0.0, 0.0, 0.0, 0.0));
	ints_dat.dist = -INFINITY;
	ints_dat.nor = vec3(0.0, 0.0, 0.0);
	ints_dat.col = vec4(0.0, 0.0, 0.0, 0.0);
	
	
	// Checking intersection with spheres
	vec4 sphere_ints_dat;
	vec3 normal_sphere;
	float dist_sphere;
	for(int i = 0 ; i < n_spheres; i++) {
	    sphere_ints_dat = calc_intersection_sphere(sphere_center_arr[i], sphere_radius_arr[i], ray_orig, ray_dir);
	    normal_sphere = sphere_ints_dat.xyz;
	    dist_sphere = sphere_ints_dat.w;
	    if( dist_sphere > 0.0 && dist_sphere < abs(ints_dat.dist) ) {
	        ints_dat.dist = dist_sphere;
		    ints_dat.nor = normal_sphere;
	        ints_dat.col = sphere_color_arr[i];
	    }
	}
	
	// Checking intersection with planes
	vec4 plane_ints_dat;
	vec3 normal_plane;
	float dist_plane;
	for(int i = 0 ; i < n_planes; i++) {
	    plane_ints_dat = calc_intersection_plane(plane_point_arr[i], plane_normal_arr[i], ray_orig, ray_dir);
		normal_plane = plane_ints_dat.xyz;
		dist_plane = plane_ints_dat.w;
		if( dist_plane > 0.0 && dist_plane < abs(ints_dat.dist) ) {
			ints_dat.dist = dist_plane;
			ints_dat.nor = normal_plane;
			ints_dat.col = plane_color_arr[i];
		}
	}
	
	// Checking intersection with sphere 1
	vec4 sphere1_ints_dat = calc_intersection_sphere(sphere1_center, sphere1_radius, ray_orig, ray_dir);
	vec3 normal_sphere1 = sphere1_ints_dat.xyz;
	float dist_sphere1 = sphere1_ints_dat.w;
	if( dist_sphere1 > 0.0 && dist_sphere1 < abs(ints_dat.dist) ) {
	    ints_dat.dist = dist_sphere1;
		ints_dat.nor = normal_sphere1;
	    ints_dat.col = sphere1_color;
	}
	
	
	//return vec4(normal_surf, dist_surf);
    return ints_dat;
	
}


vec4 calc_background_color(vec3 ray_orig, vec3 ray_dir) {

    vec4 bg_col;
    float min_intens = 0.05;
	
    //float intens = sin(10.0*ray_dir.y);
    //float intens = max(min_intens, ray_dir.y + 0.5);
    //float intens = max(0.0, dot(ray_dir, normalize(u_LightPosition - ray_orig)));
    float intens = max(0.0, dot(ray_dir, normalize(u_LightPosition - ray_orig)));
    if(intens < 0.98) {
        intens = min_intens;
    } else {
        //intens = 1.0;
    }
    bg_col = vec4(intens, intens, intens, 1.0);
	
	
    /*vec2 spot_center = vec2(0.75, 0.75);
	float radius = 1.0;
	if( length(ray_dir.xy - spot_center) < radius) {
	    bg_col = vec4(1.0, 1.0, 1.0, 1.0);
	} else {
	    bg_col = vec4(0.0, 0.0, 0.0, 0.0);
	}*/

    return bg_col;
}


New_rays_data calc_surf_color(vec3 ray_orig, vec3 ray_dir) {
    
	New_rays_data new_ray_dat;
	//vec4 color = vec4(0.0, 0.0, 0.0, 0.0);
	//vec4 color = vec4(1.0, 1.0, 1.0, 1.0);
	vec4 color;
	float illum = 1.0;
	
	//vec4 ray_ints_data = calc_IntersectionData(ray_orig, ray_dir);
	IntersectionData ray_ints_data = calc_IntersectionData(ray_orig, ray_dir);
       
	float dist = ray_ints_data.dist;
	//float dist = ray_ints_data.w;
       
	if(dist > 0.0) { // Object hit.
	    //vec3 normal_surf = ray_ints_data.xyz;    // Get surface normal for reflecting the ray.
		vec3 normal_surf = ray_ints_data.nor;    // Get surface normal for reflecting the ray.
           
        ray_orig += ray_dir * dist;                // Move the ray to the hit point.
        ray_dir = reflect(ray_dir, normal_surf);        // Reflect the ray.
    	//ray_dir = ray_dir - 2*dot(ray_dir, -normal_surf)*normal_surf; NO
        ray_orig += ray_dir * EPSILON;             // Move the ray off the surface to avoid hitting the same point twice.
			
		// Checking if we are in shadow - cast a shadow ray
		//vec4 sh_ints_data;
		IntersectionData sh_ints_data;
		vec3 light_dir = normalize(u_LightPosition - ray_orig);
		vec3 light_right = vec3(light_dir.y, light_dir.x, light_dir.z);
		vec3 light_up = cross(light_right, light_dir);
		float n_sh_rays = 4.0;
		float d_illum = illum / n_sh_rays;
		float light_dir_perturb = 0.025;
		for(int j = 0; j < n_sh_rays; j++) {
		    sh_ints_data = calc_IntersectionData(ray_orig, light_dir
			        + ((j-5.0)/10.0)*light_dir_perturb*light_up
					+ ((j-5.0)/10.0)*light_dir_perturb*light_right);
			//if(sh_ints_data.w > 0.0) illum -= d_illum;	// If there is any intersection towards the light source
			if(sh_ints_data.dist > 0.0) illum -= d_illum;	// If there is any intersection towards the light source
		}

			
		// Applying the reflected color
		//transmit *= ray_ints_data.col;
		//transmit *= vec4(0.1, 0.5, 0.8, 1.0); // Make the ray more opaque.
        
	
        // Applying diffuse lighting
		color = ray_ints_data.col;
    	vec3 light_vec_n = normalize(u_LightPosition - ray_orig);
		float diff_intens = max(dot(ray_ints_data.nor, light_vec_n), 0.0);
		//light_color += diff;
		//color += diff;
		color *= diff_intens;
		
		
		// Applying specular lighting
		//vec3 eye_vec_n = normalize(u_EyePosition - ray_orig);
		//vec3 halfway = normalize(light_vec_n + eye_vec_n);
		//float shininess = 50.0;
		//float spec = pow(max(dot(halfway, ray_ints_data.nor), 0.0), shininess);
		//light_color += spec * ray_ints_data.col;
		
		
		//light_color += transmit * background_color(ray_orig, ray_dir);
		
	} else { // Background hit.
        //light_color += transmit * calc_background_color(ray_orig, ray_dir);
        //break;	// Don't bounce off the background.
    }
	
	new_ray_dat.col = illum * color;
	new_ray_dat.ray_orig = ray_orig;
	new_ray_dat.ray_dir = ray_dir;
	/*for(int i = 0; i < NUM_NEW_RAYS; i++) {
	    new_ray_dat.ray_dir_arr[i] = ray_dir;
	}*/
	
	return new_ray_dat;
	//return color;
	
}


void main(void) {

    // Ray tracing
	//vec2 resolution = vec2(1366.0, 768.0);
	vec2 resolution = u_ScreenDimension;
	vec2 px_coord = (-1.0 + 2.0*gl_FragCoord.xy / resolution.xy) * vec2(resolution.x/resolution.y, 1.0);
	//vec3 ray_orig = vec3(0.0, 0.0, -6.0);     // The origin of the ray.
	vec3 ray_orig = u_EyePosition;     // The origin of the ray.
    vec3 ray_dir_eyespace = normalize(vec3(px_coord, -1.0)); // The direction of the ray in eye space.
	vec3 up_dir = cross(u_RightDirection, u_LookDirection);
	vec3 ray_dir =
	        ray_dir_eyespace.x * u_RightDirection
			+ ray_dir_eyespace.y * up_dir
			-ray_dir_eyespace.z * u_LookDirection;	// The direction of the ray in world space.
    //vec3 ray_dir = ray_dir_eyespace;
	//vec4 transmit = vec4(1.0, 1.0, 1.0, 1.0);          // How much light the ray lets through.
    //vec4 light_color = vec4(0.0, 0.0, 0.0, 0.0);             // How much light hits the eye through the ray.
	vec4 light_color = vec4(1.0, 1.0, 1.0, 1.0);

	//float num_rays_max = pow(NUM_NEW_RAYS, NUM_BOUNCES);
	//vec4[pow(NUM_NEW_RAYS, NUM_BOUNCES)] ray_arr;
	//vec3[4] ray_orig_arr;    ray_orig_arr[0] = ray_orig;
	//vec3[8] ray_dir_arr;    ray_dir_arr[0] = ray_dir;
	//vec3[4] ray_orig_arr_new;
	//vec3[8] ray_dir_arr_new;
	New_rays_data new_ray_dat;
	for(int i = 0; i < NUM_BOUNCES; i++) {
	
	    new_ray_dat = calc_surf_color(ray_orig, ray_dir);
		//light_color += new_ray_dat.col;
		light_color *= new_ray_dat.col;
		ray_orig = new_ray_dat.ray_orig;
		ray_dir = new_ray_dat.ray_dir;
	
	
	    // Applying gloss.
		// Cast a ray. Find the nearest intersection. Get the surface color. Initialize new rays. Repeat for every new ray.
	    /*float n_curr_origs = max(pow(NUM_NEW_RAYS, i) - 1, 1);
	    float n_curr_rays = pow(NUM_NEW_RAYS, i);
	    //for(int j = 0; j < n_curr_origs; j++) {
		    for(int k = 0; k < n_curr_rays; k++) {
			    new_ray_dat = calc_surf_color(ray_orig_arr[0], ray_dir_arr[0*NUM_NEW_RAYS + k], transmit);
		        light_color += new_ray_dat.col;
			    ray_orig_arr_new[k] = new_ray_dat.ray_orig;
				for(int l = 0; l < NUM_NEW_RAYS; l++) {
				    ray_dir_arr_new[k*NUM_NEW_RAYS + l] = new_ray_dat.ray_dir_arr[l];
				}
			}
		//}*/
		
	    
    }

	//out_Color = vec4(light_color.r, light_color.g, light_color.b, 1.0);	// Set pixel color to the amount of light seen.
	//out_Color = vec4(light_color.x, light_color.y, light_color.z, 1.0);	// Set pixel color to the amount of light seen.
    out_Color = light_color;	// Set pixel color to the amount of light seen.
	//out_Color = vec4(light_color.r, light_color.g, light_color.b, 1.0);
	//out_Color = vec4(0.2, 0.3, 0.75, 1.0);

}