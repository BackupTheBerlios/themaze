/*
 *		Autoren:   Gerber, Robert & Willemsen, Lars
 *		Version:   2.05
 *		Datum:     20.08.2003
 *		basierend auf Tutorials von Lionel Brits & Jeff Molofee, Ben Humphrey (DigiBen) 
 */

#include <windows.h>
#include <math.h>
#include <stdio.h>
#include <gl\gl.h>
#include <gl\glu.h>
#include <gl\glaux.h>
#include "3DMath.h"
#include "main.h"
#include <iostream.h>
#include <stdarg.h>
#include <string.h>
#include <mmsystem.h>
#include "../res/resource.h"
#include "../mazeio/MazeIO.h"

HDC			hDC=NULL;
HGLRC		hRC=NULL;
HWND		hWnd=NULL;
HINSTANCE	hInstance;


MazeIO _MazeIO;

char	joystring[255];
bool	button[4];

bool	keys[255];
bool	active=TRUE;
bool	fullscreen=TRUE;
bool	blend;
bool	bp;
bool	fp;
bool	blKollision=true;
bool	DoorKey1=false;
bool	blSoundOn=false;
bool    blMultiPlayerSendPosition=false;
bool	blConnectToServer=false;
bool	blClientIdSet=false;
bool	blStartNW=true;
bool	blJoystick=true;

const float piover180 = 0.0174532925f; // PI / 180
const float my_border = 10.0;

float heading;
float xpos=0.5;
float zpos=-0.5;
float xMove;
float zMove;
float PosY=0.0f,PosX=0.0f;
float DoorPosition0=0.0f;		//  0=closed 1=open
float DoorPosition1=-91.0f;		//  -90=closed 0=open
float KeyRotation=0.0f;
float fltEbene=0.1f;
float fltMovement=0.01f;

float NWx=0;
float NWy=0;
float NWz=0;

const int mySTART=0;
const int myEND  =1;
const int myGegner=2;
const int myLIFT1=0;
const int myLIFT2=1;
const int foundDown=1;
const int foundUp=2;
const int EndDoor=0;
const int HallDoor1=1;

int LiftKey1	= 0;
int LiftKey2	= 0;
int LiftUp=1    , LiftDown=0;
int LiftUp2=1   , LiftDown2=0;
int DoorPlace=0 ;				// 0=End , 1=Hallway
int mySocket	= 0;			// Socketnr.
int nBytes;						// number of bytes sent
int myCounter	= 0;
	
#define MAX_MESSAGE_SIZE 255
char buffer[MAX_MESSAGE_SIZE];
char buffer1[MAX_MESSAGE_SIZE];
char txtbuffer[80];                     // general printing buffer
char ClientId='0';
char EnemyCounter=0;
char wtfBuffer[255];

GLfloat	yrot;
GLfloat walkbias = 0.5;
GLfloat walkbiasangle = 0;
GLfloat lookupdown = 0.0f;
GLfloat	z=0.0f;
GLfloat	cnt1;
GLfloat	cnt2;

GLuint  texture[16];
GLuint	base;

char texture_files[16][30];

#define WM_SOCKET	WM_USER+1	// Socket Windows Message
// WinSock globals
SOCKET s;			// Socket handle
sockaddr_in you;	// Server's attributes

typedef struct tagVERTEX
{
	float x, y, z;
	float u, v, t;
} VERTEX;

typedef struct tagTRIANGLE
{
	VERTEX vertex[3];
} TRIANGLE;

typedef struct tagSECTOR
{
	int numtriangles;
	TRIANGLE* triangle;
} SECTOR;

SECTOR sector1;

typedef struct
{
	GLubyte	*imageData;										// Image Data (Up To 32 Bits)
	GLuint	bpp;											// Image Color Depth In Bits Per Pixel.
	GLuint	width;											// Image Width
	GLuint	height;											// Image Height
	GLuint	texID;											// Texture ID Used To Select A Texture
} TextureImage;												// Structure Name

typedef struct tagEnemyData
{
	float x;
	float y;
	float z;
	char ClientId;
} EnemyData;

TextureImage textures[1];									// Storage For One TGA-Texture

LRESULT	CALLBACK WndProc(HWND, UINT, WPARAM, LPARAM);		// Declaration For WndProc

EnemyData myEnemyData[3];

FILE *streamWrite;

int writeToFile(char* Data)
{
	  // Open file in text mode:
	int DataLength = strlen(Data);
	if( (streamWrite = fopen( "fread.txt", "a+t" )) != NULL )
    {
		if (Data[DataLength]!='\n') sprintf(Data,"%s\n",Data);
		fwrite( Data, sizeof( char ), DataLength+1, streamWrite );
    }

	fclose( streamWrite );
	return 0;
}

