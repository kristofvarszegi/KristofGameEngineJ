
* Unit tests
* Pass eye pos to the fs, not the vs
* Frustum angle to vary with speed
* Fix cuboid shadow geometry
* Fix child object position for render and shadows
* Fix DoF buffers
* Resources, scene info from file
* Fix screenshot in fullscreen
* Revive OBJ support
* Fit avatar reset to speed-based pos updating
* Height map shape shadow, collision
* Instanced rendering
* Merge ray tracing mode into the pipeline as just a render mode
* Fix the angular deadlocks long x, y lookarounds
* Forward/backpedal should be along avatar orientation, not eye orientation
* Fix post process render in windowed mode
* Realistic surface friction
* L-r: mouse, fwd-bp: w/s

# Ray Tracing
* Refraction
* Separate diffuse light (n_bounces = 1) and mirror reflection (n_bounces = N)
* Pass objects from cpu somehow

## Algorithm
1. Cast an outward ray, at first intersection
2. Add the surface diff+spec color to the pixel color
3. Add the incoming color * surface color

Gloss: cast more rays at every intersection
Soft reflection: with photon mapping

http://web.cs.wpi.edu/~emmanuel/courses/cs563/write_ups/zackw/realistic_raytracing.html
http://web.cs.wpi.edu/~emmanuel/courses/cs563/write_ups/zackw/photon_mapping/PhotonMapping.html
