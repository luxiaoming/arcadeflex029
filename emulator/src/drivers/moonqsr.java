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
 *  roms are from v0.36 romset
 *
 */
package drivers;

import static arcadeflex.libc.*;
import static mame.commonH.*;
import static mame.cpuintrf.*;
import static mame.driverH.*;
import static mame.mame.*;
import static mame.inptport.*;
import static mame.osdependH.*;
import static sndhrdw.mooncrst.*;
import static vidhrdw.generic.*;
import static vidhrdw.galaxian.*;
import static mame.memoryH.*;

public class moonqsr
{



	static MemoryReadAddress readmem[] =
	{
		new MemoryReadAddress( 0x8000, 0x83ff, MRA_RAM ),
		new MemoryReadAddress( 0x9000, 0x9fff, MRA_RAM ),	/* video RAM, screen attributes, sprites, bullets */
		new MemoryReadAddress( 0x0000, 0x3fff, MRA_ROM ),
		new MemoryReadAddress( 0xa000, 0xa000, input_port_0_r ),	/* IN0 */
		new MemoryReadAddress( 0xa800, 0xa800, input_port_1_r ),	/* IN1 */
		new MemoryReadAddress( 0xb000, 0xb000, input_port_2_r ),	/* DSW (coins per play) */
		new MemoryReadAddress( 0xb800, 0xb800, MRA_NOP ),
		new MemoryReadAddress( -1 )	/* end of table */
	};

	static MemoryWriteAddress writemem[] =
	{
		new MemoryWriteAddress( 0x8000, 0x83ff, MWA_RAM ),
		new MemoryWriteAddress( 0x9000, 0x93ff, videoram_w, videoram,videoram_size ),
		new MemoryWriteAddress( 0x9800, 0x983f, galaxian_attributes_w, galaxian_attributesram ),
		new MemoryWriteAddress( 0x9840, 0x985f, MWA_RAM, spriteram,spriteram_size ),
		new MemoryWriteAddress( 0x9860, 0x9880, MWA_RAM, galaxian_bulletsram, galaxian_bulletsram_size ),
		new MemoryWriteAddress( 0xb000, 0xb000, interrupt_enable_w ),
		new MemoryWriteAddress( 0xb800, 0xb800, mooncrst_sound_freq_w ),
                new MemoryWriteAddress( 0xa800, 0xa800, mooncrst_background_w ),
		new MemoryWriteAddress( 0xa803, 0xa803, mooncrst_noise_w ),
		new MemoryWriteAddress( 0xa805, 0xa805, mooncrst_shoot_w ),
                new MemoryWriteAddress( 0xa806, 0xa807, mooncrst_sound_freq_sel_w ),
                new MemoryWriteAddress( 0xa004, 0xa007, mooncrst_lfo_freq_w ),
                new MemoryWriteAddress( 0xb004, 0xb004, galaxian_stars_w ),
                new MemoryWriteAddress( 0x0000, 0x3fff, MWA_ROM ),
		new MemoryWriteAddress( -1 )	/* end of table */
	};



	static InputPort input_ports[] =
	{
		new InputPort(	/* IN0 */
			0x00,
			new int[] { OSD_KEY_3, 0, OSD_KEY_LEFT, OSD_KEY_RIGHT,
				OSD_KEY_CONTROL, 0, 0, 0 }
		),
		new InputPort(	/* IN1 */
			0x00,
			new int[] { OSD_KEY_1, OSD_KEY_2, 0, 0, 0, 0, 0, 0 }
		),
		new InputPort(	/* DSW */
			0x00,
			new int[] { 0, 0, 0, 0, 0, 0, 0, 0 }
		),
		new InputPort( -1 )	/* end of table */
	};

static TrakPort trak_ports[] =
{
        new TrakPort(-1 )
};


static KEYSet keys[] =
{
        new KEYSet( 0, 2, "MOVE LEFT"  ),
        new KEYSet( 0, 3, "MOVE RIGHT" ),
        new KEYSet( 0, 4, "FIRE" ),
        new KEYSet( -1 )
};


	static DSW dsw[] =
	{
		new DSW( 1, 0xc0, "DIFFICULTY", new String[] { "EASY", "MEDIUM", "HARD", "HARDEST" } ),
		new DSW( -1 )
	};