void eraseFile()
{
	  // Open file in text mode:
	if( (streamWrite = fopen( "fread.txt", "w+t" )) != NULL )
    {
		fwrite( " ", sizeof( char ), 1, streamWrite );
    }

	fclose( streamWrite );
}

	
//		$Author:		DigiBen		digiben@gametutorials.com			 //
/*-----------geaendert von den Autoren -------------------------------------*/
void CheckCameraCollision(SECTOR pVertices, int numOfVerts)
{	

	CVector3 m_vPosition;
	m_vPosition.x = xpos;
	m_vPosition.y = walkbias;
	m_vPosition.z = zpos;

	float m_radius=0.2f;
	
	// This function is pretty much a direct rip off of SpherePolygonCollision()
	// We needed to tweak it a bit though, to handle the collision detection once 
	// it was found, along with checking every triangle in the list if we collided.  
	// pVertices is the world data. If we have space partitioning, we would pass in 
	// the vertices that were closest to the camera. What happens in this function 
	// is that we go through every triangle in the list and check if the camera's 
	// sphere collided with it.  If it did, we don't stop there.  We can have 
	// multiple collisions so it's important to check them all.  One a collision 
	// is found, we calculate the offset to move the sphere off of the collided plane.

	// Go through all the triangles
	
	// --------Aenderung-------------------------------------
	for(int i = 0; i < numOfVerts; i += 1)
	{
		// Store of the current triangle we testing
		CVector3 vTriangle[3];

		vTriangle[0].x=pVertices.triangle[i].vertex[0].x;
		vTriangle[0].y=pVertices.triangle[i].vertex[0].y;
		vTriangle[0].z=pVertices.triangle[i].vertex[0].z;

		vTriangle[1].x=pVertices.triangle[i].vertex[1].x;
		vTriangle[1].y=pVertices.triangle[i].vertex[1].y;
		vTriangle[1].z=pVertices.triangle[i].vertex[1].z;

		vTriangle[2].x=pVertices.triangle[i].vertex[2].x;
		vTriangle[2].y=pVertices.triangle[i].vertex[2].y;
		vTriangle[2].z=pVertices.triangle[i].vertex[2].z;
	// --------Aenderung-Ende--------------------------------

		// 1) STEP ONE - Finding the sphere's classification
	
		// We want the normal to the current polygon being checked
		CVector3 vNormal = Normal(vTriangle);

		// This will store the distance our sphere is from the plane
		float distance = 0.0f;

		// This is where we determine if the sphere is in FRONT, BEHIND, or INTERSECTS the plane
		int classification = ClassifySphere(m_vPosition, vNormal, vTriangle[0], m_radius, distance);
		
		// If the sphere intersects the polygon's plane, then we need to check further
		if(classification == INTERSECTS) 
		{
			// 2) STEP TWO - Finding the psuedo intersection point on the plane

			// Now we want to project the sphere's center onto the triangle's plane
			CVector3 vOffset = vNormal * distance;

			// Once we have the offset to the plane, we just subtract it from the center
			// of the sphere.  "vIntersection" is now a point that lies on the plane of the triangle.
			CVector3 vIntersection = m_vPosition - vOffset;

			// 3) STEP THREE - Check if the intersection point is inside the triangles perimeter

			// We first check if our intersection point is inside the triangle, if not,
			// the algorithm goes to step 4 where we check the sphere again the polygon's edges.

			// We do one thing different in the parameters for EdgeSphereCollision though.
			// Since we have a bulky sphere for our camera, it makes it so that we have to 
			// go an extra distance to pass around a corner. This is because the edges of 
			// the polygons are colliding with our peripheral view (the sides of the sphere).  
			// So it looks likes we should be able to go forward, but we are stuck and considered 
			// to be colliding.  To fix this, we just pass in the radius / 2.  Remember, this
			// is only for the check of the polygon's edges.  It just makes it look a bit more
			// realistic when colliding around corners.  Ideally, if we were using bounding box 
			// collision, cylinder or ellipses, this wouldn't really be a problem.
			//myText("INTERSECT");

			if(InsidePolygon(vIntersection, vTriangle, 3) ||
			   EdgeSphereCollision(m_vPosition, vTriangle, 3, m_radius / 2))
			{
				// If we get here, we have collided!  To handle the collision detection
				// all it takes is to find how far we need to push the sphere back.
				// GetCollisionOffset() returns us that offset according to the normal,
				// radius, and current distance the center of the sphere is from the plane.
				vOffset = GetCollisionOffset(vNormal, m_radius, distance);

				// --------Aenderung-------------------------------------
				
				zpos=zpos+vOffset.z;
				xpos=xpos+vOffset.x;

				// --------Aenderung Ende--------------------------------

				// Now that we have the offset, we want to ADD it to the position and
				// view vector in our camera.  This pushes us back off of the plane.  We
				// don't see this happening because we check collision before we render
				// the scene.
			}
		}
	}
}

/*-------------extracted from NeHe tutorial #10-----------------------------*/
void readstr(FILE *f,char *string)
{
	do
	{
		fgets(string, 255, f);
	} while ((string[0] == '#') || (string[0] == '/') || (string[0] == '\n'));
	return;
}

void itgParser(char string[255])
{
	char seps[]   = " ;";
	char *token ;
	char *printout1;

	//printf( "%s\n\nTokens:\n", string );
   /* Establish string and get the first token: */
   token = strtok( string, seps );
   while( token != NULL )
   {
      /* While there are tokens in "string" */
      printf("%s", token );
	  writeToFile(token);
      /* Get next token: */
      token = strtok( NULL, seps );
	}
}

void SetupNewWorld()
{
	//float x, y, z, u, v, t;
	float x1, x2, yu=0.0f, yo=0.5f, z1, z2, u, v, t;
	int numtriangles;
	FILE *filein;
	char oneline[255];
	filein = fopen("data/newWorld.txt", "rt");				// File To Load World Data From

	readstr(filein,oneline);
	sscanf(oneline, "NUMPOLLIES %d\n", &numtriangles);

	numtriangles+=2;

	sector1.triangle = new TRIANGLE[numtriangles];
	sector1.numtriangles = numtriangles;

		sector1.triangle[0].vertex[0].x = 0;
		sector1.triangle[0].vertex[0].y = 0;
		sector1.triangle[0].vertex[0].z = 0;
		sector1.triangle[0].vertex[0].u = 120;//u;
		sector1.triangle[0].vertex[0].v = 0.0f;
		sector1.triangle[0].vertex[0].t = 1.0f;

		sector1.triangle[0].vertex[1].x = 60;
		sector1.triangle[0].vertex[1].y = 0;
		sector1.triangle[0].vertex[1].z = 0;
		sector1.triangle[0].vertex[1].u = 0.0f;
		sector1.triangle[0].vertex[1].v = 0.0f;
		sector1.triangle[0].vertex[1].t = 0.0f;

		sector1.triangle[0].vertex[2].x = 60;
		sector1.triangle[0].vertex[2].y = 0;
		sector1.triangle[0].vertex[2].z = -60;
		sector1.triangle[0].vertex[2].u = 0.0;
		sector1.triangle[0].vertex[2].v = 120;
		sector1.triangle[0].vertex[2].t = 0.0f;
		
		sector1.triangle[1].vertex[0].x = 60;
		sector1.triangle[1].vertex[0].y = 0;
		sector1.triangle[1].vertex[0].z = -60;
		sector1.triangle[1].vertex[0].u = 120;
		sector1.triangle[1].vertex[0].v = 0.0f;
		sector1.triangle[1].vertex[0].t = 1.0f;

		sector1.triangle[1].vertex[1].x = 0;
		sector1.triangle[1].vertex[1].y = 0;
		sector1.triangle[1].vertex[1].z = -60;
		sector1.triangle[1].vertex[1].u = 0.0f;
		sector1.triangle[1].vertex[1].v = 0;
		sector1.triangle[1].vertex[1].t = 0.0f;

		sector1.triangle[1].vertex[2].x = 0;
		sector1.triangle[1].vertex[2].y = 0;
		sector1.triangle[1].vertex[2].z = 0;
		sector1.triangle[1].vertex[2].u = 0;
		sector1.triangle[1].vertex[2].v = 120;
		sector1.triangle[1].vertex[2].t = 0.0f;
	
	
	for (int loop = 2; loop < numtriangles; loop+=2)
	{
		//for (int vert = 0; vert < 3; vert++)
		//{
		/*readstr(filein,oneline);
		sscanf(oneline, "%f %f %f ", &x1, &z1, &t);
		readstr(filein,oneline);
		sscanf(oneline, "%f %f ", &x2, &z2);*/
		//for (int onelineloop=0;onlineloop<5;onlineloop++)

		
		readstr(filein,oneline);
		//itgParser(oneline);
		
		sscanf(oneline, "%f %f %f %f %f", &x1, &z1, &x2, &z2, &t);
		
		u= (float)sqrt(pow((x2-x1),2.0)+pow((z2-z1),2.0))	;
		v= 1.0f;

		sector1.triangle[loop].vertex[0].x = x1;
		sector1.triangle[loop].vertex[0].y = yu;
		sector1.triangle[loop].vertex[0].z = z1;
		sector1.triangle[loop].vertex[0].u = u;
		sector1.triangle[loop].vertex[0].v = 0.0f;
		sector1.triangle[loop].vertex[0].t = (float)t;

		sector1.triangle[loop].vertex[1].x = x2;
		sector1.triangle[loop].vertex[1].y = yu;
		sector1.triangle[loop].vertex[1].z = z2;
		sector1.triangle[loop].vertex[1].u = 0.0f;
		sector1.triangle[loop].vertex[1].v = 0.0f;
		sector1.triangle[loop].vertex[1].t = 0.0f;

		sector1.triangle[loop].vertex[2].x = x2;
		sector1.triangle[loop].vertex[2].y = yo;
		sector1.triangle[loop].vertex[2].z = z2;
		sector1.triangle[loop].vertex[2].u = 0.0;
		sector1.triangle[loop].vertex[2].v = v;
		sector1.triangle[loop].vertex[2].t = 0.0f;
		
		sector1.triangle[loop+1].vertex[0].x = x1;
		sector1.triangle[loop+1].vertex[0].y = yu;
		sector1.triangle[loop+1].vertex[0].z = z1;
		sector1.triangle[loop+1].vertex[0].u = u;
		sector1.triangle[loop+1].vertex[0].v = 0.0f;
		sector1.triangle[loop+1].vertex[0].t = (float)t;

		sector1.triangle[loop+1].vertex[1].x = x2;
		sector1.triangle[loop+1].vertex[1].y = yo;
		sector1.triangle[loop+1].vertex[1].z = z2;
		sector1.triangle[loop+1].vertex[1].u = 0.0f;
		sector1.triangle[loop+1].vertex[1].v = v;
		sector1.triangle[loop+1].vertex[1].t = 0.0f;

		sector1.triangle[loop+1].vertex[2].x = x1;
		sector1.triangle[loop+1].vertex[2].y = yo;
		sector1.triangle[loop+1].vertex[2].z = z1;
		sector1.triangle[loop+1].vertex[2].u = u;
		sector1.triangle[loop+1].vertex[2].v = v;
		sector1.triangle[loop+1].vertex[2].t = 0.0f;
		
	}
	fclose(filein);

}

