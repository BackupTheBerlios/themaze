/*		input.cpp
 *
 *		"The Maze" input module.
 *
 *		@date		2003-10-25
 */

#include "input.h"
#include <stdio.h>
#include <math.h>

static MazeIO _MazeIO;
static const float DEG2RAD = 3.1416f / 180;
static float _lastRotY, _lastRotX, _lastPosZ;

bool inp_InitMazeIO(HWND window)
{
	long res;
	bool hmd = true, jst = true, mouse = true, keyboard = true;
	char str[128];

	MessageBox(
		window,
		"MazeIO will try to initialize the input devices now.\nWhen no HMD is connected this may take a wile.\nSo stay calm until window comes up.",
		"MazeIO Initialization",
		MB_OK);

	res = _MazeIO.open(window);
 
	if (res & MIOERR_INTERTRAX)
		hmd = false;
	if (res & MIOERR_JOYSTICK)
		jst = false;
	if (res & MIOERR_MOUSE)
		mouse = false;
	if (res & MIOERR_KEYBOARD)
		keyboard = false;

	if(res != MIO_OK)
	{
		sprintf(str, "MazeIO tried to open the input devices:\n\nHMD\t\t%d\nJoystick\t\t%d\nMouse\t\t%d\nKeyboard\t\t%d",
			hmd, jst, mouse, keyboard);
		MessageBox(window, str, "MazeIO Initialization", MB_OK);
	}

	return res == MIO_OK ? true : false;
}

void inp_Handle(Camera &camera)
{
	float			orientation[4];
	JoyState		joyState;
	DIMOUSESTATE	mouseState;
	char			keys[256];
	bool hmd, jst;

	switch(_MazeIO.getMode())
	{
		case MIOMODE_HMDJOYSTICK1:
			if (_MazeIO.getHMDState(orientation))
			{
				camera[3] = -orientation[1];
				camera[4] = orientation[0];
				camera[5] = orientation[2];
			}
			if (_MazeIO.getJoyState(&joyState))
			{
				camera[0] += joyState.pos[1] * (float)sin(DEG2RAD * camera[4]) * 0.01f;
				camera[2] -= joyState.pos[1] * (float)cos(DEG2RAD * camera[4]) * 0.01f;
			}
			break;
		case MIOMODE_HMDJOYSTICK2:

			hmd = _MazeIO.getHMDState(orientation);

			jst = _MazeIO.getJoyState(&joyState);

			camera[4] = _lastRotY;

			if (hmd) camera[3] = -orientation[1];
			if (jst) camera[4] += joyState.pos[0];
			if (hmd) camera[5] = orientation[2];

			if (jst) camera[0] += joyState.pos[1] * (float)sin(DEG2RAD * camera[4]) * 0.01f;
			if (jst) camera[2] -= joyState.pos[1] * (float)cos(DEG2RAD * camera[4]) * 0.01f;
			
			_lastRotY = camera[4];

			if (hmd) camera[4] += orientation[0];
			break;
		case MIOMODE_JOYSTICK:
			if (_MazeIO.getJoyState(&joyState))
			{
				if (joyState.buttons & JOY_BUTTON1)
				{
					camera[3] -= joyState.pos[1];
					camera[4] += joyState.pos[0];
				}
				else
				{
					camera[4] += joyState.pos[0];

					camera[0] += joyState.pos[1] * (float)sin(DEG2RAD * camera[4]) * 0.01f;
					camera[2] -= joyState.pos[1] * (float)cos(DEG2RAD * camera[4]) * 0.01f;
				}
				if (joyState.buttons & JOY_BUTTON2)
				{
					camera.upRight();
				}
			}
			break;
		case MIOMODE_MOUSE:
			if (!_MazeIO.getMouseState(&mouseState))
				return;

			if (mouseState.rgbButtons[0])
			{
				_lastRotY += mouseState.lX * 0.01f;
				camera[4] += _lastRotY;

				_lastPosZ += mouseState.lY * 0.01f;

				camera[0] += _lastPosZ * (float)sin(DEG2RAD * camera[4]) * 0.01f;
				camera[2] -= _lastPosZ * (float)cos(DEG2RAD * camera[4]) * 0.01f;
			}
			else if (mouseState.rgbButtons[1])
			{
				_lastRotY += mouseState.lX * 0.01f;
				camera[4] += _lastRotY;

				_lastRotX += mouseState.lY * 0.01f;
				camera[3] += _lastRotX;
			}
			else if (mouseState.rgbButtons[2])
			{
				camera.upRight();
			}
			else
			{
				_lastRotX = 0;
				_lastRotY = 0;
				_lastPosZ = 0;
			}
			break;
		case MIOMODE_KEYBOARD:
			if (!_MazeIO.getKeyboardState(keys))
				return;
			
			if (keys[DIK_LEFT])
			{
				camera[4] -= 1;
			}
			if (keys[DIK_RIGHT])
			{
				camera[4] += 1;
			}
			if (keys[DIK_UP])
			{
				camera[0] -= (float)sin(DEG2RAD * camera[4]) * 0.01f;
				camera[2] += (float)cos(DEG2RAD * camera[4]) * 0.01f;
			}
			if (keys[DIK_DOWN])
			{
				camera[0] += (float)sin(DEG2RAD * camera[4]) * 0.01f;
				camera[2] -= (float)cos(DEG2RAD * camera[4]) * 0.01f;
			}
			if (keys[DIK_PGUP])
			{
				camera[3] -= 1;
			}
			if (keys[DIK_PGDN])
			{
				camera[3] += 1;
			}
			if (keys[DIK_BACKSPACE])
			{
				camera.upRight();
			}
			break;
	}
}

void inp_SwitchMode(int mode)
{
	_MazeIO.setMode(mode);
}
