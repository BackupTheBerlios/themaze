/*		Joystick.h
 *
 *		Declaration of Joystick Class
 *
 *		@date		2003-10-09
 *		@version	0.6
 */

#ifndef JOYSTICK_H
#define JOYSITCK_H

#include <windows.h>

/**
 * This structure contains position and button state for simple joysticks.
 *
 * <h3>Version Info</h3>
 * <dl>
 * <dt>05.10.2003 sg	<dd> Initial version.
 * </dl>
 */
struct JoyState
{
	/**
	 * Scaled axis positions with values between -1.0 and 1.0.
	 */
	float pos[3];
	/**
	 * Button mask for up to 4 buttons. Test Windows APIs JOY_BUTTON1,
	 * JOY_BUTTON2, JOY_BUTTON3 and JOY_BUTTON4 flags to determine the buttons
	 * current state.
	 */
	UINT buttons;
};

/**
 * Enables to handle joystick devices.
 *
 * <h3>Version Info</h3>
 * <dl>
 * <dt>05.10.2003 sg	<dd> Initial version.
 * <dt>09.10.2003 sg	<dd> Changed class interface.
 * </dl>
 */
class Joystick
{

public:

	/**
	 * Initializes deadzone with 0.2
	 */
	Joystick();

	/**
	 * Open a joystick device. As the Windows API can only handle two
	 * joysticks, we try to gain access to one of both. If two joysticks are
	 * connected the first joystick is prefered.
	 *
	 * @return		true	.. on success
	 *				false	.. when no joystick driver present, no joystick
	 *				physically attached, retrieving joysticks capabilities
	 *				fails
	 */
	bool open();

	/**
	 * Retrieve joysticks current state in dependence of the deadzone.
	 *
	 * @param	joypos	Address of a JoyPos structure that contains the
	 *					position and button status of the joystick.
	 *
	 * @return		true	.. on success
	 *				false	.. on any error
	 */
	bool getState(JoyState *state);

	/**
	 * Get current deadzone.
	 *
	 * @return		Current deadzone.
	 */
	float getDeadzone();
	/**
	 * Set deadzone for all axes. Only values between 0.0 and 1.0 are allowed.
	 *
	 * @param	deadzone	New deadzone to be applied.
	 *
	 * @return		true	.. on success
	 *				false	.. when deadzone exceeds valid range
	 */
	bool setDeadzone(float deadzone);

private:

	/**
	 * Joysticks deadzone.
	 */
	float _deadzone;
	/**
	 * Contains information about joysticks capabilities.
	 */
	JOYCAPS _caps;
	/**
	 * Joystick identifier.
	 */
	UINT _id;
};

#endif //JOYSTICK_H