/*-------------extracted from NeHe tutorial #10-----------------------------*/
AUX_RGBImageRec *LoadBMP(char *Filename)                // Loads A Bitmap Image
{
        FILE *File=NULL;                                // File Handle

        if (!Filename)                                  // Make Sure A Filename Was Given
        {
                return NULL;                            // If Not Return NULL
        }

        File=fopen(Filename,"r");                       // Check To See If The File Exists

        if (File)                                       // Does The File Exist?
        {
                fclose(File);                           // Close The Handle
                return auxDIBImageLoad(Filename);       // Load The Bitmap And Return A Pointer
        }
        return NULL;                                    // If Load Failed Return NULL
}
/*-------------extracted from NeHe tutorial #10-----------------------------*/
/*-----------geaendert von den Autoren -------------------------------------*/
int LoadGLTextures()                                    // Load Bitmaps And Convert To Textures
{
        int Status=FALSE;                               // Status Indicator
		const GLuint texture_count=16;

        AUX_RGBImageRec *TextureImage[texture_count];   // Create Storage Space For The Texture

        memset(TextureImage,0,sizeof(void *)*1);        // Set The Pointer To NULL

		// --------------Aenderung--------------------------------------
		strcpy (texture_files[0],"Data/wall.bmp");
		strcpy (texture_files[1],"Data/wall2.bmp");
		strcpy (texture_files[2],"Data/wall2_mitspinne.bmp");
		strcpy (texture_files[3],"Data/floor.bmp");
		strcpy (texture_files[4],"Data/floor_mitspinne.bmp");
		strcpy (texture_files[5],"Data/tuer_neu.bmp");
		strcpy (texture_files[6],"Data/liftboden.bmp");
		strcpy (texture_files[7],"Data/liftpfosten.bmp");
		strcpy (texture_files[8],"Data/liftwand.bmp");
		strcpy (texture_files[9],"Data/finish.bmp");
		strcpy (texture_files[10],"Data/wall_mitriss1.bmp");
		strcpy (texture_files[11],"Data/wall_mitriss2.bmp");
		strcpy (texture_files[12],"Data/clouds.bmp");
		strcpy (texture_files[13],"Data/player1.bmp");
		strcpy (texture_files[14],"Data/player2.bmp");
		strcpy (texture_files[15],"Data/player3.bmp");
		
		for (int i=0;i<texture_count;i++)
		{
        if (TextureImage[i]=LoadBMP(texture_files[i]))
        {
                Status=TRUE;                            // Set The Status To TRUE

                glGenTextures(1, &texture[i]);          // Create Three Textures

				// Create MipMapped Texture
				glBindTexture(GL_TEXTURE_2D, texture[i]);
				glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_LINEAR);
				glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_LINEAR_MIPMAP_NEAREST);
				gluBuild2DMipmaps(GL_TEXTURE_2D, 3, TextureImage[i]->sizeX, TextureImage[i]->sizeY, GL_RGB, GL_UNSIGNED_BYTE, TextureImage[i]->data);
        }
        if (TextureImage[i])                            // If Texture Exists
        {
                if (TextureImage[i]->data)              // If Texture Image Exists
                {
                        free(TextureImage[i]->data);    // Free The Texture Image Memory
                }

                free(TextureImage[i]);                  // Free The Image Structure
        }


		}
		// --------------Aenderung--Ende--------------------------------

        return Status;                                  // Return The Status
}

