/*		Camera.h
 *
 *		Declaration of Camera Class
 *
 *		@date		2003-10-27
 *		@version	0.5
 */

#ifndef CAMERA_H
#define CAMERA_H

/**
 * Add documentation here.
 */
class Camera
{

public:

	void upRight();

	Camera();
	Camera(float x, float y, float z, float rx, float ry, float rz);

	float &operator[] (int i);
	
private:

	float _camera[6];

};

#endif //CAMERA_H
