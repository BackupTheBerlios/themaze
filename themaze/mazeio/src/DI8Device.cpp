/*		DI8Device.cpp
 *
 *		Implementierung der Klasse DI8Device.
 *
 *		@date		2003-10-16
 *		@version	0.5
 */

#include "DI8Device.h"
#include <stdio.h>

DI8Device::DI8Device()
{
	_DI = NULL;
	_Device = NULL;
}

DI8Device::~DI8Device()
{
	if (_Device != NULL)
	{
		_Device->Unacquire();
		_Device->Release();
	}

	if (_DI != NULL)
		_DI->Release();

	_DI = NULL;
	_Device = NULL;
}

bool DI8Device::initDirectInput()
{
	if (_DI != NULL)
		_DI->Release();
	_DI = NULL;

/*
	if (DirectInput8Create(GetModuleHandle(NULL),
						   DIRECTINPUT_VERSION, IID_IDirectInput8,
						   (void**)&_DI, NULL))
*/
	return DirectInput8Create(
				GetModuleHandle(NULL),
				DIRECTINPUT_VERSION,
				IID_IDirectInput8,
				(void **)&_DI,
				NULL) == DI_OK ?
	true : false;
}

bool DI8Device::createDevice(REFGUID rguid)
{
	return _DI->CreateDevice(rguid, &_Device, NULL) == DI_OK ? true : false;
}

bool DI8Device::setDataFormat(LPCDIDATAFORMAT dataFormat)
{
	return _Device->SetDataFormat(dataFormat) == DI_OK ? true : false;
}

bool DI8Device::getDeviceState(DWORD size, LPVOID data)
{
	return _Device->GetDeviceState(size, data) == DI_OK ? true : false;
}

bool DI8Device::setCooperativeLevel(HWND hwnd, DWORD flags)
{
	return _Device->SetCooperativeLevel(hwnd, flags) == DI_OK ? true : false;
}

HRESULT DI8Device::acquire()
{
	return _Device->Acquire();
}
