/*		Camera.cpp
 *
 *		Implementierung der Klasse Camera.
 *
 *		@date		2003-10-27
 *		@version	0.5
 */

#include "Camera.h"
#include <memory.h>

Camera::Camera()
{
	memset(_camera, 0, 6 * sizeof(float));
}

Camera::Camera(float x, float y, float z,
			   float rx, float ry, float rz)
{
	_camera[0] = x;
	_camera[1] = y;
	_camera[2] = z;
	_camera[3] = rx;
	_camera[4] = ry;
	_camera[5] = rz;
}


float &Camera::operator[](int i)
{
	return _camera[i];
}

void Camera::upRight()
{
	_camera[3] = 0;
	_camera[5] = 0;
}
