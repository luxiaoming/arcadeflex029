/*
This file is part of Arcadeflex.

Arcadeflex is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Arcadeflex is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Arcadeflex.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * ported to v0.28
 * ported to v0.27
 *
 *
 *   Notes : Roms are from v0.36 romset
 */

package drivers;

import static arcadeflex.libc.*;
import static mame.commonH.*;
import static mame.cpuintrf.*;
import static mame.driverH.*;
import static mame.mame.*;
import static mame.inptport.*;
import static mame.osdependH.*;
import static sndhrdw._8910intf.*;
import static sndhrdw.generic.*;
import static sndhrdw.timeplt.*;
import static vidhrdw.generic.*;
import static vidhrdw.timeplt.*;
import static mame.memoryH.*;
public class timeplt
{



	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0xa000, 0xbfff, MRA_RAM ),
		new MemoryReadAddress( 0x0000, 0x5fff, MRA_ROM ),
		new MemoryReadAddress( 0xc000, 0xc000, input_port_0_r ),	/* ?????????????????? */
		new MemoryReadAddress( 0xc300, 0xc300, input_port_0_r ),	/* IN0 */
		new MemoryReadAddress( 0xc320, 0xc320, input_port_1_r ),	/* IN1 */
		new MemoryReadAddress( 0xc340, 0xc340, input_port_2_r ),	/* IN2 */
		new MemoryReadAddress( 0xc360, 0xc360, input_port_3_r ),	/* DSW1 */
		new MemoryReadAddress( 0xc200, 0xc200, input_port_4_r ),	/* DSW2 */
		new MemoryReadAddress( -1 )	/* end of table */
	};

	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0xa800, 0xafff, MWA_RAM ),
		new MemoryWriteAddress( 0xa000, 0xa3ff, colorram_w, colorram ),
		new MemoryWriteAddress( 0xa400, 0xa7ff, videoram_w, videoram,videoram_size ),
		new MemoryWriteAddress( 0xb010, 0xb03f, MWA_RAM, spriteram,spriteram_size ),
		new MemoryWriteAddress( 0xb410, 0xb43f, MWA_RAM, spriteram_2 ),
		new MemoryWriteAddress( 0xc300, 0xc300, interrupt_enable_w ),
		new MemoryWriteAddress( 0xc200, 0xc200, MWA_NOP ),
		new MemoryWriteAddress( 0xc000, 0xc000, sound_command_w ),
		new MemoryWriteAddress( 0x0000, 0x5fff, MWA_ROM ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};



	static MemoryReadAddress sound_readmem[] =
	{
		new MemoryReadAddress( 0x3000, 0x33ff, MRA_RAM ),
		new MemoryReadAddress( 0x4000, 0x4000, AY8910_read_port_0_r ),
		new MemoryReadAddress( 0x6000, 0x6000, AY8910_read_port_1_r ),
		new MemoryReadAddress( 0x0000, 0x1fff, MRA_ROM ),
		new MemoryReadAddress( -1 )	/* end of table */
	};

	static MemoryWriteAddress sound_writemem[] =
	{
		new MemoryWriteAddress( 0x3000, 0x33ff, MWA_RAM ),
		new MemoryWriteAddress( 0x5000, 0x5000, AY8910_control_port_0_w ),
		new MemoryWriteAddress( 0x4000, 0x4000, AY8910_write_port_0_w ),
		new MemoryWriteAddress( 0x7000, 0x7000, AY8910_control_port_1_w ),
		new MemoryWriteAddress( 0x6000, 0x6000, AY8910_write_port_1_w ),
		new MemoryWriteAddress( 0x0000, 0x1fff, MWA_ROM ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};

       static TrakPort[] trak_ports =
       {
           new TrakPort(-1)
       };

      static KEYSet[] keys =
      {
          new KEYSet(1, 2, "MOVE UP"),
          new KEYSet(1, 0, "MOVE LEFT"),
          new KEYSet(1, 1, "MOVE RIGHT"),
          new KEYSet( 1, 3, "MOVE DOWN"),
          new KEYSet( 1, 4, "FIRE"),
          new KEYSet(-1) };


	static InputPort input_ports[] =
	{
		new InputPort(	/* IN0 */
			0xff,
			new int[] { 0, 0, OSD_KEY_3, OSD_KEY_1, OSD_KEY_2, 0, 0, 0 }
		),
		new InputPort(	/* IN1 */
			0xff,
			new int[] { OSD_KEY_LEFT, OSD_KEY_RIGHT, OSD_KEY_UP, OSD_KEY_DOWN, OSD_KEY_CONTROL, 0, 0, 0 }
		),
		new InputPort(	/* IN2 */
			0xff,
			new int[] { 0, 0, 0, 0, 0, 0, 0, 0 }
		),
		new InputPort(	/* DSW1 */
			0xff,
			new int[] { 0, 0, 0, 0, 0, 0, 0, 0 }
		),
		new InputPort(	/* DSW2 */
			0x73,
			new int[] { 0, 0, 0, 0, 0, 0, 0, 0 }
		),
		new InputPort( -1 )	/* end of table */
	};



	static DSW dsw[] =
	{
		new DSW( 4, 0x03, "LIVES", new String[] { "255", "5", "4", "3" },1 ),
		new DSW( 4, 0x08, "BONUS", new String[] { "20000 60000", "10000 50000" }, 1 ),
		new DSW( 4, 0x70, "DIFFICULTY", new String[] { "HARDEST", "HARD", "DIFFICULT", "MEDIUM", "NORMAL", "EASIER", "EASY" , "EASIEST" }, 1 ),
		new DSW( 4, 0x80, "DEMO SOUNDS", new String[] { "ON", "OFF" }, 1 ),
		new DSW( -1 )
	};



	static GfxLayout charlayout = new GfxLayout
	(
		8,8,	/* 8*8 characters */
		512,	/* 512 characters */
		2,	/* 2 bits per pixel */
		new int[] { 4, 0 },
		new int[] { 7*8, 6*8, 5*8, 4*8, 3*8, 2*8, 1*8, 0*8 },
		new int[] { 0, 1, 2, 3, 8*8+0,8*8+1,8*8+2,8*8+3 },
		16*8	/* every char takes 16 consecutive bytes */
	);
	static GfxLayout spritelayout = new GfxLayout
	(
		16,16,	/* 16*16 sprites */
		256,	/* 256 sprites */
		2,	/* 2 bits per pixel */
		new int[] { 4, 0 },
		new int[] { 39*8, 38*8, 37*8, 36*8, 35*8, 34*8, 33*8, 32*8,
					7*8, 6*8, 5*8, 4*8, 3*8, 2*8, 1*8, 0*8 },
		new int[] { 0, 1, 2, 3,  8*8, 8*8+1, 8*8+2, 8*8+3,
					16*8+0, 16*8+1, 16*8+2, 16*8+3,  24*8+0, 24*8+1, 24*8+2, 24*8+3 },
		64*8	/* every sprite takes 64 consecutive bytes */
	);
	static GfxLayout dwspritelayout = new GfxLayout
	(
		32,16,	/* 2x16*16 sprites */
		256,	/* 256 sprites */
		2,	/* 2 bits per pixel */
		new int[] { 4, 0 },
		new int[] { 39*8, 39*8, 38*8, 38*8, 37*8, 37*8, 36*8, 36*8, 35*8, 35*8, 34*8, 34*8, 33*8, 33*8, 32*8, 32*8,
					7*8, 7*8, 6*8, 6*8, 5*8, 5*8, 4*8, 4*8, 3*8, 3*8, 2*8, 2*8, 1*8, 1*8, 0*8, 0*8 },
		new int[] { 0, 1, 2, 3,  8*8, 8*8+1, 8*8+2, 8*8+3,
					16*8+0, 16*8+1, 16*8+2, 16*8+3,  24*8+0, 24*8+1, 24*8+2, 24*8+3 },
		64*8	/* every sprite takes 64 consecutive bytes */
	);



	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( 1, 0x0000, charlayout,        0, 32 ),
		new GfxDecodeInfo( 1, 0x2000, spritelayout,   32*4, 64 ),
		new GfxDecodeInfo( 1, 0x2000, dwspritelayout, 32*4, 64 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};



	/* these are NOT the original color PROMs */
	static char color_prom[] =
	{
	/* B4 - palette */
	0x00,0x00,0x05,0x06,0x07,0xFC,0x05,0xBD,0xB5,0xFD,0x05,0xB0,0xA5,0xE0,0x00,0xF7,
	0x00,0x00,0xF8,0x07,0x07,0xFD,0xF8,0xFA,0x05,0xDE,0x50,0x51,0x32,0xFD,0x30,0xF7,
	/* B5 - palette */
	0x00,0x3E,0x3E,0x80,0xFE,0x00,0xAC,0xEE,0xAC,0xC0,0x14,0x00,0x28,0x38,0x16,0xBC,
	0x00,0x3E,0x00,0xC0,0xFE,0xC0,0x3E,0x80,0x3E,0xF6,0x00,0x80,0x80,0x00,0x0C,0xBC,

	/* E9 - sprite lookup table */
	0x00,0x0D,0x0F,0x05,0x00,0x0E,0x06,0x0A,0x00,0x04,0x09,0x01,0x00,0x04,0x09,0x01,
	0x00,0x04,0x09,0x01,0x00,0x0C,0x05,0x01,0x00,0x0E,0x05,0x01,0x00,0x0D,0x05,0x01,
	0x00,0x0B,0x05,0x01,0x00,0x01,0x0F,0x04,0x00,0x01,0x0F,0x04,0x00,0x01,0x0F,0x04,
	0x00,0x08,0x07,0x0C,0x00,0x01,0x0F,0x04,0x00,0x01,0x0F,0x04,0x00,0x0A,0x05,0x01,
	0x00,0x05,0x09,0x01,0x00,0x0B,0x0D,0x05,0x00,0x06,0x05,0x01,0x00,0x0A,0x03,0x01,
	0x00,0x0C,0x03,0x01,0x00,0x0E,0x03,0x01,0x00,0x0D,0x03,0x01,0x00,0x0B,0x03,0x01,
	0x00,0x0E,0x0C,0x0F,0x00,0x05,0x03,0x01,0x00,0x0E,0x06,0x09,0x00,0x04,0x09,0x05,
	0x00,0x09,0x0E,0x06,0x00,0x04,0x0E,0x05,0x00,0x09,0x0E,0x05,0x00,0x0B,0x05,0x01,
	0x00,0x0C,0x07,0x01,0x00,0x05,0x0F,0x09,0x00,0x05,0x04,0x09,0x00,0x0A,0x03,0x01,
	0x00,0x0C,0x02,0x03,0x00,0x0C,0x06,0x03,0x00,0x0C,0x06,0x09,0x00,0x0C,0x06,0x01,
	0x00,0x0E,0x06,0x0C,0x00,0x0C,0x02,0x0F,0x00,0x0C,0x02,0x09,0x00,0x0C,0x02,0x01,
	0x00,0x01,0x08,0x0F,0x00,0x0E,0x06,0x0F,0x00,0x09,0x0A,0x0F,0x00,0x05,0x06,0x0F,
	0x00,0x0B,0x09,0x05,0x00,0x0A,0x06,0x0C,0x00,0x0A,0x06,0x09,0x00,0x0A,0x02,0x09,
	0x00,0x06,0x01,0x0F,0x00,0x04,0x01,0x0F,0x00,0x0A,0x03,0x01,0x00,0x0A,0x02,0x0C,
	0x00,0x05,0x09,0x01,0x00,0x0A,0x02,0x01,0x00,0x0E,0x02,0x09,0x00,0x0E,0x02,0x0C,
	0x00,0x01,0x04,0x0F,0x00,0x01,0x04,0x0F,0x00,0x0F,0x0F,0x0F,0x00,0x00,0x00,0x00,

	/* E12 (or F15?) - char lookup table */
	/* The PROM is 256x4, but only the first 128 bytes are used */
	0x00,0x0D,0x0F,0x0C,0x0A,0x04,0x01,0x0F,0x0B,0x04,0x01,0x0F,0x0C,0x04,0x01,0x0F,
	0x0E,0x04,0x01,0x0F,0x00,0x04,0x01,0x0F,0x0A,0x04,0x05,0x01,0x0B,0x04,0x05,0x01,
	0x0C,0x04,0x05,0x01,0x0E,0x04,0x05,0x01,0x00,0x04,0x05,0x01,0x0A,0x06,0x08,0x02,
	0x0B,0x06,0x08,0x02,0x0C,0x06,0x08,0x02,0x0E,0x06,0x08,0x02,0x00,0x06,0x08,0x02,
	0x00,0x01,0x04,0x0F,0x00,0x04,0x02,0x06,0x00,0x01,0x08,0x04,0x00,0x0D,0x01,0x05,
	0x00,0x02,0x03,0x01,0x00,0x0C,0x0F,0x03,0x00,0x05,0x02,0x08,0x0A,0x01,0x04,0x03,
	0x00,0x06,0x0F,0x02,0x00,0x0F,0x03,0x05,0x00,0x03,0x01,0x0F,0x0A,0x02,0x0D,0x05,
	0x00,0x01,0x0F,0x08,0x0A,0x02,0x0D,0x05,0x0A,0x0B,0x09,0x0F,0x09,0x09,0x09,0x09,
	0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,
	0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,
	0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,
	0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,
	0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,
	0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,
	0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,
	0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F
	};



	public static MachineDriver machine_driver = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				3072000,	/* 3.072 Mhz (?) */
				0,
				readmem, writemem, null, null,
				nmi_interrupt, 1
			),
			new MachineCPU(
				CPU_Z80 | CPU_AUDIO_CPU,
				3072000,	/* 3.072 Mhz ? */
				2,	/* memory region #2 */
				sound_readmem, sound_writemem, null, null,
				timeplt_sh_interrupt,10
			)
		},
		60,
		null,

		/* video hardware */
		32*8, 32*8, new rectangle( 2*8, 30*8-1, 0*8, 32*8-1 ),
		gfxdecodeinfo,
		32, 32*16,
		timeplt_vh_convert_color_prom,
                VIDEO_TYPE_RASTER|VIDEO_SUPPORTS_DIRTY,

		null,
		generic_vh_start,
		generic_vh_stop,
		timeplt_vh_screenrefresh,

		/* sound hardware */
		null,
		null,
		timeplt_sh_start,
		AY8910_sh_stop,
		AY8910_sh_update
	);



	/***************************************************************************

	  Game driver(s)

	***************************************************************************/
        static RomLoadPtr timeplt_rom= new RomLoadPtr(){ public void handler() {
		ROM_REGION(0x10000);	/* 64k for code */
		ROM_LOAD("tm1", 0x0000, 0x2000, 0x1551f1b9 );
		ROM_LOAD("tm2", 0x2000, 0x2000, 0x58636cb5 );
		ROM_LOAD("tm3", 0x4000, 0x2000, 0xff4e0d83 );

		ROM_REGION(0x6000);	/* temporary space for graphics (disposed after conversion) */
		ROM_LOAD("tm6", 0x0000, 0x2000, 0xc2507f40 );
		ROM_LOAD("tm4", 0x2000, 0x2000, 0x7e437c3e );
		ROM_LOAD("tm5", 0x4000, 0x2000, 0xe8ca87b9 );

		ROM_REGION(0x10000);	/* 64k for the audio CPU */
		ROM_LOAD("tm7", 0x0000, 0x1000, 0xd66da813 );
                ROM_END();
        }};

        static RomLoadPtr spaceplt_rom= new RomLoadPtr(){ public void handler() {
                ROM_REGION(0x10000);	/* 64k for code */
                ROM_LOAD( "sp1",          0x0000, 0x2000, 0xac8ca3ae );
                ROM_LOAD( "sp2",          0x2000, 0x2000, 0x1f0308ef );
                ROM_LOAD( "sp3",          0x4000, 0x2000, 0x90aeca50 );

                ROM_REGION(0x6000);	/* temporary space for graphics (disposed after conversion) */
                ROM_LOAD( "sp6", 0x0000, 0x2000, 0x76caa8af );
                ROM_LOAD( "sp4", 0x2000, 0x2000, 0x3781ce7a );
                ROM_LOAD( "tm5", 0x4000, 0x2000, 0xe8ca87b9 );

                ROM_REGION(0x10000);	/* 64k for the audio CPU */
                ROM_LOAD( "tm7", 0x0000, 0x1000, 0xd66da813 );
                ROM_END();
        }};

	 
	static HiscoreLoadPtr hiload = new HiscoreLoadPtr() { public int handler()
	{
		/* get RAM pointer (this game is multiCPU, we can't assume the global */
		/* RAM pointer is pointing to the right place) */
		char []RAM = Machine.memory_region[0];


		/* check if the hi score table has already been initialized */
	 /*TOFIX        	if (memcmp(RAM, 0xab09, new char[] { 0x00, 0x00, 0x01 }, 3) == 0 &&
				memcmp(RAM, 0xab29, new char[] { 0x00, 0x43, 0x00 }, 3) == 0)
		{
			FILE f;


			if ((f = fopen(name, "rb")) != null)
			{
				fread(RAM, 0xab08, 1, 8*5, f);
				RAM[0xa98b] = RAM[0xab09];
				RAM[0xa98c] = RAM[0xab0a];
				RAM[0xa98d] = RAM[0xab0b];
				fclose(f);
			}

			return 1;
		}
		else */return 0;	/* we can't load the hi scores yet */
	} };



	static HiscoreSavePtr hisave = new HiscoreSavePtr() { public void handler()
	{
		FILE f;
		/* get RAM pointer (this game is multiCPU, we can't assume the global */
		/* RAM pointer is pointing to the right place) */
		char []RAM = Machine.memory_region[0];


	 /*TOFIX        	if ((f = fopen(name, "wb")) != null)
		{
			fwrite(RAM, 0xab08, 1, 8*5, f);
			fclose(f);
		} */
	} };



	public static GameDriver timeplt_driver = new GameDriver
	(
                "Time Pilot",
		"timeplt",
                "NICOLA SALMORIA\nALAN J MCCORMICK\nMIKE CUDDY",
		machine_driver,

		timeplt_rom,
		null, null,
		null,

		input_ports,null, trak_ports, dsw, keys,

		color_prom, null, null,

		ORIENTATION_DEFAULT,

		hiload, hisave
	);

	public static GameDriver spaceplt_driver = new GameDriver
	(
                "Space Pilot (bootleg Time Pilot)",
		"spaceplt",
                "NICOLA SALMORIA\nALAN J MCCORMICK\nMIKE CUDDY",
		machine_driver,

		spaceplt_rom,
		null, null,
		null,

		input_ports,null, trak_ports, dsw, keys,

		color_prom, null, null,
		ORIENTATION_DEFAULT,

		hiload, hisave
	);
}