/*-------------extracted from NeHe tutorial #24-----------------------------*/
bool LoadTGA(TextureImage *texture, char *filename)			// Loads A TGA File Into Memory
{
	GLubyte		TGAheader[12]={0,0,2,0,0,0,0,0,0,0,0,0};	// Uncompressed TGA Header
	GLubyte		TGAcompare[12];								// Used To Compare TGA Header
	GLubyte		header[6];									// First 6 Useful Bytes From The Header
	GLuint		bytesPerPixel;								// Holds Number Of Bytes Per Pixel Used In The TGA File
	GLuint		imageSize;									// Used To Store The Image Size When Setting Aside Ram
	GLuint		temp;										// Temporary Variable
	GLuint		type=GL_RGBA;								// Set The Default GL Mode To RBGA (32 BPP)

	FILE *file = fopen(filename, "rb");						// Open The TGA File

	if(	file==NULL ||										// Does File Even Exist?
		fread(TGAcompare,1,sizeof(TGAcompare),file)!=sizeof(TGAcompare) ||	// Are There 12 Bytes To Read?
		memcmp(TGAheader,TGAcompare,sizeof(TGAheader))!=0				||	// Does The Header Match What We Want?
		fread(header,1,sizeof(header),file)!=sizeof(header))				// If So Read Next 6 Header Bytes
	{
		if (file == NULL)									// Did The File Even Exist? *Added Jim Strong*
			return false;									// Return False
		else
		{
			fclose(file);									// If Anything Failed, Close The File
			return false;									// Return False
		}
	}

	texture->width  = header[1] * 256 + header[0];			// Determine The TGA Width	(highbyte*256+lowbyte)
	texture->height = header[3] * 256 + header[2];			// Determine The TGA Height	(highbyte*256+lowbyte)

 	if(	texture->width	<=0	||								// Is The Width Less Than Or Equal To Zero
		texture->height	<=0	||								// Is The Height Less Than Or Equal To Zero
		(header[4]!=24 && header[4]!=32))					// Is The TGA 24 or 32 Bit?
	{
		fclose(file);										// If Anything Failed, Close The File
		return false;										// Return False
	}

	texture->bpp	= header[4];							// Grab The TGA's Bits Per Pixel (24 or 32)
	bytesPerPixel	= texture->bpp/8;						// Divide By 8 To Get The Bytes Per Pixel
	imageSize		= texture->width*texture->height*bytesPerPixel;	// Calculate The Memory Required For The TGA Data

	texture->imageData=(GLubyte *)malloc(imageSize);		// Reserve Memory To Hold The TGA Data

	if(	texture->imageData==NULL ||							// Does The Storage Memory Exist?
		fread(texture->imageData, 1, imageSize, file)!=imageSize)	// Does The Image Size Match The Memory Reserved?
	{
		if(texture->imageData!=NULL)						// Was Image Data Loaded
			free(texture->imageData);						// If So, Release The Image Data

		fclose(file);										// Close The File
		return false;										// Return False
	}

	for(GLuint i=0; i<int(imageSize); i+=bytesPerPixel)		// Loop Through The Image Data
	{														// Swaps The 1st And 3rd Bytes ('R'ed and 'B'lue)
		temp=texture->imageData[i];							// Temporarily Store The Value At Image Data 'i'
		texture->imageData[i] = texture->imageData[i + 2];	// Set The 1st Byte To The Value Of The 3rd Byte
		texture->imageData[i + 2] = temp;					// Set The 3rd Byte To The Value In 'temp' (1st Byte Value)
	}

	fclose (file);											// Close The File

	// Build A Texture From The Data
	glGenTextures(1, &texture[0].texID);					// Generate OpenGL texture IDs

	glBindTexture(GL_TEXTURE_2D, texture[0].texID);			// Bind Our Texture
	glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);	// Linear Filtered
	glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);	// Linear Filtered

	if (texture[0].bpp==24)									// Was The TGA 24 Bits
	{
		type=GL_RGB;										// If So Set The 'type' To GL_RGB
	}

	glTexImage2D(GL_TEXTURE_2D, 0, type, texture[0].width, texture[0].height, 0, type, GL_UNSIGNED_BYTE, texture[0].imageData);

	return true;											// Texture Building Went Ok, Return True
}

/*-------------extracted from NeHe tutorial #10-----------------------------*/
GLvoid ReSizeGLScene(GLsizei width, GLsizei height)		// Resize And Initialize The GL Window
{
	if (height==0)										// Prevent A Divide By Zero By
	{
		height=1;										// Making Height Equal One
	}

	glViewport(0,0,width,height);						// Reset The Current Viewport

	glMatrixMode(GL_PROJECTION);						// Select The Projection Matrix
	glLoadIdentity();									// Reset The Projection Matrix

	// Calculate The Aspect Ratio Of The Window
	gluPerspective(45.0f,(GLfloat)width/(GLfloat)height,0.1f,100.0f);

	glMatrixMode(GL_MODELVIEW);							// Select The Modelview Matrix
	glLoadIdentity();									// Reset The Modelview Matrix
}

/*-------------extracted from NeHe tutorial #10-----------------------------*/
/*-----------erweitert von den Autoren -------------------------------------*/
int InitGL(GLvoid)										// All Setup For OpenGL Goes Here
{
	if (!LoadGLTextures())								// Jump To Texture Loading Routine
	{
		return FALSE;									// If Texture Didn't Load Return FALSE
	}

	// -----------Erweiterung--------------------------------
	if (!LoadTGA(&textures[0],"Data/key.TGA"))			// Load The Font Texture
	{
		return false;									// If Loading Failed, Return False
	}

	glEnable(GL_TEXTURE_2D);							// Enable Texture Mapping
	glBlendFunc(GL_SRC_ALPHA,GL_ONE);					// Set The Blending Function For Translucency
	glClearColor(0.0f, 0.0f, 0.0f, 0.0f);				// This Will Clear The Background Color To Black
	glClearDepth(1.0);									// Enables Clearing Of The Depth Buffer
	glDepthFunc(GL_LESS);								// The Type Of Depth Test To Do
	glEnable(GL_DEPTH_TEST);							// Enables Depth Testing
	glShadeModel(GL_SMOOTH);							// Enables Smooth Color Shading
	glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);	// Really Nice Perspective Calculations
	
	SetupNewWorld();
	
	return TRUE;
}
/*-------------extracted from NeHe tutorial #10-----------------------------*/
/*-----------erweitert von den Autoren -------------------------------------*/