	static GfxLayout charlayout = new GfxLayout
	(
                8,8,	/* 8*8 characters */
                512,	/* 512 characters */
                2,	/* 2 bits per pixel */
                new int[]{ 0, 512*8*8 },	/* the two bitplanes are separated */
                new int[]{ 0, 1, 2, 3, 4, 5, 6, 7 },
                new int[]{ 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
                8*8	/* every char takes 8 consecutive bytes */
	);
	static GfxLayout spritelayout = new GfxLayout
	(
                16,16,	/* 16*16 sprites */
                128,	/* 128 sprites */
                2,	/* 2 bits per pixel */
                new int[]{ 0, 128*16*16 },	/* the two bitplanes are separated */
                new int[]{ 0, 1, 2, 3, 4, 5, 6, 7,
                                8*8+0, 8*8+1, 8*8+2, 8*8+3, 8*8+4, 8*8+5, 8*8+6, 8*8+7 },
                new int[]{ 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
                                16*8, 17*8, 18*8, 19*8, 20*8, 21*8, 22*8, 23*8 },
                32*8	/* every sprite takes 32 consecutive bytes */
	);
        static GfxLayout bulletlayout = new GfxLayout
	(
                /* there is no gfx ROM for this one, it is generated by the hardware */
                3,1,	/* 3*1 line */
                1,	/* just one */
                1,	/* 1 bit per pixel */
                new int[]{ 0 },
                new int[]{ 2, 2, 2 },	/* I "know" that this bit is 1 */
                new int[]{ 0 },	/* I "know" that this bit is 1 */
                0	/* no use */
        );



	static GfxDecodeInfo gfxdecodeinfo[] =
	{
		new GfxDecodeInfo( 1, 0x0000, charlayout,     0, 8 ),
		new GfxDecodeInfo( 1, 0x0000, spritelayout,   0, 8 ),
                new GfxDecodeInfo( 1, 0x0000, bulletlayout, 8*4, 2 ),
		new GfxDecodeInfo( -1 ) /* end of array */
	};



	static char color_prom[] =
	{
		/* palette */
		0x00,0xf6,0x79,0x4f,0x00,0xc0,0x3f,0x17,0x00,0x87,0xf8,0x7f,0x00,0xc1,0x7f,0x38,
		0x00,0x7f,0xcf,0xf9,0x00,0x57,0xb7,0xc3,0x00,0xff,0x7f,0x87,0x00,0x79,0x4f,0xff
	};

        static char samples[] =
        {
           0x88, 0x88, 0x88, 0x88, 0xaa, 0xaa, 0xaa, 0xaa,
           0xcc, 0xcc, 0xcc, 0xcc, 0xee, 0xee, 0xee, 0xee,
           0x11, 0x11, 0x11, 0x11, 0x22, 0x22, 0x22, 0x22,
           0x44, 0x44, 0x44, 0x44, 0x66, 0x66, 0x66, 0x66,
           0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44,
           0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44, 0x44,
           0xcc, 0xcc, 0xcc, 0xcc, 0xcc, 0xcc, 0xcc, 0xcc,
           0xcc, 0xcc, 0xcc, 0xcc, 0xcc, 0xcc, 0xcc, 0xcc
        };


	public static MachineDriver machine_driver = new MachineDriver
	(
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
				CPU_Z80,
				3072000,	/* 3.072 Mhz */
				0,
				readmem, writemem, null, null,
				galaxian_vh_interrupt,1
			)
		},
		60,
		null,

		/* video hardware */
		32*8, 32*8, new rectangle( 0*8, 32*8-1, 2*8, 30*8-1 ),
		gfxdecodeinfo,
                32+64,8*4+2*2,	/* 32 for the characters, 64 for the stars */
                galaxian_vh_convert_color_prom,

                VIDEO_TYPE_RASTER,
                null,
                galaxian_vh_start,
                generic_vh_stop,
                galaxian_vh_screenrefresh,

