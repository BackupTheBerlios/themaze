/*		DI8Device.h
 *
 *		Schnittstelle für die Klasse DI8Device.
 *
 *		@date		2003-10-16
 *		@version	0.5
 */

#ifndef DI8DEVICE_H
#define DI8DEVICE_H

#include <dinput.h>

/**
 *
 */
class DI8Device
{

public:
	HRESULT acquire();
	bool setCooperativeLevel(HWND hwnd, DWORD flags);
	bool getDeviceState(DWORD size,  LPVOID data);

	/**
	 * c_dfDIKeyboard c_dfDIMouse 
	 */
	bool setDataFormat(LPCDIDATAFORMAT dataFormat);
	/**
	 * GUID_SysKeyboard	.. The default system keyboard. 
	 * GUID_SysMouse	.. The default system mouse.
	 */
	bool createDevice(REFGUID rguid);
	/**
	 *
	 */
	bool initDirectInput();


	DI8Device();
	~DI8Device();

private:

	/**
	 * Referenz auf DirectInput Kontext.
	 */
	LPDIRECTINPUT8 _DI;
	/**
	 * Referenz auf eine DirectInputDevice Instanz.
	 */
	LPDIRECTINPUTDEVICE8 _Device;

};

#endif //DI8DEVICE_H