int DrawGLScene(GLvoid)
{
	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	glLoadIdentity();
	bool DrawIt=false;

	/*-----------erweitert von den Autoren -------------------------------------*/

	float fogColor[4] = {1.0, 1.0, 1.0, 1.0f};			// Let's make the Fog Color black too

	glFogi(GL_FOG_MODE, GL_EXP2);						// Set The Fog Mode
	glFogfv(GL_FOG_COLOR, fogColor);					// Set The Fog Color
	glFogf(GL_FOG_DENSITY, 0.5f);						// Set How Dense Will The Fog Be
	glHint(GL_FOG_HINT, GL_DONT_CARE);					// Set The Fog's calculation accuracy
	glFogf(GL_FOG_START, 0);							// Set The Fog's Start Depth
	glFogf(GL_FOG_END, 50.0f);							// Set The Fog's End Depth
	//glEnable(GL_FOG);

	GLfloat x_m, y_m, z_m, u_m, v_m , my_y0 , my_y1 , my_y2 , my_yMax , my_yMin , my_t;

	// Kollisionerkennung
	if (blKollision) CheckCameraCollision(sector1,sector1.numtriangles);

	
	/*-----------erweitert von den Autoren --Ende-------------------------------*/

	GLfloat xtrans = -xpos;
	GLfloat ztrans = -zpos;
	GLfloat ytrans = -walkbias;
	GLfloat sceneroty = 360.0f - yrot;

	int numtriangles;
	int upset;

	numtriangles = sector1.numtriangles;

	xpos=xpos+xMove ;
	zpos=zpos+zMove ;

	glRotatef(lookupdown,1.0f,0,0);
	glRotatef(sceneroty,0,1.0f,0);

	glTranslatef(xtrans, ytrans, ztrans);


	for (int loop_m = 0; loop_m < numtriangles; loop_m++)
	{
		my_y0=-sector1.triangle[loop_m].vertex[0].y;
		my_y1=-sector1.triangle[loop_m].vertex[1].y;
		my_y2=-sector1.triangle[loop_m].vertex[2].y;
		my_t=sector1.triangle[loop_m].vertex[0].t;

		my_yMax = __max((float)my_y0   , (float)my_y1);
		my_yMax = __max(my_yMax , (float)my_y2);

		my_yMin = __min((float)my_y0   , (float)my_y1);
		my_yMin = __min(my_yMin , (float)my_y2);

		upset=((int)sector1.triangle[loop_m].vertex[0].t);	// Nummer der Textur
	/*-----------erweitert von den Autoren -Ende--------------------------------*/

		glBindTexture(GL_TEXTURE_2D, texture[upset]);		// Textur an Polygon binden

		glBegin(GL_TRIANGLES);								// Waende zeichnen
			glNormal3f( 0.0f, 0.0f, 1.0f);
			x_m = sector1.triangle[loop_m].vertex[0].x;
			y_m = sector1.triangle[loop_m].vertex[0].y;
			z_m = sector1.triangle[loop_m].vertex[0].z;
			u_m = sector1.triangle[loop_m].vertex[0].u;
			v_m = sector1.triangle[loop_m].vertex[0].v;
			glTexCoord2f(u_m,v_m); glVertex3f(x_m,y_m,z_m);

			x_m = sector1.triangle[loop_m].vertex[1].x;
			y_m = sector1.triangle[loop_m].vertex[1].y;
			z_m = sector1.triangle[loop_m].vertex[1].z;
			u_m = sector1.triangle[loop_m].vertex[1].u;
			v_m = sector1.triangle[loop_m].vertex[1].v;
			glTexCoord2f(u_m,v_m); glVertex3f(x_m,y_m,z_m);

			x_m = sector1.triangle[loop_m].vertex[2].x;
			y_m = sector1.triangle[loop_m].vertex[2].y;
			z_m = sector1.triangle[loop_m].vertex[2].z;
			u_m = sector1.triangle[loop_m].vertex[2].u;
			v_m = sector1.triangle[loop_m].vertex[2].v;
			glTexCoord2f(u_m,v_m); glVertex3f(x_m,y_m,z_m);
		glEnd();

	}

	xMove=0.0;												// Bewegung auf 0 setzen
	zMove=0.0;												// sonst staendige Bewegung
	return TRUE;
}

/*-------------extracted from NeHe tutorial #10-----------------------------*/
GLvoid KillGLWindow(GLvoid)
{
	if (fullscreen)
	{
		ChangeDisplaySettings(NULL,0);					// If So Switch Back To The Desktop
		ShowCursor(TRUE);								// Show Mouse Pointer
	}

	if (hRC)											// Do We Have A Rendering Context?
	{
		if (!wglMakeCurrent(NULL,NULL))					// Are We Able To Release The DC And RC Contexts?
		{
			MessageBox(NULL,"Release Of DC And RC Failed.","SHUTDOWN ERROR",MB_OK | MB_ICONINFORMATION);
		}

		if (!wglDeleteContext(hRC))						// Are We Able To Delete The RC?
		{
			MessageBox(NULL,"Release Rendering Context Failed.","SHUTDOWN ERROR",MB_OK | MB_ICONINFORMATION);
		}
		hRC=NULL;										// Set RC To NULL
	}

	if (hDC && !ReleaseDC(hWnd,hDC))					// Are We Able To Release The DC
	{
		MessageBox(NULL,"Release Device Context Failed.","SHUTDOWN ERROR",MB_OK | MB_ICONINFORMATION);
		hDC=NULL;										// Set DC To NULL
	}

	if (hWnd && !DestroyWindow(hWnd))					// Are We Able To Destroy The Window?
	{
		MessageBox(NULL,"Could Not Release hWnd.","SHUTDOWN ERROR",MB_OK | MB_ICONINFORMATION);
		hWnd=NULL;										// Set hWnd To NULL
	}

	if (!UnregisterClass("OpenGL",hInstance))			// Are We Able To Unregister Class
	{
		MessageBox(NULL,"Could Not Unregister Class.","SHUTDOWN ERROR",MB_OK | MB_ICONINFORMATION);
		hInstance=NULL;									// Set hInstance To NULL
	}


}

/*-------------extracted from NeHe tutorial #10-----------------------------*/
/*	This Code Creates Our OpenGL Window.  Parameters Are:					*
 *	title			- Title To Appear At The Top Of The Window				*
 *	width			- Width Of The GL Window Or Fullscreen Mode				*
 *	height			- Height Of The GL Window Or Fullscreen Mode			*
 *	bits			- Number Of Bits To Use For Color (8/16/24/32)			*
 *	fullscreenflag	- Use Fullscreen Mode (TRUE) Or Windowed Mode (FALSE)	*/

