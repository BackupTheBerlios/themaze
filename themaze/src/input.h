/*		input.h
 *
 *		Declaration of "The Maze" input module.
 *
 *		@date		2003-10-25
 */

#include <windows.h>
#include "../mazeio/MazeIO.h"
#include "Camera.h"

/**
 * Initialize the MazeIO subsystem. Shows a message box if opening of any
 * input device fails.
 */
bool inp_InitMazeIO(HWND window);
/**
 *
 */
void inp_Handle(Camera &camera);
/**
 *
 */
void inp_SwitchMode(int mode);
