/*		MazeIO.cpp
 *
 *		Implementierung der Klasse MazeIO.
 *
 *		@date		2003-10-25
 *		@version	0.7
 */

#include "MazeIO.h"

MazeIO::MazeIO()
{
	//_mode = MIOMODE_HMDJOYSTICK1;
	//_mode = MIOMODE_HMDJOYSTICK1;
	//_mode = MIOMODE_JOYSTICK;
	//_mode = MIOMODE_MOUSE;
	_mode = MIOMODE_KEYBOARD;
}

long MazeIO::open(HWND hwnd)
{
	long err_code = MIO_OK;

	if (!_Intertrax.open())
		err_code |= MIOERR_INTERTRAX;

	if (	!_Keyboard.initDirectInput()
		||	!_Keyboard.createDevice(GUID_SysKeyboard)
		||	!_Keyboard.setCooperativeLevel(hwnd, DISCL_NONEXCLUSIVE | DISCL_FOREGROUND)
		||	!_Keyboard.setDataFormat(&c_dfDIKeyboard))
	{
		err_code |= MIOERR_KEYBOARD;
	}

	if (	!_Mouse.initDirectInput()
		||	!_Mouse.createDevice(GUID_SysMouse)
		||	!_Mouse.setCooperativeLevel(hwnd, DISCL_NONEXCLUSIVE | DISCL_FOREGROUND)
		||	!_Mouse.setDataFormat(&c_dfDIMouse))
	{
		err_code |= MIOERR_MOUSE;
	}

	if (!_Joystick.open())
		err_code |= MIOERR_JOYSTICK;

	return err_code;
}

bool MazeIO::close()
{
	_Intertrax.close();

	return true;
}

void MazeIO::setMode(int mode)
{
	_mode = mode;
}

int MazeIO::getMode()
{
	return _mode;
}

bool MazeIO::getJoyState(JoyState *joyState)
{
	return _Joystick.getState(joyState);
}

bool MazeIO::getHMDState(float orientation[])
{
	return _Intertrax.getState(orientation);
}

bool MazeIO::getMouseState(LPDIMOUSESTATE mouseState)
{
	HRESULT res;

	res = _Mouse.acquire();

	if (res == DI_OK || res == S_FALSE)
	{
		return _Mouse.getDeviceState(sizeof(DIMOUSESTATE), mouseState);
	}

	return false;
}

bool MazeIO::getKeyboardState(char *keys)
{
	HRESULT res;

	res = _Keyboard.acquire();

	if (res == DI_OK || res == S_FALSE)
	{
		return _Keyboard.getDeviceState(256, keys);
	}

	return false;
}