BOOL CreateGLWindow(char* title, int width, int height, int bits, bool fullscreenflag)
{
	GLuint		PixelFormat;			// Holds The Results After Searching For A Match
	WNDCLASS	wc;						// Windows Class Structure
	DWORD		dwExStyle;				// Window Extended Style
	DWORD		dwStyle;				// Window Style
	RECT		WindowRect;				// Grabs Rectangle Upper Left / Lower Right Values
	WindowRect.left=(long)0;			// Set Left Value To 0
	WindowRect.right=(long)width;		// Set Right Value To Requested Width
	WindowRect.top=(long)0;				// Set Top Value To 0
	WindowRect.bottom=(long)height;		// Set Bottom Value To Requested Height

	fullscreen=fullscreenflag;			// Set The Global Fullscreen Flag

	hInstance			= GetModuleHandle(NULL);				// Grab An Instance For Our Window
	wc.style			= CS_HREDRAW | CS_VREDRAW;// | CS_OWNDC;	// Redraw On Size, And Own DC For Window.
	wc.lpfnWndProc		= (WNDPROC) WndProc;					// WndProc Handles Messages
	wc.cbClsExtra		= 0;									// No Extra Window Data
	wc.cbWndExtra		= 0;									// No Extra Window Data
	wc.hInstance		= hInstance;							// Set The Instance
	wc.hIcon			= LoadIcon(hInstance, (LPCTSTR)IDI_ICON1);//LoadIcon(NULL, IDI_WINLOGO);			// Load The Default Icon
	wc.hCursor			= LoadCursor(NULL, IDC_ARROW);			// Load The Arrow Pointer
	wc.hbrBackground	= (HBRUSH)(GetSysColor (COLOR_WINDOW+1));//NULL;									// No Background Required For GL
	wc.lpszMenuName		= (LPCSTR)IDR_MENU1;//NULL;									// We Don't Want A Menu
	wc.lpszClassName	= "OpenGL";								// Set The Class Name

	if (!RegisterClass(&wc))									// Attempt To Register The Window Class
	{
		MessageBox(NULL,"Failed To Register The Window Class.","ERROR",MB_OK|MB_ICONEXCLAMATION);
		return FALSE;
	}

/*	if (fullscreen)												// Attempt Fullscreen Mode?
	{
		DEVMODE dmScreenSettings;								// Device Mode
		memset(&dmScreenSettings,0,sizeof(dmScreenSettings));	// Makes Sure Memory's Cleared
		dmScreenSettings.dmSize=sizeof(dmScreenSettings);		// Size Of The Devmode Structure
		dmScreenSettings.dmPelsWidth	= width;				// Selected Screen Width
		dmScreenSettings.dmPelsHeight	= height;				// Selected Screen Height
		dmScreenSettings.dmBitsPerPel	= bits;					// Selected Bits Per Pixel
		dmScreenSettings.dmFields=DM_BITSPERPEL|DM_PELSWIDTH|DM_PELSHEIGHT;

		// Try To Set Selected Mode And Get Results.  NOTE: CDS_FULLSCREEN Gets Rid Of Start Bar.
		if (ChangeDisplaySettings(&dmScreenSettings,CDS_FULLSCREEN)!=DISP_CHANGE_SUCCESSFUL)
		{
			// If The Mode Fails, Offer Two Options.  Quit Or Use Windowed Mode.
			if (MessageBox(NULL,"The Requested Fullscreen Mode Is Not Supported By\nYour Video Card. Use Windowed Mode Instead?","NeHe GL",MB_YESNO|MB_ICONEXCLAMATION)==IDYES)
			{
				fullscreen=FALSE;
			}
			else
			{
				// Pop Up A Message Box Letting User Know The Program Is Closing.
				MessageBox(NULL,"Program Will Now Close.","ERROR",MB_OK|MB_ICONSTOP);
				return FALSE;
			}
		}
	}*/

	if (fullscreen)												// Are We Still In Fullscreen Mode?
	{
		dwExStyle=WS_EX_APPWINDOW;								// Window Extended Style
		dwStyle=WS_POPUP;										// Windows Style
		ShowCursor(FALSE);										// Hide Mouse Pointer
	}
	else
	{
		dwExStyle=WS_EX_APPWINDOW | WS_EX_WINDOWEDGE;			// Window Extended Style
		dwStyle=WS_OVERLAPPEDWINDOW;							// Windows Style
	}

	AdjustWindowRectEx(&WindowRect, dwStyle, FALSE, dwExStyle);		// Adjust Window To True Requested Size

	// Create The Window
	if (!(hWnd=CreateWindowEx(	dwExStyle,							// Extended Style For The Window
								"OpenGL",							// Class Name
								title,								// Window Title
								dwStyle |							// Defined Window Style
								WS_CLIPSIBLINGS |					// Required Window Style
								WS_CLIPCHILDREN,					// Required Window Style
								0, 0,								// Window Position
								WindowRect.right-WindowRect.left,	// Calculate Window Width
								WindowRect.bottom-WindowRect.top,	// Calculate Window Height
								NULL,								// No Parent Window
								NULL,								// No Menu
								hInstance,							// Instance
								NULL)))								// Dont Pass Anything To WM_CREATE
	{
		KillGLWindow();
		MessageBox(NULL,"Window Creation Error.","ERROR",MB_OK|MB_ICONEXCLAMATION);
		return FALSE;
	}


	static	PIXELFORMATDESCRIPTOR pfd=				// pfd Tells Windows How We Want Things To Be
	{
		sizeof(PIXELFORMATDESCRIPTOR),				// Size Of This Pixel Format Descriptor
		1,											// Version Number
		PFD_DRAW_TO_WINDOW |						// Format Must Support Window
		PFD_SUPPORT_OPENGL |						// Format Must Support OpenGL
		PFD_DOUBLEBUFFER,							// Must Support Double Buffering
		PFD_TYPE_RGBA,								// Request An RGBA Format
		bits,										// Select Our Color Depth
		0, 0, 0, 0, 0, 0,							// Color Bits Ignored
		0,											// No Alpha Buffer
		0,											// Shift Bit Ignored
		0,											// No Accumulation Buffer
		0, 0, 0, 0,									// Accumulation Bits Ignored
		16,											// 16Bit Z-Buffer (Depth Buffer)
		0,											// No Stencil Buffer
		0,											// No Auxiliary Buffer
		PFD_MAIN_PLANE,								// Main Drawing Layer
		0,											// Reserved
		0, 0, 0										// Layer Masks Ignored
	};

	if (!(hDC=GetDC(hWnd)))							// Did We Get A Device Context?
	{
		KillGLWindow();
		MessageBox(NULL,"Can't Create A GL Device Context.","ERROR",MB_OK|MB_ICONEXCLAMATION);
		return FALSE;
	}

	if (!(PixelFormat=ChoosePixelFormat(hDC,&pfd)))	// Did Windows Find A Matching Pixel Format?
	{
		KillGLWindow();
		MessageBox(NULL,"Can't Find A Suitable PixelFormat.","ERROR",MB_OK|MB_ICONEXCLAMATION);
		return FALSE;
	}

	if(!SetPixelFormat(hDC,PixelFormat,&pfd))		// Are We Able To Set The Pixel Format?
	{
		KillGLWindow();
		MessageBox(NULL,"Can't Set The PixelFormat.","ERROR",MB_OK|MB_ICONEXCLAMATION);
		return FALSE;
	}

	if (!(hRC=wglCreateContext(hDC)))				// Are We Able To Get A Rendering Context?
	{
		KillGLWindow();
		MessageBox(NULL,"Can't Create A GL Rendering Context.","ERROR",MB_OK|MB_ICONEXCLAMATION);
		return FALSE;
	}

	if(!wglMakeCurrent(hDC,hRC))					// Try To Activate The Rendering Context
	{
		KillGLWindow();
		MessageBox(NULL,"Can't Activate The GL Rendering Context.","ERROR",MB_OK|MB_ICONEXCLAMATION);
		return FALSE;
	}

	ShowWindow(hWnd,SW_SHOW);						// Show The Window
	SetForegroundWindow(hWnd);						// Slightly Higher Priority
	SetFocus(hWnd);									// Sets Keyboard Focus To The Window
	ReSizeGLScene(width, height);					// Set Up Our Perspective GL Screen

	if (!InitGL())									// Initialize Our Newly Created GL Window
	{
		KillGLWindow();
		MessageBox(NULL,"Initialization Failed.","ERROR",MB_OK|MB_ICONEXCLAMATION);
		return FALSE;
	}

	return TRUE;									// Success
}

