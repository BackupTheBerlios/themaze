/*		MazeIO.cpp
 *
 *		Implementierung der Klasse MazeIO.
 *
 *		@date		2003-10-20
 *		@version	0.6
 */

#include "MazeIO.h"
#include <stdio.h>

MazeIO::MazeIO()
{

}

MazeIO::~MazeIO()
{

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

long MazeIO::getData(float pos[])
{
	HRESULT res;
	JoyState joyState;
	float ori[4];

	res = _Mouse.acquire();

	if (res == DI_OK || res == S_FALSE)
	{
		if (!_Mouse.getDeviceState(sizeof(DIMOUSESTATE), &_mouseState))
		{
			printf("Getting mouse data failed.\n");
			return false;
		}
	}
	
	printf("[M: x:%3d y:%3d] ", _mouseState.lX, _mouseState.lY);

	res = _Keyboard.acquire();

	if (res == DI_OK || res == S_FALSE)
	{
		if (!_Keyboard.getDeviceState(sizeof(_keys), _keys))
		{
			printf("Getting keyboard data failed.\n");
			return false;
		}
	}

	printf("[K: %d%d%d%d] ",
		_keys[DIK_UP] ? true : false,
		_keys[DIK_DOWN] ? true : false,
		_keys[DIK_LEFT] ? true : false,
		_keys[DIK_RIGHT] ? true : false);

	if (!_Joystick.getState(&joyState))
	{
		printf("Getting keyboard data failed.\n");
		return false;
	}

	pos[0] = joyState.pos[0];
	pos[1] = joyState.pos[1];
	pos[2] = joyState.pos[2];

	printf("[J: x:%2.1f y:%2.1f %d%d%d%d] ",
		joyState.pos[0],
		joyState.pos[1],
		joyState.buttons & JOY_BUTTON1 ? true : false,
		joyState.buttons & JOY_BUTTON2 ? true : false,
		joyState.buttons & JOY_BUTTON3 ? true : false,
		joyState.buttons & JOY_BUTTON4 ? true : false);

	if (!_Intertrax.getData(ori))
	{
		printf("Getting tracker data failed.\n");
		return false;
	}

	printf("[T: x:%4.1f y:%4.1f z:%4.1f] \r",
		ori[0],
		ori[1],
		ori[2]);

	return true;
}
