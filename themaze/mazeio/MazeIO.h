/*		MazeIO.h
 *
 *		Declaration of MazeIO Class
 *
 *		@date		2003-10-25
 *		@version	0.7
 */

#ifndef MAZEIO_H
#define MAZEIO_H

#include "src/Intertrax2.h"
#include "src/Joystick.h"
#include "src/DI8Device.h"

enum MIOErrorCodes
{
	MIO_OK					= 0,
	MIOERR_MOUSE			= 1,
	MIOERR_KEYBOARD			= 2,
	MIOERR_INTERTRAX		= 4,
	MIOERR_JOYSTICK			= 8
};

enum MIOModes
{
	//JOYSTICK FORWARD AND BACKWARD, MOVING IN VIEWING DIRECTION
	MIOMODE_HMDJOYSTICK1	= 1,
	//WALKING WITH JOYSTICK, LOOKING WITH HMD
	MIOMODE_HMDJOYSTICK2,
	//
	MIOMODE_JOYSTICK,
	MIOMODE_MOUSE,
	MIOMODE_KEYBOARD
};

/**
 *
 */
class MazeIO  
{

public:
	
	MazeIO();

	long open(HWND hwnd);
	bool close();

	bool getHMDState(float orientation[4]);
	bool getJoyState(JoyState *joyState);
	bool getKeyboardState(char *keys);
	bool getMouseState(LPDIMOUSESTATE mouseState);

	int getMode();
	void setMode(int mode);

private:

	Intertrax2	_Intertrax;
	Joystick	_Joystick;
	DI8Device	_Keyboard;
	DI8Device	_Mouse;

	int _mode;
};

#endif //MAZEIO_H