/*-------------extracted from NeHe tutorial #10-----------------------------*/
LRESULT CALLBACK WndProc(	HWND	hWnd,			// Handle For This Window
							UINT	uMsg,			// Message For This Window
							WPARAM	wParam,			// Additional Message Information
							LPARAM	lParam)			// Additional Message Information
{
	HDC hdc;
	PAINTSTRUCT ps;
	switch (uMsg)									// Check For Windows Messages
	{
		case WM_ACTIVATE:							// Watch For Window Activate Message
		{
			if (!HIWORD(wParam))					// Check Minimization State
			{
				active=TRUE;						// Program Is Active
			}
			else
			{
				active=FALSE;						// Program Is No Longer Active
			}

			return 0;								// Return To The Message Loop
		}

		case WM_SYSCOMMAND:							// Intercept System Commands
		{
			switch (wParam)							// Check System Calls
			{
				case SC_SCREENSAVE:					// Screensaver Trying To Start?
				case SC_MONITORPOWER:				// Monitor Trying To Enter Powersave?
				return 0;							// Prevent From Happening
			}
			break;									// Exit
		}

		case WM_PAINT:
		{
			hdc = BeginPaint (hWnd, &ps);
			// ZU ERLEDIGEN: Hier beliebigen Code zum Zeichnen hinzufügen...
			RECT rt;
			GetClientRect( hWnd, &rt );
			SetTextColor(hdc,0x00FFFFFF);
			DrawText( hdc, "Hallo!", strlen("Hallo!"), &rt, DT_RIGHT);//DT_CENTER );
			EndPaint( hWnd, &ps );
			break;
		}
		
		case WM_CLOSE:								// Did We Receive A Close Message?
		{
			PostQuitMessage(0);						// Send A Quit Message
			return 0;								// Jump Back
		}

		case WM_KEYDOWN:							// Is A Key Being Held Down?
		{
			keys[wParam] = TRUE;					// If So, Mark It As TRUE
			return 0;								// Jump Back
		}

		case WM_KEYUP:								// Has A Key Been Released?
		{
			keys[wParam] = FALSE;					// If So, Mark It As FALSE
			return 0;								// Jump Back
		}

		case WM_SIZE:								// Resize The OpenGL Window
		{
			ReSizeGLScene(LOWORD(lParam),HIWORD(lParam));	// LoWord=Width, HiWord=Height
			return 0;										// Jump Back
		}
		
	}
	return DefWindowProc(hWnd,uMsg,wParam,lParam);
}

/**
 * Initialize the MazeIO subsystem. Shows a message box if opening of any
 * input device fails.
 *
 */
