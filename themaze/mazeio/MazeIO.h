/*		MazeIO.h
 *
 *		Declaration of MazeIO Class
 *
 *		@date		2003-10-20
 *		@version	0.6
 */

#ifndef MAZEIO_H
#define MAZEIO_H

#include "src/Intertrax2.h"
#include "src/Joystick.h"
#include "src/DI8Device.h"

enum MIOErrorCodes
{
	MIO_OK				= 0,
	MIOERR_MOUSE		= 1,
	MIOERR_KEYBOARD		= 2,
	MIOERR_INTERTRAX	= 4,
	MIOERR_JOYSTICK		= 8
};

/**
 *
 */
class MazeIO  
{

public:
	long getData(float pos[3]);
	bool close();
	long open(HWND hwnd);

	MazeIO();
	virtual ~MazeIO();

private:

	Intertrax2	_Intertrax;
	Joystick	_Joystick;
	DI8Device	_Keyboard;
	DI8Device	_Mouse;

	DIMOUSESTATE	_mouseState;
	char			_keys[256];
};

#endif //MAZEIO_H
