/*		Intertrax2.cpp
 *
 *		Implementierung der Klasse Intertrax2.
 *
 *		@date		2003-10-12
 *		@version	0.7
 */

#include "Intertrax2.h"

Intertrax2::Intertrax2()
{
	_handle = NULL;
}

bool Intertrax2::open()
{
	_handle = ISD_OpenTracker(NULL, 0, FALSE, FALSE);

	return _handle > 0 ? true : false;
}

bool Intertrax2::setDataFormat(DWORD format)
{
	ISD_STATION_INFO_TYPE station;
	
	if (!ISD_GetStationConfig(_handle, &station, 1, FALSE))
		return false;

	station.AngleFormat = format;

	return ISD_SetStationConfig(_handle, &station, 1, FALSE) ? true : false;
}

bool Intertrax2::close()
{
	return ISD_CloseTracker(_handle) ? true : false;
}

bool Intertrax2::getState(float orientation[])
{
	ISD_TRACKER_DATA_TYPE data;

	if (ISD_GetData(_handle, &data))
	{
		orientation[0] = data.Station[0].Orientation[0];
		orientation[1] = data.Station[0].Orientation[1];
		orientation[2] = data.Station[0].Orientation[2];
		orientation[3] = data.Station[0].Orientation[3];
		return true;
	}
	return false;
}

bool Intertrax2::reset()
{
	return ISD_ResetHeading(_handle, 1) ? true : false;
}
