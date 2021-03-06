/*		Intertrax2.h
 *
 *		Schnittstelle f�r die Klasse Intertrax2.
 *
 *		@date		2003-10-12
 *		@version	0.7
 */

#ifndef INTERTRAX2_H
#define INTERTRAX2_H

#include <isense.h>

/**
 * Initial version 04.10.2003
 */
class Intertrax2  
{
public:

	Intertrax2();

	bool open();
	bool close();
	/**
	 * Returns data in 
	 */
	bool getState(float orientation[4]);
	/**
	 * Set data format. Tracker can return euler angles (default) or
	 * quaternions.
	 *
	 * @param	format	Allowed values are ISD_EULER and ISD_QUATERNION.
	 *
	 * @return		true	.. on success
	 *				false	.. on failure
	 */
	bool setDataFormat(DWORD format);

	bool reset();

private:

	ISD_TRACKER_HANDLE _handle;

};

#endif //INTERTRAX2_H
