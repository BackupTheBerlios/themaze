/*		Joystick.h
 *
 *		Implementation of Joystick Class
 *
 *		@date		2003-10-09
 *		@version	0.6
 */

#include "Joystick.h"
#include <stdio.h>

Joystick::Joystick()
{
	_deadzone = 0.2f;
	_id = -1;
}

bool Joystick::open()
{
	JOYINFO info;
	bool attached1, attached2;
	
	//IS A JOYSTICK DRIVER PRESENT?
	if (joyGetNumDevs() == 0)
	{
		return false;
	}

	//WHICH JOYSTICK IS ATTACHED?
	(joyGetPos(JOYSTICKID1, &info) == JOYERR_NOERROR) ? attached1 = true : attached1 = false;
	(joyGetPos(JOYSTICKID2, &info) == JOYERR_NOERROR) ? attached2 = true : attached2 = false;

	//IF ANY JOYSTICK ATTACHED, WHICH TO USE
	if (attached1 || attached2)
	{
		//FIRST JOYSTICK IS PREFERED
		_id = attached1 ? JOYSTICKID1 : JOYSTICKID2;

		if (joyGetDevCaps(_id, &_caps, sizeof(_caps)) != JOYERR_NOERROR)
			return false;

		return true;
	}

	return false;
}

bool Joystick::getState(JoyState *state)
{
	JOYINFO info;
	
	if (joyGetPos(_id, &info) == JOYERR_NOERROR)
	{
		state->buttons = info.wButtons;

		state->pos[0] = (((float)info.wXpos / _caps.wXmax) - 0.5f) * 2;
		state->pos[1] = (((float)info.wYpos / _caps.wYmax) - 0.5f) * 2;
		state->pos[2] = (((float)info.wZpos / _caps.wZmax) - 0.5f) * 2;

		for (int i = 0; i < 3; i++)
		{
			if (state->pos[i] < _deadzone && state->pos[i] > -_deadzone)
				state->pos[i] = 0.0;
		}

		return true;
	}

	return false;
}

float Joystick::getDeadzone()
{
	return _deadzone;
}

bool Joystick::setDeadzone(float deadzone)
{
	if (deadzone >= 0.0 && deadzone <= 1.0f)
	{
		_deadzone = deadzone;
		return true;
	}

	return false;
}