bool initMazeIO(HWND window)
{
	long res;
	bool hmd = true, jst = true, mouse = true, keyboard = true;
	char str[128];

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

void getInput(float pos[3])
{
	_MazeIO.getData(pos);
}

/*-------------extracted from NeHe tutorial #10-----------------------------*/
/*------------erweitert von den Autoren-------------------------------------*/
int WINAPI WinMain(	HINSTANCE	hInstance,			// Instance
					HINSTANCE	hPrevInstance,		// Previous Instance
					LPSTR		lpCmdLine,			// Command Line Parameters
					int			nCmdShow)			// Window Show State
{
	MSG		msg;									// Windows Message Structure
	char	MsgMsg[300];
	BOOL	done=FALSE;								// Bool Variable To Exit Loop

	// Ask The User Which Screen Mode They Prefer
	//if (MessageBox(NULL,"Would You Like To Run In Fullscreen Mode?", "Start FullScreen?",MB_YESNO|MB_ICONQUESTION)==IDNO)
	//{
		fullscreen=FALSE;							// Windowed Mode
	//}

	eraseFile();

	// Create Our OpenGL Window
	if (!CreateGLWindow("Createam Berlin  3D CG  -  Press 'H' for Help",640,480,16,fullscreen))
	{
		return 0;									// Quit If Window Was Not Created
	}

	//MAZEIO CAN ONLY BE INITIALIZED WHEN WE GOT THE WINDOW HANDLE
	//initMazeIO(hWnd);

	while(!done)									// Loop That Runs While done=FALSE
	{
		if (PeekMessage(&msg,NULL,0,0,PM_REMOVE))	// Is There A Message Waiting?
		{
			if (msg.message==WM_QUIT)				// Have We Received A Quit Message?
			{
				done=TRUE;							// If So done=TRUE
			}
			else									// If Not, Deal With Window Messages
			{
				TranslateMessage(&msg);				// Translate The Message
				DispatchMessage(&msg);				// Dispatch The Message
			}
		}
		else										// If There Are No Messages
		{
			if ((active && !DrawGLScene()) || keys[VK_ESCAPE])	// Active?  Was There A Quit Received?
			{
				done=TRUE;							// ESC or DrawGLScene Signalled A Quit
			}
			else
			{
				SwapBuffers(hDC);
				if (keys['B'] && !bp)
				{
					bp=TRUE;
					blend=!blend;
					if (!blend)
					{
						glDisable(GL_BLEND);
						glEnable(GL_DEPTH_TEST);
					}
					else
					{
						glEnable(GL_BLEND);
						glDisable(GL_DEPTH_TEST);
					}
				}
				if (!keys['B'])
				{
					bp=FALSE;
				}

/*-------------Erweiterung -------------------------------------------------------*/
				if (keys['S'])
				{
					xpos=0.5f;
					zpos=0.5f;
					fltEbene=0.1f;
					walkbias=-0.5f;
					yrot=0.0f;
					walkbiasangle=0.0f;
					heading=0.0f;

					LiftUp=1;
					LiftDown=0;
					LiftKey1=0;

					LiftUp2=1;
					LiftDown2=0;
					LiftKey2=0;

					DoorKey1=false;
				}

				if (keys['P'])
				{
					if (!blSoundOn) 
						blSoundOn=true;
				}

				if (keys['K'])
				{
					if (blKollision) 
						blKollision=false;
				}

				if (keys['Q'])
				{
					walkbias=+2.5f;
					fltEbene=0.5f;
					LiftUp2=0;
					LiftDown2=1;
					LiftKey2=0;
				}

				if (keys['A'])
				{
					walkbias=0.5f;
					fltEbene=0.3f;

					LiftUp=0;
					LiftDown=1;
					LiftKey1=0;
					
					LiftUp2=1;
					LiftDown2=0;
					LiftKey2=0;
				}

				if (keys['Y'])
				{
					LiftUp=1;
					LiftDown=0;
					LiftKey1=0;
					
					LiftUp2=1;
					LiftDown2=0;
					LiftKey2=0;

					walkbias=-0.5f;
					fltEbene=0.1f;
				}

				if (keys['N'])
				{
					xMove = -(float)sin((heading+90.0f)*piover180) * fltMovement; 
					zMove = -(float)cos((heading+90.0f)*piover180) * fltMovement;
				}
				
				if (keys['M'])
				{
					xMove = +(float)sin((heading+90.0f)*piover180) * fltMovement;
					zMove = +(float)cos((heading+90.0f)*piover180) * fltMovement;
				}
				
				if (keys[VK_UP])
				{
					xMove = -(float)sin(heading*piover180) * fltMovement;
					zMove = -(float)cos(heading*piover180) * fltMovement;
				}

				if (keys[VK_DOWN])
				{
					xMove = +(float)sin(heading*piover180) * fltMovement;
					zMove = +(float)cos(heading*piover180) * fltMovement;
				}

				if (keys[VK_RIGHT])
				{
					heading -= (60*fltMovement);
					yrot = heading;
					blMultiPlayerSendPosition=true;
				}

				if (keys[VK_LEFT])
				{
					heading += (60*fltMovement);
					yrot = heading;
					blMultiPlayerSendPosition=true;
				}

				if (keys[VK_PRIOR])
				{
					lookupdown-= 0.2f;
				}

				if (keys[VK_NEXT])
				{
					lookupdown+= 0.2f;
				}

				if (keys['E'])
				{
					// Render the triangles in fill mode		
					glEnable(GL_TEXTURE_2D);							// Enable Texture Mapping
					glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);	
					blKollision=true;
				}
				
				if (keys['W'])
				{
					// Render the triangles in wire frame mode
					glDisable(GL_TEXTURE_2D);				// Disable Texture Mapping
					glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);	
				}

				if (keys['H'])
				{
					strcpy(MsgMsg,"");
					strcat(MsgMsg,"Help\n");
					strcat(MsgMsg,"====\n");
					strcat(MsgMsg,"\n");
					strcat(MsgMsg,"F1		:	Fullscreen / Window\n");
					strcat(MsgMsg,"ESC		:	Quit\n");
					strcat(MsgMsg,"H		:	Show this Help\n");
					strcat(MsgMsg,"Cursor Keys	:	Move / Rotate\n");
					strcat(MsgMsg,"Page Up		:	Look up\n");
					strcat(MsgMsg,"Page Down	:	Look down\n");
					strcat(MsgMsg,"N		:	Slide left\n");
					strcat(MsgMsg,"M		:	Slide right\n");
					strcat(MsgMsg,"S		:	Reset to Startpoint \n");
					strcat(MsgMsg,"W		:	Wireframe mode on\n");
					strcat(MsgMsg,"E		:	Wireframe mode off\n");
					strcat(MsgMsg,"P		:	Sound on\n");
					strcat(MsgMsg,"F2		:	Sound off\n");
					strcat(MsgMsg,"F12		:	Network connect\n");

					MessageBox(NULL,MsgMsg,"HELP 3D",MB_OK | MB_ICONINFORMATION | MB_APPLMODAL );
					keys['H']=false;
				}
				/*-------------Erweiterung Ende -----------------------------*/

				if (keys[VK_F3])						// Is F2 Being Pressed?
				{
					if(blSoundOn) PlaySound("Data/music.wav" , NULL, SND_FILENAME | SND_ASYNC | SND_LOOP | SND_NOSTOP);
				}

				if (keys[VK_F2])						// Is F3 Being Pressed?
				{
					PlaySound(NULL,NULL,SND_LOOP | SND_ASYNC);
					blSoundOn=false;
				}

				if (keys[VK_F5])						// Is F3 Being Pressed?
				{
					fltMovement=fltMovement+0.01f;
					keys[VK_F5]=false;
				}

				if (keys[VK_F6])						// Is F3 Being Pressed?
				{
					fltMovement=fltMovement-0.01f;
					keys[VK_F6]=false;
					if (fltMovement<0.01f) fltMovement=0.01f;
				}

				if (keys[VK_F1])						// Is F1 Being Pressed?
				{
					keys[VK_F1]=FALSE;					// If So Make Key FALSE
					KillGLWindow();						// Kill Our Current Window
					fullscreen=!fullscreen;				// Toggle Fullscreen / Windowed Mode
					if (!CreateGLWindow("Createam Berlin  3D CG  -  Press 'H' for Help",640,480,16,fullscreen))
					{ 
						return 0;						// Quit If Window Was Not Created
					}
				}

				//Joystick jst;
				//JoyState state;
				//bool button[4];



				//while 
/*
				float pos[3];

				getInput(pos);

				heading -= (60*fltMovement*pos[0]);
				yrot = heading;
				xMove = +(float)sin(heading*piover180) * fltMovement * pos[1];
				zMove = +(float)cos(heading*piover180) * fltMovement * pos[1];
*/
//REPLACE WITH MAZEIO
/*
				if(state.pos[1] < 1.01 && blJoystick)
				{
					if (!jst.getState(&state))
					{
						//return 1;
						
					}
					else
					{
						button[0] = state.buttons & JOY_BUTTON1 ? true : false;
						button[1] = state.buttons & JOY_BUTTON2 ? true : false;
						button[2] = state.buttons & JOY_BUTTON3 ? true : false;
						button[3] = state.buttons & JOY_BUTTON4 ? true : false;

						heading -= (60*fltMovement*state.pos[0]);
						yrot = heading;

					
						if (!keys[VK_UP] && !keys[VK_DOWN])
						{
							xMove = +(float)sin(heading*piover180) * fltMovement * state.pos[1];
							zMove = +(float)cos(heading*piover180) * fltMovement * state.pos[1];
						}
					

						sprintf(joystring,"x:%2.2f y:%2.2f z:%2.2f b1:%d b2:%d b3:%d b4:%d xM:%f zM:%f\r",
						state.pos[0],
						state.pos[1],
						state.pos[2],
						button[0],
						button[1],
						button[2],
						button[3],
						xMove,
						zMove);

						//Sleep(10);
						if (state.pos[0] +
							state.pos[1] +
							state.pos[2] +
							button[0]    +
							button[1]    +
							button[2]    +
							button[3]>0.0)
						{
							writeToFile(joystring);
						}
					}//if (!jst.getState(&state))
				}//if(state.pos[1] < 1.0)
*/
			}//if ((active && !DrawGLScene()) || keys[VK_ESCAPE])
		}
	}
	KillGLWindow();										// Kill The Window

	// shutdown our client
	
	strcpy(MsgMsg,"");
	strcat(MsgMsg,"Der Dank der Autoren geht an\n");
	strcat(MsgMsg,"==================\n");
	strcat(MsgMsg,"\n");
	strcat(MsgMsg,"NeHe		:	nehe.gamedev.net\n");
	strcat(MsgMsg,"DigiBen		:	www.gametutorials.com\n");
	strcat(MsgMsg,"Richard Sliwa und Dave Wythe\n		:	http://www.stinsv.com\n");

	//MessageBox(NULL,MsgMsg,"THANKS 3D",MB_OK | MB_ICONINFORMATION | MB_APPLMODAL );

	return (msg.wParam);								// Exit The Program
}