		/* sound hardware */
		samples,
		mooncrst_sh_init,
		mooncrst_sh_start,
		mooncrst_sh_stop,
		mooncrst_sh_update
	);



	/***************************************************************************
	
	  Game driver(s)
	
	***************************************************************************/
	static RomLoadPtr moonqsr_rom = new RomLoadPtr(){ public void handler()  
        {
		ROM_REGION(0x10000);	/* 64k for code */
		ROM_LOAD( "mq1",          0x0000, 0x0800, 0x132c13ec );
                ROM_LOAD( "mq2",          0x0800, 0x0800, 0xc8eb74f1 );
                ROM_LOAD( "mq3",          0x1000, 0x0800, 0x33965a89 );
                ROM_LOAD( "mq4",          0x1800, 0x0800, 0xa3861d17 );
                ROM_LOAD( "mq5",          0x2000, 0x0800, 0x8bcf9c67 );
                ROM_LOAD( "mq6",          0x2800, 0x0800, 0x5750cda9 );
                ROM_LOAD( "mq7",          0x3000, 0x0800, 0x78d7fe5b );
                ROM_LOAD( "mq8",          0x3800, 0x0800, 0x4919eed5 );
	
		ROM_REGION(0x2000);/* temporary space for graphics (disposed after conversion) */
		ROM_LOAD( "mqb",          0x0000, 0x0800, 0xb55ec806 );
                ROM_LOAD( "mqd",          0x0800, 0x0800, 0x9e7d0e13 );
                ROM_LOAD( "mqa",          0x1000, 0x0800, 0x66eee0db );
                ROM_LOAD( "mqc",          0x1800, 0x0800, 0xa6db5b0d );                   
                ROM_END();
        }};



	static  char evetab[] =
	{
		0x00,0x01,0x06,0x07,0x40,0x41,0x46,0x47,0x08,0x09,0x0e,0x0f,0x48,0x49,0x4e,0x4f,
		0x10,0x11,0x16,0x17,0x50,0x51,0x56,0x57,0x18,0x19,0x1e,0x1f,0x58,0x59,0x5e,0x5f,
		0x60,0x61,0x66,0x67,0x20,0x21,0x26,0x27,0x68,0x69,0x6e,0x6f,0x28,0x29,0x2e,0x2f,
		0x70,0x71,0x76,0x77,0x30,0x31,0x36,0x37,0x78,0x79,0x7e,0x7f,0x38,0x39,0x3e,0x3f,
		0x04,0x05,0x02,0x03,0x44,0x45,0x42,0x43,0x0c,0x0d,0x0a,0x0b,0x4c,0x4d,0x4a,0x4b,
		0x14,0x15,0x12,0x13,0x54,0x55,0x52,0x53,0x1c,0x1d,0x1a,0x1b,0x5c,0x5d,0x5a,0x5b,
		0x64,0x65,0x62,0x63,0x24,0x25,0x22,0x23,0x6c,0x6d,0x6a,0x6b,0x2c,0x2d,0x2a,0x2b,
		0x74,0x75,0x72,0x73,0x34,0x35,0x32,0x33,0x7c,0x7d,0x7a,0x7b,0x3c,0x3d,0x3a,0x3b,
		0x80,0x81,0x86,0x87,0xc0,0xc1,0xc6,0xc7,0x88,0x89,0x8e,0x8f,0xc8,0xc9,0xce,0xcf,
		0x90,0x91,0x96,0x97,0xd0,0xd1,0xd6,0xd7,0x98,0x99,0x9e,0x9f,0xd8,0xd9,0xde,0xdf,
		0xe0,0xe1,0xe6,0xe7,0xa0,0xa1,0xa6,0xa7,0xe8,0xe9,0xee,0xef,0xa8,0xa9,0xae,0xaf,
		0xf0,0xf1,0xf6,0xf7,0xb0,0xb1,0xb6,0xb7,0xf8,0xf9,0xfe,0xff,0xb8,0xb9,0xbe,0xbf,
		0x84,0x85,0x82,0x83,0xc4,0xc5,0xc2,0xc3,0x8c,0x8d,0x8a,0x8b,0xcc,0xcd,0xca,0xcb,
		0x94,0x95,0x92,0x93,0xd4,0xd5,0xd2,0xd3,0x9c,0x9d,0x9a,0x9b,0xdc,0xdd,0xda,0xdb,
		0xe4,0xe5,0xe2,0xe3,0xa4,0xa5,0xa2,0xa3,0xec,0xed,0xea,0xeb,0xac,0xad,0xaa,0xab,
		0xf4,0xf5,0xf2,0xf3,0xb4,0xb5,0xb2,0xb3,0xfc,0xfd,0xfa,0xfb,0xbc,0xbd,0xba,0xbb
	};
	static  char oddtab[] =
	{
		0x00,0x01,0x42,0x43,0x04,0x05,0x46,0x47,0x08,0x09,0x4a,0x4b,0x0c,0x0d,0x4e,0x4f,
		0x10,0x11,0x52,0x53,0x14,0x15,0x56,0x57,0x18,0x19,0x5a,0x5b,0x1c,0x1d,0x5e,0x5f,
		0x24,0x25,0x66,0x67,0x20,0x21,0x62,0x63,0x2c,0x2d,0x6e,0x6f,0x28,0x29,0x6a,0x6b,
		0x34,0x35,0x76,0x77,0x30,0x31,0x72,0x73,0x3c,0x3d,0x7e,0x7f,0x38,0x39,0x7a,0x7b,
		0x40,0x41,0x02,0x03,0x44,0x45,0x06,0x07,0x48,0x49,0x0a,0x0b,0x4c,0x4d,0x0e,0x0f,
		0x50,0x51,0x12,0x13,0x54,0x55,0x16,0x17,0x58,0x59,0x1a,0x1b,0x5c,0x5d,0x1e,0x1f,
		0x64,0x65,0x26,0x27,0x60,0x61,0x22,0x23,0x6c,0x6d,0x2e,0x2f,0x68,0x69,0x2a,0x2b,
		0x74,0x75,0x36,0x37,0x70,0x71,0x32,0x33,0x7c,0x7d,0x3e,0x3f,0x78,0x79,0x3a,0x3b,
		0x80,0x81,0xc2,0xc3,0x84,0x85,0xc6,0xc7,0x88,0x89,0xca,0xcb,0x8c,0x8d,0xce,0xcf,
		0x90,0x91,0xd2,0xd3,0x94,0x95,0xd6,0xd7,0x98,0x99,0xda,0xdb,0x9c,0x9d,0xde,0xdf,
		0xa4,0xa5,0xe6,0xe7,0xa0,0xa1,0xe2,0xe3,0xac,0xad,0xee,0xef,0xa8,0xa9,0xea,0xeb,
		0xb4,0xb5,0xf6,0xf7,0xb0,0xb1,0xf2,0xf3,0xbc,0xbd,0xfe,0xff,0xb8,0xb9,0xfa,0xfb,
		0xc0,0xc1,0x82,0x83,0xc4,0xc5,0x86,0x87,0xc8,0xc9,0x8a,0x8b,0xcc,0xcd,0x8e,0x8f,
		0xd0,0xd1,0x92,0x93,0xd4,0xd5,0x96,0x97,0xd8,0xd9,0x9a,0x9b,0xdc,0xdd,0x9e,0x9f,
		0xe4,0xe5,0xa6,0xa7,0xe0,0xe1,0xa2,0xa3,0xec,0xed,0xae,0xaf,0xe8,0xe9,0xaa,0xab,
		0xf4,0xf5,0xb6,0xb7,0xf0,0xf1,0xb2,0xb3,0xfc,0xfd,0xbe,0xbf,0xf8,0xf9,0xba,0xbb
	};
	static DecodePtr moonqsr_decode = new DecodePtr()
        {
            public void handler()
            {
                int A;


                for (A = 0;A < 0x10000;A++)
                {
                   	if ((A & 1) != 0) 
                        {
                            ROM[A] = oddtab[RAM[A]];
                        }
                        else ROM[A] = evetab[RAM[A]];
                }
            }

        };




	static HiscoreLoadPtr hiload = new HiscoreLoadPtr() { public int handler()
	{
		/* check if the hi score table has already been initialized */
	 /*TOFIX        	if (memcmp(RAM, 0x804e, new char[] { 0x00, 0x50, 0x00 }, 3) == 0 &&
				memcmp(RAM, 0x805a, new char[] { 0x00, 0x50, 0x00 }, 3) == 0)
		{
			FILE f;


			if ((f = fopen(name, "rb")) != null)
			{
				fread(RAM, 0x804e, 1, 10*5, f);
				fclose(f);
			}

			return 1;
		}
		else */return 0;	/* we can't load the hi scores yet */
	} };



	static HiscoreSavePtr hisave = new HiscoreSavePtr() { public void handler()
	{
		FILE f;


	 /*TOFIX        	if ((f = fopen(name, "wb")) != null)
		{
			fwrite(RAM, 0x804e, 1, 10*5, f);
			fclose(f);
		}*/
	} };
        static String mooncrst_sample_names[] =
        {
                "shot.sam",
                "death.sam",
                "backgrnd.sam",
                null	/* end of array */
        };


	public static GameDriver moonqsr_driver = new GameDriver
	(
                "Moon Quasar",
		"moonqsr",
                "ROBERT ANSCHUETZ\nNICOLA SALMORIA\nGARY WALTON\nSIMON WALLS\nANDREW SCOTT",
		machine_driver,
	
		moonqsr_rom,
		null, moonqsr_decode,
		mooncrst_sample_names,
	
		input_ports, null, trak_ports, dsw, keys,
	
		color_prom, null, null,
		ORIENTATION_ROTATE_90,
	
		hiload, hisave
	);
}
